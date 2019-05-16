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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.EventTrigger;

/**
 * Tests for states using a submachine.
 *
 * @author Janne Valkealahti
 *
 */
public class SubmachineStateTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testSimpleSubmachineState() {
		PseudoState<TestStates,TestEvents> pseudoState = new DefaultPseudoState<TestStates,TestEvents>(PseudoStateKind.INITIAL);
		State<TestStates,TestEvents> stateSI = new EnumState<TestStates,TestEvents>(TestStates.SI, pseudoState);
		State<TestStates,TestEvents> stateS1 = new EnumState<TestStates,TestEvents>(TestStates.S1);
		State<TestStates,TestEvents> stateS2 = new EnumState<TestStates,TestEvents>(TestStates.S2);
		State<TestStates,TestEvents> stateS3 = new EnumState<TestStates,TestEvents>(TestStates.S3);

		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateSI);
		states.add(stateS1);
		states.add(stateS2);
		states.add(stateS3);

		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();

		DefaultExternalTransition<TestStates,TestEvents> transitionFromSIToS1 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateSI, stateS1, null, TestEvents.E1, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E1));

		DefaultExternalTransition<TestStates,TestEvents> transitionFromS1ToS2 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS1, stateS2, null, TestEvents.E2, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E2));

		DefaultExternalTransition<TestStates,TestEvents> transitionFromS2ToS3 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS2, stateS3, null, TestEvents.E3, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E3));

		transitions.add(transitionFromSIToS1);
		transitions.add(transitionFromS1ToS2);
		transitions.add(transitionFromS2ToS3);

		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		ObjectStateMachine<TestStates, TestEvents> machine = new ObjectStateMachine<TestStates, TestEvents>(states, transitions, stateSI);
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		machine.start();

		StateMachineState<TestStates,TestEvents> state = new StateMachineState<TestStates,TestEvents>(TestStates.S4, machine);

		assertThat(state.isSimple(), is(false));
		assertThat(state.isComposite(), is(false));
		assertThat(state.isOrthogonal(), is(false));
		assertThat(state.isSubmachineState(), is(true));

		assertThat(state.getIds(), contains(TestStates.S4, TestStates.SI));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFromSimpleToOtherSubstate() {
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
	public void testAllSubmachinesRunningInitialsTakesToDeep() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);

		assertThat(machine.isRunning(), is(true));

		State<TestStates, TestEvents> s = machine.getState();
		StateMachine<TestStates, TestEvents> m = ((StateMachineState<TestStates, TestEvents>) s).getSubmachine();
		boolean r = TestUtils.readField("running", m);
		assertThat(r, is(true));

		s = m.getState();
		m = ((StateMachineState<TestStates, TestEvents>) s).getSubmachine();
		r = TestUtils.readField("running", m);
		assertThat(r, is(true));

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S20, TestStates.S2011));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAllSubmachinesRunningInitialsNotTakeToDeep() throws Exception {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);

		assertThat(machine.isRunning(), is(true));

		State<TestStates, TestEvents> s = machine.getState();
		StateMachine<TestStates, TestEvents> m = ((StateMachineState<TestStates, TestEvents>) s).getSubmachine();
		boolean r = TestUtils.readField("running", m);
		assertThat(r, is(true));

		s = m.getState();
		m = ((StateMachineState<TestStates, TestEvents>) s).getSubmachine();
		r = TestUtils.readField("running", m);
		assertThat(r, is(true));

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21, TestStates.S212));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAllSubmachinesStopped() throws Exception {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);

		assertThat(machine.isRunning(), is(true));

		State<TestStates, TestEvents> s1 = machine.getState();
		StateMachine<TestStates, TestEvents> m1 = ((StateMachineState<TestStates, TestEvents>) s1).getSubmachine();

		State<TestStates, TestEvents> s2 = m1.getState();
		StateMachine<TestStates, TestEvents> m2 = ((StateMachineState<TestStates, TestEvents>) s2).getSubmachine();

		machine.sendEvent(TestEvents.E3);

		boolean r1 = TestUtils.readField("running", m1);
		assertThat(r1, is(false));
		boolean r2 = TestUtils.readField("running", m2);
		assertThat(r2, is(false));

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
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
						.state(TestStates.S21);
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
					.target(TestStates.S21)
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
						.and()
						.withStates()
							.parent(TestStates.S20)
							.initial(TestStates.S2011)
							.state(TestStates.S2011)
							.state(TestStates.S2012);

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1);
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
					.target(TestStates.S212)
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
					.event(TestEvents.E3);
		}

	}

}
