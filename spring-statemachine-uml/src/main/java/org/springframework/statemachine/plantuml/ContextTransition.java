package org.springframework.statemachine.plantuml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.StateContext;

@AllArgsConstructor
@Getter
public class ContextTransition<S, E> {
    final S source;
    final E event;
    final S target;

    public static <S, E> ContextTransition<S, E> of(@Nullable StateContext<S, E> stateContext) {
        if (stateContext != null) {
            return new ContextTransition<>(
                    stateContext.getSource() != null
                            ? stateContext.getSource().getId()
                            : null,
                    stateContext.getEvent(),
                    stateContext.getTarget() != null
                            ? stateContext.getTarget().getId()
                            : null
            );
        }
        return null;
    }
}
