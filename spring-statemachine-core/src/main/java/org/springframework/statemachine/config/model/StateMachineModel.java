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
package org.springframework.statemachine.config.model;

/**
 * Base abstract SPI class for state machine configuration.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class StateMachineModel<S, E> {

	/**
	 * Gets the configuration config data.
	 *
	 * @return the configuration config data
	 */
	public abstract ConfigurationData<S, E> getConfigurationData();

	/**
	 * Gets the states config data.
	 *
	 * @return the states config data
	 */
	public abstract StatesData<S, E> getStatesData();

	/**
	 * Gets the transitions config data.
	 *
	 * @return the transitions config data
	 */
	public abstract TransitionsData<S, E> getTransitionsData();
}
