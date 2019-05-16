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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.EventTrigger;

/**
 * Tests for states using a submachine.
 *
 * @author Janne Valkealahti
 *
 */
public class RegionStateTests extends AbstractStateMachineTests {

	@Test
	public void testSimpleRegionState() {
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

		Collection<Region<TestStates,TestEvents>> regions = new ArrayList<Region<TestStates,TestEvents>>();
		regions.add(machine);
		RegionState<TestStates,TestEvents> state = new RegionState<TestStates,TestEvents>(TestStates.S11, regions);

		assertThat(state.isSimple(), is(false));
		assertThat(state.isComposite(), is(true));
		assertThat(state.isOrthogonal(), is(false));
		assertThat(state.isSubmachineState(), is(false));

		assertThat(state.getIds(), containsInAnyOrder(TestStates.SI, TestStates.S11));



	}

}
