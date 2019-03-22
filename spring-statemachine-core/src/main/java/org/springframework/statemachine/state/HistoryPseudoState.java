/*
 * Copyright 2015 the original author or authors.
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

import org.springframework.statemachine.StateContext;
import org.springframework.util.Assert;

/**
 * History implementation of a {@link PseudoState}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class HistoryPseudoState<S, E> extends AbstractPseudoState<S, E> {

	private final StateHolder<S, E> defaultState;
	private final StateHolder<S, E> containingState;
	private State<S, E> state;

	/**
	 * Instantiates a new history pseudo state.
	 *
	 * @param kind the kind
	 * @param containingState the parent containing state
	 */
	public HistoryPseudoState(PseudoStateKind kind, StateHolder<S, E> containingState) {
		this(kind, containingState, new StateHolder<S, E>(null));
	}

	/**
	 * Instantiates a new history pseudo state.
	 *
	 * @param kind the kind
	 * @param defaultState the default history state
	 * @param containingState the parent containing state
	 */
	public HistoryPseudoState(PseudoStateKind kind, StateHolder<S, E> defaultState, StateHolder<S, E> containingState) {
		super(kind);
		Assert.isTrue(PseudoStateKind.HISTORY_SHALLOW == kind || PseudoStateKind.HISTORY_DEEP == kind,
				"Pseudo state must be either shallow or deep");
		Assert.notNull(defaultState, "Holder defaultState must be set");
		Assert.notNull(containingState, "Holder containingState must be set");
		this.defaultState = defaultState;
		this.containingState = containingState;
	}

	@Override
	public State<S, E> entry(StateContext<S, E> context) {
		// if no logged history or history is final state,
		// go to default state. go to containing parent if
		// we have no history and there's no default state.
		if (state == null) {
			if (defaultState.getState() == null) {
				return containingState.getState();
			} else {
				return defaultState.getState();
			}
		} else {
			if (defaultState.getState() != null && state.getPseudoState() != null && state.getPseudoState().getKind() == PseudoStateKind.END) {
				return defaultState.getState();
			} else {
				return state;
			}
		}
	}

	/**
	 * Sets the current recorded state.
	 *
	 * @param state the state
	 */
	public void setState(State<S, E> state) {
		this.state = state;
	}

	/**
	 * Gets the current recorded state.
	 *
	 * @return the current recorded state.
	 */
	public State<S, E> getState() {
		return state;
	}
}
