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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.SpelExpressionGuard;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * Default implementation of a {@link LocalTransitionConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultLocalTransitionConfigurer<S, E>
		extends AnnotationConfigurerAdapter<StateMachineTransitions<S, E>, StateMachineTransitionConfigurer<S, E>, StateMachineTransitionBuilder<S, E>>
		implements LocalTransitionConfigurer<S, E> {

	private S source;

	private S target;

	private S state;

	private E event;

	private Long period;

	private Collection<Action<S, E>> actions = new ArrayList<Action<S, E>>();

	private Guard<S, E> guard;

	@Override
	public void configure(StateMachineTransitionBuilder<S, E> builder) throws Exception {
		builder.add(source, target, state, event, period, actions, guard, TransitionKind.LOCAL);
	}

	@Override
	public LocalTransitionConfigurer<S, E> source(S source) {
		this.source = source;
		return this;
	}

	@Override
	public LocalTransitionConfigurer<S, E> target(S target) {
		this.target = target;
		return this;
	}

	@Override
	public LocalTransitionConfigurer<S, E> state(S state) {
		this.state = state;
		return this;
	}

	@Override
	public LocalTransitionConfigurer<S, E> event(E event) {
		this.event = event;
		return this;
	}

	@Override
	public LocalTransitionConfigurer<S, E> timer(long period) {
		this.period = period;
		return this;
	}

	@Override
	public LocalTransitionConfigurer<S, E> action(Action<S, E> action) {
		actions.add(action);
		return this;
	}

	@Override
	public LocalTransitionConfigurer<S, E> guard(Guard<S, E> guard) {
		this.guard = guard;
		return this;
	}

	@Override
	public LocalTransitionConfigurer<S, E> guardExpression(String expression) {
		SpelExpressionParser parser = new SpelExpressionParser(
				new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
		this.guard = new SpelExpressionGuard<S, E>(parser.parseExpression(expression));
		return this;
	}

}
