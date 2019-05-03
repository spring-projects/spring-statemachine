/*
 * Copyright 2015-2016 the original author or authors.
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
package org.springframework.statemachine.config.model;

import java.util.Collection;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;

/**
 * {@code StateData} is a data representation of a {@link State} used as an
 * abstraction between a {@link StateMachineFactory} and a state machine
 * configuration.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateData<S, E> {

	private Object parent;
	private Object region;
	private S state;
	private Collection<StateData<S, E>> submachineStateData;
	private StateMachine<S, E> submachine;
	private StateMachineFactory<S, E> submachineFactory;
	private Collection<E> deferred;
	private Collection<? extends Action<S, E>> entryActions;
	private Collection<? extends Action<S, E>> exitActions;
	private Collection<? extends Action<S, E>> stateActions;
	private boolean initial = false;
	private Action<S, E> initialAction;
	private boolean end = false;
	private PseudoStateKind pseudoStateKind;

	/**
	 * Instantiates a new state data.
	 *
	 * @param state the state
	 */
	public StateData(S state) {
		this(state, false);
	}

	/**
	 * Instantiates a new state data.
	 *
	 * @param state the state
	 * @param initial the initial
	 */
	public StateData(S state, boolean initial) {
		this(null, null, state, null, null, null, initial);
	}

	/**
	 * Instantiates a new state data.
	 *
	 * @param parent the parent
	 * @param region the region
	 * @param state the state
	 * @param initial the initial
	 */
	public StateData(Object parent, Object region, S state, boolean initial) {
		this(parent, region, state, null, null, null, initial);
	}

	/**
	 * Instantiates a new state data.
	 *
	 * @param parent the parent
	 * @param region the region
	 * @param state the state
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public StateData(Object parent, Object region, S state, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
		this(parent, region, state, deferred, entryActions, exitActions, false);
	}

	/**
	 * Instantiates a new state data.
	 *
	 * @param parent the parent
	 * @param region the region
	 * @param state the state
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param initial the initial
	 */
	public StateData(Object parent, Object region, S state, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions, boolean initial) {
		this(parent, region, state, deferred, entryActions, exitActions, initial, null);
	}

	/**
	 * Instantiates a new state data.
	 *
	 * @param parent the parent
	 * @param region the region
	 * @param state the state
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param initial the initial
	 * @param initialAction the initial action
	 */
	public StateData(Object parent, Object region, S state, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions, boolean initial, Action<S, E> initialAction) {
		this.state = state;
		this.deferred = deferred;
		this.entryActions = entryActions;
		this.exitActions = exitActions;
		this.parent = parent;
		this.region = region;
		this.initial = initial;
		this.initialAction = initialAction;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public S getState() {
		return state;
	}

	/**
	 * Gets the submachine state data.
	 *
	 * @return the submachine state data
	 */
	public Collection<StateData<S, E>> getSubmachineStateData() {
		return submachineStateData;
	}

	/**
	 * Sets the submachine state data.
	 *
	 * @param submachineStateData the submachine state data
	 */
	public void setSubmachineStateData(Collection<StateData<S, E>> submachineStateData) {
		this.submachineStateData = submachineStateData;
	}

	/**
	 * Gets the submachine.
	 *
	 * @return the submachine
	 */
	public StateMachine<S, E> getSubmachine() {
		return submachine;
	}

	/**
	 * Sets the submachine.
	 *
	 * @param submachine the submachine
	 */
	public void setSubmachine(StateMachine<S, E> submachine) {
		this.submachine = submachine;
	}

	/**
	 * Gets the submachine factory.
	 *
	 * @return the submachine factory
	 */
	public StateMachineFactory<S, E> getSubmachineFactory() {
		return submachineFactory;
	}

	/**
	 * Sets the submachine factory.
	 *
	 * @param submachineFactory the submachine factory
	 */
	public void setSubmachineFactory(StateMachineFactory<S, E> submachineFactory) {
		this.submachineFactory = submachineFactory;
	}

	/**
	 * Sets the submachine factory.
	 *
	 * @param submachineFactory the submachine factory
	 */
	public void setSubmachine(StateMachineFactory<S, E> submachineFactory) {
		this.submachineFactory = submachineFactory;
	}

	/**
	 * Gets the deferred.
	 *
	 * @return the deferred
	 */
	public Collection<E> getDeferred() {
		return deferred;
	}

	/**
	 * Sets the deferred.
	 *
	 * @param deferred the new deferred
	 */
	public void setDeferred(Collection<E> deferred) {
		this.deferred = deferred;
	}

	/**
	 * Gets the entry actions.
	 *
	 * @return the entry actions
	 */
	public Collection<? extends Action<S, E>> getEntryActions() {
		return entryActions;
	}

	/**
	 * Sets the entry actions.
	 *
	 * @param entryActions the entry actions
	 */
	public void setEntryActions(Collection<? extends Action<S, E>> entryActions) {
		this.entryActions = entryActions;
	}

	/**
	 * Gets the exit actions.
	 *
	 * @return the exit actions
	 */
	public Collection<? extends Action<S, E>> getExitActions() {
		return exitActions;
	}

	/**
	 * Sets the exit actions.
	 *
	 * @param exitActions the exit actions
	 */
	public void setExitActions(Collection<? extends Action<S, E>> exitActions) {
		this.exitActions = exitActions;
	}

	/**
	 * Gets the state actions.
	 *
	 * @return the state actions
	 */
	public Collection<? extends Action<S, E>> getStateActions() {
		return stateActions;
	}

	/**
	 * Sets the state actions.
	 *
	 * @param stateActions the state actions
	 */
	public void setStateActions(Collection<? extends Action<S, E>> stateActions) {
		this.stateActions = stateActions;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	public void setParent(Object parent) {
		this.parent = parent;
	}

	/**
	 * Gets the region.
	 *
	 * @return the region
	 */
	public Object getRegion() {
		return region;
	}

	/**
	 * Sets the region.
	 *
	 * @param region the new region
	 */
	public void setRegion(Object region) {
		this.region = region;
	}

	/**
	 * Checks if is initial.
	 *
	 * @return true, if is initial
	 */
	public boolean isInitial() {
		return initial;
	}

	/**
	 * Sets the initial.
	 *
	 * @param initial the new initial
	 */
	public void setInitial(boolean initial) {
		this.initial = initial;
	}

	/**
	 * Sets the initial action.
	 *
	 * @param action the action
	 */
	public void setInitialAction(Action<S, E> action) {
		this.initialAction = action;
	}

	public Action<S, E> getInitialAction() {
		return initialAction;
	}

	/**
	 * Checks if is end.
	 *
	 * @return true, if is end
	 */
	public boolean isEnd() {
		return end;
	}

	/**
	 * Sets the end.
	 *
	 * @param end the new end
	 */
	public void setEnd(boolean end) {
		this.end = end;
	}

	/**
	 * Gets the pseudo state kind.
	 *
	 * @return the pseudo state kind
	 */
	public PseudoStateKind getPseudoStateKind() {
		return pseudoStateKind;
	}

	/**
	 * Sets the pseudo state kind.
	 *
	 * @param pseudoStateKind the new pseudo state kind
	 */
	public void setPseudoStateKind(PseudoStateKind pseudoStateKind) {
		this.pseudoStateKind = pseudoStateKind;
	}

	@Override
	public String toString() {
		return "StateData [parent=" + parent + ", region=" + region + ", state=" + state + ", deferred=" + deferred
				+ ", entryActions=" + entryActions + ", exitActions=" + exitActions + ", initial=" + initial
				+ ", initialAction=" + initialAction + ", end=" + end + ", pseudoStateKind=" + pseudoStateKind + "]";
	}

}
