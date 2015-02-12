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
import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.region.Region;
import org.springframework.util.StringUtils;

/**
 * Base implementation of a {@link State}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractState<S, E> implements State<S, E> {

	private final PseudoState pseudoState;
	private final Collection<E> deferred;
	private final Collection<Action<S, E>> entryActions;
	private final Collection<Action<S, E>> exitActions;
	private final Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
	private final StateMachine<S, E> submachine;

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(PseudoState pseudoState) {
		this(null, null, null, pseudoState);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param deferred the deferred
	 */
	public AbstractState(Collection<E> deferred) {
		this(deferred, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public AbstractState(Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions) {
		this(deferred, entryActions, exitActions, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions,
			PseudoState pseudoState) {
		this(deferred, entryActions, exitActions, pseudoState, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param submachine the submachine
	 */
	public AbstractState(Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions,
			PseudoState pseudoState, StateMachine<S, E> submachine) {
		this(deferred, entryActions, exitActions, pseudoState, null, submachine);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 */
	public AbstractState(Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions,
			PseudoState pseudoState, Collection<Region<S, E>> regions) {
		this(deferred, entryActions, exitActions, pseudoState, regions, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 * @param submachine the submachine
	 */
	private AbstractState(Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions,
			PseudoState pseudoState, Collection<Region<S, E>> regions, StateMachine<S, E> submachine) {
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
	public void sendEvent(Message<E> event) {
	}

	@Override
	public abstract void exit(E event, StateContext<S, E> context);

	@Override
	public abstract void entry(E event, StateContext<S, E> context);

	@Override
	public abstract Collection<S> getIds();

	@Override
	public PseudoState getPseudoState() {
		return pseudoState;
	}

	@Override
	public Collection<E> getDeferredEvents() {
		return deferred;
	}

	@Override
	public Collection<Action<S, E>> getEntryActions() {
		return entryActions;
	}

	@Override
	public Collection<Action<S, E>> getExitActions() {
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
		return isSubmachineState() && isComposite();
	}

	@Override
	public boolean isSubmachineState() {
		return submachine != null;
	}

	protected StateMachine<S, E> getSubmachine() {
		return submachine;
	}

	protected Collection<Region<S, E>> getRegions() {
		return regions;
	}

	@Override
	public String toString() {
		return "AbstractState [ids=" + StringUtils.collectionToCommaDelimitedString(getIds()) + ", pseudoState=" + pseudoState + ", deferred=" + deferred
				+ ", entryActions=" + entryActions + ", exitActions=" + exitActions + "]";
	}

}
