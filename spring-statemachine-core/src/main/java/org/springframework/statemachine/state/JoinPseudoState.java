/*
 * Copyright 2015-2020 the original author or authors.
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
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.state.PseudoStateContext.PseudoAction;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
	private final List<List<State<S, E>>> joins;
	private final JoinTracker tracker;
	private final List<JoinStateData<S, E>> joinTargets;

	/**
	 * Instantiates a new join pseudo state.
	 *
	 * @param joins the joins
	 * @param joinTargets the target states
	 */
	public JoinPseudoState(List<List<State<S, E>>> joins, List<JoinStateData<S, E>> joinTargets) {
		super(PseudoStateKind.JOIN);
		this.joins = joins;
		this.joinTargets = joinTargets;
		this.tracker = new JoinTracker();
	}

	@Override
	public Mono<State<S, E>> entry(StateContext<S, E> context) {
		return Mono.defer(() -> {
			if (!tracker.isNotified()) {
				return Mono.empty();
			}
			return Flux.fromIterable(joinTargets)
				.filterWhen(jst -> evaluateInternal(jst.guard, context))
				.next()
				.map(jst -> jst.getState());
		});
	}

	@Override
	public Mono<Void> exit(StateContext<S, E> context) {
		return Mono.fromRunnable(() -> {
			tracker.reset();
		});
	}

	/**
	 * Gets the join states.
	 *
	 * @return the joins
	 */
	public List<List<State<S, E>>> getJoins() {
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

	private Mono<Boolean> evaluateInternal(Function<StateContext<S, E>, Mono<Boolean>> guard, StateContext<S, E> context) {
		if (guard == null) {
			return Mono.just(true);
		}
		try {
			return guard.apply(context);
		} catch (Exception e) {
			log.warn("Deny guard due to throw as GUARD should not error");
			return Mono.just(false);
		}
	}

	private class JoinTracker {

		private final List<List<State<S, E>>> track;
		private volatile boolean notified = false;

		public JoinTracker() {
			this.track = new ArrayList<List<State<S,E>>>(joins.size());
			for (List<State<S, E>> list : joins) {
				this.track.add(new ArrayList<State<S,E>>(list));
				for (State<S, E> tt : list) {
					final State<S, E> t = tt;
					t.addStateListener(new StateListenerAdapter<S, E>() {

						@Override
						public void onComplete(StateContext<S, E> context) {
							synchronized (track) {
								Iterator<List<State<S, E>>> iterator = track.iterator();
								while(iterator.hasNext()) {
									List<State<S,E>> next = iterator.next();
									if (next.contains(t)) {
										iterator.remove();
									}
								}
							}
							if (!notified && track.isEmpty()) {
								log.debug("Join complete");
								notified = true;
								notifyContext(new DefaultPseudoStateContext<S, E>(JoinPseudoState.this, PseudoAction.JOIN_COMPLETED));
							}
						}
					});
				}
			}
		}

		void reset() {
			track.clear();
			for (List<State<S, E>> list : joins) {
				track.add(new ArrayList<State<S,E>>(list));
			}
			notified = false;
		}

		void reset(Collection<S> ids) {
			// put pack all as normal reset
			reset();

			// remove given states to reflect correct join stage
			Iterator<List<State<S, E>>> trackIter = track.iterator();
			while (trackIter.hasNext()) {
				List<State<S, E>> list = trackIter.next();
				Iterator<State<S, E>> iterator = list.iterator();
				while (iterator.hasNext()) {
					State<S, E> next = iterator.next();
					if (ids.contains(next.getId())) {
						iterator.remove();
						trackIter.remove();
					}
				}
			}
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
		private final Function<StateContext<S, E>, Mono<Boolean>> guard;

		/**
		 * Instantiates a new join state data.
		 *
		 * @param state the state holder
		 * @param guard the guard
		 */
		public JoinStateData(StateHolder<S, E> state, Function<StateContext<S, E>, Mono<Boolean>> guard) {
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
		public Function<StateContext<S, E>, Mono<Boolean>> getGuard() {
			return guard;
		}
	}
}
