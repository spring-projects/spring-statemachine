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

import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.SpelExpressionGuard;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * Default implementation of a {@link InternalTransitionConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultInternalTransitionConfigurer<S, E> extends AbstractTransitionConfigurer<S, E>
		implements InternalTransitionConfigurer<S, E> {

	@Override
	public void configure(StateMachineTransitionBuilder<S, E> builder) throws Exception {
		builder.addTransition(getSource(), getTarget(), getState(), getEvent(), getPeriod(), getCount(), getActions(), getGuard(), TransitionKind.INTERNAL,
				getSecurityRule());
	}

	@Override
	public InternalTransitionConfigurer<S, E> source(S source) {
		setSource(source);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> state(S state) {
		setState(state);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> event(E event) {
		setEvent(event);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> timer(long period) {
		setPeriod(period);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> timerOnce(long period) {
		setPeriod(period);
		setCount(1);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> action(Action<S, E> action) {
		return action(action, null);
	}

	@Override
	public InternalTransitionConfigurer<S, E> action(Action<S, E> action, Action<S, E> error) {
		addAction(action, error);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> guard(Guard<S, E> guard) {
		setGuard(guard);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> guardExpression(String expression) {
		SpelExpressionParser parser = new SpelExpressionParser(
				new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
		setGuard(new SpelExpressionGuard<S, E>(parser.parseExpression(expression)));
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> secured(String attributes, ComparisonType match) {
		setSecurityRule(attributes, match);
		return this;
	}

	@Override
	public InternalTransitionConfigurer<S, E> secured(String expression) {
		setSecurityRule(expression);
		return this;
	}

}
