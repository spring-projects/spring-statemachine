/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.statemachine.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class SpelExpressionActionTests extends AbstractStateMachineTests {

	@Test
	public void testSpelActionSendsEvent() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config1.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(ctx);
		doStartAndAssert(machine);
		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);
		listener.reset(2, 0);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S3);
		ctx.close();
	}

	private static class TestSpelAction extends SpelExpressionAction<TestStates, TestEvents> {

		public TestSpelAction(Expression expression) {
			super(expression);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1, TestEvents.E2)
					.state(TestStates.S2)
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.action(testAction1())
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestSpelAction testAction1() {
			ExpressionParser parser = new SpelExpressionParser();
			return new TestSpelAction(
					parser.parseExpression("stateMachine.sendEvent(T(org.springframework.statemachine.AbstractStateMachineTests.TestEvents).E2)"));
		}

	}

}
