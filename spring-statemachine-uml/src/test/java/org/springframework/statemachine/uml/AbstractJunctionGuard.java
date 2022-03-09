package org.springframework.statemachine.uml;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.ObjectUtils;

public class AbstractJunctionGuard<S, E> implements Guard<S, E> {
    protected final String match;

    public AbstractJunctionGuard(String match) {
        this.match = match;
    }

    @Override
    public boolean evaluate(StateContext<S, E> context) {
        return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("junction", String.class));
    }
}
