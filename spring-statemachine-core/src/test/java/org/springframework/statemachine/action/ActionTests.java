/*
 * Copyright 2015-2016 the original author or authors.
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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for state machine actions.
 *
 * @author Janne Valkealahti
 *
 */
public class ActionTests extends AbstractStateMachineTests {

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTransitionActions() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		machine.start();

		TestCountAction testAction1 = context.getBean("testAction1", TestCountAction.class);
		TestCountAction testAction2 = context.getBean("testAction2", TestCountAction.class);
		TestCountAction testAction3 = context.getBean("testAction3", TestCountAction.class);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E3).build());
		assertThat(testAction1.count, is(1));
		assertThat(testAction2.count, is(1));
		assertThat(testAction3.count, is(1));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testTransitionActionErrors() {
		context.register(Config2.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		machine.start();

		TestCountAction testAction1 = context.getBean("testAction1", TestCountAction.class);
		TestCountAction testErrorAction = context.getBean("testErrorAction", TestCountAction.class);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(testAction1.count, is(1));
		assertThat(testErrorAction.count, is(1));
		assertThat(testErrorAction.context, notNullValue());
		assertThat(testErrorAction.context.getException(), notNullValue());
		assertThat(testErrorAction.context.getException(), instanceOf(RuntimeException.class));
		assertThat(testErrorAction.context.getException().getMessage(), is("Fake Error"));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testStateActionErrors() throws Exception {
		context.register(Config3.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		machine.start();

		TestCountAction testAction2 = context.getBean("testAction2", TestCountAction.class);
		TestCountAction testAction3 = context.getBean("testAction3", TestCountAction.class);
		TestCountAction testAction4 = context.getBean("testAction4", TestCountAction.class);
		TestCountAction testErrorAction2 = context.getBean("testErrorAction2", TestCountAction.class);
		TestCountAction testErrorAction3 = context.getBean("testErrorAction3", TestCountAction.class);
		TestCountAction testErrorAction4 = context.getBean("testErrorAction4", TestCountAction.class);

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(machine.getState().getId(), is(TestStates.S2));
		assertThat(testErrorAction3.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(testErrorAction2.latch.await(1, TimeUnit.SECONDS), is(true));

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());
		assertThat(testErrorAction4.latch.await(1, TimeUnit.SECONDS), is(true));

		assertThat(testAction2.count, is(1));
		assertThat(testErrorAction2.count, is(1));
		assertThat(testErrorAction2.context, notNullValue());
		assertThat(testErrorAction2.context.getException(), notNullValue());
		assertThat(testErrorAction2.context.getException(), instanceOf(RuntimeException.class));
		assertThat(testErrorAction2.context.getException().getMessage(), is("Fake Error"));

		assertThat(testAction3.count, is(1));
		assertThat(testErrorAction3.count, is(1));
		assertThat(testErrorAction3.context, notNullValue());
		assertThat(testErrorAction3.context.getException(), notNullValue());
		assertThat(testErrorAction3.context.getException(), instanceOf(RuntimeException.class));
		assertThat(testErrorAction3.context.getException().getMessage(), is("Fake Error"));

		assertThat(testAction4.count, is(1));
		assertThat(testErrorAction4.count, is(1));
		assertThat(testErrorAction4.context, notNullValue());
		assertThat(testErrorAction4.context.getException(), notNullValue());
		assertThat(testErrorAction4.context.getException(), instanceOf(RuntimeException.class));
		assertThat(testErrorAction4.context.getException().getMessage(), is("Fake Error"));
	}

	@Test
	public void testEventFromAction() {

	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private static class TestCountAction implements Action<TestStates, TestEvents> {

		int count = 0;
		StateContext<TestStates, TestEvents> context;
		CountDownLatch latch = new CountDownLatch(1);

		public TestCountAction() {
			count = 0;
		}

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			this.context = context;
			count++;
			latch.countDown();
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
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.action(testAction2())
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.action(testAction3());
		}

		@Bean
		public TestCountAction testAction1() {
			return new TestCountAction();
		}

		@Bean
		public TestCountAction testAction2() {
			return new TestCountAction();
		}

		@Bean
		public TestCountAction testAction3() {
			return new TestCountAction();
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
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.action(testAction1(), testErrorAction());
		}

		@Bean
		public TestCountAction testAction1() {
			return new TestCountAction() {
				@Override
				public void execute(StateContext<TestStates, TestEvents> context) {
					super.execute(context);
					throw new RuntimeException("Fake Error");
				}
			};
		}

		@Bean
		public TestCountAction testErrorAction() {
			return new TestCountAction();
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
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
					.state(TestStates.S2)
					.stateDo(TestStates.S2, testAction2(), testErrorAction2())
					.stateEntry(TestStates.S2, testAction3(), testErrorAction3())
					.stateExit(TestStates.S2, testAction4(), testErrorAction4())
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
		public TestCountAction testAction2() {
			return new TestCountAction() {
				@Override
				public void execute(StateContext<TestStates, TestEvents> context) {
					super.execute(context);
					throw new RuntimeException("Fake Error");
				}
			};
		}

		@Bean
		public TestCountAction testAction3() {
			return new TestCountAction() {
				@Override
				public void execute(StateContext<TestStates, TestEvents> context) {
					super.execute(context);
					throw new RuntimeException("Fake Error");
				}
			};
		}

		@Bean
		public TestCountAction testAction4() {
			return new TestCountAction() {
				@Override
				public void execute(StateContext<TestStates, TestEvents> context) {
					super.execute(context);
					throw new RuntimeException("Fake Error");
				}
			};
		}

		@Bean
		public TestCountAction testErrorAction2() {
			return new TestCountAction();
		}

		@Bean
		public TestCountAction testErrorAction3() {
			return new TestCountAction();
		}

		@Bean
		public TestCountAction testErrorAction4() {
			return new TestCountAction();
		}
	}
}
