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
package org.springframework.statemachine.config.builders;

import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.statemachine.config.configurers.DefaultModelConfigurer;
import org.springframework.statemachine.config.configurers.ModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;

/**
 * {@link AnnotationBuilder} for {@link ModelData}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineModelBuilder<S, E>
		extends AbstractConfiguredAnnotationBuilder<ModelData<S, E>, StateMachineModelConfigurer<S, E>, StateMachineModelBuilder<S, E>>
		implements StateMachineModelConfigurer<S, E> {

	private StateMachineModelFactory<S, E> factory;

	/**
	 * Instantiates a new state machine model builder.
	 */
	public StateMachineModelBuilder() {
		super();
	}

	/**
	 * Instantiates a new state machine model builder.
	 *
	 * @param objectPostProcessor the object post processor
	 * @param allowConfigurersOfSameType the allow configurers of same type
	 */
	public StateMachineModelBuilder(ObjectPostProcessor<Object> objectPostProcessor,
			boolean allowConfigurersOfSameType) {
		super(objectPostProcessor, allowConfigurersOfSameType);
	}

	/**
	 * Instantiates a new state machine model builder.
	 *
	 * @param objectPostProcessor the object post processor
	 */
	public StateMachineModelBuilder(ObjectPostProcessor<Object> objectPostProcessor) {
		super(objectPostProcessor);
	}

	@Override
	protected ModelData<S, E> performBuild() throws Exception {
		return new ModelData<S, E>(factory);
	}

	@Override
	public ModelConfigurer<S, E> withModel() throws Exception {
		return apply(new DefaultModelConfigurer<S, E>());
	}

	public void setStateMachineModelFactory(StateMachineModelFactory<S, E> factory) {
		this.factory = factory;
	}
}
