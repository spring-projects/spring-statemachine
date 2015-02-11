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

	/**
	 * Instantiates a new region state.
	 *
	 * @param regions the regions
	 */
	public RegionState(Collection<Region<S, E>> regions) {
		super(null, null, null, null, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param regions the regions
	 * @param deferred the deferred
	 */
	public RegionState(Collection<Region<S, E>> regions, Collection<E> deferred) {
		super(deferred, null, null, null, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param regions the regions
	 * @param pseudoState the pseudo state
	 */
	public RegionState(Collection<Region<S, E>> regions, PseudoState pseudoState) {
		super(null, null, null, pseudoState, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param regions the regions
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public RegionState(Collection<Region<S, E>> regions, Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions,
			PseudoState pseudoState) {
		super(deferred, entryActions, exitActions, pseudoState, regions);
	}

	/**
	 * Instantiates a new region state.
	 *
	 * @param regions the regions
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public RegionState(Collection<Region<S, E>> regions, Collection<E> deferred, Collection<Action<S, E>> entryActions, Collection<Action<S, E>> exitActions) {
		super(deferred, entryActions, exitActions, null, regions);
	}

	@Override
	public void exit(E event, StateContext<S, E> context) {
		// TODO Auto-generated method stub

	}
	@Override
	public void entry(E event, StateContext<S, E> context) {
		// TODO Auto-generated method stub

	}


	@Override
	public Collection<S> getIds() {
		ArrayList<S> ids = new ArrayList<S>();
		for (Region<S, E> r : getRegions()) {
			ids.addAll(r.getState().getIds());
		}
		return ids;
	}

}
