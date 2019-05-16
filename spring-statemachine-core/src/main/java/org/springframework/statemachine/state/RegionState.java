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
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.region.Region;

/**
 * A {@link State} implementation where states are wrapped in a regions..
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class RegionState<S, E> extends AbstractState<S, E> {

	private JoinPseudoState<S, E> join;

	/**
	 * Instantiates a new region state.
	 *
	 * @param id the state identifier
	 * @param regions the regions
	 */
	public RegionState(S id, Collection<Region<S, E>> regions) {
		super(id, null, null, null, null, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param id the state identifier
	 * @param regions the regions
	 * @param deferred the deferred
	 */
	public RegionState(S id, Collection<Region<S, E>> regions, Collection<E> deferred) {
		super(id, deferred, null, null, null, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param id the state identifier
	 * @param regions the regions
	 * @param pseudoState the pseudo state
	 */
	public RegionState(S id, Collection<Region<S, E>> regions, PseudoState<S, E> pseudoState) {
		super(id, null, null, null, pseudoState, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param id the state identifier
	 * @param regions the regions
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public RegionState(S id, Collection<Region<S, E>> regions, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState) {
		super(id, deferred, entryActions, exitActions, pseudoState, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param id the state identifier
	 * @param regions the regions
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public RegionState(S id, Collection<Region<S, E>> regions, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
		super(id, deferred, entryActions, exitActions, null, regions);
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		boolean accept = false;
		if (getRegions() != null) {
			for (Region<S, E> r : getRegions()) {
				accept |= r.sendEvent(event);
			}
		}
		return accept;
	}

	@Override
	public boolean shouldDefer(Message<E> event) {
		boolean defer = true;
		if (getRegions() != null) {
			for (Region<S, E> r : getRegions()) {
				State<S, E> state = r.getState();
				if (state != null) {
					Collection<E> deferredEvents = state.getDeferredEvents();
					if (deferredEvents != null && deferredEvents.contains(event.getPayload())) {
						defer = defer & true;
					} else {
						defer = false;
					}
				}
			}
		}
		return defer;
	}

	@Override
	public void exit(StateContext<S, E> context) {
		for (Region<S, E> region : getRegions()) {
			if (region.getState() != null) {
				region.getState().exit(context);
			}
			region.stop();
		}
		Collection<? extends Action<S, E>> actions = getExitActions();
		if (actions != null) {
			for (Action<S, E> action : actions) {
				action.execute(context);
			}
		}
	}

	@Override
	public void entry(StateContext<S, E> context) {
		if (join != null) {
			join.entry(context);
		}
		Collection<? extends Action<S, E>> actions = getEntryActions();
		if (actions != null) {
			for (Action<S, E> action : actions) {
				action.execute(context);
			}
		}

		if (getPseudoState() != null && getPseudoState().getKind() == PseudoStateKind.INITIAL) {
			for (Region<S, E> region : getRegions()) {
				region.start();
			}
		} else {
			for (Region<S, E> region : getRegions()) {
				if (region.getState() != null) {
					region.getState().entry(context);
				}
			}
		}
	}

	@Override
	public Collection<S> getIds() {
		ArrayList<S> ids = new ArrayList<S>();
		if (getId() != null) {
			ids.add(getId());
		}
		for (Region<S, E> r : getRegions()) {
			State<S, E> s = r.getState();
			if (s != null) {
				ids.addAll(s.getIds());
			}
		}
		return ids;
	}

	@Override
	public Collection<State<S, E>> getStates() {
		ArrayList<State<S, E>> states = new ArrayList<State<S, E>>();
		states.add(this);
		for (Region<S, E> r : getRegions()) {
			for (State<S, E> s : r.getStates()) {
				states.addAll(s.getStates());
			}
		}
		return states;
	}

	public void setJoin(JoinPseudoState<S, E> join) {
		this.join = join;
	}

	@Override
	public String toString() {
		return "RegionState [getIds()=" + getIds() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

}
