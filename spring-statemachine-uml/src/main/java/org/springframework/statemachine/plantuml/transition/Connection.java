package org.springframework.statemachine.plantuml.transition;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class Connection<S> implements Comparable<Connection<S>> {
    S source;
    S target;

    @Override
    public int compareTo(Connection<S> o) {
        int sourceComparisonResult = source.toString().compareTo(o.source.toString());
        return sourceComparisonResult == 0
                ? target.toString().compareTo(o.target.toString())
                : sourceComparisonResult;
    }
}