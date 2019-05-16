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
package org.springframework.statemachine.service;

import org.springframework.statemachine.StateMachineContext;

/**
 * Generic interface to handle serialisation in a state machine.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineSerialisationService<S, E> {

	/**
	 * Serialise state machine context into byte array.
	 *
	 * @param context the context
	 * @return the data as byte[]
	 * @throws Exception the exception when serialisation fails
	 */
	byte[] serialiseStateMachineContext(StateMachineContext<S, E> context) throws Exception;

	/**
	 * Deserialise state machine context from byte array.
	 *
	 * @param data the data
	 * @return the state machine context
	 * @throws Exception the exception when deserialisation fails
	 */
	StateMachineContext<S, E> deserialiseStateMachineContext(byte[] data) throws Exception;
}
