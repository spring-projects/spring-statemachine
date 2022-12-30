/*
 * Copyright 2015-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.recipes.persist;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;

/**
 * {@code AbstractPersistStateMachineHandler} is a base recipe which can be used to
 * handle a state change of an arbitrary entity in a persistent storage.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractPersistStateMachineHandler<S, E> extends LifecycleObjectSupport {

    protected final PersistingStateChangeInterceptor interceptor = new PersistingStateChangeInterceptor();
    protected final CompositePersistStateChangeListener listeners = new CompositePersistStateChangeListener();

    protected abstract StateMachine<S, E> getInitStateMachine();

    protected void initStateMachine(StateMachine<S, E> stateMachine) {
        List<StateMachineAccess<S, E>> withAllRegions = stateMachine.getStateMachineAccessor().withAllRegions();
        for (StateMachineAccess<S, E> a : withAllRegions) {
            a.addStateMachineInterceptor(interceptor);
        }
    }

    /**
     * Handle event with entity.
     *
     * @param event the event
     * @param state the state
     * @return true if event was accepted
     * @see #handleEventWithStateReactively(Message, Object)
     */
    @Deprecated
    public boolean handleEventWithState(Message<E> event, S state) {
        StateMachine<S, E> stateMachine = getInitStateMachine();
        stateMachine.stopReactively().block();
        List<StateMachineAccess<S, E>> withAllRegions = stateMachine.getStateMachineAccessor().withAllRegions();
        for (StateMachineAccess<S, E> a : withAllRegions) {
            a.resetStateMachineReactively(new DefaultStateMachineContext<S, E>(state, null, null, null)).block();
        }
        stateMachine.startReactively().block();
        return stateMachine.sendEvent(event);
    }

    /**
     * Handle event with entity reactively.
     *
     * @param event the event
     * @param state the state
     * @return mono for completion
     */
    public Mono<Void> handleEventWithStateReactively(Message<E> event, S state) {
        return Mono.defer(() -> {
            StateMachine<S, E> stateMachine = getInitStateMachine();
            // TODO: REACTOR add docs and revisit this function concept
            return Mono.from(stateMachine.stopReactively())
                    .thenEmpty(
                            Flux.fromIterable(stateMachine.getStateMachineAccessor().withAllRegions())
                                    .flatMap(region -> region.resetStateMachineReactively(new DefaultStateMachineContext<S, E>(state, null, null, null)))
                    )
                    .then(stateMachine.startReactively())
                    .thenMany(stateMachine.sendEvent(Mono.just(event)))
                    .then();
        });
    }

    /**
     * Adds the persist state change listener.
     *
     * @param listener the listener
     */
    public void addPersistStateChangeListener(GenericPersistStateChangeListener<S, E> listener) {
        listeners.register(listener);
    }

    /**
     * The listener interface for receiving persistStateChange events.
     * The class that is interested in processing a persistStateChange
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addPersistStateChangeListener</code> method. When
     * the persistStateChange event occurs, that object's appropriate
     * method is invoked.
     */
    public interface GenericPersistStateChangeListener<S, E> {

        /**
         * Called when state needs to be persisted.
         *
         * @param state        the state
         * @param message      the message
         * @param transition   the transition
         * @param stateMachine the state machine
         */
        void onPersist(State<S, E> state, Message<E> message, Transition<S, E> transition,
                       StateMachine<S, E> stateMachine);
    }

    private class PersistingStateChangeInterceptor extends StateMachineInterceptorAdapter<S, E> {

        @Override
        public void preStateChange(State<S, E> state, Message<E> message,
                                   Transition<S, E> transition, StateMachine<S, E> stateMachine,
                                   StateMachine<S, E> rootStateMachine) {
            listeners.onPersist(state, message, transition, stateMachine);
        }
    }

    private class CompositePersistStateChangeListener extends AbstractCompositeListener<GenericPersistStateChangeListener<S, E>> implements
            GenericPersistStateChangeListener<S, E> {

        @Override
        public void onPersist(State<S, E> state, Message<E> message,
                              Transition<S, E> transition, StateMachine<S, E> stateMachine) {
            for (Iterator<GenericPersistStateChangeListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext(); ) {
                GenericPersistStateChangeListener<S, E> listener = iterator.next();
                listener.onPersist(state, message, transition, stateMachine);
            }
        }
    }

}
