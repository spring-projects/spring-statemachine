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

import org.springframework.messaging.Message;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.transition.Transition;

import java.util.Collection;
import java.util.UUID;

/**
 * Specialisation of a {@link StateMachine} using objects
 * as its {@link State} and event types.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ObjectStateMachine<S, E> extends AbstractStateMachine<S, E> {

	/**
	 * Instantiates a new enum state machine.
	 *
	 * @param states the states
	 * @param transitions the transitions
	 * @param initialState the initial state
	 */
	public ObjectStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState) {
		super(states, transitions, initialState);
	}

	/**
	 * Instantiates a new enum state machine.
	 *
	 * @param states the states
	 * @param transitions the transitions
	 * @param initialState the initial state
	 * @param initialTransition the initial transition
	 * @param initialEvent the initial event
	 * @param extendedState the extended state
	 * @param uuid the given uuid.
	 */
	public ObjectStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, Transition<S, E> initialTransition,
			Message<E> initialEvent, ExtendedState extendedState, UUID uuid) {
		super(states, transitions, initialState, initialTransition, initialEvent, extendedState, uuid);
	}

}
