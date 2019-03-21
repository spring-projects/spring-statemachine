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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.Message;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.StringUtils;

/**
 * {@link StateMachineInterceptor} which can be registered into a {@link StateMachine}
 * order to intercept a various security related checks.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineSecurityInterceptor<S, E> extends StateMachineInterceptorAdapter<S, E> {

	private AccessDecisionManager transitionAccessDecisionManager;
	private AccessDecisionManager eventAccessDecisionManager;
	private final ExpressionParser expressionParser = new SpelExpressionParser(new SpelParserConfiguration(SpelCompilerMode.OFF, null));
	private SecurityRule eventSecurityRule;

	/**
	 * Instantiates a new state machine security interceptor.
	 */
	public StateMachineSecurityInterceptor() {
		this(null, null);
	}

	/**
	 * Instantiates a new state machine security interceptor with
	 * a custom {@link AccessDecisionManager} for both transitions
	 * and events.
	 *
	 * @param transitionAccessDecisionManager the transition access decision manager
	 * @param eventAccessDecisionManager the event access decision manager
	 */
	public StateMachineSecurityInterceptor(AccessDecisionManager transitionAccessDecisionManager, AccessDecisionManager eventAccessDecisionManager) {
		this(transitionAccessDecisionManager, eventAccessDecisionManager, null);
	}

	/**
	 * Instantiates a new state machine security interceptor with
	 * a custom {@link AccessDecisionManager} for both transitions
	 * and events and a {@link SecurityRule} for events;
	 *
	 * @param transitionAccessDecisionManager the transition access decision manager
	 * @param eventAccessDecisionManager the event access decision manager
	 * @param eventSecurityRule the event security rule
	 */
	public StateMachineSecurityInterceptor(AccessDecisionManager transitionAccessDecisionManager,
			AccessDecisionManager eventAccessDecisionManager, SecurityRule eventSecurityRule) {
		this.transitionAccessDecisionManager = transitionAccessDecisionManager;
		this.eventAccessDecisionManager = eventAccessDecisionManager;
		this.eventSecurityRule = eventSecurityRule;
	}

	@Override
	public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
		if (eventSecurityRule != null) {
			decide(eventSecurityRule, message);
		}
		return super.preEvent(message, stateMachine);
	}

	@Override
	public StateContext<S, E> preTransition(StateContext<S, E> stateContext) {
		Transition<S, E> transition = stateContext.getTransition();
		SecurityRule rule = transition.getSecurityRule();
		if (rule != null) {
			decide(rule, transition);
		}
		return super.preTransition(stateContext);
	}

	/**
	 * Sets the event access decision manager.
	 *
	 * @param eventAccessDecisionManager the new event access decision manager
	 */
	public void setEventAccessDecisionManager(AccessDecisionManager eventAccessDecisionManager) {
		this.eventAccessDecisionManager = eventAccessDecisionManager;
	}

	/**
	 * Sets the transition access decision manager.
	 *
	 * @param transitionAccessDecisionManager the new transition access decision manager
	 */
	public void setTransitionAccessDecisionManager(AccessDecisionManager transitionAccessDecisionManager) {
		this.transitionAccessDecisionManager = transitionAccessDecisionManager;
	}

	/**
	 * Sets the event security rule.
	 *
	 * @param eventSecurityRule the new event security rule
	 */
	public void setEventSecurityRule(SecurityRule eventSecurityRule) {
		this.eventSecurityRule = eventSecurityRule;
	}

	private void decide(SecurityRule rule, Message<E> object) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<ConfigAttribute> configAttributes = getEentConfigAttributes(rule);
		if (eventAccessDecisionManager != null) {
			decide(eventAccessDecisionManager, authentication, object, configAttributes);
		} else {
			decide(createDefaultEventManager(rule), authentication, object, configAttributes);
		}
	}

	private void decide(SecurityRule rule, Transition<S, E> object) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Collection<ConfigAttribute> configAttributes = getTransitionConfigAttributes(rule);
		if (transitionAccessDecisionManager != null) {
			decide(transitionAccessDecisionManager, authentication, object, configAttributes);
		} else {
			decide(createDefaultTransitionManager(rule), authentication, object, configAttributes);
		}
	}

	private Collection<ConfigAttribute> getTransitionConfigAttributes(SecurityRule rule) {
		List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
		if (rule.getAttributes() != null) {
			for (String attribute : rule.getAttributes()) {
				configAttributes.add(new SecurityConfig(attribute));
			}
		}
		if (StringUtils.hasText(rule.getExpression())) {
			configAttributes.add(new TransitionExpressionConfigAttribute(expressionParser.parseExpression(rule.getExpression())));
		}
		return configAttributes;
	}

	private Collection<ConfigAttribute> getEentConfigAttributes(SecurityRule rule) {
		List<ConfigAttribute> configAttributes = new ArrayList<ConfigAttribute>();
		if (rule.getAttributes() != null) {
			for (String attribute : rule.getAttributes()) {
				configAttributes.add(new SecurityConfig(attribute));
			}
		}
		if (StringUtils.hasText(rule.getExpression())) {
			configAttributes.add(new EventExpressionConfigAttribute(expressionParser.parseExpression(rule.getExpression())));
		}
		return configAttributes;
	}

	private void decide(AccessDecisionManager manager, Authentication authentication, Transition<S, E> object,
			Collection<ConfigAttribute> configAttributes) {
		if (manager.supports(object.getClass())) {
			manager.decide(authentication, object, configAttributes);
		}
	}

	private void decide(AccessDecisionManager manager, Authentication authentication, Message<E> object,
			Collection<ConfigAttribute> configAttributes) {
		if (manager.supports(object.getClass())) {
			manager.decide(authentication, object, configAttributes);
		}
	}

	private AbstractAccessDecisionManager createDefaultTransitionManager(SecurityRule rule) {
		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
		voters.add(new TransitionExpressionVoter());
		voters.add(new TransitionVoter<Object, Object>());
		voters.add(new RoleVoter());
		if (rule.getComparisonType() == SecurityRule.ComparisonType.ANY) {
			return new AffirmativeBased(voters);
		} else if (rule.getComparisonType() == SecurityRule.ComparisonType.ALL) {
			return new UnanimousBased(voters);
		} else if (rule.getComparisonType() == SecurityRule.ComparisonType.MAJORITY) {
			return new ConsensusBased(voters);
		} else {
			throw new IllegalStateException("Unknown SecurityRule match type: " + rule.getComparisonType());
		}
	}

	private AbstractAccessDecisionManager createDefaultEventManager(SecurityRule rule) {
		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
		voters.add(new EventExpressionVoter<Object>());
		voters.add(new EventVoter<Object>());
		voters.add(new RoleVoter());
		if (rule.getComparisonType() == SecurityRule.ComparisonType.ANY) {
			return new AffirmativeBased(voters);
		} else if (rule.getComparisonType() == SecurityRule.ComparisonType.ALL) {
			return new UnanimousBased(voters);
		} else if (rule.getComparisonType() == SecurityRule.ComparisonType.MAJORITY) {
			return new ConsensusBased(voters);
		} else {
			throw new IllegalStateException("Unknown SecurityRule match type: " + rule.getComparisonType());
		}
	}

	@Override
	public String toString() {
		return "StateMachineSecurityInterceptor [transitionAccessDecisionManager=" + transitionAccessDecisionManager
				+ ", eventAccessDecisionManager=" + eventAccessDecisionManager + ", eventSecurityRule=" + eventSecurityRule + "]";
	}

}
