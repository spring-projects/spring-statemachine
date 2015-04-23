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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

/**
 * Base class for stace machine tests.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class AbstractStateMachineTests {

	private final static Log log = LogFactory.getLog(AbstractStateMachineTests.class);

	protected AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		context = buildContext();
	}

	@After
	public void clean() {
		if (context != null) {
			context.close();
		}
	}

	protected AnnotationConfigApplicationContext buildContext() {
		return null;
	}

	public enum TestStates {
		SI,S1,S2,S3,S4,SF,
		S10,S11,S101,S111,S112,S12,S121,S122,S13,
		S20,S21,S201,S211,
		S1011,S1012,S2011,S2012,
		S30,S31,S32,S33
	}

	public enum TestEvents {
		E1,E2,E3,E4,EF
	}

	public static enum TestStates2 {
	    BUSY, PLAYING, PAUSED,
	    IDLE, CLOSED, OPEN
	}

	public static enum TestEvents2 {
	    PLAY, STOP, PAUSE, EJECT, LOAD
	}

	@Configuration
	public static class BaseConfig {

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

		@Bean
		public TaskScheduler taskScheduler() {
			return new ConcurrentTaskScheduler();
		}

	}

	public static class TestEntryAction extends AbstractTestAction {

		public TestEntryAction() {
			super();
		}

		public TestEntryAction(String message) {
			super(message);
		}

		@Override
		public String toString() {
			return "TestEntryAction [message=" + message + "]";
		}

	}

	public static class TestExitAction extends AbstractTestAction {

		public TestExitAction() {
			super();
		}

		public TestExitAction(String message) {
			super(message);
		}

		@Override
		public String toString() {
			return "TestExitAction [message=" + message + "]";
		}

	}

	public static class TestAction extends AbstractTestAction {
	}

	public static class TestGuard implements Guard<TestStates, TestEvents> {

		public CountDownLatch onEvaluateLatch = new CountDownLatch(1);
		boolean evaluationResult = true;

		public TestGuard() {
		}

		public TestGuard(boolean evaluationResult) {
			this.evaluationResult = evaluationResult;
		}

		@Override
		public boolean evaluate(StateContext<TestStates, TestEvents> context) {
			onEvaluateLatch.countDown();
			return evaluationResult;
		}
	}

	protected static class AbstractTestAction implements Action<TestStates, TestEvents> {

		protected String message = null;
		public CountDownLatch onExecuteLatch = new CountDownLatch(1);
		public List<StateContext<TestStates, TestEvents>> stateContexts = new ArrayList<StateContext<TestStates, TestEvents>>();

		public AbstractTestAction() {
		}

		public AbstractTestAction(String message) {
			this.message = message;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			if (message != null) {
				log.info(this);
			}
			onExecuteLatch.countDown();
			stateContexts.add(context);
		}
	}

}
