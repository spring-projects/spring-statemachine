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
package org.springframework.statemachine;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultLocalTransition;
import org.springframework.statemachine.transition.Transition;

public class SubStateMachineTests extends AbstractStateMachineTests {

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
		PseudoState pseudoState = new DefaultPseudoState(PseudoStateKind.INITIAL);

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
		EnumStateMachine<TestStates, TestEvents> submachine11 = new EnumStateMachine<TestStates, TestEvents>(substates111, subtransitions111, stateS111, null);

		// submachine 1
		TestEntryAction entryActionS11 = new TestEntryAction("S11");
		TestExitAction exitActionS11 = new TestExitAction("S11");
		Collection<Action<TestStates, TestEvents>> entryActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS11.add(entryActionS11);
		Collection<Action<TestStates, TestEvents>> exitActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS11.add(exitActionS11);
		StateMachineState<TestStates,TestEvents> stateS11 = new StateMachineState<TestStates,TestEvents>(submachine11, null, entryActionsS11, exitActionsS11, pseudoState);

		Collection<State<TestStates,TestEvents>> substates11 = new ArrayList<State<TestStates,TestEvents>>();
		substates11.add(stateS11);
		Collection<Transition<TestStates,TestEvents>> subtransitions11 = new ArrayList<Transition<TestStates,TestEvents>>();
		EnumStateMachine<TestStates, TestEvents> submachine1 = new EnumStateMachine<TestStates, TestEvents>(substates11, subtransitions11, stateS11, null);

		// machine
		TestEntryAction entryActionS1 = new TestEntryAction("S1");
		TestExitAction exitActionS1 = new TestExitAction("S1");
		Collection<Action<TestStates, TestEvents>> entryActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS1.add(entryActionS1);
		Collection<Action<TestStates, TestEvents>> exitActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS1.add(exitActionS1);

		StateMachineState<TestStates,TestEvents> stateS1 = new StateMachineState<TestStates,TestEvents>(submachine1, null, entryActionsS1, exitActionsS1, pseudoState);
		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateS1);
		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();
		DefaultExternalTransition<TestStates,TestEvents> transitionFromS11ToS1 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS111, stateS1, null, TestEvents.E1, null);
		transitions.add(transitionFromS11ToS1);
		EnumStateMachine<TestStates, TestEvents> machine = new EnumStateMachine<TestStates, TestEvents>(states, transitions, stateS1, null);


		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		machine.start();
		submachine1.setTaskExecutor(taskExecutor);
		submachine11.setTaskExecutor(taskExecutor);

		machine.sendEvent(TestEvents.E1);

		assertThat(entryActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));

		assertThat(entryActionS11.stateContexts.size(), is(2));
		assertThat(exitActionS11.stateContexts.size(), is(1));
		assertThat(entryActionS11.stateContexts.size(), is(2));
		assertThat(exitActionS11.stateContexts.size(), is(1));
		assertThat(entryActionS1.stateContexts.size(), is(2));
		assertThat(exitActionS1.stateContexts.size(), is(1));
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
		PseudoState pseudoState = new DefaultPseudoState(PseudoStateKind.INITIAL);

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
		EnumStateMachine<TestStates, TestEvents> submachine11 = new EnumStateMachine<TestStates, TestEvents>(substates111, subtransitions111, stateS111, null);

		// submachine 1
		TestEntryAction entryActionS11 = new TestEntryAction("S11");
		TestExitAction exitActionS11 = new TestExitAction("S11");
		Collection<Action<TestStates, TestEvents>> entryActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS11.add(entryActionS11);
		Collection<Action<TestStates, TestEvents>> exitActionsS11 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS11.add(exitActionS11);
		StateMachineState<TestStates,TestEvents> stateS11 = new StateMachineState<TestStates,TestEvents>(submachine11, null, entryActionsS11, exitActionsS11, pseudoState);

		Collection<State<TestStates,TestEvents>> substates11 = new ArrayList<State<TestStates,TestEvents>>();
		substates11.add(stateS11);
		Collection<Transition<TestStates,TestEvents>> subtransitions11 = new ArrayList<Transition<TestStates,TestEvents>>();
		EnumStateMachine<TestStates, TestEvents> submachine1 = new EnumStateMachine<TestStates, TestEvents>(substates11, subtransitions11, stateS11, null);

		// machine
		TestEntryAction entryActionS1 = new TestEntryAction("S1");
		TestExitAction exitActionS1 = new TestExitAction("S1");
		Collection<Action<TestStates, TestEvents>> entryActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS1.add(entryActionS1);
		Collection<Action<TestStates, TestEvents>> exitActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS1.add(exitActionS1);

		StateMachineState<TestStates,TestEvents> stateS1 = new StateMachineState<TestStates,TestEvents>(submachine1, null, entryActionsS1, exitActionsS1, pseudoState);
		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateS1);
		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();
		DefaultLocalTransition<TestStates,TestEvents> transitionFromS11ToS1 =
				new DefaultLocalTransition<TestStates,TestEvents>(stateS111, stateS1, null, TestEvents.E1, null);
		transitions.add(transitionFromS11ToS1);
		EnumStateMachine<TestStates, TestEvents> machine = new EnumStateMachine<TestStates, TestEvents>(states, transitions, stateS1, null);


		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		machine.start();
		submachine1.setTaskExecutor(taskExecutor);
		submachine11.setTaskExecutor(taskExecutor);

		machine.sendEvent(TestEvents.E1);

		assertThat(entryActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS111.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS11.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(false));

		assertThat(entryActionS11.stateContexts.size(), is(2));
		assertThat(exitActionS11.stateContexts.size(), is(1));
		assertThat(entryActionS11.stateContexts.size(), is(2));
		assertThat(exitActionS11.stateContexts.size(), is(1));
		assertThat(entryActionS1.stateContexts.size(), is(1));
		assertThat(exitActionS1.stateContexts.size(), is(0));
	}

}
