/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.statemachine;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class EventHeaderTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedToInitialInSubs1() throws InterruptedException {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1I = context.getBean("headerTestAction1I", HeaderTestAction.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction11 = context.getBean("headerTestAction11", HeaderTestAction.class);
		HeaderTestAction headerTestAction111 = context.getBean("headerTestAction111", HeaderTestAction.class);
		HeaderTestAction headerTestAction112 = context.getBean("headerTestAction112", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(headerTestAction1I.testHeader, is("testValue"));
		assertThat(headerTestAction1.testHeader, is("testValue"));
		assertThat(headerTestAction11.testHeader, is("testValue"));
		assertThat(headerTestAction111.testHeader, is("testValue"));
		assertThat(headerTestAction112.testHeader, nullValue());

		headerTestAction1.testHeader = null;
		headerTestAction11.testHeader = null;
		headerTestAction111.testHeader = null;

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E2").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		assertThat(headerTestAction1.testHeader, nullValue());
		assertThat(headerTestAction11.testHeader, nullValue());
		assertThat(headerTestAction111.testHeader, nullValue());
		assertThat(headerTestAction112.testHeader, is("testValue"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedToInitialInSubs2() throws InterruptedException {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction11 = context.getBean("headerTestAction11", HeaderTestAction.class);
		HeaderTestAction headerTestAction111 = context.getBean("headerTestAction111", HeaderTestAction.class);
		HeaderTestAction headerTestAction112 = context.getBean("headerTestAction112", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(headerTestAction1.testHeader, is("testValue"));
		assertThat(headerTestAction11.testHeader, is("testValue"));
		assertThat(headerTestAction111.testHeader, is("testValue"));
		assertThat(headerTestAction112.testHeader, nullValue());

		headerTestAction1.testHeader = null;
		headerTestAction11.testHeader = null;
		headerTestAction111.testHeader = null;

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E2").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		assertThat(headerTestAction1.testHeader, nullValue());
		assertThat(headerTestAction11.testHeader, nullValue());
		assertThat(headerTestAction111.testHeader, nullValue());
		assertThat(headerTestAction112.testHeader, nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedToInitialInSubs3() throws InterruptedException {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1I = context.getBean("headerTestAction1I", HeaderTestAction.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction11 = context.getBean("headerTestAction11", HeaderTestAction.class);
		HeaderTestAction headerTestAction111 = context.getBean("headerTestAction111", HeaderTestAction.class);
		HeaderTestAction headerTestAction112 = context.getBean("headerTestAction112", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E2").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		headerTestAction1I.testHeader = null;
		headerTestAction1.testHeader = null;
		headerTestAction11.testHeader = null;
		headerTestAction111.testHeader = null;
		headerTestAction112.testHeader = null;
		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E3").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		assertThat(headerTestAction1I.testHeader, nullValue());
		assertThat(headerTestAction1.testHeader, nullValue());
		assertThat(headerTestAction11.testHeader, nullValue());
		assertThat(headerTestAction111.testHeader, is("testValue"));
		assertThat(headerTestAction112.testHeader, nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedToInitialInSubs1Threading() throws InterruptedException {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1I = context.getBean("headerTestAction1I", HeaderTestAction.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction11 = context.getBean("headerTestAction11", HeaderTestAction.class);
		HeaderTestAction headerTestAction111 = context.getBean("headerTestAction111", HeaderTestAction.class);
		HeaderTestAction headerTestAction112 = context.getBean("headerTestAction112", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(headerTestAction1I.testHeader, is("testValue"));
		assertThat(headerTestAction1.testHeader, is("testValue"));
		assertThat(headerTestAction11.testHeader, is("testValue"));
		assertThat(headerTestAction111.testHeader, is("testValue"));
		assertThat(headerTestAction112.testHeader, nullValue());

		headerTestAction1.testHeader = null;
		headerTestAction11.testHeader = null;
		headerTestAction111.testHeader = null;

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E2").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		assertThat(headerTestAction1.testHeader, nullValue());
		assertThat(headerTestAction11.testHeader, nullValue());
		assertThat(headerTestAction111.testHeader, nullValue());
		assertThat(headerTestAction112.testHeader, is("testValue"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedToInitialInSubs2Threading() throws InterruptedException {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction11 = context.getBean("headerTestAction11", HeaderTestAction.class);
		HeaderTestAction headerTestAction111 = context.getBean("headerTestAction111", HeaderTestAction.class);
		HeaderTestAction headerTestAction112 = context.getBean("headerTestAction112", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(headerTestAction1.testHeader, is("testValue"));
		assertThat(headerTestAction11.testHeader, is("testValue"));
		assertThat(headerTestAction111.testHeader, is("testValue"));
		assertThat(headerTestAction112.testHeader, nullValue());

		headerTestAction1.testHeader = null;
		headerTestAction11.testHeader = null;
		headerTestAction111.testHeader = null;

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E2").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		assertThat(headerTestAction1.testHeader, nullValue());
		assertThat(headerTestAction11.testHeader, nullValue());
		assertThat(headerTestAction111.testHeader, nullValue());
		assertThat(headerTestAction112.testHeader, nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedToInitialInSubs3Threading() throws InterruptedException {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1I = context.getBean("headerTestAction1I", HeaderTestAction.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction11 = context.getBean("headerTestAction11", HeaderTestAction.class);
		HeaderTestAction headerTestAction111 = context.getBean("headerTestAction111", HeaderTestAction.class);
		HeaderTestAction headerTestAction112 = context.getBean("headerTestAction112", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E2").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		headerTestAction1I.testHeader = null;
		headerTestAction1.testHeader = null;
		headerTestAction11.testHeader = null;
		headerTestAction111.testHeader = null;
		headerTestAction112.testHeader = null;
		listener.reset(1);
		machine.sendEvent(MessageBuilder.withPayload("E3").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		assertThat(headerTestAction1I.testHeader, nullValue());
		assertThat(headerTestAction1.testHeader, nullValue());
		assertThat(headerTestAction11.testHeader, nullValue());
		assertThat(headerTestAction111.testHeader, is("testValue"));
		assertThat(headerTestAction112.testHeader, nullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedWithAnonymousTransition() throws InterruptedException {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction2 = context.getBean("headerTestAction2", HeaderTestAction.class);
		HeaderTestAction headerTestAction3 = context.getBean("headerTestAction3", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(headerTestAction1.testHeader, is("testValue"));
		assertThat(headerTestAction2.testHeader, is("testValue"));
		assertThat(headerTestAction3.testHeader, is("testValue"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHeaderPassedWithAnonymousTransitionThreading() throws InterruptedException {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		HeaderTestAction headerTestAction1 = context.getBean("headerTestAction1", HeaderTestAction.class);
		HeaderTestAction headerTestAction2 = context.getBean("headerTestAction2", HeaderTestAction.class);
		HeaderTestAction headerTestAction3 = context.getBean("headerTestAction3", HeaderTestAction.class);
		TestListener listener = new TestListener();
		listener.reset(1);
		machine.addStateListener(listener);
		machine.start();

		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));

		listener.reset(3);
		machine.sendEvent(MessageBuilder.withPayload("E1").setHeader("testHeader", "testValue").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(3));

		assertThat(headerTestAction1.testHeader, is("testValue"));
		assertThat(headerTestAction2.testHeader, is("testValue"));
		assertThat(headerTestAction3.testHeader, is("testValue"));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1", headerTestAction1(), null)
					.and()
					.withStates()
						.parent("S1")
						.initial("S11", headerTestAction1I())
						.state("S11", headerTestAction11(), null)
						.state("S12")
						.and()
						.withStates()
							.parent("S11")
							.initial("S111")
							.state("S111", headerTestAction111(), null)
							.state("S122", headerTestAction112(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1")
					.and()
				.withExternal()
					.source("S111")
					.target("S122")
					.event("E2")
					.and()
				.withExternal()
					.source("S122")
					.target("S111")
					.event("E3");
		}

		@Bean
		public HeaderTestAction headerTestAction1I() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction1() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction11() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction111() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction112() {
			return new HeaderTestAction();
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1", headerTestAction1(), null)
					.and()
					.withStates()
						.parent("S1")
						.initial("S11", headerTestAction1I())
						.state("S11", headerTestAction11(), null)
						.state("S12")
						.and()
						.withStates()
							.parent("S11")
							.initial("S111")
							.state("S111", headerTestAction111(), null)
							.state("S122", headerTestAction112(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1")
					.and()
				.withExternal()
					.source("S111")
					.target("S122")
					.event("E2")
					.and()
				.withExternal()
					.source("S122")
					.target("S111")
					.event("E3");
		}

		@Bean
		public HeaderTestAction headerTestAction1I() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction1() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction11() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction111() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction112() {
			return new HeaderTestAction();
		}

		@Bean(name = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
		public TaskExecutor taskExecutor() {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setCorePoolSize(1);
			return executor;
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1", headerTestAction1(), null)
					.state("S2", headerTestAction2(), null)
					.state("S3", headerTestAction3(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1")
					.and()
				.withExternal()
					.source("S1")
					.target("S2")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}

		@Bean
		public HeaderTestAction headerTestAction1() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction2() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction3() {
			return new HeaderTestAction();
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1", headerTestAction1(), null)
					.state("S2", headerTestAction2(), null)
					.state("S3", headerTestAction3(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1")
					.and()
				.withExternal()
					.source("S1")
					.target("S2")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}

		@Bean
		public HeaderTestAction headerTestAction1() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction2() {
			return new HeaderTestAction();
		}

		@Bean
		public HeaderTestAction headerTestAction3() {
			return new HeaderTestAction();
		}

		@Bean(name = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
		public TaskExecutor taskExecutor() {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setCorePoolSize(3);
			return executor;
		}
	}

	private static class HeaderTestAction implements Action<String, String> {

		String testHeader = null;

		@Override
		public void execute(StateContext<String, String> context) {
			testHeader = context.getMessageHeaders().get("testHeader", String.class);
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;
		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);

		@Override
		public void stateMachineStarted(StateMachine<String, String> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		public void reset(int c1) {
			stateChangedLatch = new CountDownLatch(c1);
			stateChangedCount = 0;
		}

	}

}
