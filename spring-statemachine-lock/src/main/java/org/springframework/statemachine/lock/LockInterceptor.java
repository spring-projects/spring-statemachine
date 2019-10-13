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
package org.springframework.statemachine.lock;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;

/**
 * This aims to:
 * - verify if there is a lock before executing the event, if there isn't the lock will be acquired and the event will be performed;
 * - unlock the state machine when the state changes.
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class LockInterceptor<S, E> extends StateMachineInterceptorAdapter<S, E> {

    private LockService<S, E> lockService;
    private int lockAtMostUntil;

    public LockInterceptor(LockService<S, E> lockService, int lockAtMostUntil) {
        this.lockService = lockService;
        this.lockAtMostUntil = lockAtMostUntil;
    }

    @Override
    public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
        boolean lockResult = this.lockService.lock(stateMachine, this.lockAtMostUntil);
        return lockResult ? message : null;
    }

    @Override
    public void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
        this.lockService.unLock(stateMachine);
    }

}
