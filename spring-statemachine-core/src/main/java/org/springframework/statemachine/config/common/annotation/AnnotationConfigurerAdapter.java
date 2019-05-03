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
import java.util.List;

import org.springframework.core.GenericTypeResolver;

/**
 * A base class for {@link AnnotationConfigurer} that allows subclasses to only
 * implement the methods they are interested in. It also provides a mechanism
 * for using the {@link AnnotationConfigurer} and when done gaining access to the
 * {@link AnnotationBuilder} that is being configured.
 *
 * @author Rob Winch
 * @author Janne Valkealahti
 *
 * @param <O> The Object being built by B
 * @param <I> The interface of type B
 * @param <B> The Builder that is building O and is configured by {@link AnnotationConfigurerAdapter}
 */
public abstract class AnnotationConfigurerAdapter<O,I,B extends AnnotationBuilder<O>>
		implements AnnotationConfigurer<O,B> {

	private B builder;

	private CompositeObjectPostProcessor objectPostProcessor = new CompositeObjectPostProcessor();

	@Override
	public void init(B builder) throws Exception {}

	@Override
	public void configure(B builder) throws Exception {}

	/**
	 * Return the {@link AnnotationBuilder} when done using the
	 * {@link AnnotationConfigurer}. This is useful for method chaining.
	 *
	 * @return the {@link AnnotationBuilder}
	 */
	@SuppressWarnings("unchecked")
	public I and() {
		// we're either casting to itself or its interface
		return (I) getBuilder();
	}

	/**
	 * Gets the {@link AnnotationBuilder}. Cannot be null.
	 *
	 * @return the {@link AnnotationBuilder}
	 * @throws IllegalStateException if AnnotationBuilder is null
	 */
	protected final B getBuilder() {
		if(builder == null) {
			throw new IllegalStateException("annotationBuilder cannot be null");
		}
		return builder;
	}

	/**
	 * Adds an {@link ObjectPostProcessor} to be used for this adapter. The
	 * default implementation does nothing to the object.
	 *
	 * @param objectPostProcessor the {@link ObjectPostProcessor} to use
	 */
	public void addObjectPostProcessor(ObjectPostProcessor<?> objectPostProcessor) {
		this.objectPostProcessor.addObjectPostProcessor(objectPostProcessor);
	}

	/**
	 * Sets the {@link AnnotationBuilder} to be used. This is automatically set
	 * when using
	 * {@link AbstractConfiguredAnnotationBuilder#apply(AnnotationConfigurerAdapter)}
	 *
	 * @param builder the {@link AnnotationBuilder} to set
	 */
	public void setBuilder(B builder) {
		this.builder = builder;
	}

	/**
	 * An {@link ObjectPostProcessor} that delegates work to numerous
	 * {@link ObjectPostProcessor} implementations.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final class CompositeObjectPostProcessor implements ObjectPostProcessor<Object> {

		private List<ObjectPostProcessor<? extends Object>> postProcessors = new ArrayList<ObjectPostProcessor<?>>();

		@Override
		public Object postProcess(Object object) {
			for(ObjectPostProcessor opp : postProcessors) {
				Class<?> oppClass = opp.getClass();
				Class<?> oppType = GenericTypeResolver.resolveTypeArgument(oppClass,ObjectPostProcessor.class);
				if(oppType == null || oppType.isAssignableFrom(object.getClass())) {
					object = opp.postProcess(object);
				}
			}
			return object;
		}

		/**
		 * Adds an {@link ObjectPostProcessor} to use
		 *
		 * @param objectPostProcessor the {@link ObjectPostProcessor} to add
		 * @return true if the {@link ObjectPostProcessor} was added, else false
		 */
		private boolean addObjectPostProcessor(ObjectPostProcessor<? extends Object> objectPostProcessor) {
			return this.postProcessors.add(objectPostProcessor);
		}

	}

	@Override
	public boolean isAssignable(AnnotationBuilder<O> builder) {
		return true;
	}

}
