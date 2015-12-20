/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.config.builders;

import org.springframework.statemachine.config.StateMachineConfig;
import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;

public class StateMachineConfigBuilder<S, E>
		extends
		AbstractConfiguredAnnotationBuilder<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>, StateMachineConfigBuilder<S, E>> {

	@SuppressWarnings("unchecked")
	@Override
	protected StateMachineConfig<S, E> performBuild() throws Exception {
		StateMachineConfigurationBuilder<?, ?> configurationBuilder = getSharedObject(StateMachineConfigurationBuilder.class);
		StateMachineTransitionBuilder<?, ?> transitionBuilder = getSharedObject(StateMachineTransitionBuilder.class);
		StateMachineStateBuilder<?, ?> stateBuilder = getSharedObject(StateMachineStateBuilder.class);

		// build config first as it's shared with transition builder
		StateMachineConfigurationConfig<S, E> config = (StateMachineConfigurationConfig<S, E>) configurationBuilder.build();
		transitionBuilder.setSharedObject(StateMachineConfigurationConfig.class, config);
		StateMachineTransitions<S, E> transitions = (StateMachineTransitions<S, E>) transitionBuilder.build();
		StateMachineStates<S, E> states = (StateMachineStates<S, E>) stateBuilder.build();
		StateMachineConfig<S, E> bean = new StateMachineConfig<S, E>(config, transitions, states);
		return bean;
	}

}
