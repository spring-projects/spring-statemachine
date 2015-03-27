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
package org.springframework.statemachine.config;

import java.util.Collection;

import org.springframework.statemachine.action.Action;
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
	private S state;
	private Collection<E> deferred;
	private Collection<? extends Action<S, E>> entryActions;
	private Collection<? extends Action<S, E>> exitActions;
	private boolean initial = false;
	private Action<S, E> initialAction;
	private boolean end = false;

	public StateData(Object parent, S state, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
		this.state = state;
		this.deferred = deferred;
		this.entryActions = entryActions;
		this.exitActions = exitActions;
		this.parent = parent;
	}

	public S getState() {
		return state;
	}

	public Collection<E> getDeferred() {
		return deferred;
	}

	public Collection<? extends Action<S, E>> getEntryActions() {
		return entryActions;
	}

	public Collection<? extends Action<S, E>> getExitActions() {
		return exitActions;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
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

	@Override
	public String toString() {
		return "StateData [parent=" + parent + ", state=" + state + ", deferred=" + deferred + ", entryActions="
				+ entryActions + ", exitActions=" + exitActions + ", initial=" + initial + ", end=" + end + "]";
	}

}
