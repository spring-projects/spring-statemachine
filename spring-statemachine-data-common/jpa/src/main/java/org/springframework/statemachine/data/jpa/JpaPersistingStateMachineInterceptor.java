/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.data.jpa;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.persist.AbstractPersistingStateMachineInterceptor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.util.Assert;

/**
 * {@code JPA} implementation of a {@link AbstractPersistingStateMachineInterceptor}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @param <T> the type of persister context object
 */
public class JpaPersistingStateMachineInterceptor<S, E, T> extends AbstractPersistingStateMachineInterceptor<S, E, T>
		implements StateMachineRuntimePersister<S, E, T> {

	private final JpaRepositoryStateMachinePersist<S, E> persist;

	/**
	 * Instantiates a new jpa persisting state machine interceptor.
	 *
	 * @param jpaStateMachineRepository the jpa state machine repository
	 */
	public JpaPersistingStateMachineInterceptor(JpaStateMachineRepository jpaStateMachineRepository) {
		Assert.notNull(jpaStateMachineRepository, "'jpaStateMachineRepository' must be set");
		this.persist = new JpaRepositoryStateMachinePersist<S, E>(jpaStateMachineRepository);
	}

	/**
	 * Instantiates a new jpa persisting state machine interceptor.
	 *
	 * @param persist the persist
	 */
	public JpaPersistingStateMachineInterceptor(JpaRepositoryStateMachinePersist<S, E> persist) {
		Assert.notNull(persist, "'persist' must be set");
		this.persist = persist;
	}

	@Override
	public StateMachineInterceptor<S, E> getInterceptor() {
		return this;
	}

	@Override
	public void write(StateMachineContext<S, E> context, T contextObj) throws Exception {
		persist.write(context, contextObj);
	}

	@Override
	public StateMachineContext<S, E> read(Object contextObj) throws Exception {
		return persist.read(contextObj);
	}
}
