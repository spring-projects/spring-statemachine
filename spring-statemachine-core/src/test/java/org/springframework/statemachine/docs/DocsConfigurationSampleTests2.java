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
package org.springframework.statemachine.docs;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public class DocsConfigurationSampleTests2 extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withInternal()
					.source("S2")
					.action(timerAction())
					.timer(1000);
		}

		@Bean
		public TimerAction timerAction() {
			return new TimerAction();
		}
	}

	public class TimerAction implements Action<String, String> {

		@Override
		public void execute(StateContext<String, String> context) {
			// do something in every 1 sec
		}
	}
// end::snippetA[]

// tag::snippetB[]
	@Configuration
	public class Config3 {

		@Bean
		@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(true)
					.taskExecutor(new SyncTaskExecutor());
			builder.configureStates()
				.withStates()
					.initial("S1")
					.state("S2");
			builder.configureTransitions()
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
			StateMachine<String, String> stateMachine = builder.build();
			return stateMachine;
		}

	}
// end::snippetB[]

// tag::snippetC[]
	@Configuration
	@EnableStateMachine
	@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
	public static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
		}

	}
// end::snippetC[]

// tag::snippetD[]
	@Controller
	public class StateMachineController {

		@Autowired
		StateMachine<String, String> stateMachine;

		@RequestMapping(path="/state", method=RequestMethod.POST)
		public HttpEntity<Void> setState(@RequestParam("event") String event) {
			stateMachine.sendEvent(event);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		}

		@RequestMapping(path="/state", method=RequestMethod.GET)
		@ResponseBody
		public String getState() {
			return stateMachine.getState().getId();
		}
	}
// end::snippetD[]

	@Test
	public void testConfig51() throws Exception {
		context.register(Config5.class, ExecutorConfig.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(1));
		listener.reset(0, 0, 2);
		machine.sendEvent("DEPLOY");
		machine.sendEvent("DEPLOY");
		assertThat(listener.readyStateEnteredLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(2));
	}

	@Test
	public void testConfig52() throws Exception {
		context.register(Config5.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(1));
		listener.reset(0, 0, 2);
		machine.sendEvent("DEPLOY");
		machine.sendEvent("DEPLOY");
		assertThat(listener.readyStateEnteredLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(2));
	}

	@Test
	public void testConfig61() throws Exception {
		context.register(Config6.class, ExecutorConfig.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(1));
		listener.reset(0, 0, 2);
		machine.sendEvent("DEPLOY");
		machine.sendEvent("DEPLOY");
		assertThat(listener.readyStateEnteredLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(2));
	}

	@Test
	public void testConfig62() throws Exception {
		context.register(Config6.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(1));
		listener.reset(0, 0, 2);
		machine.sendEvent("DEPLOY");
		machine.sendEvent("DEPLOY");
		assertThat(listener.readyStateEnteredLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.readyStateEnteredCount, is(2));
	}

	@Configuration
	static class ExecutorConfig {

		@Bean(name=StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
		public TaskExecutor taskExecutor() {
			ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
			taskExecutor.setCorePoolSize(1);
			return taskExecutor;
		}
	}

// tag::snippetE[]
	@Configuration
	@EnableStateMachine
	static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("READY")
					.state("DEPLOYPREPARE", "DEPLOY")
					.state("DEPLOYEXECUTE", "DEPLOY");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("READY").target("DEPLOYPREPARE")
					.event("DEPLOY")
					.and()
				.withExternal()
					.source("DEPLOYPREPARE").target("DEPLOYEXECUTE")
					.and()
				.withExternal()
					.source("DEPLOYEXECUTE").target("READY");
		}
	}
// end::snippetE[]

// tag::snippetF[]
	@Configuration
	@EnableStateMachine
	static class Config6 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("READY")
					.state("DEPLOY", "DEPLOY")
					.state("DONE")
					.and()
					.withStates()
						.parent("DEPLOY")
						.initial("DEPLOYPREPARE")
						.state("DEPLOYPREPARE", "DONE")
						.state("DEPLOYEXECUTE");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("READY").target("DEPLOY")
					.event("DEPLOY")
					.and()
				.withExternal()
					.source("DEPLOYPREPARE").target("DEPLOYEXECUTE")
					.and()
				.withExternal()
					.source("DEPLOYEXECUTE").target("READY")
					.and()
				.withExternal()
					.source("READY").target("DONE")
					.event("DONE")
					.and()
				.withExternal()
					.source("DEPLOY").target("DONE")
					.event("DONE");
		}
	}
// end::snippetF[]

	static class TestListener extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch readyStateEnteredLatch = new CountDownLatch(1);
		volatile int readyStateEnteredCount = 0;

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedLatch.countDown();
		}

		@Override
		public void stateEntered(State<String, String> state) {
			if (state.getId().equals("READY")) {
				readyStateEnteredCount++;
				readyStateEnteredLatch.countDown();
			}
		}

		@Override
		public void stateMachineStarted(StateMachine<String, String> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		public void reset(int c1, int c2, int c3) {
			stateChangedLatch = new CountDownLatch(c1);
			stateMachineStartedLatch = new CountDownLatch(c2);
			readyStateEnteredLatch = new CountDownLatch(c3);
			readyStateEnteredCount = 0;
		}

	}

}
