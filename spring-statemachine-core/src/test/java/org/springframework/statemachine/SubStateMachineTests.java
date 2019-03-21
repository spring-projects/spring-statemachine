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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultLocalTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.EventTrigger;

public class SubStateMachineTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testExternalTransition() throws Exception {
		/**
		 *          +-------------------------------------------+
		 *  *-init->|                    S1                     |
		 *          +-------------------------------------------+
		 *          |  entry/                                   |
		 *          |  exit/                                    |
		 *          |         +--------------------------+      |
		 *          |     *-->|           S11            |      |
		 *          |         +--------------------------+      |
		 *          |         | entry/                   |      |
		 *          |         | exit/                    |      |
		 *          |         |        +-----------+     |      |
		 *          |         |    *-->|   S111    |     |      |
		 *          |         |        +-----------+     |      |
		 *          |         |        | entry/    |     |      |
		 *          |<----E1-----------| exit/     |     |      |
		 *          |         |        |           |     |      |
		 *          |         |        +-----------+     |      |
		 *          |         |                          |      |
		 *          |         +--------------------------+      |
		 *          |                                           |
		 *          +-------------------------------------------+
		 */
		PseudoState<TestStates,TestEvents> pseudoState = new DefaultPseudoState<TestStates,TestEvents>(PseudoStateKind.INITIAL);

		TestEntryAction entryActionS111 = new TestEntryAction("S111");
		TestExitAction exitActionS111 = new TestExitAction("S111");
		Collection<Action<TestStates, TestEvents>> entryActionsS111 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS111.add(entryActionS111);
		Collection<Action<TestStates, TestEvents>> exitActionsS111 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS111.add(exitActionS111);
		State<TestStates,TestEvents> stateS111 = new EnumState<TestStates,TestEvents>(TestStates.S111, null, entryActionsS111, exitActionsS111, pseudoState);

		// submachine 11
		Collection<State<TestStates,TestEvents>> substates111 = new ArrayList<State<TestStates,TestEvents>>();
		substates111.add(stateS111);
		Collection<Transition<TestStates,TestEvents>> subtransitions111 = new ArrayList<Transition<TestStates,TestEvents>>();
		ObjectStateMachine<TestStates, TestEvents> submachine11 = new ObjectStateMachine<TestStates, TestEvents>(substates111, subtransitions111, stateS111);

		// submachine 1
		TestEntryAction entryActionS11 = new TestEntryAction("S11");
		TestExitAction exitActionS11 = new TestExitAction("S11");
		Collection<Action<TestStates, TestEvents>> entryActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS11.add(entryActionS11);
		Collection<Action<TestStates, TestEvents>> exitActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS11.add(exitActionS11);
		StateMachineState<TestStates,TestEvents> stateS11 = new StateMachineState<TestStates,TestEvents>(TestStates.S11, submachine11, null, entryActionsS11, exitActionsS11, pseudoState);

		Collection<State<TestStates,TestEvents>> substates11 = new ArrayList<State<TestStates,TestEvents>>();
		substates11.add(stateS11);
		Collection<Transition<TestStates,TestEvents>> subtransitions11 = new ArrayList<Transition<TestStates,TestEvents>>();
		ObjectStateMachine<TestStates, TestEvents> submachine1 = new ObjectStateMachine<TestStates, TestEvents>(substates11, subtransitions11, stateS11);

		// machine
		TestEntryAction entryActionS1 = new TestEntryAction("S1");
		TestExitAction exitActionS1 = new TestExitAction("S1");
		Collection<Action<TestStates, TestEvents>> entryActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS1.add(entryActionS1);
		Collection<Action<TestStates, TestEvents>> exitActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS1.add(exitActionS1);

		StateMachineState<TestStates,TestEvents> stateS1 = new StateMachineState<TestStates,TestEvents>(TestStates.S1, submachine1, null, entryActionsS1, exitActionsS1, pseudoState);
		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateS1);
		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();
		DefaultExternalTransition<TestStates,TestEvents> transitionFromS111ToS1 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS111, stateS1, null, TestEvents.E1, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E1));
		transitions.add(transitionFromS111ToS1);
		ObjectStateMachine<TestStates, TestEvents> machine = new ObjectStateMachine<TestStates, TestEvents>(states, transitions, stateS1);


		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		submachine1.setTaskExecutor(taskExecutor);
		submachine1.afterPropertiesSet();
		submachine11.setTaskExecutor(taskExecutor);
		submachine11.afterPropertiesSet();
		machine.start();

		machine.sendEvent(TestEvents.E1);

		assertThat(entryActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));

		assertThat(entryActionS111.stateContexts.size(), is(2));
		assertThat(exitActionS111.stateContexts.size(), is(1));
		assertThat(entryActionS11.stateContexts.size(), is(2));
		assertThat(exitActionS11.stateContexts.size(), is(1));
		assertThat(entryActionS1.stateContexts.size(), is(1));
		assertThat(exitActionS1.stateContexts.size(), is(1));
	}

	@Test
	public void testExternalTransition2() throws Exception {

		/**
		 *          +---------------------------------------------------+
		 *  *-init->|                         S1                        |
		 *          +---------------------------------------------------+
		 *          |  entry/                                           |
		 *          |  exit/                                            |
		 *          |            +-----------+          +-----------+   |
		 *          |        *-->|   S111    |          |   S112    |   |
		 *          |            +-----------+          +-----------+   |
		 *          |            | entry/    |          | entry/    |   |
		 *          |            | exit/     |----E1--->| exit/     |   |
		 *          |            |           |          |           |   |
		 *          |            |           |<---E2----|           |   |
		 *          |            |           |          |           |   |
		 *          |            +-----------+          +-----------+   |
		 *          |                                                   |
		 *          +---------------------------------------------------+
		 */


		PseudoState<TestStates,TestEvents> pseudoState = new DefaultPseudoState<TestStates,TestEvents>(PseudoStateKind.INITIAL);

		TestEntryAction entryActionS111 = new TestEntryAction("S111");
		TestExitAction exitActionS111 = new TestExitAction("S111");
		Collection<Action<TestStates, TestEvents>> entryActionsS111 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS111.add(entryActionS111);
		Collection<Action<TestStates, TestEvents>> exitActionsS111 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS111.add(exitActionS111);
		State<TestStates,TestEvents> stateS111 = new EnumState<TestStates,TestEvents>(TestStates.S111, null, entryActionsS111, exitActionsS111, pseudoState);

		TestEntryAction entryActionS112 = new TestEntryAction("S112");
		TestExitAction exitActionS112 = new TestExitAction("S112");
		Collection<Action<TestStates, TestEvents>> entryActionsS112 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS112.add(entryActionS112);
		Collection<Action<TestStates, TestEvents>> exitActionsS112 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS112.add(exitActionS112);
		State<TestStates,TestEvents> stateS112 = new EnumState<TestStates,TestEvents>(TestStates.S112, null, entryActionsS112, exitActionsS112, null);

		// submachine 1
		Collection<State<TestStates,TestEvents>> substates11 = new ArrayList<State<TestStates,TestEvents>>();
		substates11.add(stateS111);
		substates11.add(stateS112);
		Collection<Transition<TestStates,TestEvents>> subtransitions11 = new ArrayList<Transition<TestStates,TestEvents>>();
		ObjectStateMachine<TestStates, TestEvents> submachine11 = new ObjectStateMachine<TestStates, TestEvents>(substates11, subtransitions11, stateS111);

		// machine
		TestEntryAction entryActionS1 = new TestEntryAction("S1");
		TestExitAction exitActionS1 = new TestExitAction("S1");
		Collection<Action<TestStates, TestEvents>> entryActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS1.add(entryActionS1);
		Collection<Action<TestStates, TestEvents>> exitActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS1.add(exitActionS1);

		StateMachineState<TestStates,TestEvents> stateS1 = new StateMachineState<TestStates,TestEvents>(TestStates.S1, submachine11, null, entryActionsS1, exitActionsS1, pseudoState);
		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateS1);
		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();
		DefaultExternalTransition<TestStates,TestEvents> transitionFromS111ToS112 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS111, stateS112, null, TestEvents.E1, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E1));
		transitions.add(transitionFromS111ToS112);
		ObjectStateMachine<TestStates, TestEvents> machine = new ObjectStateMachine<TestStates, TestEvents>(states, transitions, stateS1);


		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		submachine11.setTaskExecutor(taskExecutor);
		submachine11.afterPropertiesSet();
		machine.start();

		machine.sendEvent(TestEvents.E1);

		assertThat(entryActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS112.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS112.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(entryActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));

		assertThat(entryActionS111.stateContexts.size(), is(1));
		assertThat(exitActionS111.stateContexts.size(), is(1));
		assertThat(entryActionS112.stateContexts.size(), is(1));
		assertThat(exitActionS112.stateContexts.size(), is(0));
		assertThat(entryActionS1.stateContexts.size(), is(1));
		assertThat(exitActionS1.stateContexts.size(), is(0));
	}


	@Test
	public void testLocalTransition() throws Exception {
		/**
		 *          +-------------------------------------------+
		 *  *-init->|                    S1                     |
		 *          +-------------------------------------------+
		 *          |  entry/                                   |
		 *          |  exit/                                    |
		 *          |         +--------------------------+      |
		 *          |     *-->|           S11            |      |
		 *          |         +--------------------------+      |
		 *          |         | entry/                   |      |
		 *          |         | exit/                    |      |
		 *          |         |        +-----------+     |      |
		 *          |         |    *-->|   S111    |     |      |
		 *          |         |        +-----------+     |      |
		 *          |         |        | entry/    |     |      |
		 *          |<----E1-----------| exit/     |     |      |
		 *          |         |        |           |     |      |
		 *          |         |        +-----------+     |      |
		 *          |         |                          |      |
		 *          |         +--------------------------+      |
		 *          |                                           |
		 *          +-------------------------------------------+
		 */
		PseudoState<TestStates,TestEvents> pseudoState = new DefaultPseudoState<TestStates,TestEvents>(PseudoStateKind.INITIAL);

		TestEntryAction entryActionS111 = new TestEntryAction("S111");
		TestExitAction exitActionS111 = new TestExitAction("S111");
		Collection<Action<TestStates, TestEvents>> entryActionsS111 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS111.add(entryActionS111);
		Collection<Action<TestStates, TestEvents>> exitActionsS111 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS111.add(exitActionS111);
		State<TestStates,TestEvents> stateS111 = new EnumState<TestStates,TestEvents>(TestStates.S111, null, entryActionsS111, exitActionsS111, pseudoState);

		// submachine 11
		Collection<State<TestStates,TestEvents>> substates111 = new ArrayList<State<TestStates,TestEvents>>();
		substates111.add(stateS111);
		Collection<Transition<TestStates,TestEvents>> subtransitions111 = new ArrayList<Transition<TestStates,TestEvents>>();
		ObjectStateMachine<TestStates, TestEvents> submachine11 = new ObjectStateMachine<TestStates, TestEvents>(substates111, subtransitions111, stateS111);

		// submachine 1
		TestEntryAction entryActionS11 = new TestEntryAction("S11");
		TestExitAction exitActionS11 = new TestExitAction("S11");
		Collection<Action<TestStates, TestEvents>> entryActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS11.add(entryActionS11);
		Collection<Action<TestStates, TestEvents>> exitActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS11.add(exitActionS11);
		StateMachineState<TestStates,TestEvents> stateS11 = new StateMachineState<TestStates,TestEvents>(TestStates.S11, submachine11, null, entryActionsS11, exitActionsS11, pseudoState);

		Collection<State<TestStates,TestEvents>> substates11 = new ArrayList<State<TestStates,TestEvents>>();
		substates11.add(stateS11);
		Collection<Transition<TestStates,TestEvents>> subtransitions11 = new ArrayList<Transition<TestStates,TestEvents>>();
		ObjectStateMachine<TestStates, TestEvents> submachine1 = new ObjectStateMachine<TestStates, TestEvents>(substates11, subtransitions11, stateS11);

		// machine
		TestEntryAction entryActionS1 = new TestEntryAction("S1");
		TestExitAction exitActionS1 = new TestExitAction("S1");
		Collection<Action<TestStates, TestEvents>> entryActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS1.add(entryActionS1);
		Collection<Action<TestStates, TestEvents>> exitActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS1.add(exitActionS1);

		StateMachineState<TestStates,TestEvents> stateS1 = new StateMachineState<TestStates,TestEvents>(TestStates.S1, submachine1, null, entryActionsS1, exitActionsS1, pseudoState);
		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateS1);
		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();
		DefaultLocalTransition<TestStates,TestEvents> transitionFromS11ToS1 =
				new DefaultLocalTransition<TestStates,TestEvents>(stateS111, stateS1, null, TestEvents.E1, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E1));
		transitions.add(transitionFromS11ToS1);
		ObjectStateMachine<TestStates, TestEvents> machine = new ObjectStateMachine<TestStates, TestEvents>(states, transitions, stateS1);


		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		submachine1.setTaskExecutor(taskExecutor);
		submachine1.afterPropertiesSet();
		submachine11.setTaskExecutor(taskExecutor);
		submachine11.afterPropertiesSet();
		machine.start();

		machine.sendEvent(TestEvents.E1);

		assertThat(entryActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));

		assertThat(entryActionS111.stateContexts.size(), is(2));
		assertThat(exitActionS111.stateContexts.size(), is(1));
		assertThat(entryActionS11.stateContexts.size(), is(2));
		assertThat(exitActionS11.stateContexts.size(), is(1));
		assertThat(entryActionS1.stateContexts.size(), is(1));
		assertThat(exitActionS1.stateContexts.size(), is(0));
	}

	@Test
	public void testExternalTransition3() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		machine.start();

		machine.sendEvent(TestEvents.E1);

		TestEntryAction entryActionS111 = context.getBean("entryActionS111", TestEntryAction.class);
		TestExitAction exitActionS111 = context.getBean("exitActionS111", TestExitAction.class);
		TestEntryAction entryActionS11 = context.getBean("entryActionS11", TestEntryAction.class);
		TestExitAction exitActionS11 = context.getBean("exitActionS11", TestExitAction.class);
		TestEntryAction entryActionS1 = context.getBean("entryActionS1", TestEntryAction.class);
		TestExitAction exitActionS1 = context.getBean("exitActionS1", TestExitAction.class);

		assertThat(entryActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));

		assertThat(entryActionS111.stateContexts.size(), is(2));
		assertThat(exitActionS111.stateContexts.size(), is(1));
		assertThat(entryActionS11.stateContexts.size(), is(2));
		assertThat(exitActionS11.stateContexts.size(), is(1));
		assertThat(entryActionS1.stateContexts.size(), is(1));
		assertThat(exitActionS1.stateContexts.size(), is(1));

	}

	@Test
	public void testMixedStates() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine, notNullValue());

		assertThat(machine.getState().getIds(), contains(TestStates.S1, TestStates.S10));
	}

	@Test
	public void testStateChangeWithinMachine() {
		context.register(BaseConfig.class, Config3.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates2,TestEvents2> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		assertThat(machine, notNullValue());
		assertThat(machine.getState().getIds(), contains(TestStates2.IDLE, TestStates2.CLOSED));
		machine.sendEvent(TestEvents2.EJECT);
		assertThat(machine.getState().getIds(), contains(TestStates2.IDLE, TestStates2.OPEN));
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1, Arrays.asList(entryActionS1()), Arrays.asList(exitActionS1()))
					.and()
					.withStates()
						.parent(TestStates.S1)
						.initial(TestStates.S11)
						.state(TestStates.S11, Arrays.asList(entryActionS11()), Arrays.asList(exitActionS11()))
						.and()
						.withStates()
							.parent(TestStates.S11)
							.initial(TestStates.S111)
							.state(TestStates.S111, Arrays.asList(entryActionS111()), Arrays.asList(exitActionS111()));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S111)
					.target(TestStates.S1)
					.event(TestEvents.E1);
		}

		@Bean(name = "entryActionS111")
		public Action<TestStates, TestEvents> entryActionS111() {
			return new TestEntryAction("S111");
		}

		@Bean(name = "exitActionS111")
		public Action<TestStates, TestEvents> exitActionS111() {
			return new TestExitAction("S111");
		}

		@Bean(name = "entryActionS11")
		public Action<TestStates, TestEvents> entryActionS11() {
			return new TestEntryAction("S11");
		}

		@Bean(name = "exitActionS11")
		public Action<TestStates, TestEvents> exitActionS11() {
			return new TestExitAction("S11");
		}

		@Bean(name = "entryActionS1")
		public Action<TestStates, TestEvents> entryActionS1() {
			return new TestEntryAction("S1");
		}

		@Bean(name = "exitActionS1")
		public Action<TestStates, TestEvents> exitActionS1() {
			return new TestExitAction("S1");
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
						.parent(TestStates.S1)
						.initial(TestStates.S10)
						.state(TestStates.S10);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates2, TestEvents2> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates2, TestEvents2> states) throws Exception {
			states
				.withStates()
					.initial(TestStates2.IDLE)
					.state(TestStates2.IDLE)
					.and()
					.withStates()
						.parent(TestStates2.IDLE)
						.initial(TestStates2.CLOSED)
						.state(TestStates2.CLOSED)
						.state(TestStates2.OPEN)
						.and()
				.withStates()
					.state(TestStates2.BUSY)
					.and()
					.withStates()
						.parent(TestStates2.BUSY)
						.initial(TestStates2.PLAYING)
						.state(TestStates2.PLAYING)
						.state(TestStates2.PAUSED);

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates2, TestEvents2> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates2.CLOSED)
					.target(TestStates2.OPEN)
					.event(TestEvents2.EJECT)
					.and()
				.withExternal()
					.source(TestStates2.OPEN)
					.target(TestStates2.CLOSED)
					.event(TestEvents2.EJECT);
		}

	}

}
