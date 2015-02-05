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

import org.springframework.statemachine.action.Action;

/**
 * Base implementation of a {@link State}.
 * 
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractState<S, E> implements State<S, E> {

	private final S id;
	private final PseudoState pseudoState;
	private final Collection<E> deferred;
	private final Collection<Action> entryActions;
	private final Collection<Action> exitActions;

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the id
	 */
	public AbstractState(S id) {
		this(id, null, null, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the id
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(S id, PseudoState pseudoState) {
		this(id, null, null, null, pseudoState);
	}
	
	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 */
	public AbstractState(S id, Collection<E> deferred) {
		this(id, deferred, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public AbstractState(S id, Collection<E> deferred, Collection<Action> entryActions, Collection<Action> exitActions) {
		this(id, deferred, entryActions, exitActions, null);
	}
	
	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(S id, Collection<E> deferred, Collection<Action> entryActions, Collection<Action> exitActions, PseudoState pseudoState) {
		this.id = id;
		this.deferred = deferred;
		this.entryActions = entryActions;
		this.exitActions = exitActions;
		this.pseudoState = pseudoState;
	}
	
	@Override
	public S getId() {
		return id;
	}
	
	@Override
	public PseudoState getPseudoState() {
		return pseudoState;
	}

	@Override
	public Collection<E> getDeferredEvents() {
		return deferred;
	}
	
	@Override
	public Collection<Action> getEntryActions() {
		return entryActions;
	}
	
	@Override
	public Collection<Action> getExitActions() {
		return exitActions;
	}

	@Override
	public String toString() {
		return "AbstractState [id=" + id + ", pseudoState=" + pseudoState + ", deferred=" + deferred
				+ ", entryActions=" + entryActions + ", exitActions=" + exitActions + "]";
	}
	
}
