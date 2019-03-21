/*
 * Copyright 2017-2018 the original author or authors.
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
package org.springframework.statemachine.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.springframework.statemachine.AbstractStateMachineTests.TestEvents;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionConflictPolicy;
import org.springframework.statemachine.trigger.EventTrigger;

/**
 * Tests for {@link TransitionComparator}.
 *
 * @author Janne Valkealahti
 *
 */
public class TransitionComparatorTests {

	@Test
	public void testCompareWithParentAndChild() {
		PseudoState<TestStates, TestEvents> pseudoState = new DefaultPseudoState<TestStates, TestEvents>(PseudoStateKind.INITIAL);

		State<TestStates, TestEvents> stateS111 = new EnumState<TestStates, TestEvents>(TestStates.S111, null, null, null, pseudoState);

		// submachine 11
		Collection<State<TestStates, TestEvents>> substates111 = new ArrayList<State<TestStates, TestEvents>>();
		substates111.add(stateS111);
		Collection<Transition<TestStates, TestEvents>> subtransitions111 = new ArrayList<Transition<TestStates, TestEvents>>();
		ObjectStateMachine<TestStates, TestEvents> submachine11 = new ObjectStateMachine<TestStates, TestEvents>(substates111,
				subtransitions111, stateS111);

		// submachine 1
		StateMachineState<TestStates, TestEvents> stateS11 = new StateMachineState<TestStates, TestEvents>(TestStates.S11, submachine11,
				null, null, null, pseudoState);

		Collection<State<TestStates, TestEvents>> substates11 = new ArrayList<State<TestStates, TestEvents>>();
		substates11.add(stateS11);
		Collection<Transition<TestStates, TestEvents>> subtransitions11 = new ArrayList<Transition<TestStates, TestEvents>>();
		ObjectStateMachine<TestStates, TestEvents> submachine1 = new ObjectStateMachine<TestStates, TestEvents>(substates11,
				subtransitions11, stateS11);

		// machine
		StateMachineState<TestStates, TestEvents> stateS1 = new StateMachineState<TestStates, TestEvents>(TestStates.S1, submachine1, null,
				null, null, pseudoState);

		DefaultExternalTransition<TestStates, TestEvents> transitionFromS111ToS1 = new DefaultExternalTransition<TestStates, TestEvents>(
				stateS111, stateS1, null, TestEvents.E1, null, new EventTrigger<TestStates, TestEvents>(TestEvents.E1));
		DefaultExternalTransition<TestStates, TestEvents> transitionFromS11ToS1 = new DefaultExternalTransition<TestStates, TestEvents>(
				stateS11, stateS1, null, TestEvents.E1, null, new EventTrigger<TestStates, TestEvents>(TestEvents.E1));

		TransitionComparator<TestStates, TestEvents> comparator = new TransitionComparator<>(null);
		assertThat(comparator.compare(transitionFromS111ToS1, transitionFromS11ToS1), is(-1));
		assertThat(comparator.compare(transitionFromS11ToS1, transitionFromS111ToS1), is(1));
		assertThat(comparator.compare(transitionFromS111ToS1, transitionFromS111ToS1), is(0));
		assertThat(comparator.compare(transitionFromS11ToS1, transitionFromS11ToS1), is(0));

		comparator = new TransitionComparator<>(TransitionConflictPolicy.CHILD);
		assertThat(comparator.compare(transitionFromS111ToS1, transitionFromS11ToS1), is(-1));
		assertThat(comparator.compare(transitionFromS11ToS1, transitionFromS111ToS1), is(1));
		assertThat(comparator.compare(transitionFromS111ToS1, transitionFromS111ToS1), is(0));
		assertThat(comparator.compare(transitionFromS11ToS1, transitionFromS11ToS1), is(0));

		comparator = new TransitionComparator<>(TransitionConflictPolicy.PARENT);
		assertThat(comparator.compare(transitionFromS111ToS1, transitionFromS11ToS1), is(1));
		assertThat(comparator.compare(transitionFromS11ToS1, transitionFromS111ToS1), is(-1));
		assertThat(comparator.compare(transitionFromS111ToS1, transitionFromS111ToS1), is(0));
		assertThat(comparator.compare(transitionFromS11ToS1, transitionFromS11ToS1), is(0));
	}
}
