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
import java.util.List;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.PseudoStateContext.PseudoAction;
import org.springframework.util.Assert;

/**
 * Join implementation of a {@link PseudoState}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class JoinPseudoState<S, E> extends AbstractPseudoState<S, E> {

	private final List<State<S, E>> joins;
	private volatile JoinTracker tracker;
	private final StateHolder<S, E> state;

	/**
	 * Instantiates a new join pseudo state.
	 *
	 * @param joins the joins
	 * @param state the holder for target state
	 */
	public JoinPseudoState(List<State<S, E>> joins, StateHolder<S, E> state) {
		super(PseudoStateKind.JOIN);
		Assert.notNull(state, "Holder must be set");
		this.joins = joins;
		this.state = state;
	}

	@Override
	public State<S, E> entry(StateContext<S, E> context) {
		tracker = new JoinTracker(this, new ArrayList<State<S,E>>(joins));
		context.getStateMachine().addStateListener(tracker);
		return state.getState();
	}

	@Override
	public void exit(StateContext<S, E> context) {
		if (context != null) {
			context.getStateMachine().removeStateListener(tracker);
		}
		tracker = null;
	}

	/**
	 * Gets the join states.
	 *
	 * @return the joins
	 */
	public List<State<S, E>> getJoins() {
		return joins;
	}

	private class JoinTracker extends StateMachineListenerAdapter<S, E> {

		private final PseudoState<S, E> pseudoState;
		private final List<State<S, E>> track;
		private volatile boolean notified = false;

		public JoinTracker(PseudoState<S, E> pseudoState, List<State<S, E>> track) {
			this.pseudoState = pseudoState;
			this.track = track;
		}

		@Override
		public synchronized void stateChanged(State<S, E> from, State<S, E> to) {
			if (!notified && track.size() > 0) {
				track.remove(to);
				if (track.size() == 0) {
					notified = true;
					notifyContext(new DefaultPseudoStateContext<S, E>(pseudoState, PseudoAction.JOIN_COMPLETED));
				}
			}
		}

	}

}
