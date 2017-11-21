/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.service;

import org.springframework.statemachine.StateMachine;

/**
 * Service class helping to persist and restore {@link StateMachine}s
 * in a runtime environment.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineService<S, E> {

	/**
	 * Acquires the state machine.
	 *
	 * @param machineId the machine id
	 * @return the state machine
	 */
	StateMachine<S, E> acquireStateMachine(String machineId);

	/**
	 * Release the state machine.
	 *
	 * @param machineId the machine id
	 */
	void releaseStateMachine(String machineId);
}
