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
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;

/**
 * {@link AnnotationConfigurer} exposing configurers for states and transitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineConfigurer<S, E> extends
		AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>> {

	/**
	 * Callback for {@link StateMachineModelConfigurer}.
	 *
	 * @param model the {@link StateMachineModelConfigurer}
	 * @throws Exception if configuration error happens
	 */
	void configure(StateMachineModelConfigurer<S, E> model) throws Exception;

	/**
	 * Callback for {@link StateMachineConfigurationConfigurer}.
	 *
	 * @param config the {@link StateMachineConfigurationConfigurer}
	 * @throws Exception if configuration error happens
	 */
	void configure(StateMachineConfigurationConfigurer<S, E> config) throws Exception;

	/**
	 * Callback for {@link StateMachineStateConfigurer}.
	 *
	 * @param states the {@link StateMachineStateConfigurer}
	 * @throws Exception if configuration error happens
	 */
	void configure(StateMachineStateConfigurer<S, E> states) throws Exception;

	/**
	 * Callback for {@link StateMachineTransitionConfigurer}.
	 *
	 * @param transitions the {@link StateMachineTransitionConfigurer}
	 * @throws Exception if configuration error happens
	 */
	void configure(StateMachineTransitionConfigurer<S, E> transitions) throws Exception;
}