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
package org.springframework.statemachine.listener;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Tests for state machine listener functionality.
 *
 * @author Janne Valkealahti
 *
 */
public class ListenerTests extends AbstractStateMachineTests {

	@Test
	public void testStateEvents() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config1.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();

		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		assertThat(machine, notNullValue());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "jee1").build());
		assertThat(listener.states.size(), is(1));
		assertThat(listener.states.get(0).from.getIds(), contains(TestStates.S1));
		assertThat(listener.states.get(0).to.getIds(), contains(TestStates.S2));
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).setHeader("foo", "jee2").build());
		assertThat(listener.states.size(), is(2));
		assertThat(listener.states.get(1).from.getIds(), contains(TestStates.S2));
		assertThat(listener.states.get(1).to.getIds(), contains(TestStates.S3));
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E4).setHeader("foo", "jee2").build());
		assertThat(listener.states.size(), is(2));

		ctx.close();
	}

	@Test
	public void testStartEndEvents() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config2.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		assertThat(listener.stopLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.started, is(1));
		assertThat(listener.stopped, is(1));
		ctx.close();
	}

	@Test
	public void testExtendedStateEvents() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config2.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);
		machine.start();

		machine.getExtendedState().getVariables().put("foo", "jee");
		assertThat(listener.extendedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.extended.size(), is(1));
		assertThat(listener.extended.get(0).key, is("foo"));
		assertThat(listener.extended.get(0).value, is("jee"));
		ctx.close();
	}

	private static class LoggingAction implements Action<TestStates, TestEvents> {

		private static final Log log = LogFactory.getLog(LoggingAction.class);

		private String message;

		public LoggingAction(String message) {
			this.message = message;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			log.info("Hello from LoggingAction " + message + " foo=" + context.getMessageHeaders().get("foo"));
		}

	}

	private static class TestStateMachineListener implements StateMachineListener<TestStates, TestEvents> {

		ArrayList<Holder> states = new ArrayList<Holder>();
		volatile int started = 0;
		volatile int stopped = 0;
		CountDownLatch stopLatch = new CountDownLatch(1);
		ArrayList<Holder2> extended = new ArrayList<Holder2>();
		CountDownLatch extendedLatch = new CountDownLatch(1);


		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			states.add(new Holder(from, to));
		}

		@Override
		public void stateEntered(State<TestStates, TestEvents> state) {
		}

		@Override
		public void stateExited(State<TestStates, TestEvents> state) {
		}

		static class Holder {
			State<TestStates, TestEvents> from;
			State<TestStates, TestEvents> to;
			public Holder(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
				this.from = from;
				this.to = to;
			}
		}

		static class Holder2 {
			Object key;
			Object value;
			public Holder2(Object key, Object value) {
				this.key = key;
				this.value = value;
			}

		}

		@Override
		public void eventNotAccepted(Message<TestEvents> event) {
		}

		@Override
		public void transition(Transition<TestStates, TestEvents> transition) {
		}

		@Override
		public void transitionStarted(Transition<TestStates, TestEvents> transition) {
		}

		@Override
		public void transitionEnded(Transition<TestStates, TestEvents> transition) {
		}

		@Override
		public void stateMachineStarted(StateMachine<TestStates, TestEvents> stateMachine) {
			started++;
		}

		@Override
		public void stateMachineStopped(StateMachine<TestStates, TestEvents> stateMachine) {
			stopped++;
			stopLatch.countDown();
		}

		@Override
		public void stateMachineError(StateMachine<TestStates, TestEvents> stateMachine, Exception exception) {
		}

		@Override
		public void extendedStateChanged(Object key, Object value) {
			extended.add(new Holder2(key, value));
			extendedLatch.countDown();
		}

		@Override
		public void stateContext(StateContext<TestStates, TestEvents> stateContext) {
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
					.end(TestStates.S3)
					.state(TestStates.S1)
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
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

}
