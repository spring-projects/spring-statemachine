/*
 * Copyright 2015-2016 the original author or authors.
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

import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionsData;

public class StateMachineConfig<S, E> {

	public final ConfigurationData<S, E> stateMachineConfigurationConfig;

	public final TransitionsData<S, E> transitions;

	public final StatesData<S, E> states;

	public StateMachineConfig(ConfigurationData<S, E> stateMachineConfigurationConfig, TransitionsData<S, E> transitions, StatesData<S, E> states) {
		this.stateMachineConfigurationConfig = stateMachineConfigurationConfig;
		this.transitions = transitions;
		this.states = states;
	}

	public ConfigurationData<S, E> getStateMachineConfigurationConfig() {
		return stateMachineConfigurationConfig;
	}

	public TransitionsData<S, E> getTransitions() {
		return transitions;
	}

	public StatesData<S, E> getStates() {
		return states;
	}

}