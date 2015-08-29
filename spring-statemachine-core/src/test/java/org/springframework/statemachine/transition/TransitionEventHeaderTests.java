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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import org.springframework.statemachine.transition.TransitionTests.TestListener;

/**
 * Tests for making sure that events are passed through various
 * transition stages.
 *
 * @author Janne Valkealahti
 *
 */
public class TransitionEventHeaderTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEventPassedOnWithTrigggerless() throws Exception {
		context.register(Config1.class);
		context.refresh();

		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		EventCheckAction eventCheckAction1 = context.getBean("eventCheckAction1", EventCheckAction.class);
		EventCheckAction eventCheckAction2 = context.getBean("eventCheckAction2", EventCheckAction.class);
		EventCheckAction eventCheckAction3 = context.getBean("eventCheckAction3", EventCheckAction.class);
		EventCheckAction eventCheckAction4 = context.getBean("eventCheckAction4", EventCheckAction.class);

		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));
		assertThat(machine.getState().getIds(), contains(TestStates.S4));

		assertThat(eventCheckAction1.context.getEvent(), nullValue());
		assertThat(eventCheckAction2.context.getEvent(), is(TestEvents.E1));
		assertThat(eventCheckAction3.context.getEvent(), is(TestEvents.E1));
		assertThat(eventCheckAction4.context.getEvent(), is(TestEvents.E1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEventPassedThroughChoice1() throws Exception {
		context.register(Config2.class);
		context.refresh();

		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		EventCheckAction eventCheckAction1 = context.getBean("eventCheckAction1", EventCheckAction.class);
		EventCheckAction eventCheckAction3 = context.getBean("eventCheckAction3", EventCheckAction.class);
		EventCheckAction eventCheckAction4 = context.getBean("eventCheckAction4", EventCheckAction.class);

		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(TestStates.S4));

		assertThat(eventCheckAction1.context.getEvent(), nullValue());
		assertThat(eventCheckAction3.context, nullValue());
		assertThat(eventCheckAction4.context.getEvent(), is(TestEvents.E1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEventPassedThroughChoice2() throws Exception {
		context.register(Config3.class);
		context.refresh();

		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		EventCheckAction eventCheckAction1 = context.getBean("eventCheckAction1", EventCheckAction.class);
		EventCheckAction eventCheckAction3 = context.getBean("eventCheckAction3", EventCheckAction.class);
		EventCheckAction eventCheckAction4 = context.getBean("eventCheckAction4", EventCheckAction.class);

		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(machine.getState().getIds(), contains(TestStates.S1));

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(TestStates.S3));

		assertThat(eventCheckAction1.context.getEvent(), nullValue());
		assertThat(eventCheckAction3.context.getEvent(), is(TestEvents.E1));
		assertThat(eventCheckAction4.context, nullValue());
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1, eventCheckAction1(), null)
					.state(TestStates.S2, eventCheckAction2(), null)
					.state(TestStates.S3, eventCheckAction3(), null)
					.state(TestStates.S4, eventCheckAction4(), null);
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
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4);
		}

		@Bean
		public EventCheckAction eventCheckAction1() {
			return new EventCheckAction();
		}

		@Bean
		public EventCheckAction eventCheckAction2() {
			return new EventCheckAction();
		}

		@Bean
		public EventCheckAction eventCheckAction3() {
			return new EventCheckAction();
		}

		@Bean
		public EventCheckAction eventCheckAction4() {
			return new EventCheckAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1, eventCheckAction1(), null)
					.choice(TestStates.S2)
					.state(TestStates.S3, eventCheckAction3(), null)
					.state(TestStates.S4, eventCheckAction4(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withChoice()
					.source(TestStates.S2)
					.first(TestStates.S3, guard())
					.last(TestStates.S4);
		}

		@Bean
		public EventCheckAction eventCheckAction1() {
			return new EventCheckAction();
		}

		@Bean
		public EventCheckAction eventCheckAction3() {
			return new EventCheckAction();
		}

		@Bean
		public EventCheckAction eventCheckAction4() {
			return new EventCheckAction();
		}

		@Bean
		public Guard<TestStates, TestEvents> guard() {
			return new Guard<TestStates, TestEvents>() {

				@Override
				public boolean evaluate(StateContext<TestStates, TestEvents> context) {
					return false;
				}
			};
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
					.state(TestStates.S1, eventCheckAction1(), null)
					.choice(TestStates.S2)
					.state(TestStates.S3, eventCheckAction3(), null)
					.state(TestStates.S4, eventCheckAction4(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withChoice()
					.source(TestStates.S2)
					.first(TestStates.S3, guard())
					.last(TestStates.S4);
		}

		@Bean
		public EventCheckAction eventCheckAction1() {
			return new EventCheckAction();
		}

		@Bean
		public EventCheckAction eventCheckAction3() {
			return new EventCheckAction();
		}

		@Bean
		public EventCheckAction eventCheckAction4() {
			return new EventCheckAction();
		}

		@Bean
		public Guard<TestStates, TestEvents> guard() {
			return new Guard<TestStates, TestEvents>() {

				@Override
				public boolean evaluate(StateContext<TestStates, TestEvents> context) {
					return true;
				}
			};
		}
	}

	private static class EventCheckAction implements Action<TestStates, TestEvents> {

		StateContext<TestStates, TestEvents> context;

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			this.context = context;
		}

	}

}
