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
package org.springframework.statemachine.persist;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineContextRepository;
import org.springframework.statemachine.StateMachinePersist;

/**
 * A {@link StateMachinePersist} using a generic {@link StateMachineContextRepository}
 * for persisting {@link StateMachineContext}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class RepositoryStateMachinePersist<S, E> implements StateMachinePersist<S, E, String> {

	private final StateMachineContextRepository<S, E, StateMachineContext<S,E>> repository;

	/**
	 * Instantiates a new repository state machine persist.
	 *
	 * @param repository the repository
	 */
	public RepositoryStateMachinePersist(StateMachineContextRepository<S, E, StateMachineContext<S, E>> repository) {
		this.repository = repository;
	}

	@Override
	public void write(StateMachineContext<S, E> context, String contextOjb) throws Exception {
		repository.save(context, contextOjb);
	}

	@Override
	public StateMachineContext<S, E> read(String contextOjb) throws Exception {
		return repository.getContext(contextOjb);
	}

}
