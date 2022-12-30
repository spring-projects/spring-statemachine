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
package org.springframework.statemachine.recipes.docs;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.recipes.tasks.TasksHandler;
import org.springframework.statemachine.recipes.tasks.TasksHandler.TasksListenerAdapter;

public class DocsTasksSampleTests {

	public void sample1() {
// tag::snippetB[]
		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("2", sleepRunnable())
				.task("3", sleepRunnable())
				.build();

		handler.runTasks();
// end::snippetB[]
	}

	public void sample2() {
// tag::snippetC[]
		MyTasksListener listener1 = new MyTasksListener();
		MyTasksListener listener2 = new MyTasksListener();

		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("2", sleepRunnable())
				.task("3", sleepRunnable())
				.listener(listener1)
				.build();

		handler.addTasksListener(listener2);
		handler.removeTasksListener(listener2);

		handler.runTasks();
// end::snippetC[]
	}

	public void sample3() {
// tag::snippetD[]
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

		handler.runTasks();
// end::snippetD[]
	}

	public void sample4() {
// tag::snippetE[]
		TasksHandler handler = TasksHandler.builder()
				.task("1", sleepRunnable())
				.task("2", sleepRunnable())
				.task("3", sleepRunnable())
				.build();

				handler.runTasks();
				handler.fixCurrentProblems();
				handler.continueFromError();
// end::snippetE[]
	}

// tag::snippetAA[]
	private Runnable sleepRunnable() {
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
// end::snippetAA[]

// tag::snippetAB[]
	private class MyTasksListener extends TasksListenerAdapter {

		@Override
		public void onTasksStarted() {
		}

		@Override
		public void onTasksContinue() {
		}

		@Override
		public void onTaskPreExecute(Object id) {
		}

		@Override
		public void onTaskPostExecute(Object id) {
		}

		@Override
		public void onTaskFailed(Object id, Exception exception) {
		}

		@Override
		public void onTaskSuccess(Object id) {
		}

		@Override
		public void onTasksSuccess() {
		}

		@Override
		public void onTasksError() {
		}

		@Override
		public void onTasksAutomaticFix(TasksHandler handler, StateContext<String, String> context) {
		}
	}
// end::snippetAB[]

}
