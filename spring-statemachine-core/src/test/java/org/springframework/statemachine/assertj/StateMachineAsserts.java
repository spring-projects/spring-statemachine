/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.assertj;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;

/**
 * Entry point for all {@code assertj} definitions for a {@code StateMachine}.
 * <p>
 * NOTE: we build assertj features here in core tests before moving them into spring-statemachine-test.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineAsserts {

	/**
	 * Creates a new instance of {@link StateContextAssert} allowing to perform assertions on it.
	 *
	 * @param stateContext the state context
	 * @return the created assertion object.
	 */
	public static StateContextAssert assertThat(StateContext<?, ?> stateContext) {
		return new StateContextAssert(stateContext);
	}

	/**
	 * Creates a new instance of {@link StateMachineAssert} allowing to perform assertions on it.
	 *
	 * @param stateMachine the state machine
	 * @return the created assertion object.
	 */
	public static StateMachineAssert assertThat(StateMachine<?, ?> stateMachine) {
		return new StateMachineAssert(stateMachine);
	}

	/**
	 * Creates a new instance of {@link StateMachineEventResultAssert} allowing to perform assertions on it.
	 *
	 * @param stateMachineEventResult the state machine event result
	 * @return the created assertion object.
	 */
	public static StateMachineEventResultAssert assertThat(StateMachineEventResult<?, ?> stateMachineEventResult) {
		return new StateMachineEventResultAssert(stateMachineEventResult);
	}
}
