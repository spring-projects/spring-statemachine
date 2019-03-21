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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
import org.springframework.statemachine.config.configurers.StateConfigurer.History;

public class HistoryStateTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testShallowInSubmachine() {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDeep() {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);
		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21, TestStates.S212));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testShallow() {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21, TestStates.S211));
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
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.history(TestStates.SH, History.SHALLOW);

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
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
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
					.state(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.history(TestStates.SH, History.DEEP)
						.and()
						.withStates()
							.parent(TestStates.S21)
							.initial(TestStates.S211)
							.state(TestStates.S211)
							.state(TestStates.S212);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S211)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S211)
					.target(TestStates.S212)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S212)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.E4);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.history(TestStates.SH, History.SHALLOW)
						.and()
						.withStates()
							.parent(TestStates.S21)
							.initial(TestStates.S211)
							.state(TestStates.S211)
							.state(TestStates.S212);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S211)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S211)
					.target(TestStates.S212)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S212)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.E4);
		}

	}

}
