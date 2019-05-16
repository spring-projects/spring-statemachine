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

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.transition.Transition;

/**
 * {@code StateContext} is representing a current context used in
 * {@link Transition}s, {@link Action}s and {@link Guard}s order to get access
 * to event headers and {@link ExtendedState}.
 *
 * @author Janne Valkealahti
 *
 */
public interface StateContext<S, E> {

	/**
	 * Gets the event associated with a context. Event may be null if transition
	 * is not triggered by a signal.
	 * 
	 * @return the event
	 */
	E getEvent();
	
	/**
	 * Gets the event message headers.
	 *
	 * @return the event message headers
	 */
	MessageHeaders getMessageHeaders();

	/**
	 * Gets the message header. If header is not a {@code String} object's
	 * {@link Object#toString()} method is used to resolve a key name.
	 *
	 * @param header the header
	 * @return the message header
	 */
	Object getMessageHeader(Object header);

	/**
	 * Gets the state machine extended state.
	 *
	 * @return the state machine extended state
	 */
	ExtendedState getExtendedState();

	/**
	 * Gets the transition.
	 *
	 * @return the transition
	 */
	Transition<S, E> getTransition();

	/**
	 * Gets the state machine.
	 *
	 * @return the state machine
	 */
	StateMachine<S, E> getStateMachine();

}
