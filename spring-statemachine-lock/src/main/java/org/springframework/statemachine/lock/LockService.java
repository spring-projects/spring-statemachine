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

import org.springframework.statemachine.StateMachine;

/**
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface LockService<S, E> {

    /**
     * Start the lock on a given state machine.
     *
     * @param stateMachine
     * @param lockAtMostUntil the time in seconds that express when the lock expires. This is useful to limit deadlocks.
     * @return
     */
    boolean lock(StateMachine<S, E> stateMachine, int lockAtMostUntil);

    /**
     * Unlock a target state machine.
     *
     * @param stateMachine
     */
    void unLock(StateMachine<S, E> stateMachine);

}
