/*
 * Copyright 2015-2019 the original author or authors.
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
package org.springframework.statemachine.state;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for state entry and exit actions.
 *
 * @author Janne Valkealahti
 *
 */
public class StateActionTests extends AbstractStateMachineTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testStateEntryExit() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config1.class);
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestExitAction testExitAction = ctx.getBean("testExitAction", TestExitAction.class);
		TestEntryAction testEntryAction = ctx.getBean("testEntryAction", TestEntryAction.class);
		assertThat(testExitAction, notNullValue());
		assertThat(testEntryAction, notNullValue());

		machine.start();
		machine.sendEvent(TestEvents.E1);
		assertThat(testExitAction.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testEntryAction.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));

		ctx.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testEndStateEntryAction() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config2.class);
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestEntryAction testEntryAction = ctx.getBean("testEntryAction", TestEntryAction.class);
		machine.start();

		assertThat(machine, notNullValue());
		assertThat(machine.isComplete(), is(false));
		assertThat(machine.getState().getIds(), contains(TestStates.SI));

		machine.sendEvent(TestEvents.E1);
		assertThat(machine.isComplete(), is(true));
		assertThat(machine.getState().getIds(), contains(TestStates.SF));
		assertThat(testEntryAction.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));

		ctx.close();
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			Collection<Action<TestStates, TestEvents>> entryActions = Arrays.asList(testEntryAction());
			Collection<Action<TestStates, TestEvents>> exitActions = Arrays.asList(testExitAction());
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1, null, exitActions)
					.state(TestStates.S2, entryActions, null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1);
		}

		@Bean
		public Action<TestStates, TestEvents> testEntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public Action<TestStates, TestEvents> testExitAction() {
			return new TestExitAction();
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.SI)
					.state(TestStates.SF, testEntryAction(), null)
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.SF)
					.event(TestEvents.E1);
		}

		@Bean
		public Action<TestStates, TestEvents> testEntryAction() {
			return new TestEntryAction();
		}
	}
}
