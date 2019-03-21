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
package org.springframework.statemachine.guard;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests.TestAction;
import org.springframework.statemachine.AbstractStateMachineTests.TestEvents;
import org.springframework.statemachine.AbstractStateMachineTests.TestGuard;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for state machine guards.
 *
 * @author Janne Valkealahti
 *
 */
public class GuardTests {

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testGuardEvaluated() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config1.class);
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestGuard testGuard = ctx.getBean("testGuard", TestGuard.class);
		TestAction testAction = ctx.getBean("testAction", TestAction.class);
		assertThat(testGuard, notNullValue());
		assertThat(testAction, notNullValue());

		machine.start();
		machine.sendEvent(TestEvents.E1);
		assertThat(testGuard.onEvaluateLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));

		ctx.close();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testGuardDenyAction() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config2.class);
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestGuard testGuard = ctx.getBean("testGuard", TestGuard.class);
		TestAction testAction = ctx.getBean("testAction", TestAction.class);
		assertThat(testGuard, notNullValue());
		assertThat(testAction, notNullValue());

		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));

		machine.sendEvent(TestEvents.E1);
		assertThat(testGuard.onEvaluateLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction.onExecuteLatch.await(2, TimeUnit.SECONDS), is(false));
		assertThat(machine.getState().getIds(), contains(TestStates.S1));

		ctx.close();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testGuardThrows() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config3.class);
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E2);
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E3);
		assertThat(machine.getState().getIds(), contains(TestStates.S2));
		ctx.close();
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.action(testAction())
					.guard(testGuard());
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

		@Bean
		public TestGuard testGuard() {
			return new TestGuard(true);
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.action(testAction())
					.guard(testGuard());
		}

		@Bean
		public TestGuard testGuard() {
			return new TestGuard(false);
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.guard(testGuard1())
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E2)
					.guard(testGuard2())
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E3);
		}

		@Bean
		public Guard<TestStates, TestEvents> testGuard1() {
			return new Guard<TestStates, TestEvents>() {

				@Override
				public boolean evaluate(StateContext<TestStates, TestEvents> context) {
					throw new RuntimeException();
				}
			};
		}

		@Bean
		public Guard<TestStates, TestEvents> testGuard2() {
			return new Guard<TestStates, TestEvents>() {

				@Override
				public boolean evaluate(StateContext<TestStates, TestEvents> context) {
					throw new Error();
				}
			};
		}
	}
}
