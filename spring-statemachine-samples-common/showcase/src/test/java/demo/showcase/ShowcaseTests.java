/*
 * Copyright 2015-2020 the original author or authors.
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
package demo.showcase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.doStopAndAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import demo.CommonConfiguration;
import demo.showcase.Application.Events;
import demo.showcase.Application.States;

public class ShowcaseTests {

	private AnnotationConfigApplicationContext context;

	private StateMachine<States,Events> machine;

	private TestListener listener;

	@Test
	public void testInitialState() throws Exception {
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S11);
		assertThat(listener.statesEntered).hasSize(3);
		assertThat(listener.statesEntered.get(0).getId()).isEqualTo(States.S0);
		assertThat(listener.statesEntered.get(1).getId()).isEqualTo(States.S1);
		assertThat(listener.statesEntered.get(2).getId()).isEqualTo(States.S11);
		assertThat(listener.statesExited).isEmpty();
	}

	@Test
	public void testA() throws Exception {
		testInitialState();
		listener.reset(1, 2, 2);
		doSendEventAndConsumeAll(machine, Events.A);
		// machine.sendEvent(Events.A);
		// variable foo is 0, guard denies transition
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(listener.stateExitedLatch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S11);
	}

	@Test
	public void testB() throws Exception {
		testInitialState();
		listener.reset(1, 2, 2);
		doSendEventAndConsumeAll(machine, Events.B);
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateExitedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.statesExited).hasSize(2);
		assertThat(listener.statesEntered).hasSize(2);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S11);
	}

	@Test
	public void testCHCA() throws Exception {
		testInitialState();
		listener.reset(3, 0, 0);
		doSendEventAndConsumeAll(machine, Events.C);
		doSendEventAndConsumeAll(machine, Events.H);
		doSendEventAndConsumeAll(machine, Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);

		listener.reset(1, 2, 2, 1);
		doSendEventAndConsumeAll(machine, Events.A);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		listener.stateExitedLatch.await(1, TimeUnit.SECONDS);
		listener.transitionLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S11);
		assertThat(listener.statesEntered).hasSize(2);
		assertThat(listener.statesEntered.get(0).getId()).isEqualTo(States.S1);
		assertThat(listener.statesEntered.get(1).getId()).isEqualTo(States.S11);
		assertThat(listener.statesExited).hasSize(2);
		assertThat(listener.statesExited.get(0).getId()).isEqualTo(States.S11);
		assertThat(listener.statesExited.get(1).getId()).isEqualTo(States.S1);
		assertThat(listener.transitionCount).isEqualTo(2);
	}

	@Test
	public void testC() throws Exception {
		testInitialState();
		listener.reset(1, 3, 0);
		doSendEventAndConsumeAll(machine, Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S2, States.S21, States.S211);
		assertThat(listener.statesEntered).hasSize(3);
		assertThat(listener.statesEntered.get(0).getId()).isEqualTo(States.S2);
		assertThat(listener.statesEntered.get(1).getId()).isEqualTo(States.S21);
		assertThat(listener.statesEntered.get(2).getId()).isEqualTo(States.S211);
	}

	@Test
	public void testCC() throws Exception {
		testInitialState();
		listener.reset(1, 3, 0);
		doSendEventAndConsumeAll(machine, Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S2, States.S21, States.S211);
		listener.reset(1, 2, 0);
		doSendEventAndConsumeAll(machine, Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S11);
		assertThat(listener.statesEntered).hasSize(2);
		assertThat(listener.statesEntered.get(0).getId()).isEqualTo(States.S1);
		assertThat(listener.statesEntered.get(1).getId()).isEqualTo(States.S11);
	}

	@Test
	public void testD() throws Exception {
		testInitialState();
		listener.reset(3, 3, 0);
		doSendEventAndConsumeAll(machine, Events.D);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S11);
		assertThat(listener.statesEntered).hasSize(3);
		assertThat(listener.statesEntered.get(0).getId()).isEqualTo(States.S0);
		assertThat(listener.statesEntered.get(1).getId()).isEqualTo(States.S1);
		assertThat(listener.statesEntered.get(2).getId()).isEqualTo(States.S11);
		assertThat(listener.statesExited).hasSize(3);
	}

	@Test
	public void testCD() throws Exception {
		testInitialState();
		listener.reset(1, 3, 0);
		doSendEventAndConsumeAll(machine, Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.reset(1, 2, 0);
		doSendEventAndConsumeAll(machine, Events.D);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S2, States.S21, States.S211);
		assertThat(listener.statesEntered).hasSize(2);
		assertThat(listener.statesEntered.get(0).getId()).isEqualTo(States.S21);
		assertThat(listener.statesEntered.get(1).getId()).isEqualTo(States.S211);
	}

	@Test
	public void testI() throws Exception {
		testInitialState();
		listener.reset(1, 1, 1);
		doSendEventAndConsumeAll(machine, Events.I);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		listener.stateExitedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S12);
		assertThat(listener.statesEntered).hasSize(1);
		assertThat(listener.statesEntered.get(0).getId()).isEqualTo(States.S12);
		assertThat(listener.statesExited).hasSize(1);
		assertThat(listener.statesExited.get(0).getId()).isEqualTo(States.S11);
	}

	@Test
	public void testII() throws Exception {
		testInitialState();
		listener.reset(1, 1, 1);
		doSendEventAndConsumeAll(machine, Events.I);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateExitedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.statesEntered).hasSize(1);
		assertThat(listener.statesExited).hasSize(1);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S1, States.S12);

		listener.reset(1, 3, 2);
		doSendEventAndConsumeAll(machine, Events.I);
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateExitedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.statesEntered).hasSize(3);
		assertThat(listener.statesExited).hasSize(2);
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S2, States.S21, States.S212);
	}

	@Test
	public void testH() throws Exception {
		testInitialState();
		listener.reset(0, 0, 0, 1);
		doSendEventAndConsumeAll(machine, Events.H);
		listener.transitionLatch.await(1, TimeUnit.SECONDS);
		assertThat(listener.transitionCount).isEqualTo(1);
		assertThat(listener.transitions.get(0).getSource().getId()).isEqualTo(States.S1);
	}

	@Test
	public void testCH() throws Exception {
		testInitialState();
		doSendEventAndConsumeAll(machine, Events.C);
		listener.reset(0, 0, 0, 1);
		doSendEventAndConsumeAll(machine, Events.H);
		listener.transitionLatch.await(1, TimeUnit.SECONDS);
		assertThat(listener.transitionCount).isEqualTo(1);
		assertThat(listener.transitions.get(0).getSource().getId()).isEqualTo(States.S0);
	}

	@Test
	public void testACH() throws Exception {
		testInitialState();
		doSendEventAndConsumeAll(machine, Events.A);
		doSendEventAndConsumeAll(machine, Events.C);
		listener.reset(0, 0, 0, 1);
		doSendEventAndConsumeAll(machine, Events.H);
		doSendEventAndConsumeAll(machine, Events.A);
		listener.transitionLatch.await(1, TimeUnit.SECONDS);
		assertThat(listener.transitionCount).isEqualTo(1);
		assertThat(listener.transitions.get(0).getSource().getId()).isEqualTo(States.S0);
	}

	@Test
	public void testE() throws Exception {
		testInitialState();
		listener.reset(1, 4, 3, 0);
		doSendEventAndConsumeAll(machine, Events.E);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateExitedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S2, States.S21, States.S211);
		assertThat(listener.statesExited).hasSize(3);
		assertThat(listener.statesEntered).hasSize(4);
	}

	@Test
	public void testF() throws Exception {
		testInitialState();
		listener.reset(1, 3, 2, 0);
		doSendEventAndConsumeAll(machine, Events.F);
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateExitedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S2, States.S21, States.S211);
		assertThat(listener.statesExited).hasSize(2);
		assertThat(listener.statesEntered).hasSize(3);
	}

	@Test
	public void testG() throws Exception {
		testInitialState();
		listener.reset(1, 3, 2, 0);
		doSendEventAndConsumeAll(machine, Events.G);
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateEnteredLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateExitedLatch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsExactly(States.S0, States.S2, States.S21, States.S211);
		assertThat(listener.statesExited).hasSize(2);
		assertThat(listener.statesEntered).hasSize(3);
	}

	static class Config {

		@Autowired
		private StateMachine<States,Events> machine;

		@Bean
		public StateMachineListener<States, Events> stateMachineListener() {
			TestListener listener = new TestListener();
			machine.addStateListener(listener);
			return listener;
		}
	}

	static class TestListener extends StateMachineListenerAdapter<States, Events> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(3);
		volatile CountDownLatch stateExitedLatch = new CountDownLatch(0);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile List<Transition<States, Events>> transitions = new ArrayList<Transition<States,Events>>();
		List<State<States, Events>> statesEntered = new ArrayList<State<States,Events>>();
		List<State<States, Events>> statesExited = new ArrayList<State<States,Events>>();
		volatile int transitionCount = 0;

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			stateChangedLatch.countDown();
		}

		@Override
		public void stateEntered(State<States, Events> state) {
			statesEntered.add(state);
			stateEnteredLatch.countDown();
		}

		@Override
		public void stateExited(State<States, Events> state) {
			statesExited.add(state);
			stateExitedLatch.countDown();
		}

		@Override
		public void transition(Transition<States, Events> transition) {
			transitions.add(transition);
			transitionLatch.countDown();
			transitionCount++;
		}

		public void reset(int c1, int c2, int c3) {
			reset(c1, c2, c3, 0);
		}

		public void reset(int c1, int c2, int c3, int c4) {
			stateChangedLatch = new CountDownLatch(c1);
			stateEnteredLatch = new CountDownLatch(c2);
			stateExitedLatch = new CountDownLatch(c3);
			transitionLatch = new CountDownLatch(c4);
			statesEntered.clear();
			statesExited.clear();
			transitionCount = 0;
			transitions.clear();
		}

	}

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void setup() {
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, Config.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		listener = context.getBean(TestListener.class);
		doStartAndAssert(machine);
	}

	@AfterEach
	public void clean() {
		doStopAndAssert(machine);
		context.close();
		context = null;
		machine = null;
	}

}
