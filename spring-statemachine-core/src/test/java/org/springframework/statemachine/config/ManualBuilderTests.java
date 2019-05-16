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
package org.springframework.statemachine.config;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class ManualBuilderTests {

	@Test
	public void testManualBuildConcept() throws Exception {
		StateMachineConfigBuilder<String, String> builder = new StateMachineConfigBuilder<String, String>();
		Config config = new Config();
		builder.apply(config);
		StateMachineConfig<String, String> stateMachineConfig = builder.getOrBuild();

		TransitionsData<String, String> stateMachineTransitions = stateMachineConfig.getTransitions();
		StatesData<String, String> stateMachineStates = stateMachineConfig.getStates();
		ConfigurationData<String, String> stateMachineConfigurationConfig = stateMachineConfig.getStateMachineConfigurationConfig();
		ObjectStateMachineFactory<String, String> stateMachineFactory = new ObjectStateMachineFactory<String, String>(
				new DefaultStateMachineModel<String, String>(stateMachineConfigurationConfig, stateMachineStates, stateMachineTransitions));

		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean(StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME, new SyncTaskExecutor());
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
		beanFactory.addBean(StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME, new SyncTaskExecutor());
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
	public void testManualBuildTaskViaBeanFactory2() throws Exception {
		Builder<MyStates, MyEvents> builder = StateMachineBuilder.builder();

		StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
		beanFactory.addBean(StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME, new SyncTaskExecutor());
		beanFactory.addBean("taskScheduler", new ConcurrentTaskScheduler());

		builder.configureConfiguration()
			.withConfiguration()
				.beanFactory(beanFactory);

		builder.configureStates()
			.withStates()
				.initial(MyStates.S1).state(MyStates.S2);

		builder.configureTransitions()
			.withExternal()
				.source(MyStates.S1).target(MyStates.S2).event(MyEvents.E1)
				.and()
			.withExternal()
				.source(MyStates.S2).target(MyStates.S1).event(MyEvents.E2);

		StateMachine<MyStates, MyEvents> stateMachine = builder.build();
		assertThat(stateMachine, notNullValue());
		TestListener2 listener = new TestListener2();
		stateMachine.addStateListener(listener);
		stateMachine.start();

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(MyStates.S1));

		listener.reset(1);
		stateMachine.sendEvent(MyEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(MyStates.S2));
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

		listener.reset(1);
		stateMachine.sendEvent("E1");
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Test
	public void testManualBuildDefaultTaskExecutor() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();

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

		assertThat(TestUtils.readField("taskExecutor", stateMachine), notNullValue());
		assertThat(TestUtils.readField("taskScheduler", stateMachine), notNullValue());
	}

	@Test
	public void testAutoStartFlagOn() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.autoStartup(true)
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

		assertThat(((SmartLifecycle)stateMachine).isAutoStartup(), is(true));
		assertThat(((SmartLifecycle)stateMachine).isRunning(), is(true));
	}

	static enum MyStates {
		S1, S2
	}

	static enum MyEvents {
		E1, E2
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
		volatile int stateChangedCount = 0;

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

	private static class TestListener2 extends StateMachineListenerAdapter<MyStates, MyEvents> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;

		@Override
		public void stateChanged(State<MyStates, MyEvents> from, State<MyStates, MyEvents> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		public void reset(int a1) {
			stateChangedCount = 0;
			stateChangedLatch = new CountDownLatch(a1);
		}

	}

}
