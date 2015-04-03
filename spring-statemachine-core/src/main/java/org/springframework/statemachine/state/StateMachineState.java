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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * A {@link State} implementation where state is wrapped in a substatemachine.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineState<S, E> extends AbstractState<S, E> {

	private final Collection<S> ids;

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param id the state identifier
	 * @param submachine the submachine
	 */
	public StateMachineState(S id, StateMachine<S, E> submachine) {
		super(id, null, null, null, null, submachine);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param id the state identifier
	 * @param submachine the submachine
	 * @param deferred the deferred
	 */
	public StateMachineState(S id, StateMachine<S, E> submachine, Collection<E> deferred) {
		super(id, deferred, null, null, null, submachine);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param id the state identifier
	 * @param submachine the submachine
	 * @param pseudoState the pseudo state
	 */
	public StateMachineState(S id, StateMachine<S, E> submachine, PseudoState pseudoState) {
		super(id, null, null, null, pseudoState, submachine);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param id the state identifier
	 * @param submachine the submachine
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public StateMachineState(S id, StateMachine<S, E> submachine, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState pseudoState) {
		super(id, deferred, entryActions, exitActions, pseudoState, submachine);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	/**
	 * Instantiates a new state machine state.
	 *
	 * @param id the state identifier
	 * @param submachine the submachine
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public StateMachineState(S id, StateMachine<S, E> submachine, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
		super(id, deferred, entryActions, exitActions, null, submachine);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	@Override
	public Collection<S> getIds() {

		Collection<S> ret = new ArrayList<S>(ids);
		State<S, E> state = getSubmachine().getState();
		if (state != null) {
			ret.addAll(state.getIds());
		}
		return ret;
	}

	@Override
	public Collection<State<S, E>> getStates() {
		ArrayList<State<S, E>> states = new ArrayList<State<S, E>>();
		states.add(this);
		for (State<S, E> s : getSubmachine().getStates()) {
			states.addAll(s.getStates());
		}
		return states;
	}

	@Override
	public void exit(E event, StateContext<S, E> context) {
		// don't stop if it looks like we're coming back
		// stop would cause start with entry which would
		// enable default transition and state
		if (getSubmachine().getState() != null && context.getTransition().getSource().getId() != getSubmachine().getState().getId()) {
			getSubmachine().stop();
		}
		Collection<? extends Action<S, E>> actions = getExitActions();
		if (actions != null && !isLocal(context)) {
			for (Action<S, E> action : actions) {
				action.execute(context);
			}
		}
	}

	@Override
	public void entry(E event, StateContext<S, E> context) {
		Collection<? extends Action<S, E>> actions = getEntryActions();
		if (actions != null && !isLocal(context)) {
			for (Action<S, E> action : actions) {
				action.execute(context);
			}
		}

		if (getPseudoState() != null && getPseudoState().getKind() == PseudoStateKind.INITIAL) {
			getSubmachine().start();
		}
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		StateMachine<S, E> machine = getSubmachine();
		if (machine != null) {
			return machine.sendEvent(event);
		}
		return super.sendEvent(event);
	}

	private boolean isLocal(StateContext<S, E> context) {
		Transition<S, E> transition = context.getTransition();
		if (transition != null && TransitionKind.LOCAL == transition.getKind() && this == transition.getTarget()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "StateMachineState [getIds()=" + getIds() + ", toString()=" + super.toString() + ", getClass()="
				+ getClass() + "]";
	}

}
