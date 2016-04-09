/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.config.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

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
public abstract class AbstractStateMachineModelFactory<S, E> implements StateMachineComponentResolver<S, E>, BeanFactoryAware {

	private BeanFactory beanFactory;
	private final Map<String, Action<S, E>> registeredActions = new HashMap<>();
	private final Map<String, Guard<S, E>> registeredGuards = new HashMap<>();

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Register {@link Action} into factory with a given id.
	 *
	 * @param id the id
	 * @param action the action
	 */
	public void registerAction(String id, Action<S, E> action) {
		registeredActions.put(id, action);
	}

	/**
	 * Register {@link Guard} into factory with a given id.
	 *
	 * @param id the id
	 * @param guard the guard
	 */
	public void registerGuard(String id, Guard<S, E> guard) {
		registeredGuards.put(id, guard);
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
	 * Resolve action.
	 *
	 * @param id the id
	 * @return the action
	 */
	@SuppressWarnings("unchecked")
	public Action<S, E> resolveAction(String id) {
		Action<S, E> a = null;
		a = registeredActions.get(id);
		if (a == null && beanFactory != null) {
			a = beanFactory.getBean(id, Action.class);
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
	@SuppressWarnings("unchecked")
	public Guard<S, E> resolveGuard(String id) {
		Guard<S, E> g = null;
		g = registeredGuards.get(id);
		if (g == null && beanFactory != null) {
			g = beanFactory.getBean(id, Guard.class);
		}
		if (g == null) {
			throw new RuntimeException("Can't resolve guard with id " + id + " either from registered guards nor beanfactory");
		}
		return g;
	}
}
