/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.recipes;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.recipes.tasks.TasksHandler;
import org.springframework.statemachine.recipes.tasks.TasksHandler.TasksListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class TasksHandlerTests {

	@Test
	public void testRunOnceSimpleNoFailures() throws InterruptedException {
		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("2", sleepRunnable())
				.task("3", sleepRunnable())
				.build();

		TestListener listener = new TestListener();
		listener.reset(10, 0, 0);
		StateMachine<String, String> machine = handler.getStateMachine();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(1, TimeUnit.SECONDS), is(true));

		handler.runTasks();

		assertThat(listener.stateChangedLatch.await(8, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(10));
		assertThat(machine.getState().getIds(), contains(TasksHandler.STATE_READY));
		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));
	}

	@Test
	public void testRunFailAndContinue() throws InterruptedException {
		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("2", sleepRunnable())
				.task("3", failRunnable())
				.build();

		TestListener listener = new TestListener();
		listener.reset(11, 0, 0);
		StateMachine<String, String> machine = handler.getStateMachine();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(1, TimeUnit.SECONDS), is(true));

		handler.runTasks();

		assertThat(listener.stateChangedLatch.await(8, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(11));
		assertThat(machine.getState().getIds(), contains(TasksHandler.STATE_ERROR, TasksHandler.STATE_AUTOMATIC));
		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));
	}

	@Test
	public void testDagSingleRoot() throws InterruptedException {
		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("1", "12", sleepRunnable())
				.task("1", "13", sleepRunnable())
				.build();

		TestListener listener = new TestListener();
		listener.reset(10, 0, 0);
		StateMachine<String, String> machine = handler.getStateMachine();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(1, TimeUnit.SECONDS), is(true));

		handler.runTasks();

		assertThat(listener.stateChangedLatch.await(12, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(10));
		assertThat(machine.getState().getIds(), contains(TasksHandler.STATE_READY));
		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));
	}

	@Test
	public void testDagMultiRoot() throws InterruptedException {
		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("1", "12", sleepRunnable())
				.task("1", "13", sleepRunnable())
				.task("2", sleepRunnable())
				.task("2", "22", sleepRunnable())
				.task("2", "23", sleepRunnable())
				.task("3", sleepRunnable())
				.task("3", "32", sleepRunnable())
				.task("3", "33", sleepRunnable())
				.build();

		TestListener listener = new TestListener();
		listener.reset(22, 0, 0);
		StateMachine<String, String> machine = handler.getStateMachine();

		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(1, TimeUnit.SECONDS), is(true));

		handler.runTasks();

		assertThat(listener.stateChangedLatch.await(20, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(22));
		assertThat(machine.getState().getIds(), contains(TasksHandler.STATE_READY));
		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(9));
	}

	@Test
	public void testEvents() throws InterruptedException {
		TestTasksListener tasksListener = new TestTasksListener();

		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("2", sleepRunnable())
				.task("3", sleepRunnable())
				.build();

		handler.addTasksListener(tasksListener);

		TestListener listener = new TestListener();
		listener.reset(10, 0, 0);
		StateMachine<String, String> machine = handler.getStateMachine();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(1, TimeUnit.SECONDS), is(true));

		handler.runTasks();

		assertThat(listener.stateChangedLatch.await(8, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(10));
		assertThat(machine.getState().getIds(), contains(TasksHandler.STATE_READY));
		Map<Object, Object> variables = machine.getExtendedState().getVariables();
		assertThat(variables.size(), is(3));

		assertThat(tasksListener.onTasksStartedLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(tasksListener.onTasksStarted, is(1));
	}

	private static Runnable sleepRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		};
	}

	private static Runnable failRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				throw new RuntimeException();
			}
		};
	}

	static class TestListener extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(2);
		volatile CountDownLatch stateExitedLatch = new CountDownLatch(0);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;
		volatile int transitionCount = 0;
		List<State<String, String>> statesEntered = new ArrayList<State<String, String>>();
		List<State<String, String>> statesExited = new ArrayList<State<String, String>>();

		@Override
		public void stateMachineStarted(StateMachine<String, String> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void stateEntered(State<String, String> state) {
			statesEntered.add(state);
			stateEnteredLatch.countDown();
		}

		@Override
		public void stateExited(State<String, String> state) {
			statesExited.add(state);
			stateExitedLatch.countDown();
		}

		@Override
		public void transitionEnded(Transition<String, String> transition) {
			transitionCount++;
			transitionLatch.countDown();
		}

		public void reset(int c1, int c2, int c3) {
			reset(c1, c2, c3, 0);
		}

		public void reset(int c1, int c2, int c3, int c4) {
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

	private class TestTasksListener implements TasksListener {

		volatile CountDownLatch onTasksStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch onTasksContinueLatch = new CountDownLatch(1);
		volatile CountDownLatch onTaskPreExecuteLatch = new CountDownLatch(1);
		volatile CountDownLatch onTaskPostExecuteLatch = new CountDownLatch(1);
		volatile CountDownLatch onTaskFailedLatch = new CountDownLatch(1);
		volatile CountDownLatch onTasksSuccessLatch = new CountDownLatch(1);
		volatile CountDownLatch onTasksErrorLatch = new CountDownLatch(1);

		volatile int onTasksStarted;

		@Override
		public void onTasksStarted() {
			onTasksStarted++;
			onTasksStartedLatch.countDown();
		}

		@Override
		public void onTasksContinue() {
			onTasksContinueLatch.countDown();
		}

		@Override
		public void onTaskPreExecute(Object id) {
			onTaskPreExecuteLatch.countDown();
		}

		@Override
		public void onTaskPostExecute(Object id) {
			onTaskPostExecuteLatch.countDown();
		}

		@Override
		public void onTaskFailed(Object id, Exception exception) {
			onTaskFailedLatch.countDown();
		}

		@Override
		public void onTasksSuccess() {
			onTasksSuccessLatch.countDown();
		}

		@Override
		public void onTasksError() {
			onTasksErrorLatch.countDown();
		}

		public void reset(int c1, int c2, int c3, int c4, int c5, int c6, int c7) {
			onTasksStartedLatch = new CountDownLatch(c1);
			onTasksContinueLatch = new CountDownLatch(c2);
			onTaskPreExecuteLatch = new CountDownLatch(c3);
			onTaskPostExecuteLatch = new CountDownLatch(c4);
			onTaskFailedLatch = new CountDownLatch(c5);
			onTasksSuccessLatch = new CountDownLatch(c6);
			onTasksErrorLatch = new CountDownLatch(c7);
			onTasksStarted = 0;
		}

	}

}
