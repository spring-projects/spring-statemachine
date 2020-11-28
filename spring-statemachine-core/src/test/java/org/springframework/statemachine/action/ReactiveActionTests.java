/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import reactor.core.publisher.Mono;

/**
 * Tests for state machine reactive actions.
 *
 * @author Janne Valkealahti
 *
 */
public class ReactiveActionTests extends AbstractStateMachineTests {

	@Test
	public void testSimpleReactiveActions() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		TestCountAction testAction1 = context.getBean("testAction1", TestCountAction.class);
		TestCountAction testAction2 = context.getBean("testAction2", TestCountAction.class);
		TestCountAction testAction3 = context.getBean("testAction3", TestCountAction.class);
		TestCountAction testAction4 = context.getBean("testAction4", TestCountAction.class);
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(testAction1.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(testAction2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(testAction3.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(testAction4.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(testAction1.count).isEqualTo(1);
		assertThat(testAction2.count).isEqualTo(1);
		assertThat(testAction3.count).isEqualTo(1);
		assertThat(testAction4.count).isEqualTo(1);
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.stateExitFunction(TestStates.S2, testAction2())
					.stateDoFunction(TestStates.S3, testAction3())
					.stateEntryFunction(TestStates.S3, testAction4());
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.actionFunction(testAction1())
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestCountAction testAction1() {
			return new TestCountAction();
		}

		@Bean
		public TestCountAction testAction2() {
			return new TestCountAction();
		}

		@Bean
		public TestCountAction testAction3() {
			return new TestCountAction();
		}

		@Bean
		public TestCountAction testAction4() {
			return new TestCountAction();
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private static class TestCountAction implements ReactiveAction<TestStates, TestEvents> {

		int count = 0;
		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public Mono<Void> apply(StateContext<TestStates, TestEvents> context) {
			return Mono.fromRunnable(() -> {
				count++;
				latch.countDown();
			});
		}
	}
}
