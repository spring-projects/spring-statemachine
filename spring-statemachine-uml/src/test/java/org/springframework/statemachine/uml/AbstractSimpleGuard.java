package org.springframework.statemachine.uml;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

public class AbstractSimpleGuard<S, E> implements Guard<S, E> {
    protected final boolean deny;

    public AbstractSimpleGuard(boolean deny) {
        this.deny = deny;
    }

    @Override
    public boolean evaluate(StateContext<S, E> context) {
        return deny;
    }
}
