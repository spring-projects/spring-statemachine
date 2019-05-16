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
package org.springframework.statemachine.config.builders;

import org.springframework.statemachine.config.configurers.ConfigurationConfigurer;
import org.springframework.statemachine.config.configurers.DistributedStateMachineConfigurer;

/**
 * Configurer interface exposing generic config.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineConfigurationConfigurer<S, E> {

	/**
	 * Gets a configurer for generic config.
	 *
	 * @return {@link ConfigurationConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	ConfigurationConfigurer<S, E> withConfiguration() throws Exception;

	/**
	 * Gets a configurer for distributed state machine config.
	 *
	 * @return {@link DistributedStateMachineConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	DistributedStateMachineConfigurer<S, E> withDistributed() throws Exception;

}
