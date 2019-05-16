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
package org.springframework.statemachine;


/**
 * {@code StateMachinePersist} is an interface handling serialization
 * logic of a {@link StateMachineContext}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @param <T> the type of context object
 */
public interface StateMachinePersist<S, E, T> {

	/**
	 * Write a {@link StateMachineContext} into a persistent store
	 * with a context object {@code T}.
	 *
	 * @param context the context
	 * @param contextOjb the context ojb
	 * @throws Exception the exception
	 */
	void write(StateMachineContext<S, E> context, T contextOjb) throws Exception;

	/**
	 * Read a {@link StateMachineContext} from a persistent store
	 * with a context object {@code T}.
	 *
	 * @param contextOjb the context ojb
	 * @return the state machine context
	 * @throws Exception the exception
	 */
	StateMachineContext<S, E> read(T contextOjb) throws Exception;

}
