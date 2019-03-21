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

import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;

/**
 * Adapter base implementation for {@link StateMachineConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractStateMachineConfigurerAdapter<S, E> implements StateMachineConfigurer<S, E> {

	private StateMachineTransitionBuilder<S, E> transitionBuilder;
	private StateMachineStateBuilder<S, E> stateBuilder;
	private StateMachineConfigurationBuilder<S, E> configurationBuilder;

	@Override
	public final void init(StateMachineConfigBuilder<S, E> config) throws Exception {
		config.setSharedObject(StateMachineTransitionBuilder.class, getStateMachineTransitionBuilder());
		config.setSharedObject(StateMachineStateBuilder.class, getStateMachineStateBuilder());
		config.setSharedObject(StateMachineConfigurationBuilder.class, getStateMachineConfigurationBuilder());
	}

	@Override
	public void configure(StateMachineConfigBuilder<S, E> config) throws Exception {
	}

	@Override
	public void configure(StateMachineConfigurationConfigurer<S, E> config) throws Exception {
	}

	@Override
	public void configure(StateMachineStateConfigurer<S, E> states) throws Exception {
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<S, E> transitions) throws Exception {
	}

	@Override
	public boolean isAssignable(AnnotationBuilder<StateMachineConfig<S, E>> builder) {
		return builder instanceof StateMachineConfigBuilder;
	}

	protected final StateMachineTransitionBuilder<S, E> getStateMachineTransitionBuilder() throws Exception {
		if (transitionBuilder != null) {
			return transitionBuilder;
		}
		transitionBuilder = new StateMachineTransitionBuilder<S, E>(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR, true);
		configure(transitionBuilder);
		return transitionBuilder;
	}

	protected final StateMachineStateBuilder<S, E> getStateMachineStateBuilder() throws Exception {
		if (stateBuilder != null) {
			return stateBuilder;
		}
		stateBuilder = new StateMachineStateBuilder<S, E>(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR, true);
		configure(stateBuilder);
		return stateBuilder;
	}

	protected final StateMachineConfigurationBuilder<S, E> getStateMachineConfigurationBuilder() throws Exception {
		if (configurationBuilder != null) {
			return configurationBuilder;
		}
		configurationBuilder = new StateMachineConfigurationBuilder<S, E>(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR, true);
		configure(configurationBuilder);
		return configurationBuilder;
	}

}
