/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.config.model;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.Assert;

/**
 * Base implementation of a {@link StateMachineModelFactory} providing
 * some common grounds for various implementations.
 *
 * This factory is able to resolve {@link Action}s or {@link Guard}s either from
 * a {@link BeanFactory} if knows, or from manually registered instances. Manually
 * registered actions or guards are needed if those are not created as beans or if
 * whole state machine is working outside of an application context.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractStateMachineModelFactory<S, E>
		implements StateMachineComponentResolver<S, E>, StateMachineModelFactory<S, E>, BeanFactoryAware, ResourceLoaderAware {

	private BeanFactory beanFactory;
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private StateMachineComponentResolver<S, E> stateMachineComponentResolver;
	private final DefaultStateMachineComponentResolver<S, E> internalResolver = new DefaultStateMachineComponentResolver<S, E>();

	/**
	 * Instantiates a new abstract state machine model factory.
	 */
	public AbstractStateMachineModelFactory() {
	}

	/**
	 * Instantiates a new abstract state machine model factory.
	 *
	 * @param resourceLoader the resource loader
	 * @param stateMachineComponentResolver the state machine component resolver
	 */
	public AbstractStateMachineModelFactory(ResourceLoader resourceLoader,
			StateMachineComponentResolver<S, E> stateMachineComponentResolver) {
		this.resourceLoader = resourceLoader;
		this.stateMachineComponentResolver = stateMachineComponentResolver;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		internalResolver.setBeanFactory(beanFactory);
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "resourceLoader cannot be null");
		this.resourceLoader = resourceLoader;
	}

	@Override
	public StateMachineModel<S, E> build(String machineId) {
		return build();
	}

	@Override
	public abstract StateMachineModel<S, E> build();

	/**
	 * Sets the state machine component resolver.
	 *
	 * @param stateMachineComponentResolver the state machine component resolver
	 */
	public void setStateMachineComponentResolver(StateMachineComponentResolver<S, E> stateMachineComponentResolver) {
		this.stateMachineComponentResolver = stateMachineComponentResolver;
	}

	/**
	 * Register {@link Action} into factory with a given id.
	 *
	 * @param id the id
	 * @param action the action
	 */
	public void registerAction(String id, Action<S, E> action) {
		internalResolver.registerAction(id, action);
	}

	/**
	 * Register {@link Guard} into factory with a given id.
	 *
	 * @param id the id
	 * @param guard the guard
	 */
	public void registerGuard(String id, Guard<S, E> guard) {
		internalResolver.registerGuard(id, guard);
	}

	/**
	 * Gets the state machine component resolver.
	 *
	 * @return the state machine component resolver
	 */
	public StateMachineComponentResolver<S, E> getStateMachineComponentResolver() {
		return stateMachineComponentResolver;
	}

	/**
	 * Resolve action.
	 *
	 * @param id the id
	 * @return the action
	 */
	public Action<S, E> resolveAction(String id) {
		Action<S, E> a = internalResolver.resolveAction(id);
		if (a == null && stateMachineComponentResolver != null) {
			a = stateMachineComponentResolver.resolveAction(id);
		}
		if (a == null) {
			throw new RuntimeException("Can't resolve action with id " + id + " either from registered actions nor beanfactory");
		}
		return a;
	}

	/**
	 * Resolve guard.
	 *
	 * @param id the id
	 * @return the guard
	 */
	public Guard<S, E> resolveGuard(String id) {
		Guard<S, E> a = internalResolver.resolveGuard(id);
		if (a == null && stateMachineComponentResolver != null) {
			a = stateMachineComponentResolver.resolveGuard(id);
		}
		if (a == null) {
			throw new RuntimeException("Can't resolve guard with id " + id + " either from registered guards nor beanfactory");
		}
		return a;
	}

	/**
	 * Gets the bean factory.
	 *
	 * @return the bean factory
	 */
	protected final BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Gets the resource loader.
	 *
	 * @return the resource loader
	 */
	protected ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
}
