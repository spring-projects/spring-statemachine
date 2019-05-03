/*
 * Copyright 2015-2017 the original author or authors.
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.util.ObjectUtils;

public class ChoiceStateTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFirst() {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("choice", "s30").build());

		assertThat(machine.getState().getIds(), contains(TestStates.S30));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThen1() {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("choice", "s31").build());

		assertThat(machine.getState().getIds(), contains(TestStates.S31));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testThen2() {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("choice", "s32").build());

		assertThat(machine.getState().getIds(), contains(TestStates.S32));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLast() {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(machine.getState().getIds(), contains(TestStates.S33));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOnlyLast() {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(machine.getState().getIds(), contains(TestStates.S33));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSubsequentChoiceStates() {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("choice", "s2").build());

		assertThat(machine.getState().getIds(), contains(TestStates.S21));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBackToItself() {
		context.register(BaseConfig.class, Config4.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
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
		listener.reset();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());
		assertThat(machine.getState().getIds(), contains(TestStates.S4));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTransitionToChoiceActionCalled1() throws InterruptedException {
		context.register(Config5.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		LatchAction sIToChoice = context.getBean("sIToChoice", LatchAction.class);
		LatchAction choiceToS30 = context.getBean("choiceToS30", LatchAction.class);
		LatchAction choiceToS33 = context.getBean("choiceToS33", LatchAction.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("choice", "s30").build());
		assertThat(sIToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS30.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS33.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(machine.getState().getIds(), contains(TestStates.S30));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTransitionToChoiceActionCalled2() throws InterruptedException {
		context.register(Config5.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		LatchAction sIToChoice = context.getBean("sIToChoice", LatchAction.class);
		LatchAction choiceToS30 = context.getBean("choiceToS30", LatchAction.class);
		LatchAction choiceToS33 = context.getBean("choiceToS33", LatchAction.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(sIToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS30.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choiceToS33.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(TestStates.S33));
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
					.choice(TestStates.S3)
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S3)
					.event(TestEvents.E1)
					.and()
				.withChoice()
					.source(TestStates.S3)
					.first(TestStates.S30, s30Guard())
					.then(TestStates.S31, s31Guard())
					.then(TestStates.S32, s32Guard())
					.last(TestStates.S33);
		}

		@Bean
		public Guard<TestStates, TestEvents> s30Guard() {
			return new ChoiceGuard("s30");
		}

		@Bean
		public Guard<TestStates, TestEvents> s31Guard() {
			return new ChoiceGuard("s31");
		}

		@Bean
		public Guard<TestStates, TestEvents> s32Guard() {
			return new ChoiceGuard("s32");
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
					.states(EnumSet.allOf(TestStates.class))
					.choice(TestStates.S3)
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S3)
					.event(TestEvents.E1)
					.and()
				.withChoice()
					.source(TestStates.S3)
					.last(TestStates.S33);
		}

		@Bean
		public Guard<TestStates, TestEvents> s30Guard() {
			return new ChoiceGuard("s30");
		}

		@Bean
		public Guard<TestStates, TestEvents> s31Guard() {
			return new ChoiceGuard("s31");
		}

		@Bean
		public Guard<TestStates, TestEvents> s32Guard() {
			return new ChoiceGuard("s32");
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
					.states(EnumSet.allOf(TestStates.class))
					.choice(TestStates.S3)
					.choice(TestStates.S2)
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S3)
					.event(TestEvents.E1)
					.and()
				.withChoice()
					.source(TestStates.S3)
					.first(TestStates.S2, s2Guard())
					.last(TestStates.S33)
					.and()
				.withChoice()
					.source(TestStates.S2)
					.first(TestStates.S20, s20Guard())
					.last(TestStates.S21);
		}

		@Bean
		public Guard<TestStates, TestEvents> s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public Guard<TestStates, TestEvents> s20Guard() {
			return new ChoiceGuard("s20");
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
					.states(EnumSet.allOf(TestStates.class))
					.choice(TestStates.S2);
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
					.source(TestStates.SI)
					.target(TestStates.S4)
					.event(TestEvents.E2)
					.and()
				.withChoice()
					.source(TestStates.S2)
					.last(TestStates.SI);
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
					.states(EnumSet.allOf(TestStates.class))
					.choice(TestStates.S3)
					.end(TestStates.SF);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S3)
					.action(sIToChoice())
					.event(TestEvents.E1)
					.and()
				.withChoice()
					.source(TestStates.S3)
					.first(TestStates.S30, s30Guard(), choiceToS30())
					.then(TestStates.S31, s31Guard())
					.then(TestStates.S32, s32Guard())
					.last(TestStates.S33, choiceToS33(), choiceToS33Error());
		}

		@Bean
		public Guard<TestStates, TestEvents> s30Guard() {
			return new ChoiceGuard("s30");
		}

		@Bean
		public Guard<TestStates, TestEvents> s31Guard() {
			return new ChoiceGuard("s31");
		}

		@Bean
		public Guard<TestStates, TestEvents> s32Guard() {
			return new ChoiceGuard("s32");
		}

		@Bean
		public Action<TestStates, TestEvents> sIToChoice() {
			return new LatchAction();
		}

		@Bean
		public Action<TestStates, TestEvents> choiceToS30() {
			return new LatchAction();
		}

		@Bean
		public Action<TestStates, TestEvents> choiceToS33() {
			return new LatchAction();
		}

		@Bean
		public Action<TestStates, TestEvents> choiceToS33Error() {
			return new LatchAction();
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

	private static class ChoiceGuard implements Guard<TestStates, TestEvents> {

		private final String match;

		public ChoiceGuard(String match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<TestStates, TestEvents> context) {
			return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("choice", String.class));
		}
	}

	private static class LatchAction implements Action<TestStates, TestEvents> {
		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			latch.countDown();
		}
	}
}
