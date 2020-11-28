/*
 * Copyright 2016-2020 the original author or authors.
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
package org.springframework.statemachine.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineMessageHeaders;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class StateDoActivityActionTests extends AbstractStateMachineTests {

	@Test
	public void testSimpleStateActions() throws Exception {
		context.register(Config1.class);
		context.refresh();
		TestAction testActionS1 = context.getBean("testActionS1", TestAction.class);
		TestAction testActionS2 = context.getBean("testActionS2", TestAction.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		assertThat(testActionS1.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E1);

		assertThat(testActionS2.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E2);
	}

	@Test
	public void testExitAbortsAction() throws Exception {
		context.register(Config2.class);
		context.refresh();
		TestSleepAction testActionS1 = context.getBean("testActionS1", TestSleepAction.class);
		TestSleepAction testActionS2 = context.getBean("testActionS2", TestSleepAction.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		assertThat(testActionS1.onExecuteStartLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(testActionS1.interruptedLatch.await(6, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS1.onExecuteLatch.await(6, TimeUnit.SECONDS)).isTrue();

		assertThat(testActionS2.onExecuteStartLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(testActionS2.interruptedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S3);
	}

	@Test
	public void testInternalTransitionDoesNotAbort() throws Exception {
		context.register(Config3.class);
		context.refresh();
		TestSleepAction testActionS1 = context.getBean("testActionS1", TestSleepAction.class);
		TestSleepAction testActionS2 = context.getBean("testActionS2", TestSleepAction.class);
		TestAction testActionS1I = context.getBean("testActionS1I", TestAction.class);
		TestAction testActionS2I = context.getBean("testActionS2I", TestAction.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);
		doSendEventAndConsumeAll(machine, TestEvents.E3);
		assertThat(testActionS1I.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS1.interruptedLatch.await(2, TimeUnit.SECONDS)).isFalse();

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S2);

		doSendEventAndConsumeAll(machine, TestEvents.E4);
		assertThat(testActionS2I.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.interruptedLatch.await(2, TimeUnit.SECONDS)).isFalse();
		doSendEventAndConsumeAll(machine, TestEvents.E2);

		assertThat(testActionS1.interruptedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.interruptedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS1.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S3);
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1, testActionS1())
					.state(TestStates.S2, testActionS2())
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestAction testActionS1() {
			return new TestAction();
		}

		@Bean
		public TestAction testActionS2() {
			return new TestAction();
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
					.state(TestStates.S1, testActionS1())
					.state(TestStates.S2, testActionS2())
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestSleepAction testActionS1() {
			return new TestSleepAction(5000);
		}

		@Bean
		public TestSleepAction testActionS2() {
			return new TestSleepAction(5000);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1, testActionS1())
					.state(TestStates.S2, testActionS2())
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.and()
				.withInternal()
					.source(TestStates.S1)
					.event(TestEvents.E3)
					.action(testActionS1I())
					.and()
				.withInternal()
					.source(TestStates.S2)
					.event(TestEvents.E4)
					.action(testActionS2I());
		}

		@Bean
		public TestSleepAction testActionS1() {
			return new TestSleepAction(5000);
		}

		@Bean
		public TestSleepAction testActionS2() {
			return new TestSleepAction(5000);
		}

		@Bean
		public TestAction testActionS1I() {
			return new TestAction();
		}

		@Bean
		public TestAction testActionS2I() {
			return new TestAction();
		}
	}

	@Test
	public void testStateDoActionNotCancelledWithEventTimeout() throws Exception {
		context.register(Config4.class);
		context.refresh();
		TestSleepAction testActionS2 = context.getBean("testActionS2", TestSleepAction.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		Message<TestEvents> event = MessageBuilder.withPayload(TestEvents.E1)
				.setHeader(StateMachineMessageHeaders.HEADER_DO_ACTION_TIMEOUT, 4000).build();
		doSendEventAndConsumeAll(machine, event);
		assertThat(testActionS2.onExecuteStartLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(testActionS2.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.interruptedLatch.await(2, TimeUnit.SECONDS)).isFalse();
	}

	@Test
	public void testStateDoActionCancelledWithEventTimeout() throws Exception {
		context.register(Config4.class);
		context.refresh();
		TestSleepAction testActionS2 = context.getBean("testActionS2", TestSleepAction.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		Message<TestEvents> event = MessageBuilder.withPayload(TestEvents.E1)
				.setHeader(StateMachineMessageHeaders.HEADER_DO_ACTION_TIMEOUT, 100).build();
		doSendEventAndConsumeAll(machine, event);
		assertThat(testActionS2.onExecuteStartLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(testActionS2.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.interruptedLatch.await(2, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	public void testStateDoActionCancelledWithConfigSetting() throws Exception {
		context.register(Config5.class);
		context.refresh();
		TestSleepAction testActionS2 = context.getBean("testActionS2", TestSleepAction.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(testActionS2.onExecuteStartLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(testActionS2.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.interruptedLatch.await(2, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	public void testStateDoActionNotCancelledWithConfigTimeout() throws Exception {
		context.register(Config6.class);
		context.refresh();
		TestSleepAction testActionS2 = context.getBean("testActionS2", TestSleepAction.class);
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		doStartAndAssert(machine);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(testActionS2.onExecuteStartLatch.await(2, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(testActionS2.onExecuteLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(testActionS2.interruptedLatch.await(2, TimeUnit.SECONDS)).isFalse();
	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.stateDoActionPolicy(StateDoActionPolicy.TIMEOUT_CANCEL);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2, testActionS2())
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestSleepAction testActionS2() {
			return new TestSleepAction(2000);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config5 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.stateDoActionPolicy(StateDoActionPolicy.IMMEDIATE_CANCEL);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2, testActionS2())
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestSleepAction testActionS2() {
			return new TestSleepAction(2000);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config6 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.stateDoActionPolicy(StateDoActionPolicy.TIMEOUT_CANCEL)
					.stateDoActionPolicyTimeout(10, TimeUnit.SECONDS);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2, testActionS2())
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}

		@Bean
		public TestSleepAction testActionS2() {
			return new TestSleepAction(2000);
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
