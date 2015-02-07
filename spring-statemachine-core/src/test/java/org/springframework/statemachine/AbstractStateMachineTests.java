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

import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
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
		SI,S1,S2,S3,S4
	}

	public enum TestSubStates {
		SUBSI,SUBS1,SUBS2,SUBS3,SUBS4
	}
	
	public enum TestEvents {
		E1,E2,E3,E4
	}

	public enum TestSubEvents {
		SUBE1,SUBE2,SUBE3,SUBE4
	}
	
	@Configuration
	public static class BaseConfig {

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}
		
	}

	public static class TestEntryAction extends AbstractTestAction {
	}

	public static class TestExitAction extends AbstractTestAction {
	}

	public static class TestAction extends AbstractTestAction {
	}
	
	public static class TestGuard implements Guard {
		
		public CountDownLatch onEvaluateLatch = new CountDownLatch(1);
		
		boolean evaluationResult = true;

		public TestGuard() {
		}
		
		public TestGuard(boolean evaluationResult) {
			this.evaluationResult = evaluationResult;
		}

		@Override
		public boolean evaluate(StateContext context) {
			onEvaluateLatch.countDown();
			return evaluationResult;
		}
	}	

	protected static class AbstractTestAction implements Action {

		public CountDownLatch onExecuteLatch = new CountDownLatch(1);
		
		@Override
		public void execute(StateContext context) {
			onExecuteLatch.countDown();
		}
	}
	
}
