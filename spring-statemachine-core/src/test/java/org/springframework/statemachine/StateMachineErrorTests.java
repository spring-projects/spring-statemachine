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
package org.springframework.statemachine;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.OnStateMachineError;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;

/**
 * Tests for various errors and error handling.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineErrorTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testEvents() throws Exception {
		context.register(EventListenerConfig1.class, Config1.class);
		context.refresh();

		TestApplicationEventListener1 listener1 = context.getBean(TestApplicationEventListener1.class);
		TestApplicationEventListener2 listener3 = context.getBean(TestApplicationEventListener2.class);

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		assertThat(machine.hasStateMachineError(), is(false));

		TestStateMachineListener listener2 = new TestStateMachineListener();
		machine.addStateListener(listener2);

		machine.start();
		machine.setStateMachineError(new RuntimeException("myerror"));

		assertThat(listener1.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener1.count, is(1));
		assertThat(listener3.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener3.count, is(1));
		assertThat(listener2.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener2.count, is(1));
		assertThat(machine.hasStateMachineError(), is(true));
	}

	@Test
	public void testInterceptHandlesError() throws Exception {
		context.register(EventListenerConfig1.class, Config1.class);
		context.refresh();

		TestApplicationEventListener1 listener1 = context.getBean(TestApplicationEventListener1.class);

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		assertThat(machine.hasStateMachineError(), is(false));

		machine.getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<TestStates,TestEvents>>() {

			@Override
			public void apply(StateMachineAccess<TestStates, TestEvents> function) {
				function.addStateMachineInterceptor(new StateMachineInterceptorAdapter<TestStates,TestEvents>() {
					@Override
					public Exception stateMachineError(StateMachine<TestStates, TestEvents> stateMachine,
							Exception exception) {
						return null;
					}
				});
			}
		});

		TestStateMachineListener listener2 = new TestStateMachineListener();
		machine.addStateListener(listener2);

		machine.start();
		machine.setStateMachineError(new RuntimeException("myerror"));

		assertThat(listener1.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(listener1.count, is(0));
		assertThat(listener2.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(listener2.count, is(0));
		assertThat(machine.hasStateMachineError(), is(false));
	}

	@Test
	public void testErrorActive() throws Exception {
		context.register(Config1.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		assertThat(machine.hasStateMachineError(), is(false));
		machine.start();
		machine.setStateMachineError(new RuntimeException("myerror"));
		assertThat(machine.hasStateMachineError(), is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));
	}

	@Test
	public void testListenerErrorsCauseNoMalfunction() throws Exception {
		context.register(EventListenerConfig2.class, Config1.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		StartedStateMachineListener listener1 = new StartedStateMachineListener();
		ErroringStateMachineListener listener2 = new ErroringStateMachineListener();
		StateChangedStateMachineListener listener3 = new StateChangedStateMachineListener();
		machine.addStateListener(listener1);
		machine.addStateListener(listener2);

		machine.start();
		assertThat(listener1.latch.await(2, TimeUnit.SECONDS), is(true));
		machine.addStateListener(listener3);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener3.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2));
	}

	@Test
	public void testListenerErrorsCauseNoMalfunction2() throws Exception {
		context.register(EventListenerConfig2.class, Config1.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		StartedStateMachineListener listener1 = new StartedStateMachineListener();
		ErroringStateMachineListener2 listener2 = new ErroringStateMachineListener2();
		StateChangedStateMachineListener listener3 = new StateChangedStateMachineListener();
		machine.addStateListener(listener1);
		machine.addStateListener(listener2);

		machine.start();
		assertThat(listener1.latch.await(2, TimeUnit.SECONDS), is(true));
		machine.addStateListener(listener3);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener3.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
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
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.S3)
					.event(TestEvents.E4);
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

	}

	@Configuration
	static class EventListenerConfig1 {

		@Bean
		public TestApplicationEventListener1 testApplicationEventListener1() {
			return new TestApplicationEventListener1();
		}

		@Bean
		public TestApplicationEventListener2 testApplicationEventListener2() {
			return new TestApplicationEventListener2();
		}
	}

	@Configuration
	static class EventListenerConfig2 {

		@Bean
		public ErroringApplicationEventListener1 erroringApplicationEventListener1() {
			return new ErroringApplicationEventListener1();
		}
	}

	static class TestStateMachineListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		CountDownLatch latch = new CountDownLatch(1);
		int count = 0;

		@Override
		public void stateMachineError(StateMachine<TestStates, TestEvents> stateMachine, Exception exception) {
			count++;
			latch.countDown();
		}
	}

	static class TestApplicationEventListener1 implements ApplicationListener<StateMachineEvent> {

		CountDownLatch latch = new CountDownLatch(1);
		int count = 0;

		@Override
		public void onApplicationEvent(StateMachineEvent event) {
			if (event instanceof OnStateMachineError) {
				count++;
				latch.countDown();
			}
		}
	}

	static class TestApplicationEventListener2 implements ApplicationListener<OnStateMachineError> {

		CountDownLatch latch = new CountDownLatch(1);
		int count = 0;

		@Override
		public void onApplicationEvent(OnStateMachineError event) {
			count++;
			latch.countDown();
		}

	}

	static class ErroringApplicationEventListener1 implements ApplicationListener<StateMachineEvent> {

		@Override
		public void onApplicationEvent(StateMachineEvent event) {
			throw new RuntimeException();
		}
	}


	static class StateChangedStateMachineListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			latch.countDown();
		}

		void reset(int a) {
			latch = new CountDownLatch(a);
		}
	}

	static class StartedStateMachineListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void stateMachineStarted(StateMachine<TestStates, TestEvents> stateMachine) {
			latch.countDown();
		}
	}

	static class ErroringStateMachineListener implements StateMachineListener<TestStates, TestEvents> {

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			throw new RuntimeException();
		}

		@Override
		public void stateEntered(State<TestStates, TestEvents> state) {
			throw new RuntimeException();
		}

		@Override
		public void stateExited(State<TestStates, TestEvents> state) {
			throw new RuntimeException();
		}

		@Override
		public void eventNotAccepted(Message<TestEvents> event) {
			throw new RuntimeException();
		}

		@Override
		public void transition(Transition<TestStates, TestEvents> transition) {
			throw new RuntimeException();
		}

		@Override
		public void transitionStarted(Transition<TestStates, TestEvents> transition) {
			throw new RuntimeException();
		}

		@Override
		public void transitionEnded(Transition<TestStates, TestEvents> transition) {
			throw new RuntimeException();
		}

		@Override
		public void stateMachineStarted(StateMachine<TestStates, TestEvents> stateMachine) {
			throw new RuntimeException();
		}

		@Override
		public void stateMachineStopped(StateMachine<TestStates, TestEvents> stateMachine) {
			throw new RuntimeException();
		}

		@Override
		public void stateMachineError(StateMachine<TestStates, TestEvents> stateMachine, Exception exception) {
			throw new RuntimeException();
		}

		@Override
		public void extendedStateChanged(Object key, Object value) {
			throw new RuntimeException();
		}

		@Override
		public void stateContext(StateContext<TestStates, TestEvents> stateContext) {
			throw new RuntimeException();
		}
	}

	static class ErroringStateMachineListener2 implements StateMachineListener<TestStates, TestEvents> {

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			throw new Error();
		}

		@Override
		public void stateEntered(State<TestStates, TestEvents> state) {
			throw new Error();
		}

		@Override
		public void stateExited(State<TestStates, TestEvents> state) {
			throw new Error();
		}

		@Override
		public void eventNotAccepted(Message<TestEvents> event) {
			throw new Error();
		}

		@Override
		public void transition(Transition<TestStates, TestEvents> transition) {
			throw new Error();
		}

		@Override
		public void transitionStarted(Transition<TestStates, TestEvents> transition) {
			throw new Error();
		}

		@Override
		public void transitionEnded(Transition<TestStates, TestEvents> transition) {
			throw new Error();
		}

		@Override
		public void stateMachineStarted(StateMachine<TestStates, TestEvents> stateMachine) {
			throw new Error();
		}

		@Override
		public void stateMachineStopped(StateMachine<TestStates, TestEvents> stateMachine) {
			throw new Error();
		}

		@Override
		public void stateMachineError(StateMachine<TestStates, TestEvents> stateMachine, Exception exception) {
			throw new Error();
		}

		@Override
		public void extendedStateChanged(Object key, Object value) {
			throw new Error();
		}

		@Override
		public void stateContext(StateContext<TestStates, TestEvents> stateContext) {
			throw new Error();
		}
	}

}
