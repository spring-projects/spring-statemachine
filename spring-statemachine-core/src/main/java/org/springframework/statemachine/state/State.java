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
	 * Gets the state identifier.
	 *
	 * @return the identifier
	 */
	S getId();

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
	Collection<Action> getEntryActions();

	/**
	 * Gets {@link Action}s executed exiting from this state.
	 *
	 * @return the state exit actions
	 */
	Collection<Action> getExitActions();
	
}
