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
package org.springframework.statemachine;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class StateMachineTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testLoggingEvents() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "jee1").build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).setHeader("foo", "jee2").build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E4).setHeader("foo", "jee2").build());
	}

	@Test
	public void testTimerTransition() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();

		TestAction testAction1 = context.getBean("testAction1", TestAction.class);
		TestAction testAction2 = context.getBean("testAction2", TestAction.class);
		TestAction testAction3 = context.getBean("testAction3", TestAction.class);
		TestAction testAction4 = context.getBean("testAction4", TestAction.class);

		@SuppressWarnings("unchecked")
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		listener.reset(1);
		machine.start();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(testAction2.stateContexts.size(), is(0));


		listener.reset(0, 1);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction1.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction1.stateContexts.size(), is(1));
		assertThat(testAction2.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction2.stateContexts.size(), is(1));

		listener.reset(0, 1);
		machine.sendEvent(TestEvents.E2);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction3.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction3.stateContexts.size(), is(1));

		// timer still fires but should not cause transition anymore
		// after we sleep and do next event
		int timedTriggered = testAction2.stateContexts.size();
		Thread.sleep(2000);
		assertThat(testAction2.stateContexts.size(), is(timedTriggered));

		listener.reset(0, 1);
		machine.sendEvent(TestEvents.E3);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction4.onExecuteLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction4.stateContexts.size(), is(1));
		assertThat(testAction2.stateContexts.size(), is(timedTriggered));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testForkJoin() throws Exception {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		assertThat(machine, notNullValue());

		listener.reset(1);
		machine.start();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(TestStates.SI));

		listener.reset(3);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S30));

		listener.reset(1);
		machine.sendEvent(TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S30));

		listener.reset(2);
		machine.sendEvent(TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S4));
	}

	@Test
	public void testStringStatesAndEvents() throws Exception {
		context.register(Config4.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		TestListener2 listener = new TestListener2();
		machine.addStateListener(listener);


		assertThat(machine, notNullValue());
		machine.start();
		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("foo", "jee1").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder("S1"));
	}

	@Test
	public void testBackToItself() {
		context.register(BaseConfig.class, Config5.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestStateEntryExitListener listener = new TestStateEntryExitListener();
		machine.addStateListener(listener);
		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.SI));
		listener.reset();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(machine.getState().getIds(), contains(TestStates.SI));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
	}

	private static class LoggingAction implements Action<TestStates, TestEvents> {

		private static final Log log = LogFactory.getLog(StateMachineTests.LoggingAction.class);

		private String message;

		public LoggingAction(String message) {
			this.message = message;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			log.info("Hello from LoggingAction " + message + " foo=" + context.getMessageHeaders().get("foo"));
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
					.state(TestStates.S3, TestEvents.E4)
					.state(TestStates.S4);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.action(loggingAction())
					.action(loggingAction())
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.action(loggingAction())
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.action(loggingAction())
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.S3)
					.event(TestEvents.E4)
					.action(loggingAction());
		}

		@Bean
		public LoggingAction loggingAction() {
			return new LoggingAction("as bean");
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
				.withInternal()
					.source(TestStates.S2)
					.timer(1000)
					.action(testAction2())
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.action(testAction3())
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.action(testAction4());
		}

		@Bean
		public TestAction testAction1() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction2() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction3() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction4() {
			return new TestAction();
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.SI)
					.fork(TestStates.S1)
					.state(TestStates.S2)
					.end(TestStates.SF)
					.join(TestStates.S3)
					.state(TestStates.S4)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30)
						.state(TestStates.S31);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S30)
					.target(TestStates.S31)
					.event(TestEvents.E3)
					.and()
				.withFork()
					.source(TestStates.S1)
					.target(TestStates.S20)
					.target(TestStates.S30)
					.and()
				.withJoin()
					.source(TestStates.S21)
					.source(TestStates.S31)
					.target(TestStates.S3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1");
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
					.states(EnumSet.allOf(TestStates.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.SI)
					.event(TestEvents.E1);
		}
	}

	private static class TestListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<TestStates, TestEvents> transition) {
			transitionLatch.countDown();
		}

		public void reset(int c1) {
			reset(c1, 0);
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			transitionLatch = new CountDownLatch(c2);
			stateChangedCount = 0;
		}

	}

	private static class TestListener2 extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<String, String> transition) {
			transitionLatch.countDown();
		}

		public void reset(int c1) {
			reset(c1, 0);
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			transitionLatch = new CountDownLatch(c2);
			stateChangedCount = 0;
		}

	}

	private static class TestStateEntryExitListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		List<State<TestStates, TestEvents>> entered = new ArrayList<>();
		List<State<TestStates, TestEvents>> exited = new ArrayList<>();

		@Override
		public void stateEntered(State<TestStates, TestEvents> state) {
			entered.add(state);
		}

		@Override
		public void stateExited(State<TestStates, TestEvents> state) {
			exited.add(state);
		}

		public void reset() {
			entered.clear();
			exited.clear();
		}
	}
}
