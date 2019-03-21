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

import org.springframework.util.Assert;

/**
 * Default implementation of a {@link StateMachineModel}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateMachineModel<S, E> extends StateMachineModel<S, E> {

	private final ConfigurationData<S, E> configuration;
	private final StatesData<S, E> states;
	private final TransitionsData<S, E> transitions;

	/**
	 * Instantiates a new default state machine model.
	 *
	 * @param configurationData the configuration
	 * @param statesData the states
	 * @param transitionsData the transitions
	 */
	public DefaultStateMachineModel(ConfigurationData<S, E> configurationData, StatesData<S, E> statesData,
			TransitionsData<S, E> transitionsData) {
		Assert.notNull(configurationData, "Configuration must be set");
		Assert.notNull(statesData, "States must be set");
		Assert.notNull(transitionsData, "Transitions must be set");
		this.configuration = configurationData;
		this.states = statesData;
		this.transitions = transitionsData;
	}

	@Override
	public ConfigurationData<S, E> getConfigurationData() {
		return configuration;
	}

	@Override
	public StatesData<S, E> getStatesData() {
		return states;
	}

	@Override
	public TransitionsData<S, E> getTransitionsData() {
		return transitions;
	}
}
