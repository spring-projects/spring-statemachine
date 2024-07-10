package org.springframework.statemachine.plantuml.helper;

import org.springframework.statemachine.transition.Transition;

import java.util.Comparator;

public class TransitionComparator<S, E> implements Comparator<Transition<S, E>> {

    @Override
    public int compare(Transition<S, E> transition1, Transition<S, E> transition2) {
        // First compare by source
        int compareBySource = transition1.getSource().toString().compareTo(transition2.getSource().toString());
        if (compareBySource != 0) {
            return compareBySource;
        }
        // If sources are equal, then compare by target
        return transition1.getTarget().toString().compareTo(transition2.getTarget().toString());
    }
}
