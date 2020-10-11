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

import java.util.List;

import org.springframework.statemachine.StateContext;

import reactor.core.publisher.Mono;

/**
 * Entrypoint implementation of a {@link PseudoState}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class EntryPseudoState<S, E> implements PseudoState<S, E> {

	private final State<S, E> state;

	/**
	 * Instantiates a new entry pseudo state.
	 *
	 * @param state the state
	 */
	public EntryPseudoState(State<S, E> state) {
		this.state = state;
	}

	@Override
	final public PseudoStateKind getKind() {
		return PseudoStateKind.ENTRY;
	}

	@Override
	public Mono<State<S, E>> entry(StateContext<S, E> context) {
		return Mono.just(state);
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
}
