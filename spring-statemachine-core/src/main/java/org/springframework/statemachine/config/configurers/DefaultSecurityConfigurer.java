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
package org.springframework.statemachine.config.configurers;

import org.springframework.security.access.AccessDecisionManager;
import org.springframework.statemachine.config.builders.StateMachineConfigurationBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;

/**
 * Default implementation of a {@link SecurityConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultSecurityConfigurer<S, E>
		extends AnnotationConfigurerAdapter<ConfigurationData<S, E>, StateMachineConfigurationConfigurer<S, E>, StateMachineConfigurationBuilder<S, E>>
		implements SecurityConfigurer<S, E> {

	private boolean enabled = true;
	private AccessDecisionManager transitionAccessDecisionManager;
	private AccessDecisionManager eventAccessDecisionManager;
	private SecurityRule eventSecurityRule;
	private SecurityRule transitionSecurityRule;

	@Override
	public void configure(StateMachineConfigurationBuilder<S, E> builder) throws Exception {
		if (enabled) {
			builder.setSecurityEnabled(true);
			builder.setTransitionSecurityAccessDecisionManager(transitionAccessDecisionManager);
			builder.setEventSecurityAccessDecisionManager(eventAccessDecisionManager);
			builder.setEventSecurityRule(eventSecurityRule);
			builder.setTransitionSecurityRule(transitionSecurityRule);
		}
	}

	@Override
	public SecurityConfigurer<S, E> enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public SecurityConfigurer<S, E> transitionAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
		this.transitionAccessDecisionManager = accessDecisionManager;
		return this;
	}

	@Override
	public SecurityConfigurer<S, E> eventAccessDecisionManager(AccessDecisionManager accessDecisionManager) {
		this.eventAccessDecisionManager = accessDecisionManager;
		return this;
	}

	@Override
	public SecurityConfigurer<S, E> event(String attributes, ComparisonType match) {
		if (eventSecurityRule == null) {
			eventSecurityRule = new SecurityRule();
		}
		eventSecurityRule.setAttributes(SecurityRule.commaDelimitedListToSecurityAttributes(attributes));
		return this;
	}

	@Override
	public SecurityConfigurer<S, E> event(String expression) {
		if (eventSecurityRule == null) {
			eventSecurityRule = new SecurityRule();
		}
		eventSecurityRule.setExpression(expression);
		return this;
	}

	@Override
	public SecurityConfigurer<S, E> transition(String attributes, ComparisonType match) {
		if (transitionSecurityRule == null) {
			transitionSecurityRule = new SecurityRule();
		}
		transitionSecurityRule.setAttributes(SecurityRule.commaDelimitedListToSecurityAttributes(attributes));
		return this;
	}

	@Override
	public SecurityConfigurer<S, E> transition(String expression) {
		if (transitionSecurityRule == null) {
			transitionSecurityRule = new SecurityRule();
		}
		transitionSecurityRule.setExpression(expression);
		return this;
	}

}
