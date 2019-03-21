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
package org.springframework.statemachine.config.builders;

import org.springframework.statemachine.config.StateMachineConfig;
import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionsData;

/**
 * {@link AnnotationBuilder} handling all shared builders which effectively contructs
 * full configuration for {@link StateMachineConfig}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineConfigBuilder<S, E>
		extends
		AbstractConfiguredAnnotationBuilder<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>, StateMachineConfigBuilder<S, E>> {

	@SuppressWarnings("unchecked")
	@Override
	protected StateMachineConfig<S, E> performBuild() throws Exception {
		// TODO: should prevent calling state/transition builder if model is given
		StateMachineModelBuilder<?, ?> modelBuilder = getSharedObject(StateMachineModelBuilder.class);
		StateMachineConfigurationBuilder<?, ?> configurationBuilder = getSharedObject(StateMachineConfigurationBuilder.class);
		StateMachineTransitionBuilder<?, ?> transitionBuilder = getSharedObject(StateMachineTransitionBuilder.class);
		StateMachineStateBuilder<?, ?> stateBuilder = getSharedObject(StateMachineStateBuilder.class);
		ModelData<S, E> model = (ModelData<S, E>) modelBuilder.build();
		ConfigurationData<S, E> stateMachineConfigurationConfig = null;
		stateMachineConfigurationConfig = (ConfigurationData<S, E>) configurationBuilder.build();
		transitionBuilder.setSharedObject(ConfigurationData.class, stateMachineConfigurationConfig);
		TransitionsData<S, E> transitions = (TransitionsData<S, E>) transitionBuilder.build();
		StatesData<S, E> states = (StatesData<S, E>) stateBuilder.build();
		return new StateMachineConfig<S, E>(stateMachineConfigurationConfig, transitions, states, model);
	}
}
