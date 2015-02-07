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

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;

/**
 * A {@link State} implementation where state is wrapped in a substatemachine.
 * 
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineState<S, E> extends AbstractState<S, E> {

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param submachine the submachine
	 */
	public StateMachineState(StateMachine<S, E> submachine) {
		super(null, null, null, null, submachine);
	}

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param submachine the submachine
	 * @param deferred the deferred
	 */
	public StateMachineState(StateMachine<S, E> submachine, Collection<E> deferred) {
		super(deferred, null, null, null, submachine);
	}

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param submachine the submachine
	 * @param pseudoState the pseudo state
	 */
	public StateMachineState(StateMachine<S, E> submachine, PseudoState pseudoState) {
		super(null, null, null, pseudoState, submachine);
	}
	
	/**
	 * Instantiates a new state machine state.
	 *
	 * @param submachine the submachine
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public StateMachineState(StateMachine<S, E> submachine, Collection<E> deferred, Collection<Action> entryActions, Collection<Action> exitActions,
			PseudoState pseudoState) {
		super(deferred, entryActions, exitActions, pseudoState, submachine);
	}

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param submachine the submachine
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public StateMachineState(StateMachine<S, E> submachine, Collection<E> deferred, Collection<Action> entryActions, Collection<Action> exitActions) {
		super(deferred, entryActions, exitActions, null, submachine);
	}

	@Override
	public Collection<S> getIds() {
		return getSubmachine().getState().getIds();
	}
	
}
