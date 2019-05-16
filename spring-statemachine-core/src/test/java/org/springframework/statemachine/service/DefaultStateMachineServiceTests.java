/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for {@link DefaultStateMachineService}.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("unchecked")
public class DefaultStateMachineServiceTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testAcquireNotStarted() {
		context.register(Config1.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", false);
		assertThat(((Lifecycle)machine1).isRunning(), is(false));
	}

	@Test
	public void testAcquireStarted() {
		context.register(Config1.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", true);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
	}

	@Test
	public void testReleaseStopMachine() {
		context.register(Config1.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", true);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
		service.releaseStateMachine("m1");
		assertThat(((Lifecycle)machine1).isRunning(), is(false));
	}

	@Test
	public void testReleaseDoesNotStopMachine() {
		context.register(Config1.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", true);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
		service.releaseStateMachine("m1", false);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
	}

	@Test
	public void testAcquireNotStartedThreading() {
		context.register(Config2.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", false);
		assertThat(((Lifecycle)machine1).isRunning(), is(false));
	}

	@Test
	public void testAcquireStartedThreading() {
		context.register(Config2.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", true);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
	}

	@Test
	public void testReleaseStopsMachineThreading() {
		context.register(Config2.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", true);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
		service.releaseStateMachine("m1");
		assertThat(((Lifecycle)machine1).isRunning(), is(false));
	}

	@Test
	public void testReleaseDoesNotStopMachineThreading() {
		context.register(Config2.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", true);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
		service.releaseStateMachine("m1", false);
		assertThat(((Lifecycle)machine1).isRunning(), is(true));
	}

	@Test
	public void testServiceStop() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);

		DefaultStateMachineService<TestStates, TestEvents> service = new DefaultStateMachineService<>(stateMachineFactory);
		StateMachine<TestStates,TestEvents> machine1 = service.acquireStateMachine("m1", false);
		StateMachine<TestStates,TestEvents> machine2 = service.acquireStateMachine("m2", false);
		assertThat(((Lifecycle)machine1).isRunning(), is(false));
		assertThat(((Lifecycle)machine2).isRunning(), is(false));
		Map<?, ?> machines = TestUtils.readField("machines", service);
		assertThat(machines.size(), is(2));
		service.destroy();
		assertThat(machines.size(), is(0));
	}

	@Configuration
	@EnableStateMachineFactory
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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
					.event(TestEvents.E1);
		}
	}

	@Configuration
	@EnableStateMachineFactory
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.taskExecutor(stateMachineTaskExecutor());
		}

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
					.event(TestEvents.E1);
		}

		@Bean
		public TaskExecutor stateMachineTaskExecutor() {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setCorePoolSize(4);
			return executor;
		}

	}
}
