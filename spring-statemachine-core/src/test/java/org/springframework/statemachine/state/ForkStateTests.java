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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

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
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

public class ForkStateTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testForkEventPassed() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		TestEntryAction s20EntryAction = context.getBean("s20EntryAction", TestEntryAction.class);
		TestEntryAction s21EntryAction = context.getBean("s21EntryAction", TestEntryAction.class);
		TestEntryAction s30EntryAction = context.getBean("s30EntryAction", TestEntryAction.class);
		TestEntryAction s31EntryAction = context.getBean("s31EntryAction", TestEntryAction.class);
		assertThat(machine, notNullValue());
		machine.start();

		listener.reset(2);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "bar").build());

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));

		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S31));
		assertThat(s20EntryAction.stateContexts.size(), is(0));
		assertThat(s21EntryAction.stateContexts.size(), is(1));
		assertThat(s30EntryAction.stateContexts.size(), is(0));
		assertThat(s31EntryAction.stateContexts.size(), is(1));
		assertThat((String)s21EntryAction.stateContexts.get(0).getMessageHeader("foo"), is("bar"));
		assertThat((String)s31EntryAction.stateContexts.get(0).getMessageHeader("foo"), is("bar"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testForkToSuperEventNotPassed() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		TestEntryAction s20EntryAction = context.getBean("s20EntryAction", TestEntryAction.class);
		TestEntryAction s21EntryAction = context.getBean("s21EntryAction", TestEntryAction.class);
		TestEntryAction s30EntryAction = context.getBean("s30EntryAction", TestEntryAction.class);
		TestEntryAction s31EntryAction = context.getBean("s31EntryAction", TestEntryAction.class);
		assertThat(machine, notNullValue());
		machine.start();

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "bar").build());

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S30));
		assertThat(s20EntryAction.stateContexts.size(), is(1));
		assertThat(s21EntryAction.stateContexts.size(), is(0));
		assertThat(s30EntryAction.stateContexts.size(), is(1));
		assertThat(s31EntryAction.stateContexts.size(), is(0));
		assertThat((String)s20EntryAction.stateContexts.get(0).getMessageHeader("foo"), nullValue());
		assertThat((String)s30EntryAction.stateContexts.get(0).getMessageHeader("foo"), nullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testForkToSuperAndSubEventPassed() throws Exception {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		TestEntryAction s20EntryAction = context.getBean("s20EntryAction", TestEntryAction.class);
		TestEntryAction s21EntryAction = context.getBean("s21EntryAction", TestEntryAction.class);
		TestEntryAction s30EntryAction = context.getBean("s30EntryAction", TestEntryAction.class);
		TestEntryAction s31EntryAction = context.getBean("s31EntryAction", TestEntryAction.class);
		assertThat(machine, notNullValue());
		machine.start();

		listener.reset(2);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "bar").build());

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(2));

		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S31));
		assertThat(s20EntryAction.stateContexts.size(), is(1));
		assertThat(s21EntryAction.stateContexts.size(), is(0));
		assertThat(s30EntryAction.stateContexts.size(), is(0));
		assertThat(s31EntryAction.stateContexts.size(), is(1));
		assertThat((String)s20EntryAction.stateContexts.get(0).getMessageHeader("foo"), nullValue());
		assertThat((String)s31EntryAction.stateContexts.get(0).getMessageHeader("foo"), is("bar"));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.fork(TestStates.S1)
					.state(TestStates.SI)
					.state(TestStates.S2)
					.end(TestStates.SF)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20, s20EntryAction(), null)
						.state(TestStates.S21, s21EntryAction(), null)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30, s30EntryAction(), null)
						.state(TestStates.S31, s31EntryAction(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withFork()
					.source(TestStates.S1)
					.target(TestStates.S21)
					.target(TestStates.S31);
		}

		@Bean
		public TestEntryAction s20EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s21EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s30EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s31EntryAction() {
			return new TestEntryAction();
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
					.fork(TestStates.S1)
					.state(TestStates.SI)
					.state(TestStates.S2)
					.end(TestStates.SF)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20, s20EntryAction(), null)
						.state(TestStates.S21, s21EntryAction(), null)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30, s30EntryAction(), null)
						.state(TestStates.S31, s31EntryAction(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withFork()
					.source(TestStates.S1)
					.target(TestStates.S2);
		}

		@Bean
		public TestEntryAction s20EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s21EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s30EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s31EntryAction() {
			return new TestEntryAction();
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
					.fork(TestStates.S1)
					.state(TestStates.SI)
					.state(TestStates.S2)
					.end(TestStates.SF)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20, s20EntryAction(), null)
						.state(TestStates.S21, s21EntryAction(), null)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30, s30EntryAction(), null)
						.state(TestStates.S31, s31EntryAction(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withFork()
					.source(TestStates.S1)
					.target(TestStates.S31);
		}

		@Bean
		public TestEntryAction s20EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s21EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s30EntryAction() {
			return new TestEntryAction();
		}

		@Bean
		public TestEntryAction s31EntryAction() {
			return new TestEntryAction();
		}

	}

	static class TestListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			stateChangedLatch.countDown();
			stateChangedCount++;
		}

		public void reset(int c1) {
			stateChangedLatch = new CountDownLatch(c1);
			stateChangedCount = 0;
		}

	}

}
