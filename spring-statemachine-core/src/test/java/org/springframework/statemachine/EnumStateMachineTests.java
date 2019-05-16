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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultInternalTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.EventTrigger;

public class EnumStateMachineTests extends AbstractStateMachineTests {

	@Test
	public void testSimpleStateSwitch() {
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

		Collection<Action<TestStates,TestEvents>> actionsFromSIToS1 = new ArrayList<Action<TestStates,TestEvents>>();
		actionsFromSIToS1.add(new LoggingAction("actionsFromSIToS1"));
		DefaultExternalTransition<TestStates,TestEvents> transitionFromSIToS1 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateSI, stateS1, actionsFromSIToS1, TestEvents.E1, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E1));

		Collection<Action<TestStates,TestEvents>> actionsFromS1ToS2 = new ArrayList<Action<TestStates,TestEvents>>();
		actionsFromS1ToS2.add(new LoggingAction("actionsFromS1ToS2"));
		DefaultExternalTransition<TestStates,TestEvents> transitionFromS1ToS2 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS1, stateS2, actionsFromS1ToS2, TestEvents.E2, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E2));

		Collection<Action<TestStates,TestEvents>> actionsFromS2ToS3 = new ArrayList<Action<TestStates,TestEvents>>();
		actionsFromS1ToS2.add(new LoggingAction("actionsFromS2ToS3"));
		DefaultExternalTransition<TestStates,TestEvents> transitionFromS2ToS3 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS2, stateS3, actionsFromS2ToS3, TestEvents.E3, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E3));

		transitions.add(transitionFromSIToS1);
		transitions.add(transitionFromS1ToS2);
		transitions.add(transitionFromS2ToS3);

		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		ObjectStateMachine<TestStates, TestEvents> machine = new ObjectStateMachine<TestStates, TestEvents>(states, transitions, stateSI);
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		machine.start();

		State<TestStates,TestEvents> initialState = machine.getInitialState();
		assertThat(initialState, is(stateSI));

		State<TestStates,TestEvents> state = machine.getState();
		assertThat(state, is(stateSI));

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		state = machine.getState();
		assertThat(state, is(stateS1));

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());
		state = machine.getState();
		assertThat(state, is(stateS2));

		// not processed
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		state = machine.getState();
		assertThat(state, is(stateS2));

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E3).build());
		state = machine.getState();
		assertThat(state, is(stateS3));
	}

	@Test
	public void testDeferredEvents() {
		PseudoState<TestStates,TestEvents> pseudoState = new DefaultPseudoState<TestStates,TestEvents>(PseudoStateKind.INITIAL);

		Collection<TestEvents> deferred = new ArrayList<TestEvents>();
		deferred.add(TestEvents.E2);
		deferred.add(TestEvents.E3);

		// states
		State<TestStates,TestEvents> stateSI = new EnumState<TestStates,TestEvents>(TestStates.SI, deferred, null, null, pseudoState);
		State<TestStates,TestEvents> stateS1 = new EnumState<TestStates,TestEvents>(TestStates.S1);
		State<TestStates,TestEvents> stateS2 = new EnumState<TestStates,TestEvents>(TestStates.S2);
		State<TestStates,TestEvents> stateS3 = new EnumState<TestStates,TestEvents>(TestStates.S3);

		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateSI);
		states.add(stateS1);
		states.add(stateS2);
		states.add(stateS3);

		// transitions
		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();

		Collection<Action<TestStates,TestEvents>> actionsFromSIToS1 = new ArrayList<Action<TestStates,TestEvents>>();
		actionsFromSIToS1.add(new LoggingAction("actionsFromSIToS1"));
		DefaultExternalTransition<TestStates,TestEvents> transitionFromSIToS1 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateSI, stateS1, actionsFromSIToS1, TestEvents.E1, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E1));

		Collection<Action<TestStates,TestEvents>> actionsFromS1ToS2 = new ArrayList<Action<TestStates,TestEvents>>();
		actionsFromS1ToS2.add(new LoggingAction("actionsFromS1ToS2"));
		DefaultExternalTransition<TestStates,TestEvents> transitionFromS1ToS2 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS1, stateS2, actionsFromS1ToS2, TestEvents.E2, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E2));

		Collection<Action<TestStates,TestEvents>> actionsFromS2ToS3 = new ArrayList<Action<TestStates,TestEvents>>();
		actionsFromS1ToS2.add(new LoggingAction("actionsFromS2ToS3"));
		DefaultExternalTransition<TestStates,TestEvents> transitionFromS2ToS3 =
				new DefaultExternalTransition<TestStates,TestEvents>(stateS2, stateS3, actionsFromS2ToS3, TestEvents.E3, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E3));

		transitions.add(transitionFromSIToS1);
		transitions.add(transitionFromS1ToS2);
		transitions.add(transitionFromS2ToS3);

		// create machine
		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		ObjectStateMachine<TestStates, TestEvents> machine = new ObjectStateMachine<TestStates, TestEvents>(states, transitions, stateSI);
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		machine.start();

		State<TestStates,TestEvents> initialState = machine.getInitialState();
		assertThat(initialState, is(stateSI));

		State<TestStates,TestEvents> state = machine.getState();
		assertThat(state, is(stateSI));


		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E3).build());
		state = machine.getState();
		assertThat(state, is(stateSI));


		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		state = machine.getState();
		assertThat(state, is(stateS3));
	}

	@Test
	public void testInternalTransitions() {
		PseudoState<TestStates,TestEvents> pseudoState = new DefaultPseudoState<TestStates,TestEvents>(PseudoStateKind.INITIAL);
		State<TestStates,TestEvents> stateSI = new EnumState<TestStates,TestEvents>(TestStates.SI, pseudoState);

		Collection<State<TestStates,TestEvents>> states = new ArrayList<State<TestStates,TestEvents>>();
		states.add(stateSI);

		Collection<Action<TestStates,TestEvents>> actionsInSI = new ArrayList<Action<TestStates,TestEvents>>();
		actionsInSI.add(new LoggingAction("actionsInSI"));
		DefaultInternalTransition<TestStates,TestEvents> transitionInternalSI =
				new DefaultInternalTransition<TestStates,TestEvents>(stateSI, actionsInSI, TestEvents.E1, null, new EventTrigger<TestStates,TestEvents>(TestEvents.E1));

		// transitions
		Collection<Transition<TestStates,TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();
		transitions.add(transitionInternalSI);

		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		ObjectStateMachine<TestStates, TestEvents> machine = new ObjectStateMachine<TestStates, TestEvents>(states, transitions, stateSI);
		machine.setTaskExecutor(taskExecutor);
		machine.afterPropertiesSet();
		machine.start();

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
	}

	private static class LoggingAction implements Action<TestStates, TestEvents> {

		private static final Log log = LogFactory.getLog(LoggingAction.class);

		private String message;

		public LoggingAction(String message) {
			this.message = message;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			log.info("Hello from LoggingAction " + message);
		}

	}

}
