/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.config.common.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

/**
 * A base {@link AnnotationBuilder} that allows {@link AnnotationConfigurer}s to be
 * applied to it. This makes modifying the {@link AnnotationBuilder} a strategy
 * that can be customised and broken up into a number of
 * {@link AnnotationConfigurer} objects that have more specific goals than that
 * of the {@link AnnotationBuilder}.
 * <p>
 *
 *
 * @author Rob Winch
 * @author Janne Valkealahti
 *
 * @param <O> The object that this builder returns
 * @param <I> The interface of type B
 * @param <B> The type of this builder (that is returned by the base class)
 */
public abstract class AbstractConfiguredAnnotationBuilder<O,I,B extends AnnotationBuilder<O>>
		extends AbstractAnnotationBuilder<O> {

	private final static Log log = LogFactory.getLog(AbstractConfiguredAnnotationBuilder.class);

	/** Configurers which are added to this builder before the configure step */
	private final LinkedHashMap<Class<? extends AnnotationConfigurer<O, B>>, List<AnnotationConfigurer<O, B>>> mainConfigurers =
			new LinkedHashMap<Class<? extends AnnotationConfigurer<O, B>>, List<AnnotationConfigurer<O, B>>>();

	/** Configurers which are added to this builder during the configuration phase */
	private final LinkedHashMap<Class<? extends AnnotationConfigurer<O, B>>, List<AnnotationConfigurer<O, B>>> postConfigurers =
			new LinkedHashMap<Class<? extends AnnotationConfigurer<O, B>>, List<AnnotationConfigurer<O, B>>>();

	private final Map<Class<Object>, Object> sharedObjects = new HashMap<Class<Object>, Object>();

	private final boolean allowConfigurersOfSameType;

	/** Current state of this builder */
	private BuildState buildState = BuildState.UNBUILT;

	private ObjectPostProcessor<Object> objectPostProcessor;

	/**
	 * Instantiates a new annotation builder.
	 */
	protected AbstractConfiguredAnnotationBuilder() {
		this(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR);
	}

	/**
	 * Instantiates a new annotation builder.
	 *
	 * @param objectPostProcessor the object post processor
	 */
	protected AbstractConfiguredAnnotationBuilder(ObjectPostProcessor<Object> objectPostProcessor) {
		this(objectPostProcessor,false);
	}

	/**
	 * Instantiates a new annotation builder.
	 *
	 * @param objectPostProcessor the object post processor
	 * @param allowConfigurersOfSameType the allow configurers of same type
	 */
	protected AbstractConfiguredAnnotationBuilder(ObjectPostProcessor<Object> objectPostProcessor, boolean allowConfigurersOfSameType) {
		Assert.notNull(objectPostProcessor, "objectPostProcessor cannot be null");
		this.objectPostProcessor = objectPostProcessor;
		this.allowConfigurersOfSameType = allowConfigurersOfSameType;
	}

	@Override
	protected final O doBuild() throws Exception {
		synchronized (mainConfigurers) {
			buildState = BuildState.INITIALIZING_MAINS;
			beforeInit();
			initMainConfigurers();

			buildState = BuildState.CONFIGURING_MAINS;
			beforeConfigureMains();
			configureMainConfigurers();

			buildState = BuildState.CONFIGURING_POSTS;
			beforeConfigurePosts();
			configurePostConfigurers();

			buildState = BuildState.BUILDING;
			O result = performBuild();

			buildState = BuildState.BUILT;
			return result;
		}
	}

	/**
	 * Similar to {@link #build()} and {@link #getObject()} but checks the state
	 * to determine if {@link #build()} needs to be called first.
	 *
	 * @return the result of {@link #build()} or {@link #getObject()}. If an
	 *         error occurs while building, returns null.
	 */
	public O getOrBuild() {
		if (isUnbuilt()) {
			try {
				return build();
			} catch (Exception e) {
				log.error("Failed to perform build. Returning null", e);
				return null;
			}
		} else {
			return getObject();
		}
	}

	/**
	 * Applies a {@link AnnotationConfigurerAdapter} to this
	 * {@link AnnotationBuilder} and invokes
	 * {@link AnnotationConfigurerAdapter#setBuilder(AnnotationBuilder)}.
	 *
	 * @param configurer the configurer
	 * @param <C> type of AnnotationConfigurer
	 * @return Configurer passed as parameter
	 * @throws Exception if error occurred
	 */
	@SuppressWarnings("unchecked")
	public <C extends AnnotationConfigurerAdapter<O,I,B>> C apply(C configurer) throws Exception {
		add(configurer);
		configurer.addObjectPostProcessor(objectPostProcessor);
		configurer.setBuilder((B) this);
		return configurer;
	}

	/**
	 * Similar to {@link #apply(AnnotationConfigurer)} but checks the state
	 * to determine if {@link #apply(AnnotationConfigurer)} needs to be called first.
	 *
	 * @param configurer the configurer
	 * @param <C> type of AnnotationConfigurer
	 * @return Configurer passed as parameter
	 * @throws Exception if error occurred
	 */
	@SuppressWarnings("unchecked")
	public <C extends AnnotationConfigurerAdapter<O,I,B>> C getOrApply(C configurer) throws Exception {
		C existing = (C) getConfigurer(configurer.getClass());
		if (existing != null) {
			return existing;
		}
		return apply(configurer);
	}

	/**
	 * Applies a {@link AnnotationConfigurer} to this {@link AnnotationBuilder}
	 * overriding any {@link AnnotationConfigurer} of the exact same class. Note
	 * that object hierarchies are not considered.
	 *
	 * @param configurer the configurer
	 * @param <C> type of AnnotationConfigurer
	 * @return Configurer passed as parameter
	 * @throws Exception if error occurred
	 */
	public <C extends AnnotationConfigurer<O, B>> C apply(C configurer) throws Exception {
		add(configurer);
		return configurer;
	}

	/**
	 * Sets an object that is shared by multiple {@link AnnotationConfigurer}.
	 *
	 * @param sharedType the Class to key the shared object by.
	 * @param object the Object to store
	 * @param <C> type of share object
	 */
	@SuppressWarnings("unchecked")
	public <C> void setSharedObject(Class<C> sharedType, C object) {
		this.sharedObjects.put((Class<Object>) sharedType, object);
	}

	/**
	 * Gets a shared Object. Note that object hierarchies are not considered.
	 *
	 * @param sharedType the type of the shared Object
	 * @param <C> type of share object
	 * @return the shared Object or null if it is not found
	 */
	@SuppressWarnings("unchecked")
	public <C> C getSharedObject(Class<C> sharedType) {
		return (C) this.sharedObjects.get(sharedType);
	}

	/**
	 * Gets the shared objects.
	 *
	 * @return Shared objects
	 */
	public Map<Class<Object>, Object> getSharedObjects() {
		return Collections.unmodifiableMap(this.sharedObjects);
	}

	/**
	 * Adds {@link AnnotationConfigurer} ensuring that it is allowed and
	 * invoking {@link AnnotationConfigurer#init(AnnotationBuilder)} immediately
	 * if necessary.
	 *
	 * @param configurer the {@link AnnotationConfigurer} to add
	 * @param <C> type of AnnotationConfigurer
	 * @throws Exception if an error occurs
	 */
	@SuppressWarnings("unchecked")
	private <C extends AnnotationConfigurer<O, B>> void add(C configurer) throws Exception {
		Assert.notNull(configurer, "configurer cannot be null");

		Class<? extends AnnotationConfigurer<O, B>> clazz =
				(Class<? extends AnnotationConfigurer<O, B>>) configurer.getClass();

		if (!buildState.isConfigured()) {
			synchronized (mainConfigurers) {
				List<AnnotationConfigurer<O, B>> configs = allowConfigurersOfSameType ? this.mainConfigurers.get(clazz) : null;
				if (configs == null) {
					configs = new ArrayList<AnnotationConfigurer<O, B>>(1);
				}
				configs.add(configurer);
				this.mainConfigurers.put(clazz, configs);
				if (buildState.isInitializing()) {
					configurer.init((B) this);
				}
			}
		} else {
			synchronized (postConfigurers) {
				List<AnnotationConfigurer<O, B>> configs = allowConfigurersOfSameType ? this.postConfigurers.get(clazz) : null;
				if (configs == null) {
					configs = new ArrayList<AnnotationConfigurer<O, B>>(1);
				}
				configs.add(configurer);
				this.postConfigurers.put(clazz, configs);
				configurer.init((B) this);
			}
		}
	}

	/**
	 * Gets all the {@link AnnotationConfigurer} instances by its class name or an
	 * empty List if not found. Note that object hierarchies are not considered.
	 *
	 * @param clazz the {@link AnnotationConfigurer} class to look for
	 * @param <C> type of AnnotationConfigurer
	 * @return All configurers
	 */
	@SuppressWarnings("unchecked")
	public <C extends AnnotationConfigurer<O, B>> List<C> getConfigurers(Class<C> clazz) {
		List<C> configs = (List<C>) this.mainConfigurers.get(clazz);
		if (configs == null) {
			return new ArrayList<C>();
		}
		return new ArrayList<C>(configs);
	}

	/**
	 * Removes all the {@link AnnotationConfigurer} instances by its class name or an
	 * empty List if not found. Note that object hierarchies are not considered.
	 *
	 * @param clazz the {@link AnnotationConfigurer} class to look for
	 * @param <C> type of AnnotationConfigurer
	 * @return Empty list of configurers
	 */
	@SuppressWarnings("unchecked")
	public <C extends AnnotationConfigurer<O, B>> List<C> removeConfigurers(Class<C> clazz) {
		List<C> configs = (List<C>) this.mainConfigurers.remove(clazz);
		if (configs == null) {
			return new ArrayList<C>();
		}
		return new ArrayList<C>(configs);
	}

	/**
	 * Gets the {@link AnnotationConfigurer} by its class name or
	 * <code>null</code> if not found. Note that object hierarchies are not
	 * considered.
	 *
	 * @param clazz the configurer class type
	 * @param <C> type of AnnotationConfigurer
	 * @return Matched configurers
	 */
	@SuppressWarnings("unchecked")
	public <C extends AnnotationConfigurer<O, B>> C getConfigurer(Class<C> clazz) {
		List<AnnotationConfigurer<O, B>> configs = this.mainConfigurers.get(clazz);
		if (configs == null) {
			return null;
		}
		if (configs.size() != 1) {
			throw new IllegalStateException("Only one configurer expected for type " + clazz + ", but got " + configs);
		}
		return (C) configs.get(0);
	}

	/**
	 * Removes and returns the {@link AnnotationConfigurer} by its class name or
	 * <code>null</code> if not found. Note that object hierarchies are not
	 * considered.
	 *
	 * @param clazz the configurer class type
	 * @param <C> type of AnnotationConfigurer
	 * @return Matched configurers
	 */
	@SuppressWarnings("unchecked")
	public <C extends AnnotationConfigurer<O, B>> C removeConfigurer(Class<C> clazz) {
		List<AnnotationConfigurer<O, B>> configs = this.mainConfigurers.remove(clazz);
		if (configs == null) {
			return null;
		}
		if (configs.size() != 1) {
			throw new IllegalStateException("Only one configurer expected for type " + clazz + ", but got " + configs);
		}
		return (C) configs.get(0);
	}

	/**
	 * Specifies the {@link ObjectPostProcessor} to use.
	 * @param objectPostProcessor the {@link ObjectPostProcessor} to use. Cannot be null
	 * @return the {@link AnnotationBuilder} for further customizations
	 */
	@SuppressWarnings("unchecked")
	public O objectPostProcessor(ObjectPostProcessor<Object> objectPostProcessor) {
		Assert.notNull(objectPostProcessor,"objectPostProcessor cannot be null");
		this.objectPostProcessor = objectPostProcessor;
		return (O) this;
	}

	/**
	 * Performs post processing of an object. The default is to delegate to the
	 * {@link ObjectPostProcessor}.
	 *
	 * @param object the Object to post process
	 * @param <P> type of processed object
	 * @return the possibly modified Object to use
	 */
	protected <P> P postProcess(P object) {
		return (P) this.objectPostProcessor.postProcess(object);
	}

	/**
	 * Invoked prior to invoking each
	 * {@link AnnotationConfigurer#init(AnnotationBuilder)} method. Subclasses may
	 * override this method to hook into the lifecycle without using a
	 * {@link AnnotationConfigurer}.
	 * 
	 * @throws Exception if error occurred
	 */
	protected void beforeInit() throws Exception {
	}

	/**
	 * Invoked prior to invoking each main
	 * {@link AnnotationConfigurer#configure(AnnotationBuilder)} method.
	 * Subclasses may override this method to hook into the lifecycle without
	 * using a {@link AnnotationConfigurer}.
	 * 
	 * @throws Exception if error occurred
	 */
	protected void beforeConfigureMains() throws Exception {
	}

	/**
	 * Invoked prior to invoking each post
	 * {@link AnnotationConfigurer#configure(AnnotationBuilder)} method.
	 * Subclasses may override this method to hook into the lifecycle without
	 * using a {@link AnnotationConfigurer}.
	 * 
	 * @throws Exception if error occurred
	 */
	protected void beforeConfigurePosts() throws Exception {
	}

	/**
	 * Subclasses must implement this method to build the object that is being returned.
	 *
	 * @return Object build by this builder
	 * @throws Exception if error occurred
	 */
	protected abstract O performBuild() throws Exception;

	@SuppressWarnings("unchecked")
	private void initMainConfigurers() throws Exception {
		for (AnnotationConfigurer<O, B> configurer : getMainConfigurers()) {
			configurer.init((B) this);
		}
	}

	@SuppressWarnings("unchecked")
	private void configureMainConfigurers() throws Exception {
		for (AnnotationConfigurer<O, B> configurer : getMainConfigurers()) {
			configurer.configure((B) this);
		}
	}

	@SuppressWarnings("unchecked")
	private void configurePostConfigurers() throws Exception {
		for (AnnotationConfigurer<O, B> configurer : getPostConfigurers()) {
			configurer.configure((B) this);
		}
	}

	/**
	 * Gets all configurers.
	 *
	 * @return the configurers
	 */
	private Collection<AnnotationConfigurer<O, B>> getMainConfigurers() {
		List<AnnotationConfigurer<O, B>> result = new ArrayList<AnnotationConfigurer<O, B>>();
		for (List<AnnotationConfigurer<O, B>> configs : this.mainConfigurers.values()) {
			result.addAll(configs);
		}
		return result;
	}

	private Collection<AnnotationConfigurer<O, B>> getPostConfigurers() {
		List<AnnotationConfigurer<O, B>> result = new ArrayList<AnnotationConfigurer<O, B>>();
		for (List<AnnotationConfigurer<O, B>> configs : this.postConfigurers.values()) {
			result.addAll(configs);
		}
		return result;
	}

	/**
	 * Determines if the object is unbuilt.
	 *
	 * @return true, if unbuilt else false
	 */
	private boolean isUnbuilt() {
		synchronized (mainConfigurers) {
			return buildState == BuildState.UNBUILT;
		}
	}

	/**
	 * The build state for the application
	 */
	private static enum BuildState {

		/**
		 * This is the state before the {@link AnnotationBuilder#build()} is invoked
		 */
		UNBUILT(0),

		/**
		 * The state from when {@link AnnotationBuilder#build()} is first invoked until
		 * all the {@link AnnotationConfigurer#init(AnnotationBuilder)} methods have
		 * been invoked.
		 */
		INITIALIZING_MAINS(1),

		/**
		 * The state from after all main
		 * {@link AnnotationConfigurer#init(AnnotationBuilder)}
		 * have been invoked until after all the
		 * {@link AnnotationConfigurer#configure(AnnotationBuilder)}
		 * methods have been invoked.
		 */
		CONFIGURING_MAINS(2),

		/**
		 * The state from after all post
		 * {@link AnnotationConfigurer#init(AnnotationBuilder)}
		 * have been invoked until after all the
		 * {@link AnnotationConfigurer#configure(AnnotationBuilder)}
		 * methods have been invoked.
		 */
		CONFIGURING_POSTS(3),

		/**
		 * From the point after all the
		 * {@link AnnotationConfigurer#configure(AnnotationBuilder)}
		 * have completed to just after
		 * {@link AbstractConfiguredAnnotationBuilder#performBuild()}.
		 */
		BUILDING(4),

		/**
		 * After the object has been completely built.
		 */
		BUILT(5);

		private final int order;

		BuildState(int order) {
			this.order = order;
		}

		/**
		 * Checks if is initializing.
		 *
		 * @return true, if is initializing
		 */
		public boolean isInitializing() {
			return INITIALIZING_MAINS.order == order;
		}

		/**
		 * Determines if the state is CONFIGURING or later
		 *
		 * @return true, if configured
		 */
		public boolean isConfigured() {
			return order >= CONFIGURING_MAINS.order;
		}
	}

}
