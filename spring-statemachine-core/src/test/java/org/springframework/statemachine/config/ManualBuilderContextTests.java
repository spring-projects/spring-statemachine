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
package org.springframework.statemachine.config;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class ManualBuilderContextTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testAsBeanViaBuilder1() throws Exception {
		context.register(Config1.class);
		context.refresh();
		TestListener listener = context.getBean(TestListener.class);
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		listener.reset(1);
		stateMachine.sendEvent("E1");
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Test
	public void testAsBeanViaBuilder2() throws Exception {
		context.register(Config2.class);
		context.refresh();
		TestListener listener = context.getBean(TestListener.class);
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		listener.reset(1);
		stateMachine.sendEvent("E1");
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Configuration
	static class Config1 {

		@Bean
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(true)
					.listener(testListener())
					.taskExecutor(new SyncTaskExecutor());
			builder.configureStates()
				.withStates()
					.initial("S1").state("S2");
			builder.configureTransitions()
				.withExternal()
					.source("S1").target("S2").event("E1")
					.and()
				.withExternal()
					.source("S2").target("S1").event("E2");
			StateMachine<String, String> stateMachine = builder.build();
			return stateMachine;
		}

		@Bean
		TestListener testListener() {
			return new TestListener();
		}

	}

	@Configuration
	static class Config2 {

		@Bean
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(false)
					.listener(testListener())
					.taskExecutor(new SyncTaskExecutor());
			builder.configureStates()
				.withStates()
					.initial("S1").state("S2");
			builder.configureTransitions()
				.withExternal()
					.source("S1").target("S2").event("E1")
					.and()
				.withExternal()
					.source("S2").target("S1").event("E2");
			StateMachine<String, String> stateMachine = builder.build();
			return stateMachine;
		}

		@Bean
		TestListener testListener() {
			return new TestListener();
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;

		@Override
		public void stateMachineStarted(StateMachine<String, String> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		public void reset(int a1) {
			stateChangedCount = 0;
			stateChangedLatch = new CountDownLatch(a1);
		}

	}

}
