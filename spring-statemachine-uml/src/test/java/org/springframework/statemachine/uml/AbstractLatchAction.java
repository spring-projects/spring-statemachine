package org.springframework.statemachine.uml;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

import java.util.concurrent.CountDownLatch;

public abstract class AbstractLatchAction<S, E> implements Action<S, E> {
    CountDownLatch latch = new CountDownLatch(1);

    @Override
    public void execute(StateContext<S, E> context) {
        latch.countDown();
    }
}
