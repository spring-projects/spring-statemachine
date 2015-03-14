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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class StateMachineTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testLoggingEvents() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		EnumStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "jee1").build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).setHeader("foo", "jee2").build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E4).setHeader("foo", "jee2").build());
	}

	@Test
	public void testTimerTransition() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();

		TestAction testAction1 = context.getBean("testAction1", TestAction.class);
		TestAction testAction2 = context.getBean("testAction2", TestAction.class);
		TestAction testAction3 = context.getBean("testAction3", TestAction.class);
		TestAction testAction4 = context.getBean("testAction4", TestAction.class);

		@SuppressWarnings("unchecked")
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		machine.start();
		Thread.sleep(2000);
		assertThat(testAction2.stateContexts.size(), is(0));
		machine.sendEvent(TestEvents.E1);
		assertThat(testAction1.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(testAction1.stateContexts.size(), is(1));
		assertThat(testAction2.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));

		assertThat(testAction2.stateContexts.size(), is(1));

		machine.sendEvent(TestEvents.E2);
		assertThat(testAction3.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(testAction3.stateContexts.size(), is(1));

		// timer still fires but should not cause transition anymore
		// after we sleep and do next event
		int timedTriggered = testAction2.stateContexts.size();
		Thread.sleep(2000);
		assertThat(testAction2.stateContexts.size(), is(timedTriggered));

		machine.sendEvent(TestEvents.E3);
		assertThat(testAction4.onExecuteLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(testAction4.stateContexts.size(), is(1));

		assertThat(testAction2.stateContexts.size(), is(timedTriggered));
	}

	private static class LoggingAction implements Action<TestStates, TestEvents> {

		private static final Log log = LogFactory.getLog(StateMachineTests.LoggingAction.class);

		private String message;

		public LoggingAction(String message) {
			this.message = message;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			log.info("Hello from LoggingAction " + message + " foo=" + context.getMessageHeaders().get("foo"));
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3, TestEvents.E4)
					.state(TestStates.S4);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.action(loggingAction())
					.action(loggingAction())
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.action(loggingAction())
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.action(loggingAction())
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.S3)
					.event(TestEvents.E4)
					.action(loggingAction());
		}

		@Bean
		public LoggingAction loggingAction() {
			return new LoggingAction("as bean");
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3)
					.state(TestStates.S4);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.action(testAction1())
					.and()
				.withInternal()
					.source(TestStates.S2)
					.timer(1000)
					.action(testAction2())
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.action(testAction3())
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.action(testAction4());
		}

		@Bean
		public TestAction testAction1() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction2() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction3() {
			return new TestAction();
		}

		@Bean
		public TestAction testAction4() {
			return new TestAction();
		}

	}

}
