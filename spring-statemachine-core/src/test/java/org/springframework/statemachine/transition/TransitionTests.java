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
package org.springframework.statemachine.transition;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

/**
 * Tests for state machine transitions.
 *
 * @author Janne Valkealahti
 *
 */
public class TransitionTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTriggerlessTransition() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();

		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));

		listener.reset(2);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertThat(machine.getState().getIds(), contains(TestStates.S3));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTriggerlessTransitionFromInitial() throws Exception {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S2));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTriggerlessTransitionFromInitialToEnd() throws Exception {
		context.register(BaseConfig.class, Config4.class);
		context.refresh();

		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		// end state terminates sm so state is null
		assertThat(machine.getState(), nullValue());
		assertThat(machine.isComplete(), is(true));
		assertThat(machine.isRunning(), is(false));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTriggerlessTransitionInRegionsDefinedInSubStates() throws Exception {
		context.register(BaseConfig.class, Config5.class);
		context.refresh();

		TestAction testAction1 = context.getBean("testAction1", TestAction.class);
		TestAction testAction20 = context.getBean("testAction20", TestAction.class);
		TestAction testAction21 = context.getBean("testAction21", TestAction.class);

		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(testAction1.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction1.stateContexts.size(), is(1));
		assertThat(testAction20.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction20.stateContexts.size(), is(1));
		assertThat(testAction21.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction21.stateContexts.size(), is(1));

		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S201, TestStates.S211));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTriggerlessTransitionInRegions() throws Exception {
		context.register(BaseConfig.class, Config6.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S201, TestStates.S211));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testInternalTransition() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		TestExitAction testExitAction = context.getBean("testExitAction", TestExitAction.class);
		TestEntryAction testEntryAction = context.getBean("testEntryAction", TestEntryAction.class);
		TestAction externalTestAction = context.getBean("externalTestAction", TestAction.class);
		TestAction internalTestAction = context.getBean("internalTestAction", TestAction.class);

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
	}

	@Test
	public void testTransitDirectlyToSubstateSkipInitial() throws InterruptedException {
		context.register(BaseConfig.class, Config7.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates2,TestEvents2> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener2 listener = new TestListener2();
		machine.addStateListener(listener);
		listener.reset(2);

		machine.start();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertThat(machine.getState().getIds(), contains(TestStates2.IDLE, TestStates2.CLOSED));

		listener.reset(0, 2);
		machine.sendEvent(TestEvents2.PAUSE);
		assertThat(listener.stateEnteredLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateEnteredCount, is(3));
		assertThat(machine.getState().getIds(), contains(TestStates2.BUSY, TestStates2.PAUSED));
	}

	@Test
	public void testTransitDeepDirectlyToSubstateSkipInitial() throws InterruptedException {
		context.register(BaseConfig.class, Config8.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates2,TestEvents2> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener2 listener = new TestListener2();
		machine.addStateListener(listener);
		listener.reset(2);

		machine.start();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertThat(machine.getState().getIds(), contains(TestStates2.IDLE, TestStates2.CLOSED));

		listener.reset(0, 3);
		machine.sendEvent(TestEvents2.PAUSE);
		assertThat(listener.stateEnteredLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateEnteredCount, is(3));
		assertThat(machine.getState().getIds(), contains(TestStates2.BUSY, TestStates2.PAUSED, TestStates2.PAUSED2));
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

	@Configuration
	@EnableStateMachine
	public static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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
					.target(TestStates.S2);
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.SF)
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SF);
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config5 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S201)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S21)
						.state(TestStates.S211);
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
					.state(TestStates.S2)
					.source(TestStates.S20)
					.target(TestStates.S201)
					.action(testAction20())
					.and()
				.withExternal()
					.state(TestStates.S2)
					.source(TestStates.S21)
					.target(TestStates.S211)
					.action(testAction21());
		}

		@Bean
		public TestAction testAction1() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction20() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction21() {
			return new TestAction();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config6 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S201)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S21)
						.state(TestStates.S211);
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
					.source(TestStates.S20)
					.target(TestStates.S201)
					.and()
				.withExternal()
					.source(TestStates.S21)
					.target(TestStates.S211);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config7 extends EnumStateMachineConfigurerAdapter<TestStates2, TestEvents2> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates2, TestEvents2> states) throws Exception {
			states
				.withStates()
					.initial(TestStates2.IDLE)
					.state(TestStates2.IDLE)
					.and()
					.withStates()
						.parent(TestStates2.IDLE)
						.initial(TestStates2.CLOSED)
						.state(TestStates2.CLOSED)
						.state(TestStates2.OPEN)
						.and()
				.withStates()
					.state(TestStates2.BUSY)
					.and()
					.withStates()
						.parent(TestStates2.BUSY)
						.initial(TestStates2.PLAYING)
						.state(TestStates2.PLAYING)
						.state(TestStates2.PAUSED);

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates2, TestEvents2> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates2.CLOSED)
					.target(TestStates2.OPEN)
					.event(TestEvents2.EJECT)
					.and()
				.withExternal()
					.source(TestStates2.OPEN)
					.target(TestStates2.CLOSED)
					.event(TestEvents2.EJECT)
					.and()
				.withExternal()
					.source(TestStates2.CLOSED)
					.target(TestStates2.PAUSED)
					.event(TestEvents2.PAUSE);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config8 extends EnumStateMachineConfigurerAdapter<TestStates2, TestEvents2> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates2, TestEvents2> states) throws Exception {
			states
				.withStates()
					.initial(TestStates2.IDLE)
					.state(TestStates2.IDLE)
					.and()
					.withStates()
						.parent(TestStates2.IDLE)
						.initial(TestStates2.CLOSED)
						.state(TestStates2.OPEN)
						.and()
				.withStates()
					.state(TestStates2.BUSY)
					.and()
					.withStates()
						.parent(TestStates2.BUSY)
						.initial(TestStates2.PLAYING)
						.state(TestStates2.PAUSED)
						.and()
						.withStates()
							.parent(TestStates2.PAUSED)
							.initial(TestStates2.PAUSED1)
							.state(TestStates2.PAUSED2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates2, TestEvents2> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates2.CLOSED)
					.target(TestStates2.OPEN)
					.event(TestEvents2.EJECT)
					.and()
				.withExternal()
					.source(TestStates2.OPEN)
					.target(TestStates2.CLOSED)
					.event(TestEvents2.EJECT)
					.and()
				.withExternal()
					.source(TestStates2.CLOSED)
					.target(TestStates2.PAUSED2)
					.event(TestEvents2.PAUSE);
		}

	}

	static class TestListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		public void reset(int c1) {
			stateChangedLatch = new CountDownLatch(c1);
			stateChangedCount = 0;
		}

	}

	static class TestListener2 extends StateMachineListenerAdapter<TestStates2, TestEvents2> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(1);
		volatile int stateEnteredCount = 0;

		@Override
		public void stateChanged(State<TestStates2, TestEvents2> from, State<TestStates2, TestEvents2> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void stateEntered(State<TestStates2, TestEvents2> state) {
			stateEnteredCount++;
			stateEnteredLatch.countDown();
		}

		public void reset(int c1) {
			reset(c1, 0);
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			stateChangedCount = 0;
			stateEnteredLatch = new CountDownLatch(c2);
			stateEnteredCount = 0;
		}

	}

}
