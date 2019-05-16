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
package org.springframework.statemachine.persist;

import org.springframework.statemachine.StateMachine;

/**
 * Interface persisting and restoring a {@link StateMachine} from
 * a persistent storage.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @param <T> the type of context object
 */
public interface StateMachinePersister<S, E, T> {

	/**
	 * Persist a state machine with a given context object.
	 *
	 * @param stateMachine the state machine
	 * @param contextOjb the context ojb
	 * @throws Exception the exception in case or any persist error
	 */
	void persist(StateMachine<S, E> stateMachine, T contextOjb) throws Exception;

	/**
	 * Reset a state machine with a given context object.
	 * Returned machine has been reseted and is ready to be used.
	 *
	 * @param stateMachine the state machine
	 * @param contextOjb the context ojb
	 * @return the state machine
	 * @throws Exception the exception in case or any persist error
	 */
	StateMachine<S, E> restore(StateMachine<S, E> stateMachine, T contextOjb) throws Exception;
}
