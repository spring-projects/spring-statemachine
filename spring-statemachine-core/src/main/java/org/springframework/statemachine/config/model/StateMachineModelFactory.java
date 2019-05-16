/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.config.model;

/**
 * A generic builder interface for building {@link StateMachineModel}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineModelFactory<S, E> {

	/**
	 * Builds the state machine model.
	 *
	 * @return the state machine model
	 */
	StateMachineModel<S, E> build();

	/**
	 * Builds the state machine model with a given {@code machineId}.
	 * Implementation is free to choose what to do with a given {@code machineId}
	 * but usually it might map to a different configurations supported
	 * by storage or repository in a factory.
	 *
	 * @param machineId the machine id
	 * @return the state machine model
	 */
	StateMachineModel<S, E> build(String machineId);
}
