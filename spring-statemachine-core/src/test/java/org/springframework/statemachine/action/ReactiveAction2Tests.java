/*
 * Copyright 2021 the original author or authors.
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
import static org.springframework.statemachine.TestUtils.doSendEventsAndConsumeAllWithComplete;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

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
public class ReactiveAction2Tests extends AbstractStateMachineTests {

	@Test
	public void testSimpleReactiveActions() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		TestCountAction testAction3 = context.getBean("testAction3", TestCountAction.class);
		TestCountAction testAction4 = context.getBean("testAction4", TestCountAction.class);
		doSendEventsAndConsumeAllWithComplete(machine, TestEvents.E1, TestEvents.E2);
		assertThat(testAction3.latch.await(6, TimeUnit.SECONDS)).isTrue();
		assertThat(testAction4.latch.await(6, TimeUnit.SECONDS)).isTrue();
		assertThat(testAction4.time.get() - testAction3.time.get()).isGreaterThan(1000);
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.stateEntryFunction(TestStates.S2, testAction3())
					.stateEntryFunction(TestStates.S3, testAction4());
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestCountAction testAction3() {
			return new TestCountAction("ACTION3");
		}

		@Bean
		public TestCountAction testAction4() {
			return new TestCountAction("ACTION4");
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private static class TestCountAction implements ReactiveAction<TestStates, TestEvents> {

		private final String id;
		int count = 0;
		CountDownLatch latch = new CountDownLatch(1);
		AtomicLong time = new AtomicLong();

		TestCountAction(String id) {
			this.id = id;
		}

		@Override
		public Mono<Void> apply(StateContext<TestStates, TestEvents> context) {
			return Mono.delay(Duration.ofMillis(2000))
				.doFinally(x -> {
					count++;
					time.set(System.currentTimeMillis());
					latch.countDown();
				})
				.then()
				.log(id);
		}
	}
}
