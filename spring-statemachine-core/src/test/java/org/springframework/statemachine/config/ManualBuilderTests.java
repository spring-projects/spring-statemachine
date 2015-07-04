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
package org.springframework.statemachine.config;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfig;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class ManualBuilderTests {

	@Test
	public void testManualBuildConcept() throws Exception {
		StateMachineConfigBuilder<String, String> builder = new StateMachineConfigBuilder<String, String>();
		Config config = new Config();
		builder.apply(config);
		StateMachineConfig<String, String> stateMachineConfig = builder.getOrBuild();

		StateMachineTransitions<String, String> stateMachineTransitions = stateMachineConfig.getTransitions();
		StateMachineStates<String, String> stateMachineStates = stateMachineConfig.getStates();
		StateMachineConfigurationConfig<String, String> stateMachineConfigurationConfig = stateMachineConfig.getStateMachineConfigurationConfig();
		ObjectStateMachineFactory<String, String> stateMachineFactory = new ObjectStateMachineFactory<String, String>(
				stateMachineConfigurationConfig, stateMachineTransitions, stateMachineStates);

		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean("taskExecutor", new SyncTaskExecutor());
		beanFactory.addBean("taskScheduler", new ConcurrentTaskScheduler());
		stateMachineFactory.setBeanFactory(beanFactory);

		TestListener listener = new TestListener();
		StateMachine<String,String> stateMachine = stateMachineFactory.getStateMachine();
		stateMachine.addStateListener(listener);
		stateMachine.start();

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
	}

	@Test
	public void testManualBuildTaskViaBeanFactory() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();

		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean("taskExecutor", new SyncTaskExecutor());
		beanFactory.addBean("taskScheduler", new ConcurrentTaskScheduler());

		builder.configureConfiguration()
			.withConfiguration()
				.beanFactory(beanFactory);

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
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
	}

	@Test
	public void testManualBuildExplicitTaskExecutorAndScheduler() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.taskExecutor(new SyncTaskExecutor())
				.taskScheduler(new ConcurrentTaskScheduler());

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
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
	}

	static class Config extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1").state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2").event("E1")
					.and()
				.withExternal()
					.source("S2").target("S1").event("E2");
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<String, String> transition) {
			transitionLatch.countDown();
		}

	}

}
