/*
 * Copyright 2015-2019 the original author or authors.
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
import java.util.Collections;
import java.util.function.Function;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.region.Region;

import reactor.core.publisher.Mono;

/**
 * Base implementation of a {@link State} having a single state identifier.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractSimpleState<S, E> extends AbstractState<S, E> {

	private final Collection<S> ids;

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the state identifier
	 */
	public AbstractSimpleState(S id) {
		this(id, null, null, null, null);
	}

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public AbstractSimpleState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions) {
		this(id, deferred, entryActions, exitActions, null);
	}

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 */
	public AbstractSimpleState(S id, Collection<E> deferred) {
		this(id, deferred, null, null, null);
	}

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the state identifier
	 * @param pseudoState the pseudo state
	 */
	public AbstractSimpleState(S id, PseudoState<S, E> pseudoState) {
		this(id, null, null, null, pseudoState);
	}

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 */
	public AbstractSimpleState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions, PseudoState<S, E> pseudoState,
			Collection<Region<S, E>> regions) {
		super(id, deferred, entryActions, exitActions, pseudoState, regions);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param submachine the submachine
	 */
	public AbstractSimpleState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions, PseudoState<S, E> pseudoState,
			StateMachine<S, E> submachine) {
		super(id, deferred, entryActions, exitActions, pseudoState, submachine);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public AbstractSimpleState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions, PseudoState<S, E> pseudoState) {
		super(id, deferred, entryActions, exitActions, pseudoState);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	/**
	 * Instantiates a new abstract simple state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param stateActions the state actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 * @param submachine the submachine
	 */
	public AbstractSimpleState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> stateActions, PseudoState<S, E> pseudoState,
			Collection<Region<S, E>> regions, StateMachine<S, E> submachine) {
		super(id, deferred, entryActions, exitActions, stateActions, pseudoState, regions, submachine);
		this.ids = new ArrayList<S>();
		this.ids.add(id);
	}

	@Override
	public Collection<S> getIds() {
		return Collections.unmodifiableCollection(ids);
	}

	@Override
	public Collection<State<S, E>> getStates() {
		ArrayList<State<S, E>> states = new ArrayList<State<S, E>>();
		states.add(this);
		return states;
	}

}
