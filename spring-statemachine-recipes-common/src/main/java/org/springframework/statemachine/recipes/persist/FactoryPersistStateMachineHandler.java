package org.springframework.statemachine.recipes.persist;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.util.Assert;

/**
 * {@code FactoryPersistStateMachineHandler} is a recipe can be used to
 * handle a state change of an arbitrary entity in a persistent storage.
 * <br>
 * This implementation accepts {@link StateMachineFactory}
 * or {@link StateMachineBuilder.Builder} to provide thread safe feature
 * without sharing same state machine in concurrent environment.
 * New state machine will be created when handling method is called.
 *
 * @author Ng Zouyiu
 */
public class FactoryPersistStateMachineHandler<S, E> extends AbstractPersistStateMachineHandler<S, E> {

    protected final StateMachineFactory<S, E> factory;
    protected final StateMachineBuilder.Builder<S, E> builder;

    public FactoryPersistStateMachineHandler(StateMachineBuilder.Builder<S, E> builder) {
        Assert.notNull(builder, "State machine builder must be set");
        this.builder = builder;
        factory = null;
    }

    public FactoryPersistStateMachineHandler(StateMachineFactory<S, E> factory) {
        Assert.notNull(factory, "State machine factory must be set");
        this.factory = factory;
        builder = null;
    }

    @Override
    protected StateMachine<S, E> getInitStateMachine() {
        StateMachine<S, E> stateMachine;
        if (factory != null) {
            stateMachine = factory.getStateMachine();
        } else if (builder != null) {
            stateMachine = builder.build();
        } else {
            throw new StateMachineException("Factory or builder must be set to build state machine for handler");
        }
        initStateMachine(stateMachine);
        return stateMachine;
    }
}
