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

import org.springframework.statemachine.StateMachine;

/**
 * {@code PersistStateMachineHandler} is a recipe which can be used to
 * handle a state change of an arbitrary entity in a persistent storage.
 * <br>
 * For concurrent usage, please consider using {@link FactoryPersistStateMachineHandler}
 * to provide thread safe feature instead.
 *
 * @author Janne Valkealahti
 */
public class PersistStateMachineHandler extends GenericPersistStateMachineHandler<String, String> {

    public PersistStateMachineHandler(StateMachine<String, String> stateMachine) {
        super(stateMachine);
    }

    public interface PersistStateChangeListener extends GenericPersistStateChangeListener<String, String> {
    }

}
