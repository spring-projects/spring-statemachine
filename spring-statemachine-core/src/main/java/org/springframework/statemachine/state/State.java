/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.state;

import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * {@code State} is an interface representing possible state in a state machine.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface State<S, E> {

	/**
	 * Send an event {@code E} wrapped with a {@link Message} to the state.
	 *
	 * @param event the wrapped event to send
	 */
	void sendEvent(Message<E> event);

	/**
	 * Initiate an exit sequence for the state.
	 *
	 * @param event the event
	 * @param context the context
	 */
	void exit(E event, StateContext<S, E> context);

	/**
	 * Initiate an entry sequence for the state.
	 *
	 * @param event the event
	 * @param context the context
	 */
	void entry(E event, StateContext<S, E> context);

	/**
	 * Gets the state identifier.
	 *
	 * @return the state identifiers
	 */
	S getId();

	/**
	 * Gets the state identifiers. Usually returned collection contains only one
	 * identifier except in a case where state is an orthogonal.
	 *
	 * @return the state identifiers
	 */
	Collection<S> getIds();

	/**
	 * Gets a {@link PseudoState} attached to a {@code State}.
	 * {@link PseudoState} is not required and thus this method return
	 * {@code NULL} if it's not set.
	 *
	 * @return pseudostate or null if state doesn't have one
	 */
	PseudoState getPseudoState();

	/**
	 * Gets the deferred events for this state.
	 *
	 * @return the state deferred events
	 */
	Collection<E> getDeferredEvents();

	/**
	 * Gets {@link Action}s executed entering in this state.
	 *
	 * @return the state entry actions
	 */
	Collection<? extends Action<S, E>> getEntryActions();

	/**
	 * Gets {@link Action}s executed exiting from this state.
	 *
	 * @return the state exit actions
	 */
	Collection<? extends Action<S, E>> getExitActions();

	/**
	 * Checks if state is a simple state. A simple state does not have any
	 * regions and it does not refer to any submachine state machine.
	 *
	 * @return true, if state is a simple state
	 */
	boolean isSimple();

	/**
	 * Checks if state is a composite state. A composite state is a state that
	 * contains at least one region.
	 *
	 * @return true, if state is a composite state
	 */
	boolean isComposite();

	/**
	 * Checks if state is an orthogonal state. An orthogonal composite state
	 * contains two or more regions. If this method returns {@code TRUE},
	 * {@link #isComposite()} will also always return {@code TRUE}.
	 *
	 * @return true, if state is an orthogonal state
	 */
	boolean isOrthogonal();

	/**
	 * Checks if state is a submachine state. This kind of state refers to a
	 * state machine(submachine).
	 *
	 * @return true, if state is a submachine state
	 */
	boolean isSubmachineState();

}
