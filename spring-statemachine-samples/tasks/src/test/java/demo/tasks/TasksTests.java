/*
 * Copyright 2015-2018 the original author or authors.
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
package demo.tasks;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.util.StringUtils;

import demo.CommonConfiguration;
import demo.tasks.Application.Events;
import demo.tasks.Application.States;

public class TasksTests {

	private final static Log log = LogFactory.getLog(TasksTests.class);

	private AnnotationConfigApplicationContext context;

	private StateMachine<States,Events> machine;

	private Tasks tasks;

	private TestListener listener;

	@Test
	public void testInitialState() throws InterruptedException {
		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(0));
	}

	@Test
	public void testRunOnce() throws InterruptedException {
		listener.reset(8, 8, 0);
		tasks.run();
		assertThat(listener.stateEnteredLatch.await(8, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));
		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));
	}

	@Test
	public void testRunTwice() throws InterruptedException {
		listener.reset(8, 8, 0);
		tasks.run();
		assertThat(listener.stateEnteredLatch.await(8, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));

		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));

		listener.reset(8, 8, 0);
		tasks.run();
		assertThat(listener.stateEnteredLatch.await(8, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));

		variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));
	}

	@Test
	public void testRunSmoke() throws InterruptedException {
		for (int i = 0; i < 20; i++) {
			log.info("testRunSmoke SMOKE START " + i);
			listener.reset(8, 8, 0);
			tasks.run();

			boolean await = listener.stateEnteredLatch.await(8, TimeUnit.SECONDS);
			String reason = "Machine was " + machine + " " + StringUtils.collectionToCommaDelimitedString(listener.statesEntered);
			assertThat(reason , await, is(true));
			assertThat(machine.getState().getIds(), contains(States.READY));
			log.info("testRunSmoke SMOKE STOP " + i);
		}
	}

	@Test
	public void testFailAutomaticFix() throws InterruptedException {
		listener.reset(10, 0, 0);
		tasks.fail("T1");
		tasks.run();
		assertThat(listener.stateChangedLatch.await(6, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(10));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@Test
	public void testFailManualFix() throws InterruptedException {
		listener.reset(10, 0, 0);
		tasks.fail("T2");
		tasks.run();
		assertThat(listener.stateChangedLatch.await(6, TimeUnit.SECONDS), is(true));

		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));

		assertThat(machine.getState().getIds(), contains(States.ERROR, States.MANUAL));
		listener.reset(1, 0, 0);
		tasks.fix();
		assertThat(listener.stateChangedLatch.await(6, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, TestConfig.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		tasks = context.getBean(Tasks.class);
		listener = context.getBean(TestListener.class);
		machine.start();
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@After
	public void clean() {
		machine.stop();
		context.close();
		context = null;
		machine = null;
		tasks = null;
		listener = null;
	}

	static class TestConfig {

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

		final Object lock = new Object();

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(2);
		volatile CountDownLatch stateExitedLatch = new CountDownLatch(0);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;
		volatile int transitionCount = 0;
		List<State<States, Events>> statesEntered = new ArrayList<State<States,Events>>();
		List<State<States, Events>> statesExited = new ArrayList<State<States,Events>>();

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			synchronized (lock) {
				stateChangedCount++;
				stateChangedLatch.countDown();
			}
		}

		@Override
		public void stateEntered(State<States, Events> state) {
			synchronized (lock) {
				statesEntered.add(state);
				stateEnteredLatch.countDown();
			}
		}

		@Override
		public void stateExited(State<States, Events> state) {
			synchronized (lock) {
				statesExited.add(state);
				stateExitedLatch.countDown();
			}
		}

		@Override
		public void transitionEnded(Transition<States, Events> transition) {
			synchronized (lock) {
				transitionCount++;
				transitionLatch.countDown();
			}
		}

		public void reset(int c1, int c2, int c3) {
			reset(c1, c2, c3, 0);
		}

		public void reset(int c1, int c2, int c3, int c4) {
			synchronized (lock) {
				stateChangedLatch = new CountDownLatch(c1);
				stateEnteredLatch = new CountDownLatch(c2);
				stateExitedLatch = new CountDownLatch(c3);
				transitionLatch = new CountDownLatch(c4);
				stateChangedCount = 0;
				transitionCount = 0;
				statesEntered.clear();
				statesExited.clear();
			}
		}

	}

}
