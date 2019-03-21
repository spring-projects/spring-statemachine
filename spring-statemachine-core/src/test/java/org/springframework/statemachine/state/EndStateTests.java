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
package org.springframework.statemachine.state;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class EndStateTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testEndStateCompletes() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine, notNullValue());
		assertThat(machine.isComplete(), is(false));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.isComplete(), is(false));
		machine.sendEvent(TestEvents.E2);
		assertThat(machine.isComplete(), is(false));
		machine.sendEvent(TestEvents.E3);
		assertThat(machine.isComplete(), is(false));
		machine.sendEvent(TestEvents.E4);
		assertThat(machine.isComplete(), is(false));
		machine.sendEvent(TestEvents.EF);
		assertThat(machine.isComplete(), is(true));
		assertThat(machine.getState().getIds(), contains(TestStates.SF));
	}

	@Test
	public void testEndStatesWithRegions() throws InterruptedException {
		context.register(Config2.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates3,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates3.READY));
	}

	@Test
	public void testEndStatesWithRegionsDefinedInStates() throws InterruptedException {
		context.register(Config3.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates3,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates3.READY));
	}

	@Test
	public void testEndStateCompletesSubmachine() {
		context.register(Config4.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine, notNullValue());
		assertThat(machine.isComplete(), is(false));
		assertThat(machine.getState().getIds(), contains(TestStates.SI));

		machine.sendEvent(TestEvents.E1);
		assertThat(machine.isComplete(), is(false));
		assertThat(machine.getState().getIds(), contains(TestStates.S1, TestStates.S11));

		machine.sendEvent(TestEvents.E2);
		assertThat(machine.isComplete(), is(false));
		assertThat(machine.getState().getIds(), contains(TestStates.S1, TestStates.S12));

		machine.sendEvent(TestEvents.E3);
		assertThat(machine.isComplete(), is(false));
		assertThat(machine.getState().getIds(), contains(TestStates.S1, TestStates.SF));
	}

	@Test
	public void testMultipleTransitionsToSameEndState() {
		context.register(Config5.class);
		context.refresh();
	}

	@Test
	public void testMultipleTransitionsToSameEndStateFromChoices() {
		context.register(Config6.class);
		context.refresh();
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.states(EnumSet.allOf(TestStates.class))
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E4)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.SF)
					.event(TestEvents.EF);
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates3, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates3, TestEvents> states)
				throws Exception {
			states
				.withStates()
					.initial(TestStates3.READY)
					.fork(TestStates3.FORK)
					.state(TestStates3.TASKS)
					.join(TestStates3.JOIN)
					.and()
					.withStates()
						.parent(TestStates3.TASKS)
						.initial(TestStates3.T1)
						.end(TestStates3.T1E)
						.and()
					.withStates()
						.parent(TestStates3.TASKS)
						.initial(TestStates3.T2)
						.end(TestStates3.T2E)
						.and()
					.withStates()
						.parent(TestStates3.TASKS)
						.initial(TestStates3.T3)
						.end(TestStates3.T3E);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates3, TestEvents> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(TestStates3.READY).target(TestStates3.FORK)
					.event(TestEvents.E1)
					.and()
				.withFork()
					.source(TestStates3.FORK)
					.target(TestStates3.T1)
					.target(TestStates3.T2)
					.target(TestStates3.T3)
					.and()
				.withExternal()
					.source(TestStates3.T1).target(TestStates3.T1E)
					.and()
				.withExternal()
					.source(TestStates3.T2).target(TestStates3.T2E)
					.and()
				.withExternal()
					.source(TestStates3.T3).target(TestStates3.T3E)
					.and()
				.withJoin()
					.source(TestStates3.T1E)
					.source(TestStates3.T2E)
					.source(TestStates3.T3E)
					.target(TestStates3.JOIN)
					.and()
				.withExternal()
					.source(TestStates3.JOIN).target(TestStates3.READY);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates3, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates3, TestEvents> states)
				throws Exception {
			states
				.withStates()
					.initial(TestStates3.READY)
					.fork(TestStates3.FORK)
					.state(TestStates3.TASKS)
					.join(TestStates3.JOIN)
					.and()
					.withStates()
						.parent(TestStates3.TASKS)
						.initial(TestStates3.T1)
						.state(TestStates3.T1E)
						.end(TestStates3.T1E)
						.and()
					.withStates()
						.parent(TestStates3.TASKS)
						.initial(TestStates3.T2)
						.state(TestStates3.T2E)
						.end(TestStates3.T2E)
						.and()
					.withStates()
						.parent(TestStates3.TASKS)
						.initial(TestStates3.T3)
						.state(TestStates3.T3E)
						.end(TestStates3.T3E);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates3, TestEvents> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(TestStates3.READY).target(TestStates3.FORK)
					.event(TestEvents.E1)
					.and()
				.withFork()
					.source(TestStates3.FORK)
					.target(TestStates3.T1)
					.target(TestStates3.T2)
					.target(TestStates3.T3)
					.and()
				.withExternal()
					.state(TestStates3.TASKS)
					.source(TestStates3.T1).target(TestStates3.T1E)
					.and()
				.withExternal()
					.state(TestStates3.TASKS)
					.source(TestStates3.T2).target(TestStates3.T2E)
					.and()
				.withExternal()
					.state(TestStates3.TASKS)
					.source(TestStates3.T3).target(TestStates3.T3E)
					.and()
				.withJoin()
					.source(TestStates3.T1E)
					.source(TestStates3.T2E)
					.source(TestStates3.T3E)
					.target(TestStates3.JOIN)
					.and()
				.withExternal()
					.source(TestStates3.JOIN).target(TestStates3.READY);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.S1)
					.and()
					.withStates()
						.parent(TestStates.S1)
						.initial(TestStates.S11)
						.state(TestStates.S12)
						.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S11)
					.target(TestStates.S12)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S12)
					.target(TestStates.SF)
					.event(TestEvents.E3);
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config5 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S2)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SF)
					.event(TestEvents.E3)
					.and()
					.withExternal()
					.source(TestStates.S2)
					.target(TestStates.SF)
					.event(TestEvents.E3);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config6 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.choice("JOIN1")
					.choice("JOIN2")
					.end("SF");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("JOIN1")
					.event("E1")
					.and()
				.withExternal()
					.source("SI")
					.target("JOIN2")
					.event("E2")
					.and()
				.withChoice()
					.source("JOIN1")
					.last("SF")
					.and()
				.withChoice()
					.source("JOIN2")
					.last("SF");
		}
	}
}
