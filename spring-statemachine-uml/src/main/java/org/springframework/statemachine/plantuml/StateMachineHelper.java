/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.statemachine.plantuml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StateMachineHelper {

	public static <S, E> StateMachine<S, E> buildStateMachine(
			StateMachineModelFactory<S, E> stateMachineModelFactory
	) throws Exception {
		StateMachineBuilder.Builder<S, E> builder = StateMachineBuilder.builder();
		builder.configureModel().withModel().factory(stateMachineModelFactory);
		builder.configureConfiguration().withConfiguration();
		return builder.build();
	}

	public static <S, E> List<S> getCurrentStates(StateMachine<S, E> stateMachine) {
		ArrayList<S> currentState = new ArrayList<>();
		collectCurrentStates(stateMachine, currentState);
		return currentState;
	}

	private static <S, E> void collectCurrentStates(
			Region<S, E> region,
			ArrayList<S> currentStateAccumulator
	) {
		visiteRegion(region, seRegion -> {
			if (region.getState() != null) {
				currentStateAccumulator.add(region.getState().getId());
			}
		});
	}

	public static <S, E> List<State<S, E>> getAllStates(StateMachine<S, E> stateMachine) {
		ArrayList<State<S, E>> allStates = new ArrayList<>();
		collectAllStates(stateMachine, allStates);
		return allStates;
	}

	private static <S, E> void collectAllStates(
			Region<S, E> region,
			ArrayList<State<S, E>> allStatesAccumulator
	) {
		visiteRegion(region, seRegion -> {
			if (seRegion.getStates() != null) {
				allStatesAccumulator.addAll(seRegion.getStates());
			}
		});
	}

	private static <S, E> void visiteRegion(
			Region<S, E> region,
			Consumer<Region<S, E>> stateCollector
	) {
		stateCollector.accept(region);

		region.getStates().forEach(state -> {
			if (state.isSubmachineState()) {
				if (state instanceof AbstractState<S, E> abstractState) {
					visiteRegion(abstractState.getSubmachine(), stateCollector);
				}
			} else if (state.isOrthogonal() || state.isComposite()) {
				if (state instanceof RegionState<S, E> regionState) {
					regionState.getRegions().stream()
							.toList()
							.forEach(subRegion -> visiteRegion(subRegion, stateCollector));
				}
			}
		});
	}

	public static <S, E> Map<State<S, E>, String> collectHistoryStates(StateMachine<S, E> stateMachine) {
		HashMap<State<S, E>, String> historyStatesToHistoryId = new HashMap<State<S, E>, String>();
		collectHistoryStates(stateMachine, null, historyStatesToHistoryId);
		return historyStatesToHistoryId;
	}

	private static <S, E> void collectHistoryStates(
			Region<S, E> region,
			@Nullable State<S, E> parentState,
			Map<State<S, E>, String> historyStatesToHistoryId
	) {
		region.getStates().forEach(state -> {
			if (state.isSimple()) {
				collectHistoryState(state, parentState, historyStatesToHistoryId);
			} else if (state.isSubmachineState()) {
				if (state instanceof AbstractState<S, E> abstractState) {
					collectHistoryStates(abstractState.getSubmachine(), state, historyStatesToHistoryId);
				}
			} else if (state.isOrthogonal() || state.isComposite()) {
				if (state instanceof RegionState<S, E> regionState) {
					regionState.getRegions().stream()
							.toList()
							.forEach(subRegion -> collectHistoryStates(subRegion, state, historyStatesToHistoryId));
				}
			}
		});
	}

	private static <S, E> void collectHistoryState(
			State<S, E> state,
			@Nullable State<S, E> parentState,
			Map<State<S, E>, String> historyStatesToHistoryId
	) {
		if (state.getPseudoState() != null
				&& (
				state.getPseudoState().getKind() == PseudoStateKind.HISTORY_DEEP
						|| state.getPseudoState().getKind() == PseudoStateKind.HISTORY_SHALLOW
		)
		) {
			historyStatesToHistoryId.put(state, historyId(parentState, state.getPseudoState().getKind()));
		}
	}

	private static <S, E> String historyId(
			@Nullable State<S, E> parentState,
			PseudoStateKind pseudoStateKind
	) {
		String prefix = parentState == null ? "" : parentState.getId().toString();
		return switch (pseudoStateKind) {
			case HISTORY_DEEP -> prefix + "[H*]";
			case HISTORY_SHALLOW -> prefix + "[H]";
			default -> throw new IllegalArgumentException("pseudoStateKind must be an 'history'");
		};
	}

	public static String toString(State<?, ?> state) {
		if (state == null) {
			return "null";
		} else if (state.getId() != null) {
			return state.getId().toString();
		} else {
			return state.toString();
		}
	}

	@Nullable
	public static <S, E> State<S, E> findStateInStateMachine(S stateToFind, StateMachine<S, E> stateMachine) {
		return getAllStates(stateMachine).stream()
				.filter(state -> state.getId().equals(stateToFind))
				.findFirst()
				.orElse(null);
	}
}
