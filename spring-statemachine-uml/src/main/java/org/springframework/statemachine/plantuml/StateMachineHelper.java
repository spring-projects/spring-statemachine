package org.springframework.statemachine.plantuml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (region.getState() != null) {
            currentStateAccumulator.add(region.getState().getId());
        }

        region.getStates().forEach(state -> {
            if (state.isSubmachineState()) {
                if (state instanceof AbstractState<S, E> abstractState) {
                    collectCurrentStates(abstractState.getSubmachine(), currentStateAccumulator);
                }
            } else if (state.isOrthogonal() || state.isComposite()) {
                if (state instanceof RegionState<S, E> regionState) {
                    regionState.getRegions().stream()
                            .toList()
                            .forEach(subRegion -> collectCurrentStates(subRegion, currentStateAccumulator));
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
}
