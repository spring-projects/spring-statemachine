/*
 * Copyright 2015-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
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

	private final static Log log = LogFactory.getLog(JoinPseudoState.class);
	private final List<State<S, E>> joins;
	private final JoinTracker tracker;
	private final List<JoinStateData<S, E>> joinTargets;

	/**
	 * Instantiates a new join pseudo state.
	 *
	 * @param joins the joins
	 * @param joinTargets the target states
	 */
	public JoinPseudoState(List<State<S, E>> joins, List<JoinStateData<S, E>> joinTargets) {
		super(PseudoStateKind.JOIN);
		this.joins = joins;
		this.joinTargets = joinTargets;
		this.tracker = new JoinTracker();
	}

	@Override
	public State<S, E> entry(StateContext<S, E> context) {
		if (!tracker.isNotified()) {
			return null;
		}
		State<S, E> s = null;
		for (JoinStateData<S, E> c : joinTargets) {
			s = c.getState();
			if (c.guard != null && evaluateInternal(c.guard, context)) {
				break;
			}
		}
		return s;
	}

	@Override
	public void exit(StateContext<S, E> context) {
		tracker.reset();
	}

	/**
	 * Gets the join states.
	 *
	 * @return the joins
	 */
	public List<State<S, E>> getJoins() {
		return joins;
	}

	/**
	 * Resets join state according to given state ids
	 * so that we can continue with correct tracking.
	 *
	 * @param ids the state id's
	 */
	public void reset(Collection<S> ids) {
		tracker.reset(ids);
	}

	private boolean evaluateInternal(Guard<S, E> guard, StateContext<S, E> context) {
		try {
			return guard.evaluate(context);
		} catch (Throwable t) {
			log.warn("Deny guard due to throw as GUARD should not error", t);
			return false;
		}
	}

	private class JoinTracker {

		private final List<State<S, E>> track;
		private volatile boolean notified = false;

		public JoinTracker() {
			this.track = new ArrayList<State<S,E>>(joins);
			for (State<S, E> tt : joins) {
				final State<S, E> t = tt;
				t.addStateListener(new StateListenerAdapter<S, E>() {

					@Override
					public void onComplete(StateContext<S, E> context) {
						boolean trackSizeZero = false;
						synchronized (track) {
							track.remove(t);
							if (track.size() == 0) {
								trackSizeZero = true;
							}
						}
						if (!notified && trackSizeZero) {
							log.debug("Join complete");
							notified = true;
							notifyContext(new DefaultPseudoStateContext<S, E>(JoinPseudoState.this, PseudoAction.JOIN_COMPLETED));
						}
					}
				});
			}
		}

		void reset() {
			track.clear();
			track.addAll(joins);
			notified = false;
		}

		void reset(Collection<S> ids) {
			track.clear();
			for (State<S, E> j : joins) {
				if (!ids.contains(j.getId())) {
					track.add(j);
				}
			}
			notified = false;
		}

		public boolean isNotified() {
			return notified;
		}
	}

	/**
	 * Data class wrapping join {@link State} and {@link Guard}
	 * together.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 */
	public static class JoinStateData<S, E> {
		private final StateHolder<S, E> state;
		private final Guard<S, E> guard;

		/**
		 * Instantiates a new join state data.
		 *
		 * @param state the state holder
		 * @param guard the guard
		 */
		public JoinStateData(StateHolder<S, E> state, Guard<S, E> guard) {
			Assert.notNull(state, "Holder must be set");
			this.state = state;
			this.guard = guard;
		}

		/**
		 * Gets the state holder.
		 *
		 * @return the state holder
		 */
		public StateHolder<S, E> getStateHolder() {
			return state;
		}

		/**
		 * Gets the state.
		 *
		 * @return the state
		 */
		public State<S, E> getState() {
			return state.getState();
		}

		/**
		 * Gets the guard.
		 *
		 * @return the guard
		 */
		public Guard<S, E> getGuard() {
			return guard;
		}
	}
}
