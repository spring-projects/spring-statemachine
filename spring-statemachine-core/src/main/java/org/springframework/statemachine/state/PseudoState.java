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
package org.springframework.statemachine.state;

import org.springframework.statemachine.StateContext;

/**
 * A {@code PseudoState} is an abstraction that encompasses different types of
 * transient states or vertices in the state machine.
 *
 * <p>
 * Pseudostates are typically used to connect multiple transitions into more
 * complex state transitions paths. For example, by combining a transition
 * entering a fork pseudostate with a set of transitions exiting the fork
 * pseudostate, we get a compound transition that leads to a set of orthogonal
 * target states.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface PseudoState<S, E> {

	/**
	 * Gets the pseudostate kind.
	 *
	 * @return the pseudostate kind
	 */
	PseudoStateKind getKind();

	/**
	 * Initiate an entry sequence for the state and return a next
	 * state where state machine should go.
	 *
	 * @param context the context
	 * @return the next state or null
	 */
	State<S, E> entry(StateContext<S, E> context);

	/**
	 * Initiate an exit sequence for the state.
	 *
	 * @param context the context
	 */
	void exit(StateContext<S, E> context);
	
	/**
	 * Registers a new {@link PseudoStateListener}.
	 *
	 * @param listener the listener
	 */
	void addPseudoStateListener(PseudoStateListener<S, E> listener);

}
