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
package org.springframework.statemachine.event;

import org.springframework.statemachine.StateMachine;

/**
 * Generic event representing that state machine has been started.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("serial")
public class OnStateMachineStart extends StateMachineEvent {

	private final StateMachine<?, ?> stateMachine;

	/**
	 * Instantiates a new on state exit event.
	 *
	 * @param source the source
	 * @param stateMachine the statemachine
	 */
	public OnStateMachineStart(Object source, StateMachine<?, ?> stateMachine) {
		super(source);
		this.stateMachine = stateMachine;
	}

	/**
	 * Gets the statemachine.
	 *
	 * @return the statemachine
	 */
	public StateMachine<?, ?> getStateMachine() {
		return stateMachine;
	}

	@Override
	public String toString() {
		return "OnStateMachineStart [stateMachine=" + stateMachine + "]";
	}

}
