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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

/**
 * Default implementation of a {@link StateMachineComponentResolver} which resolves
 * from a {@link BeanFactory} if given or from a manually registered actions and guards.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateMachineComponentResolver<S, E> implements StateMachineComponentResolver<S, E> {

	private BeanFactory beanFactory;
	private final Map<String, Action<S, E>> registeredActions;
	private final Map<String, Guard<S, E>> registeredGuards;

	/**
	 * Instantiates a new default state machine component resolver.
	 */
	public DefaultStateMachineComponentResolver() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new default state machine component resolver.
	 *
	 * @param registeredActions the registered actions
	 * @param registeredGuards the registered guards
	 */
	public DefaultStateMachineComponentResolver(Map<String, Action<S, E>> registeredActions, Map<String, Guard<S, E>> registeredGuards) {
		this(null, registeredActions, registeredGuards);
	}

	/**
	 * Instantiates a new default state machine component resolver.
	 *
	 * @param beanFactory the bean factory
	 * @param registeredActions the registered actions
	 * @param registeredGuards the registered guards
	 */
	public DefaultStateMachineComponentResolver(BeanFactory beanFactory, Map<String, Action<S, E>> registeredActions,
			Map<String, Guard<S, E>> registeredGuards) {
		this.beanFactory = beanFactory;
		this.registeredActions = registeredActions != null ? registeredActions : new HashMap<String, Action<S, E>>();
		this.registeredGuards =  registeredGuards != null ? registeredGuards : new HashMap<String, Guard<S, E>>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Action<S, E> resolveAction(String id) {
		Action<S, E> a = null;
		a = registeredActions.get(id);
		if (a == null && beanFactory != null) {
			a = beanFactory.getBean(id, Action.class);
		}
		return a;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Guard<S, E> resolveGuard(String id) {
		Guard<S, E> g = null;
		g = registeredGuards.get(id);
		if (g == null && beanFactory != null) {
			g = beanFactory.getBean(id, Guard.class);
		}
		return g;
	}

	/**
	 * Register {@link Action} with a given id.
	 *
	 * @param id the id
	 * @param action the action
	 */
	public void registerAction(String id, Action<S, E> action) {
		registeredActions.put(id, action);
	}

	/**
	 * Register {@link Guard} with a given id.
	 *
	 * @param id the id
	 * @param guard the guard
	 */
	public void registerGuard(String id, Guard<S, E> guard) {
		registeredGuards.put(id, guard);
	}

	/**
	 * Sets the bean factory.
	 *
	 * @param beanFactory the new bean factory
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
