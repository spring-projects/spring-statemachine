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
 * Generic event representing that state has been changed.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("serial")
public class OnStateChangedEvent extends StateMachineEvent {

	private final State<?, ?> sourceState;
	private final State<?, ?> targetState;

	/**
	 * Instantiates a new on state changed event.
	 *
	 * @param source the component that published the event (never {@code null})
	 * @param sourceState the source state
	 * @param targetState the target state
	 */
	public OnStateChangedEvent(Object source, State<?, ?> sourceState, State<?, ?> targetState) {
		super(source);
		this.sourceState = sourceState;
		this.targetState = targetState;
	}

	/**
	 * Gets the source state for this event.
	 *
	 * @return the source state
	 */
	public State<?, ?> getSourceState() {
		return sourceState;
	}

	/**
	 * Gets the target state for this event.
	 *
	 * @return the target state
	 */
	public State<?, ?> getTargetState() {
		return targetState;
	}

	@Override
	public String toString() {
		return "OnStateChangedEvent [sourceState=" + sourceState + ", targetState=" + targetState + "]";
	}

}
