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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.region.Region;

/**
 * A {@link State} implementation where state and event is object based.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ObjectState<S, E> extends AbstractSimpleState<S, E> {

	private static final Log log = LogFactory.getLog(ObjectState.class);

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 */
	public ObjectState(S id) {
		super(id);
	}

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 * @param pseudoState the pseudo state
	 */
	public ObjectState(S id, PseudoState<S, E> pseudoState) {
		super(id, pseudoState);
	}

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 */
	public ObjectState(S id, Collection<E> deferred) {
		super(id, deferred);
	}

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public ObjectState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
		super(id, deferred, entryActions, exitActions);
	}

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public ObjectState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState<S, E> pseudoState) {
		super(id, deferred, entryActions, exitActions, pseudoState);
	}

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 */
	public ObjectState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState<S, E> pseudoState, Collection<Region<S, E>> regions) {
		super(id, deferred, entryActions, exitActions, pseudoState, regions);
	}

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param submachine the submachine
	 */
	public ObjectState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState<S, E> pseudoState, StateMachine<S, E> submachine) {
		super(id, deferred, entryActions, exitActions, pseudoState, submachine);
	}

	/**
	 * Instantiates a new object state.
	 *
	 * @param id the id
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param stateActions the state actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 * @param submachine the submachine
	 */
	public ObjectState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, Collection<? extends Action<S, E>> stateActions,
			PseudoState<S, E> pseudoState, Collection<Region<S, E>> regions, StateMachine<S, E> submachine) {
		super(id, deferred, entryActions, exitActions, stateActions, pseudoState, regions, submachine);
	}

	@Override
	public void exit(StateContext<S, E> context) {
		super.exit(context);
		for (Action<S, E> action : getExitActions()) {
			try {
				executeAction(action, context);
			} catch (Exception e) {
				log.error("Action execution resulted error", e);
			}
		}
	}

	@Override
	public void entry(StateContext<S, E> context) {
		super.entry(context);
		for (Action<S, E> action : getEntryActions()) {
			try {
				executeAction(action, context);
			} catch (Exception e) {
				log.error("Action execution resulted error", e);
			}
		}
	}

	@Override
	public String toString() {
		return "ObjectState [getIds()=" + getIds() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

}
