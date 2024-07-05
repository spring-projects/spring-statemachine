package org.springframework.statemachine.plantuml.helper;

import org.springframework.statemachine.transition.Transition;

import java.util.Comparator;

public class TransitionComparator<S, E> implements Comparator<Transition<S, E>> {

    @Override
    public int compare(Transition<S, E> transition1, Transition<S, E> transition2) {
        return transition1.getSource().toString().compareTo(transition2.getTarget().toString());
    }
}
