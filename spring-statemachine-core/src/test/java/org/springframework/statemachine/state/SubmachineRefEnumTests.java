/*
 * Copyright 2016 the original author or authors.
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for submachine references.
 *
 * @author Janne Valkealahti
 *
 */
public class SubmachineRefEnumTests extends AbstractStateMachineTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testSubmachineRef() throws Exception {
		context.register(Config2.class, Config1.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20));
		machine.sendEvent(TestEvents.E2);
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S30));
		machine.sendEvent(TestEvents.E3);
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S31));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSubmachineRefDifferentTypes() throws Exception {
		context.register(Config4.class, Config3.class);
		context.refresh();
		StateMachine<Object, Object> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(States1.S1));
		machine.sendEvent(Events1.E1);
		assertThat(machine.getState().getIds(), containsInAnyOrder(States1.S2, States2.S20));
		machine.sendEvent(Events2.E2);
		assertThat(machine.getState().getIds(), containsInAnyOrder(States1.S2, States2.S21, States2.S30));
		machine.sendEvent(Events2.E3);
		assertThat(machine.getState().getIds(), containsInAnyOrder(States1.S2, States2.S21, States2.S31));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Autowired
		@Qualifier("subStateMachine")
		private StateMachine<TestStates, TestEvents> subStateMachine;

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2, subStateMachine);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1).target(TestStates.S2).event(TestEvents.E1);
		}

	}

	@Configuration
	@EnableStateMachine(name = "subStateMachine")
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S20)
					.state(TestStates.S21)
					.and()
					.withStates()
						.parent(TestStates.S21)
						.initial(TestStates.S30)
						.state(TestStates.S31);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S20).target(TestStates.S21).event(TestEvents.E2).and()
				.withExternal()
					.source(TestStates.S30).target(TestStates.S31).event(TestEvents.E3);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<Object, Object> {

		@Autowired
		@Qualifier("subStateMachine")
		private StateMachine<Object, Object> subStateMachine;

		@Override
		public void configure(StateMachineStateConfigurer<Object, Object> states) throws Exception {
			states
				.withStates()
					.initial(States1.S1)
					.state(States1.S2, subStateMachine);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<Object, Object> transitions) throws Exception {
			transitions
				.withExternal()
					.source(States1.S1).target(States1.S2).event(Events1.E1);
		}

	}

	@Configuration
	@EnableStateMachine(name = "subStateMachine")
	static class Config4 extends StateMachineConfigurerAdapter<Object, Object> {

		@Override
		public void configure(StateMachineStateConfigurer<Object, Object> states) throws Exception {
			states
				.withStates()
					.initial(States2.S20)
					.state(States2.S21)
					.and()
					.withStates()
						.parent(States2.S21)
						.initial(States2.S30)
						.state(States2.S31);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<Object, Object> transitions) throws Exception {
			transitions
				.withExternal()
					.source(States2.S20).target(States2.S21).event(Events2.E2).and()
				.withExternal()
					.source(States2.S30).target(States2.S31).event(Events2.E3);
		}

	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	enum States1 {
		S1, S2;
	}

	enum Events1 {
		E1
	}

	enum States2 {
		S20, S21, S30, S31;
	}

	enum Events2 {
		E2, E3;
	}
}
