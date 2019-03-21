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
package org.springframework.statemachine.region;

import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * A region is an orthogonal part of either a composite state or a state
 * machine. It contains states and transitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface Region<S, E> {

	/**
	 * Gets the region and state machine unique id.
	 *
	 * @return the region and state machine unique id
	 */
	String getId();

	/**
	 * Start the region.
	 */
	void start();

	/**
	 * Stop the region.
	 */
	void stop();

	/**
	 * Send an event {@code E} wrapped with a {@link Message} to the region.
	 *
	 * @param event the wrapped event to send
	 * @return true if event was accepted
	 */
	boolean sendEvent(Message<E> event);

	/**
	 * Send an event {@code E} to the region.
	 *
	 * @param event the event to send
	 * @return true if event was accepted
	 */
	boolean sendEvent(E event);

	/**
	 * Gets the current {@link State}.
	 *
	 * @return current state
	 */
	State<S,E> getState();

	/**
	 * Gets the {@link State}s defined in this region. Returned collection is
	 * an unmodifiable copy because states in a state machine are immutable.
	 *
	 * @return immutable copy of states
	 */
	Collection<State<S, E>> getStates();

	/**
	 * Gets a {@link Transition}s for this region.
	 *
	 * @return immutable copy of transitions
	 */
	Collection<Transition<S,E>> getTransitions();

	/**
	 * Checks if region complete. Region is considered to be completed if it has
	 * reached its end state and no further event processing is happening.
	 *
	 * @return true, if complete
	 */
	boolean isComplete();

	/**
	 * Adds the state listener.
	 *
	 * @param listener the listener
	 */
	void addStateListener(StateMachineListener<S, E> listener);

	/**
	 * Removes the state listener.
	 *
	 * @param listener the listener
	 */
	void removeStateListener(StateMachineListener<S, E> listener);

}
