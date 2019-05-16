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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.region.Region;

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
	private final PseudoState<S, E> pseudoState;
	private final Collection<E> deferred;
	private final Collection<? extends Action<S, E>> entryActions;
	private final Collection<? extends Action<S, E>> exitActions;
	private final Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
	private final StateMachine<S, E> submachine;

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(S id, PseudoState<S, E> pseudoState) {
		this(id, null, null, null, pseudoState);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 */
	public AbstractState(S id, Collection<E> deferred) {
		this(id, deferred, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions) {
		this(id, deferred, entryActions, exitActions, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState) {
		this(id, deferred, entryActions, exitActions, pseudoState, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param submachine the submachine
	 */
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState, StateMachine<S, E> submachine) {
		this(id, deferred, entryActions, exitActions, pseudoState, null, submachine);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 */
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState, Collection<Region<S, E>> regions) {
		this(id, deferred, entryActions, exitActions, pseudoState, regions, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 * @param submachine the submachine
	 */
	private AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState, Collection<Region<S, E>> regions,
			StateMachine<S, E> submachine) {
		this.id = id;
		this.deferred = deferred;
		this.entryActions = entryActions;
		this.exitActions = exitActions;
		this.pseudoState = pseudoState;

		// use of private ctor should prevent user to
		// add regions and a submachine which is not allowed.
		if (regions != null) {
			this.regions.addAll(regions);
		}
		this.submachine = submachine;
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		return false;
	}

	@Override
	public boolean shouldDefer(Message<E> event) {
		return deferred != null && deferred.contains(event.getPayload());
	}

	@Override
	public abstract void exit(StateContext<S, E> context);

	@Override
	public abstract void entry(StateContext<S, E> context);

	@Override
	public S getId() {
		return id;
	}

	@Override
	public abstract Collection<S> getIds();

	@Override
	public abstract Collection<State<S, E>> getStates();

	@Override
	public PseudoState<S, E> getPseudoState() {
		return pseudoState;
	}

	@Override
	public Collection<E> getDeferredEvents() {
		return deferred;
	}

	@Override
	public Collection<? extends Action<S, E>> getEntryActions() {
		return entryActions;
	}

	@Override
	public Collection<? extends Action<S, E>> getExitActions() {
		return exitActions;
	}

	@Override
	public boolean isComposite() {
		return !regions.isEmpty();
	}

	@Override
	public boolean isOrthogonal() {
		return regions.size() > 1;
	}

	@Override
	public boolean isSimple() {
		return !isSubmachineState() && !isComposite();
	}

	@Override
	public boolean isSubmachineState() {
		return submachine != null;
	}

	/**
	 * Gets the submachine.
	 *
	 * @return the submachine or null if not set
	 */
	public StateMachine<S, E> getSubmachine() {
		return submachine;
	}

	/**
	 * Gets the regions.
	 *
	 * @return the regions or empty collection if no regions
	 */
	public Collection<Region<S, E>> getRegions() {
		return regions;
	}

	@Override
	public String toString() {
		return "AbstractState [id=" + id + ", pseudoState=" + pseudoState + ", deferred=" + deferred
				+ ", entryActions=" + entryActions + ", exitActions=" + exitActions + ", regions=" + regions
				+ ", submachine=" + submachine + "]";
	}

}
