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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

/**
 * This listener is responsible of the unlock phase.
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class LockStateMachineListener<S, E> extends StateMachineListenerAdapter<S, E> {

    private final static Log log = LogFactory.getLog(LockStateMachineListener.class);

    private final LockService<S, E> lockService;

    public LockStateMachineListener(LockService<S, E> lockService) {
        this.lockService = lockService;
    }

    @Override
    public void stateContext(StateContext<S, E> stateContext) {
        if (Stage.TRANSITION_END.equals(stateContext.getStage())) {
            if (log.isDebugEnabled()) {
                log.debug("Starting unlock for state machine with id: " + stateContext.getStateMachine().getId());
            }
            lockService.unLock(stateContext.getStateMachine());
        }
    }

}
