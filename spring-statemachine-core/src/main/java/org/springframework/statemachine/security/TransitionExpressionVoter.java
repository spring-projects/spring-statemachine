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
package org.springframework.statemachine.security;

import java.util.Collection;

import org.springframework.expression.EvaluationContext;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.transition.Transition;

/**
 * {@link AccessDecisionVoter} evaluating access via security spel expression.
 *
 * @author Janne Valkealahti
 *
 */
public class TransitionExpressionVoter implements AccessDecisionVoter<Transition<?, ?>> {

	private final SecurityExpressionHandler<Transition<?, ?>> expressionHandler = new DefaultTransitionSecurityExpressionHandler();

	@Override
	public boolean supports(ConfigAttribute attribute) {
		return attribute instanceof TransitionExpressionConfigAttribute;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Transition.class.isAssignableFrom(clazz);
	}

	@Override
	public int vote(Authentication authentication, Transition<?, ?> object, Collection<ConfigAttribute> attributes) {
		TransitionExpressionConfigAttribute teca = findConfigAttribute(attributes);
		if (teca == null) {
			return ACCESS_ABSTAIN;
		}
		EvaluationContext ctx = expressionHandler.createEvaluationContext(authentication, object);
		return ExpressionUtils.evaluateAsBoolean(teca.getAuthorizeExpression(), ctx) ? ACCESS_GRANTED : ACCESS_DENIED;
	}

	private TransitionExpressionConfigAttribute findConfigAttribute(Collection<ConfigAttribute> attributes) {
		for (ConfigAttribute attribute : attributes) {
			if (attribute instanceof TransitionExpressionConfigAttribute) {
				return (TransitionExpressionConfigAttribute) attribute;
			}
		}
		return null;
	}

}
