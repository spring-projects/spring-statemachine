/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.config.model;

/**
 * Default implementation of a {@link StateMachineModel}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateMachineModel<S, E> extends StateMachineModel<S, E> {

	private final StateMachineConfigurationConfig<S, E> configuration;
	private final StateMachineStates<S, E> states;
	private final StateMachineTransitions<S, E> transitions;

	/**
	 * Instantiates a new default state machine model.
	 *
	 * @param configuration the configuration
	 * @param states the states
	 * @param transitions the transitions
	 */
	public DefaultStateMachineModel(StateMachineConfigurationConfig<S, E> configuration, StateMachineStates<S, E> states,
			StateMachineTransitions<S, E> transitions) {
		this.configuration = configuration;
		this.states = states;
		this.transitions = transitions;
	}

	@Override
	public StateMachineConfigurationConfig<S, E> getConfiguration() {
		return configuration;
	}

	@Override
	public StateMachineStates<S, E> getStates() {
		return states;
	}

	@Override
	public StateMachineTransitions<S, E> getTransitions() {
		return transitions;
	}
}
