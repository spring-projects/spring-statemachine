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

import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.State;

/**
 * {@code StateMachine} provides an APIs for generic finite state machine needed
 * for basic operations like working with states, events and a lifecycle.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachine<S, E> extends Region<S, E> {

	/**
	 * Gets the initial state {@code S}.
	 *
	 * @return initial state
	 */
	State<S,E> getInitialState();

	/**
	 * Gets the state machine extended state.
	 *
	 * @return extended state
	 */
	ExtendedState getExtendedState();

	/**
	 * Gets the state machine accessor.
	 *
	 * @return the state machine accessor
	 */
	StateMachineAccessor<S, E> getStateMachineAccessor();

	/**
	 * Sets the state machine error.
	 *
	 * @param exception the new state machine error
	 */
	void setStateMachineError(Exception exception);

	/**
	 * Checks for state machine error.
	 *
	 * @return true, if error has been set
	 */
	boolean hasStateMachineError();

}
