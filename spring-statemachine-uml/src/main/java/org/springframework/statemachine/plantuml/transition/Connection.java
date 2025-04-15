package org.springframework.statemachine.plantuml.transition;

public record Connection<S>(S source, S target) implements Comparable<Connection<S>> {

    @Override
    public int compareTo(Connection<S> otherConnection) {
        int sourceComparisonResult = source.toString().compareTo(otherConnection.source.toString());
        return sourceComparisonResult == 0
                ? target.toString().compareTo(otherConnection.target.toString())
                : sourceComparisonResult;
    }
}