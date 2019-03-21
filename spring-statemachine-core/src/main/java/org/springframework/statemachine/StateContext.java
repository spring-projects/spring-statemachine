/*
 * Copyright 2015-2016 the original author or authors.
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

import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * {@code StateContext} is representing of a current context used in
 * various stages in a state machine execution. These include for example
 * {@link Transition}s, {@link Action}s and {@link Guard}s order to get access
 * to event headers and {@link ExtendedState}.
 * <p>
 * Context really is not a current state of a state machine but
 * more like a snapshot of where state machine is when this context
 * is passed to various methods.
 *
 * @author Janne Valkealahti
 *
 */
public interface StateContext<S, E> {

	/**
	 * Gets the stage this context is attached.
	 *
	 * @return the stage
	 */
	Stage getStage();

	/**
	 * Gets the message associated with a context. Message may be null if transition
	 * is not triggered by a signal.
	 *
	 * @return the message
	 */
	Message<E> getMessage();

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

	/**
	 * Gets the source state of this context. Generally source
	 * is where a state machine is coming from which may be different
	 * than what the transition source is.
	 *
	 * @return the source state
	 */
	State<S, E> getSource();

	/**
	 * Gets the source states of this context. Multiple sources are
	 * only valid during a context when machine is joining from multiple
	 * orthogonal regions.
	 *
	 * @return the source state
	 * @see #getSource()
	 */
	Collection<State<S, E>> getSources();

	/**
	 * Gets the target state of this context. Generally target
	 * is where a state machine going to which may be different
	 * than what the transition target is.
	 *
	 * @return the target state
	 */
	State<S, E> getTarget();

	/**
	 * Gets the target states of this context. Multiple targets are
	 * only valid during a context when machine is forking into multiple
	 * orthogonal regions.
	 *
	 * @return the target states
	 * @see #getTarget()
	 */
	Collection<State<S, E>> getTargets();

	/**
	 * Gets the exception associated with a context.
	 *
	 * @return the exception
	 */
	Exception getException();

	/**
	 * Enumeration of possible stages context is attached.
	 */
	public static enum Stage {
		EVENT_NOT_ACCEPTED,
		EXTENDED_STATE_CHANGED,
		STATE_CHANGED,
		STATE_ENTRY,
		STATE_EXIT,
		STATEMACHINE_ERROR,
		STATEMACHINE_START,
		STATEMACHINE_STOP,
		TRANSITION,
		TRANSITION_START,
		TRANSITION_END;
	}
}
