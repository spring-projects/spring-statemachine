/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.config;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionsData;

import java.util.UUID;

/**
 * {@code StateMachineFactory} is a strategy interface building {@link StateMachine}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineFactory<S, E> {

	/**
	 * Build a new {@link StateMachine} instance.
	 *
	 * @return a new state machine instance.
	 */
	StateMachine<S, E> getStateMachine();

	/**
	 * Build a new {@link StateMachine} instance
	 * with a given machine id.
	 *
	 * @param machineId the machine id
	 * @return a new state machine instance.
	 */
	StateMachine<S, E> getStateMachine(String machineId);

	/**
	 * Build a new {@link StateMachine} instance
	 * with a given machine uuid.
	 *
	 * @param uuid to be used internally
	 * @return a new state machine instance.
	 */
	StateMachine<S, E> getStateMachine(UUID uuid);

	static <S, E> ObjectStateMachineFactory<S, E> create(StateMachineConfigBuilder<S, E> builder) {
		StateMachineConfig<S, E> stateMachineConfig = builder.getOrBuild();

		TransitionsData<S, E> stateMachineTransitions = stateMachineConfig.getTransitions();
		StatesData<S, E> stateMachineStates = stateMachineConfig.getStates();
		ConfigurationData<S, E> stateMachineConfigurationConfig = stateMachineConfig.getStateMachineConfigurationConfig();

		ObjectStateMachineFactory<S, E> stateMachineFactory = null;
		if (stateMachineConfig.getModel() != null && stateMachineConfig.getModel().getFactory() != null) {
			stateMachineFactory = new ObjectStateMachineFactory<S, E>(
					new DefaultStateMachineModel<S, E>(stateMachineConfigurationConfig, null, null),
					stateMachineConfig.getModel().getFactory());
		} else {
			stateMachineFactory = new ObjectStateMachineFactory<S, E>(new DefaultStateMachineModel<S, E>(
					stateMachineConfigurationConfig, stateMachineStates, stateMachineTransitions), null);
		}
		return stateMachineFactory;
	}
}
