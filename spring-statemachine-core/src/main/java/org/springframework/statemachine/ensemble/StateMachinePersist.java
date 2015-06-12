/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.ensemble;

import org.springframework.statemachine.StateMachineContext;

/**
 * {@code StateMachinePersist} is an interface handling serialization
 * logic of a {@link StateMachineContext}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachinePersist<S, E> {

	/**
	 * Serialize a {@link StateMachineContext}.
	 *
	 * @param context the state machine context
	 * @return the serialized data
	 */
	byte[] serialize(StateMachineContext<S, E> context);

	/**
	 * Deserialize a data into a {@link StateMachineContext}.
	 *
	 * @param data the data
	 * @return the state machine context
	 */
	StateMachineContext<S, E> deserialize(byte[] data);

}
