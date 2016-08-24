package org.springframework.statemachine.action;

import org.springframework.statemachine.StateContext;

/**
 * Action Utilities.
 */
public final class Actions {

    private Actions() {
        // This helper class should not be instantiated.
    }

    /**
     *
     * @param <S>
     * @param <E>
     * @return an empty (Noop) Action.
     */
    public static <S, E> Action<S, E> emptyAction() {
        return new Action<S, E>() {
            @Override
            public void execute(final StateContext<S, E> context) {
                // Nothing to do;
            }
        };
    }
}
