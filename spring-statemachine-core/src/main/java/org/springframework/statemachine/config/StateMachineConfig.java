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

import org.springframework.statemachine.config.builders.StateMachineConfigurationConfig;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitions;

public class StateMachineConfig<S, E> {

	public final StateMachineConfigurationConfig<S, E> stateMachineConfigurationConfig;

	public final StateMachineTransitions<S, E> transitions;

	public final StateMachineStates<S, E> states;

	public StateMachineConfig(StateMachineConfigurationConfig<S, E> stateMachineConfigurationConfig, StateMachineTransitions<S, E> transitions, StateMachineStates<S, E> states) {
		this.stateMachineConfigurationConfig = stateMachineConfigurationConfig;
		this.transitions = transitions;
		this.states = states;
	}

	public StateMachineConfigurationConfig<S, E> getStateMachineConfigurationConfig() {
		return stateMachineConfigurationConfig;
	}

	public StateMachineTransitions<S, E> getTransitions() {
		return transitions;
	}

	public StateMachineStates<S, E> getStates() {
		return states;
	}

}