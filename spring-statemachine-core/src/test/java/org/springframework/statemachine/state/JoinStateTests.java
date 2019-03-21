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
package org.springframework.statemachine.state;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;

public class JoinStateTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoin() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		listener.reset(1);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		listener.reset(1);
		machine.sendEvent(TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(machine.getState().getIds(), contains(TestStates.S4));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoinLoopTwice() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);

		assertThat(machine.getState().getIds(), contains(TestStates.S4));

		listener.reset(1);
		machine.sendEvent(TestEvents.E4);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(TestStates.SI));

		listener.reset(3);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		listener.reset(1);
		machine.sendEvent(TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(machine.getState().getIds(), contains(TestStates.S4));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoinSuper() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		listener.reset(1);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		listener.reset(1);
		machine.sendEvent(TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(machine.getState().getIds(), contains(TestStates.S4));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoinSuperLoopTwice() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);

		assertThat(machine.getState().getIds(), contains(TestStates.S4));

		listener.reset(1);
		machine.sendEvent(TestEvents.E4);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(TestStates.SI));

		listener.reset(3);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		listener.reset(1);
		machine.sendEvent(TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(machine.getState().getIds(), contains(TestStates.S4));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.S2)
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
				.withJoin()
					.source(TestStates.S21)
					.source(TestStates.S31)
					.target(TestStates.S3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.SI)
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
					.initial(TestStates.SI)
					.state(TestStates.S2)
					.join(TestStates.S3)
					.state(TestStates.S4)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.end(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.end(TestStates.S31);
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
				.withJoin()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.SI)
					.event(TestEvents.E4);
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;
		final List<Transition<TestStates, TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<TestStates, TestEvents> transition) {
			transitions.add(transition);
			transitionLatch.countDown();
		}

		public void reset(int c1) {
			reset(c1, 0);
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			transitionLatch = new CountDownLatch(c2);
			stateChangedCount = 0;
			transitions.clear();
		}

	}

}
