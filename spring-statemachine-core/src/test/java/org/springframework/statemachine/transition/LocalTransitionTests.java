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
package org.springframework.statemachine.transition;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@SuppressWarnings("unchecked")
public class LocalTransitionTests extends AbstractStateMachineTests {

	@Test
	public void testExternalSuperDoesEntryExitToSub() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E20");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S21"));
	}

	@Test
	public void testLocalSuperDoesNotEntryExitToSub() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E30");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S21"));
	}

	@Test
	public void testExternalToNonInitialSuperDoesEntryExitToSub() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E21");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S22"));
	}

	@Test
	public void testLocalToNonInitialSuperDoesNotEntryExitToSub() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E31");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S22"));
	}

	@Test
	public void testExternalSuperDoesEntryExitToParent() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E22");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S21"));
	}

	@Test
	public void testLocalSuperDoesNotEntryExitToParent() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E32");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S21"));
	}

	@Test
	public void testExternalToNonInitialSuperDoesEntryExitToParent() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E21");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S22"));

		listener.reset();
		machine.sendEvent("E23");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S22"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S22"));
	}

	@Test
	public void testLocalToNonInitialSuperDoesNotEntryExitToParent() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		machine.sendEvent("E31");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S22"));

		listener.reset();
		machine.sendEvent("E33");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S22"));
		assertThat(listener.entered, containsInAnyOrder("S22"));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.and()
					.withStates()
						.parent("S2")
						.initial("S21")
						.state("S22");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S21")
					.event("E20")
					.and()
				.withExternal()
					.source("S2")
					.target("S22")
					.event("E21")
					.and()
				.withExternal()
					.source("S21")
					.target("S2")
					.event("E22")
					.and()
				.withExternal()
					.source("S22")
					.target("S2")
					.event("E23")
					.and()
				.withLocal()
					.source("S2")
					.target("S21")
					.event("E30")
					.and()
				.withLocal()
					.source("S2")
					.target("S22")
					.event("E31")
					.and()
				.withLocal()
					.source("S21")
					.target("S2")
					.event("E32")
					.and()
				.withLocal()
					.source("S22")
					.target("S2")
					.event("E33");
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<String, String> {

		final ArrayList<String> entered = new ArrayList<>();
		final ArrayList<String> exited = new ArrayList<>();

		@Override
		public void stateEntered(State<String, String> state) {
			entered.add(state.getId());
		}

		@Override
		public void stateExited(State<String, String> state) {
			exited.add(state.getId());
		}

		public void reset() {
			entered.clear();
			exited.clear();
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
