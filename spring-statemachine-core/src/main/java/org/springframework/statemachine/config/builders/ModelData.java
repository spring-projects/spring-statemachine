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

import org.springframework.statemachine.config.configurers.ModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;

/**
 * Data object used return and build data from a {@link ModelConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ModelData<S, E> {

	private StateMachineModelFactory<S, E> factory;

	/**
	 * Instantiates a new model data.
	 *
	 * @param factory the factory
	 */
	public ModelData(StateMachineModelFactory<S, E> factory) {
		this.factory = factory;
	}

	/**
	 * Gets the factory.
	 *
	 * @return the factory
	 */
	public StateMachineModelFactory<S, E> getFactory() {
		return factory;
	}
}
