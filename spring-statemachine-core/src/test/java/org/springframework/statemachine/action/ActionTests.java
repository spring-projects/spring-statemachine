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
package org.springframework.statemachine.action;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for state machine actions.
 *
 * @author Janne Valkealahti
 *
 */
public class ActionTests extends AbstractStateMachineTests {

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTransitionActions() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config1.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		machine.start();

		TestCountAction testAction1 = ctx.getBean("testAction1", TestCountAction.class);
		TestCountAction testAction2 = ctx.getBean("testAction2", TestCountAction.class);
		TestCountAction testAction3 = ctx.getBean("testAction3", TestCountAction.class);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E3).build());
		assertThat(testAction1.count, is(1));
		assertThat(testAction2.count, is(1));
		assertThat(testAction3.count, is(1));
		ctx.close();
	}

	@Test
	public void testEventFromAction() {

	}

	private static class TestCountAction implements Action<TestStates, TestEvents> {

		int count = 0;

		public TestCountAction() {
			count = 0;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			count++;
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
					.state(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3)
					.state(TestStates.S4);
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
					.event(TestEvents.E2)
					.action(testAction2())
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.action(testAction3());
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
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

}
