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

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Base class for transition configurers.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractTransitionConfigurer<S, E> extends
		AnnotationConfigurerAdapter<TransitionsData<S, E>, StateMachineTransitionConfigurer<S, E>, StateMachineTransitionBuilder<S, E>> {

	private S source;
	private S target;
	private S state;
	private E event;
	private Long period;
	private Integer count;
	private final Collection<Action<S, E>> actions = new ArrayList<>();
	private Guard<S, E> guard;
	private SecurityRule securityRule;

	protected S getSource() {
		return source;
	}

	protected S getTarget() {
		return target;
	}

	protected S getState() {
		return state;
	}

	protected E getEvent() {
		return event;
	}

	protected Long getPeriod() {
		return period;
	}

	/**
	 *
	 * @return trigger count
	 */
	public Integer getCount() {
		return count;
	}

	protected Collection<Action<S, E>> getActions() {
		return actions;
	}

	protected Guard<S, E> getGuard() {
		return guard;
	}

	protected SecurityRule getSecurityRule() {
		return securityRule;
	}

	protected void setSource(S source) {
		this.source = source;
	}

	protected void setTarget(S target) {
		this.target = target;
	}

	protected void setState(S state) {
		this.state = state;
	}

	protected void setEvent(E event) {
		this.event = event;
	}

	protected void setPeriod(long period) {
		this.period = period;
	}

	/**
	 *
	 * @param count to set how many time the trigger will be called.
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	protected void addAction(Action<S, E> action) {
		addAction(action, null);
	}

	protected void addAction(Action<S, E> action, Action<S, E> error) {
		this.actions.add(error != null ? Actions.errorCallingAction(action, error) : action);
	}

	protected void setGuard(Guard<S, E> guard) {
		this.guard = guard;
	}

	protected void setSecurityRule(String attributes, ComparisonType match) {
		if (securityRule == null) {
			securityRule = new SecurityRule();
		}
		securityRule.setAttributes(SecurityRule.commaDelimitedListToSecurityAttributes(attributes));
	}

	protected void setSecurityRule(String expression) {
		if (securityRule == null) {
			securityRule = new SecurityRule();
		}
		securityRule.setExpression(expression);
	}

}
