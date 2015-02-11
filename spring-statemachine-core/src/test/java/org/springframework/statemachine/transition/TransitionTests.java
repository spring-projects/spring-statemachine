/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.transition;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for state machine transitions.
 * 
 * @author Janne Valkealahti
 *
 */
public class TransitionTests extends AbstractStateMachineTests {

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTriggerlessTransition() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BaseConfig.class, Config1.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		EnumStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(machine.getState().getIds(), contains(TestStates.S3));
		ctx.close();

	}
	
	@SuppressWarnings({ "unchecked" })
	@Test
	public void testInternalTransition() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BaseConfig.class, Config2.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		EnumStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		
		TestExitAction testExitAction = ctx.getBean("testExitAction", TestExitAction.class);
		TestEntryAction testEntryAction = ctx.getBean("testEntryAction", TestEntryAction.class);
		TestAction externalTestAction = ctx.getBean("externalTestAction", TestAction.class);
		TestAction internalTestAction = ctx.getBean("internalTestAction", TestAction.class);
		
		assertThat(machine.getState().getIds(), contains(TestStates.S1));		
		assertThat(testExitAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(testEntryAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));		
		
		machine.sendEvent(TestEvents.E1);
		assertThat(testExitAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(testEntryAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));		
		assertThat(internalTestAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		
		machine.sendEvent(TestEvents.E2);
		assertThat(testExitAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(testEntryAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));		
		assertThat(externalTestAction.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		
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
					.states(EnumSet.allOf(TestStates.class));
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
					.target(TestStates.S3);
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			@SuppressWarnings("unchecked")
			Collection<Action<TestStates, TestEvents>> entryActions = Arrays.asList(testEntryAction());
			@SuppressWarnings("unchecked")
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
				.withInternal()
					.source(TestStates.S1)
					.event(TestEvents.E1)
					.action(internalTestAction())
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E2)
					.action(externalTestAction());
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
		public Action<TestStates, TestEvents> externalTestAction() {
			return new TestAction();
		}

		@Bean
		public Action<TestStates, TestEvents> internalTestAction() {
			return new TestAction();
		}
		
	}
	
}
