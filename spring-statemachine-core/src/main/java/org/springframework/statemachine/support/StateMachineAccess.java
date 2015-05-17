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
package org.springframework.statemachine.support;

import org.springframework.statemachine.StateMachine;

/**
 * Functional interface for {@link StateMachine} to allow more programmetic
 * access to underlying functionality.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineAccess<S, E> {

	/**
	 * Execute given {@link StateMachineFunction} with all recursive regions.
	 *
	 * @param stateMachineAccess the state machine access
	 */
	void doWithAllRegions(StateMachineFunction<StateMachineAccess<S, E>> stateMachineAccess);

	/**
	 * Sets the relay state machine.
	 *
	 * @param stateMachine the state machine
	 */
	void setRelay(StateMachine<S, E> stateMachine);

}
