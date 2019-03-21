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
package org.springframework.statemachine.config.configurers;

import org.springframework.statemachine.config.builders.StateMachineConfigurationBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfig;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;

/**
 * Default implementation of a {@link DistributedStateMachineConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultDistributedStateMachineConfigurer<S, E>
		extends AnnotationConfigurerAdapter<StateMachineConfigurationConfig<S, E>, StateMachineConfigurationConfigurer<S, E>, StateMachineConfigurationBuilder<S, E>>
		implements DistributedStateMachineConfigurer<S, E> {

	private StateMachineEnsemble<S, E> ensemble;

	@Override
	public void configure(StateMachineConfigurationBuilder<S, E> builder) throws Exception {
		builder.setStateMachineEnsemble(ensemble);
	}

	@Override
	public DistributedStateMachineConfigurer<S, E> ensemble(StateMachineEnsemble<S, E> ensemble) {
		this.ensemble = ensemble;
		return this;
	}

}
