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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests.TestEntryAction;
import org.springframework.statemachine.AbstractStateMachineTests.TestEvents;
import org.springframework.statemachine.AbstractStateMachineTests.TestExitAction;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.Transition;

/**
 * Statemachine tests using regions.
 *
 * @author Janne Valkealahti
 *
 */
public class RegionMachineTests {

	@Test
	public void testSimpleRegion() throws Exception {
		TestEntryAction entryActionS1 = new TestEntryAction("S1");
		TestExitAction exitActionS1 = new TestExitAction("S1");
		Collection<Action<TestStates, TestEvents>> entryActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		entryActionsS1.add(entryActionS1);
		Collection<Action<TestStates, TestEvents>> exitActionsS1 = new ArrayList<Action<TestStates, TestEvents>>();
		exitActionsS1.add(exitActionS1);


		State<TestStates,TestEvents> stateSI = new EnumState<TestStates,TestEvents>(TestStates.SI);
		State<TestStates,TestEvents> stateS1 = new EnumState<TestStates,TestEvents>(TestStates.S1, null, entryActionsS1, exitActionsS1);
		State<TestStates,TestEvents> stateS2 = new EnumState<TestStates,TestEvents>(TestStates.S2);
		State<TestStates,TestEvents> stateS3 = new EnumState<TestStates,TestEvents>(TestStates.S3);

		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateSI);
		states.add(stateS1);
		states.add(stateS2);
		states.add(stateS3);

		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();

		DefaultExternalTransition<TestStates,TestEvents> transitionFromSIToS1 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateSI, stateS1, null, TestEvents.E1, null);

		DefaultExternalTransition<TestStates,TestEvents> transitionFromS1ToS2 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS1, stateS2, null, TestEvents.E2, null);

		DefaultExternalTransition<TestStates,TestEvents> transitionFromS2ToS3 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS2, stateS3, null, TestEvents.E3, null);

		transitions.add(transitionFromSIToS1);
		transitions.add(transitionFromS1ToS2);
		transitions.add(transitionFromS2ToS3);

		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		EnumStateMachine<TestStates, TestEvents> machine = new EnumStateMachine<TestStates, TestEvents>(states, transitions, stateSI, null);
		machine.setTaskExecutor(taskExecutor);
		machine.start();

		Collection<Region<TestStates,TestEvents>> regions = new ArrayList<Region<TestStates,TestEvents>>();
		regions.add(machine);
		RegionState<TestStates,TestEvents> state = new RegionState<TestStates,TestEvents>(regions);

		assertThat(state.isSimple(), is(false));
		assertThat(state.isComposite(), is(true));
		assertThat(state.isOrthogonal(), is(false));
		assertThat(state.isSubmachineState(), is(false));

		assertThat(state.getIds(), contains(TestStates.SI));

		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);

		assertThat(entryActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(exitActionS1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(entryActionS1.stateContexts.size(), is(1));
		assertThat(exitActionS1.stateContexts.size(), is(1));
	}

}
