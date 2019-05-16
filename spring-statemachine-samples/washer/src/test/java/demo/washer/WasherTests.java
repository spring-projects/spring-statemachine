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
package demo.washer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import demo.washer.Application.Events;
import demo.washer.Application.States;

public class WasherTests {

	private AnnotationConfigApplicationContext context;

	private StateMachine<States, Events> machine;

	private TestListener listener;

	@Test
	public void testInitialState() throws Exception {
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.RUNNING, States.WASHING));
		assertThat(listener.statesEntered.size(), is(2));
		assertThat(listener.statesEntered.get(0).getId(), is(States.RUNNING));
		assertThat(listener.statesEntered.get(1).getId(), is(States.WASHING));
		assertThat(listener.statesExited.size(), is(0));
	}

	@Test
	public void testRinse() throws Exception {
		listener.reset(1, 0, 0);
		machine.sendEvent(Events.RINSE);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.RUNNING, States.RINSING));
	}

	@Test
	public void testRinseCutPower() throws Exception {
		listener.reset(1, 0, 0);
		machine.sendEvent(Events.RINSE);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);

		listener.reset(1, 0, 0);
		machine.sendEvent(Events.CUTPOWER);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.POWEROFF));
	}

	@Test
	public void testRinseCutRestorePower() throws Exception {
		listener.reset(1, 0, 0);
		machine.sendEvent(Events.RINSE);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);

		listener.reset(1, 0, 0);
		machine.sendEvent(Events.CUTPOWER);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);

		listener.reset(1, 0, 0);
		machine.sendEvent(Events.RESTOREPOWER);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.RUNNING, States.RINSING));
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
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(2);
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
	@Before
	public void setup() {
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, Config.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		listener = context.getBean(TestListener.class);
		machine.start();
	}

	@After
	public void clean() {
		machine.stop();
		context.close();
		context = null;
		machine = null;
	}

}
