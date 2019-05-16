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
package org.springframework.statemachine.config;

import java.util.Collection;

import org.springframework.statemachine.action.Action;
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
	private Collection<E> deferred;
	private Collection<? extends Action<S, E>> entryActions;
	private Collection<? extends Action<S, E>> exitActions;
	private boolean initial = false;
	private Action<S, E> initialAction;
	private boolean end = false;
	private PseudoStateKind pseudoStateKind;

	public StateData(Object parent, Object region, S state, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
		this.state = state;
		this.deferred = deferred;
		this.entryActions = entryActions;
		this.exitActions = exitActions;
		this.parent = parent;
		this.region = region;
	}

	public S getState() {
		return state;
	}

	public Collection<E> getDeferred() {
		return deferred;
	}
	
	public void setDeferred(Collection<E> deferred) {
		this.deferred = deferred;
	}

	public Collection<? extends Action<S, E>> getEntryActions() {
		return entryActions;
	}
	
	public void setEntryActions(Collection<? extends Action<S, E>> entryActions) {
		this.entryActions = entryActions;
	}

	public Collection<? extends Action<S, E>> getExitActions() {
		return exitActions;
	}
	
	public void setExitActions(Collection<? extends Action<S, E>> exitActions) {
		this.exitActions = exitActions;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	public Object getRegion() {
		return region;
	}

	public void setRegion(Object region) {
		this.region = region;
	}

	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
	}

	public void setInitialAction(Action<S, E> action) {
		this.initialAction = action;
	}

	public Action<S, E> getInitialAction() {
		return initialAction;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public PseudoStateKind getPseudoStateKind() {
		return pseudoStateKind;
	}

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
