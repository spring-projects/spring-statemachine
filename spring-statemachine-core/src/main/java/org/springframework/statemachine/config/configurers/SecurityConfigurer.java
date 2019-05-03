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

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerBuilder;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;

/**
 * Base {@code ConfigConfigurer} interface for configuring generic config.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface SecurityConfigurer<S, E> extends
		AnnotationConfigurerBuilder<StateMachineConfigurationConfigurer<S, E>> {

	/**
	 * Specify if security is enabled. On default security is enabled
	 * if configurer is used.
	 *
	 * @param enabled the enable flag
	 * @return configurer for chaining
	 */
	SecurityConfigurer<S, E> enabled(boolean enabled);

	/**
	 * Specify a custom {@link AccessDecisionManager} for transitions.
	 *
	 * @param accessDecisionManager the access decision manager
	 * @return configurer for chaining
	 */
	SecurityConfigurer<S, E> transitionAccessDecisionManager(AccessDecisionManager accessDecisionManager);

	/**
	 * Specify a custom {@link AccessDecisionManager} for events.
	 *
	 * @param accessDecisionManager the access decision manager
	 * @return configurer for chaining
	 */
	SecurityConfigurer<S, E> eventAccessDecisionManager(AccessDecisionManager accessDecisionManager);

	/**
	 * Specify a security attributes for events.
	 *
	 * @param attributes the security attributes
	 * @param match the match type
	 * @return configurer for chaining
	 */
	SecurityConfigurer<S, E> event(String attributes, ComparisonType match);

	/**
	 * Specify a security attributes for events.
	 *
	 * @param expression the the security expression
	 * @return configurer for chaining
	 */
	SecurityConfigurer<S, E> event(String expression);

	/**
	 * Specify a security attributes for transitions.
	 *
	 * @param attributes the security attributes
	 * @param match the match type
	 * @return configurer for chaining
	 */
	SecurityConfigurer<S, E> transition(String attributes, ComparisonType match);

	/**
	 * Specify a security attributes for transitions.
	 *
	 * @param expression the the security expression
	 * @return configurer for chaining
	 */
	SecurityConfigurer<S, E> transition(String expression);

}
