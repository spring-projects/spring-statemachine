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
package org.springframework.statemachine.config.configurers;

import org.springframework.statemachine.config.builders.ModelData;
import org.springframework.statemachine.config.builders.StateMachineModelBuilder;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.config.model.StateMachineModelFactory;

/**
 * Default implementation of a {@link ModelConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultModelConfigurer<S, E>
		extends AnnotationConfigurerAdapter<ModelData<S, E>, StateMachineModelConfigurer<S, E>, StateMachineModelBuilder<S, E>>
		implements ModelConfigurer<S, E> {

	private StateMachineModelFactory<S, E> factory;

	@Override
	public void configure(StateMachineModelBuilder<S, E> builder) throws Exception {
		builder.setStateMachineModelFactory(factory);
	}

	@Override
	public ModelConfigurer<S, E> factory(StateMachineModelFactory<S, E> factory) {
		this.factory = factory;
		return this;
	}
}
