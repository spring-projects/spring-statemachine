/*
 * Copyright 2015-2019 the original author or authors.
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
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.region.RegionExecutionPolicy;
import org.springframework.statemachine.support.StateMachineUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A {@link State} implementation where states are wrapped in a regions..
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class RegionState<S, E> extends AbstractState<S, E> {

	private RegionExecutionPolicy regionExecutionPolicy;

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
	public Flux<StateMachineEventResult<S, E>> sendEvent(Message<E> event) {
		if(regionExecutionPolicy == RegionExecutionPolicy.PARALLEL) {
			return Flux.fromIterable(getRegions())
				.parallel()
				.runOn(Schedulers.parallel())
				.flatMap(r -> r.sendEvent(Mono.just(event)))
				.sequential();
		} else {
			return Flux.fromIterable(getRegions())
				.flatMap(r -> r.sendEvent(Mono.just(event)));
		}
	}

	@Override
	public boolean shouldDefer(Message<E> event) {
		boolean defer = true;
		if (getRegions() != null) {
			for (Region<S, E> r : getRegions()) {
				State<S, E> state = r.getState();
				if (state != null) {
					if (state.getDeferredEvents().contains(event.getPayload())) {
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
	public Mono<Void> exit(StateContext<S, E> context) {
		return super.exit(context).and(Mono.defer(() -> {
			return Flux.fromIterable(getRegions())
				.flatMap(r -> r.stopReactively())
				.then(Flux.fromIterable(getExitActions())
					.doOnNext(ea -> {
						executeAction(ea, context);
					})
				.then());
		}));
	}

	private Mono<Void> startOrEntry(StateContext<S, E> context) {
		if (getPseudoState() != null && getPseudoState().getKind() == PseudoStateKind.INITIAL) {
			if (regionExecutionPolicy == RegionExecutionPolicy.PARALLEL) {
				return Flux.fromIterable(getRegions())
					.filter(r -> !StateMachineUtils.containsAtleastOne(r.getStates(), context.getTargets()))
					.parallel()
					.runOn(Schedulers.parallel())
					.flatMap(r -> r.startReactively())
					.sequential()
					.then();
			} else {
				return Flux.fromIterable(getRegions())
					.filter(r -> !StateMachineUtils.containsAtleastOne(r.getStates(), context.getTargets()))
					.flatMap(r -> r.startReactively())
					.then();

			}
		} else {
			return Flux.fromIterable(getRegions())
				.filter(r -> r.getState() != null)
				.doOnNext(r -> r.getState().entry(context))
				.then();
		}
	}

	@Override
	public Mono<Void> entry(StateContext<S, E> context) {
		return super.entry(context)
			.and(Flux.fromIterable(getEntryActions())
			.doOnNext(ea -> {
				executeAction(ea, context);
			})
			.then(startOrEntry(context)));
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

	/**
	 * Sets the region execution policy.
	 *
	 * @param regionExecutionPolicy the new region execution policy
	 */
	public void setRegionExecutionPolicy(RegionExecutionPolicy regionExecutionPolicy) {
		this.regionExecutionPolicy = regionExecutionPolicy;
	}

	@Override
	public String toString() {
		return "RegionState [getIds()=" + getIds() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}
}
