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

import java.util.List;

import org.springframework.statemachine.StateContext;

/**
 * Base implementation of a {@link PseudoState}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractPseudoState<S, E> implements PseudoState<S, E> {

	private final PseudoStateKind kind;

	private final CompositePseudoStateListener<S, E> pseudoStateListener = new CompositePseudoStateListener<S, E>();

	/**
	 * Instantiates a new abstract pseudo state.
	 *
	 * @param kind the kind
	 */
	public AbstractPseudoState(PseudoStateKind kind) {
		this.kind = kind;
	}

	@Override
	public PseudoStateKind getKind() {
		return kind;
	}

	@Override
	public State<S, E> entry(StateContext<S, E> context) {
		return null;
	}

	@Override
	public void exit(StateContext<S, E> context) {
	}

	@Override
	public void addPseudoStateListener(PseudoStateListener<S, E> listener) {
		pseudoStateListener.register(listener);
	}

	@Override
	public void setPseudoStateListeners(List<PseudoStateListener<S, E>> listeners) {
		pseudoStateListener.setListeners(listeners);
	}

	/**
	 * Notify all {@link PseudoStateListener}s of a new context.
	 *
	 * @param context the new context
	 */
	protected void notifyContext(PseudoStateContext<S, E> context) {
		pseudoStateListener.onContext(context);
	}

}
