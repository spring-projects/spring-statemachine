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
	 * Acquires the state machine. Machine from this method
	 * is returned started.
	 *
	 * @param machineId the machine id
	 * @return the state machine
	 * @see #acquireStateMachine(String, boolean)
	 */
	StateMachine<S, E> acquireStateMachine(String machineId);

	/**
	 * Acquires the state machine.
	 *
	 * @param machineId the machine id
	 * @param start indicating if machine should be returned started
	 * @return the state machine
	 */
	StateMachine<S, E> acquireStateMachine(String machineId, boolean start);

	/**
	 * Release the state machine. Machine with this method
	 * is stopped.
	 *
	 * @param machineId the machine id
	 * @see #releaseStateMachine(String, boolean)
	 */
	void releaseStateMachine(String machineId);

	/**
	 * Release state machine.
	 *
	 * @param machineId the machine id
	 * @param stop indicating if machine should be stopped
	 */
	void releaseStateMachine(String machineId, boolean stop);
}
