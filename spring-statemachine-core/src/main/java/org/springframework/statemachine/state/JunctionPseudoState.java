/*
 * Copyright 2016-2020 the original author or authors.
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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Junction implementation of a {@link PseudoState}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class JunctionPseudoState<S, E> implements PseudoState<S, E> {

	private final static Log log = LogFactory.getLog(JunctionPseudoState.class);
	private final List<JunctionStateData<S, E>> junctions;

	/**
	 * Instantiates a new junction pseudo state.
	 *
	 * @param junctions the junctions
	 */
	public JunctionPseudoState(List<JunctionStateData<S, E>> junctions) {
		this.junctions = junctions;
	}

	@Override
	public PseudoStateKind getKind() {
		return PseudoStateKind.JUNCTION;
	}

	@Override
	public Mono<State<S, E>> entry(StateContext<S, E> context) {
		return Mono.defer(() -> {
			JunctionStateData<S, E> jsd = null;
			for (JunctionStateData<S, E> j : junctions) {
				jsd = j;
				if (j.guard != null && evaluateInternal(j.guard, context)) {
					break;
				}
			}
			return Mono.justOrEmpty(jsd);
		})
		.flatMap(jsd -> {
			return Flux.fromIterable(jsd.getActions())
				.flatMap(a -> a.apply(context))
				.then(Mono.just(jsd.getState()));
		});
	}


	@Override
	public Mono<Void> exit(StateContext<S, E> context) {
		return Mono.empty();
	}

	@Override
	public void addPseudoStateListener(PseudoStateListener<S, E> listener) {
	}

	@Override
	public void setPseudoStateListeners(List<PseudoStateListener<S, E>> listeners) {
	}

	private boolean evaluateInternal(Guard<S, E> guard, StateContext<S, E> context) {
		try {
			return guard.evaluate(context);
		} catch (Throwable t) {
			log.warn("Deny guard due to throw as GUARD should not error", t);
			return false;
		}
	}

	/**
	 * Data class wrapping choice {@link State} and {@link Guard}
	 * together.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 */
	public static class JunctionStateData<S, E> {
		private final StateHolder<S, E> state;
		private final Guard<S, E> guard;
		private final Collection<Function<StateContext<S, E>, Mono<Void>>> actions;

		/**
		 * Instantiates a new junction state data.
		 *
		 * @param state the state holder
		 * @param guard the guard
		 * @param actions the actions
		 */
		public JunctionStateData(StateHolder<S, E> state, Guard<S, E> guard,
				Collection<Function<StateContext<S, E>, Mono<Void>>> actions) {
			Assert.notNull(state, "Holder must be set");
			this.state = state;
			this.guard = guard;
			this.actions = actions;
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

		/**
		 * Gets the actions.
		 *
		 * @return the actions
		 */
		public Collection<Function<StateContext<S, E>, Mono<Void>>> getActions() {
			return actions;
		}
	}
}
