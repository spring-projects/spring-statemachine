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
package org.springframework.statemachine.config.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.statemachine.action.Action;

public class StateMachineStates<S, E> {

	private Collection<StateData<S, E>> states;

	private final S initialState;

	private final S endState;
	
	public StateMachineStates(S initialState, S endState, Collection<StateData<S, E>> states) {
		this.states = states;
		this.initialState = initialState;
		this.endState = endState;
	}

	public Collection<StateData<S, E>> getStates() {
		return states;
	}

	public S getInitialState() {
		return initialState;
	}

	public S getEndState() {
		return endState;
	}
	
	public static class StateData<S, E> {
		private S state;
		private Collection<E> deferred;
		private Collection<Action<S, E>> entryActions;
		private Collection<Action<S, E>> exitActions;
		public StateData(S state, Collection<E> deferred) {
			this(state, deferred, null, null);
		}
		public StateData(S state, Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions) {
			this.state = state;
			this.deferred = deferred;
			this.entryActions = entryActions;
			this.exitActions = exitActions;
		}
		public StateData(S state, E[] deferred) {
			this(state, deferred != null ? Arrays.asList(deferred) : new ArrayList<E>());
		}
		public S getState() {
			return state;
		}
		public Collection<E> getDeferred() {
			return deferred;
		}
		public Collection<Action<S, E>> getEntryActions() {
			return entryActions;
		}
		public Collection<Action<S, E>> getExitActions() {
			return exitActions;
		}
	}

}
