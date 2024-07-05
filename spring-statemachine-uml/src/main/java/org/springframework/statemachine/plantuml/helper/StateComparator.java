package org.springframework.statemachine.plantuml.helper;

import org.springframework.statemachine.state.State;

import java.util.Comparator;

public class StateComparator<S, E> implements Comparator<State<S, E>> {
    @Override
    public int compare(State<S, E> state1, State<S, E> state2) {
        return state1.getId().toString().compareTo(state2.getId().toString());
    }
}
