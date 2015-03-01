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
package org.springframework.statemachine.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests from state machine app context events.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineEventTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testContextEvents() throws Exception {
		context.register(BaseConfig.class, StateMachineEventPublisherConfiguration.class, Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		TestEventListener listener = context.getBean(TestEventListener.class);
		@SuppressWarnings("unchecked")
		EnumStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		machine.start();
		assertThat(machine, notNullValue());
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);
		machine.sendEvent(TestEvents.EF);

		// 6 events instead of 5, first one is initial transition
		// to SI where source state is null
		assertThat(listener.onEventLatch.await(5, TimeUnit.SECONDS), is(true));
		assertThat(listener.events.size(), is(6));
	}

	@Test
	public void testSubmachineHandlesEvent() throws Exception {
		context.register(BaseConfig.class, StateMachineEventPublisherConfiguration.class, Config2.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		EnumStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		machine.start();
		assertThat(machine, notNullValue());

		assertThat(machine.getState().getIds(), contains(TestStates.S1, TestStates.S10));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates.S1, TestStates.S12));
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
		public TestEventListener testEventListener() {
			return new TestEventListener();
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
					.and()
					.withStates()
						.parent(TestStates.S1)
						.initial(TestStates.S10)
						.state(TestStates.S10)
						.state(TestStates.S11)
						.state(TestStates.S12)
						.state(TestStates.S13);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S11)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.state(TestStates.S1)
					.source(TestStates.S10)
					.target(TestStates.S12)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.state(TestStates.S1)
					.source(TestStates.S10)
					.target(TestStates.S13)
					.event(TestEvents.E2);
		}

	}

	static class TestEventListener implements ApplicationListener<StateMachineEvent> {

		CountDownLatch onEventLatch = new CountDownLatch(6);

		ArrayList<StateMachineEvent> events = new ArrayList<StateMachineEvent>();

		@Override
		public void onApplicationEvent(StateMachineEvent event) {
			if (event instanceof OnStateChangedEvent) {
				events.add(event);
				onEventLatch.countDown();
			}
		}

	}

}
