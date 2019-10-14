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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

/**
 * The guard will try to lock the state machine.
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class LockStateMachineGuard<S, E> implements Guard<S, E> {

    private final static Logger log = LoggerFactory.getLogger(LockStateMachineListener.class);

    private final LockService<S, E> lockService;
    private final int lockAtMostUntil;

    /**
     *
     * @param lockService
     * @param lockAtMostUntil defines when the lock expires
     */
    public LockStateMachineGuard(LockService<S, E> lockService, int lockAtMostUntil) {
        this.lockService = lockService;
        this.lockAtMostUntil = lockAtMostUntil;
    }

    /**
     * @param stateContext
     * @return true, if the lock has success, false otherwise.
     */
    @Override
    public boolean evaluate(StateContext<S, E> stateContext) {
        if (log.isDebugEnabled()) {
			String id = stateContext.getStateMachine().getId();
			log.debug("Starting lock on {} with event {} and status from {} to {}", id, stateContext.getEvent(), stateContext.getSource(), stateContext.getTarget());
        }
        return lockService.lock(stateContext.getStateMachine(), this.lockAtMostUntil);
    }

}
