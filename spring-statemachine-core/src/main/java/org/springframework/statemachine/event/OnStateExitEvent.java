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

import org.springframework.statemachine.state.State;

/**
 * Generic event representing that state has been exited.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("serial")
public class OnStateExitEvent extends StateMachineEvent {

	private final State<?, ?> state;

	/**
	 * Instantiates a new on state exit event.
	 *
	 * @param source the source
	 * @param state the state
	 */
	public OnStateExitEvent(Object source, State<?, ?> state) {
		super(source);
		this.state = state;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public State<?, ?> getState() {
		return state;
	}

	@Override
	public String toString() {
		return "OnStateExitEvent [state=" + state + "]";
	}

}
