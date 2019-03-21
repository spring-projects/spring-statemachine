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
package org.springframework.statemachine;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;

import org.junit.Test;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.ObjectStateMachineFactory;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class StateMachineFactoryTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testMachineFromFactory() {
		context.register(Config1.class);
		context.refresh();

		ObjectStateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, ObjectStateMachineFactory.class);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();
		machine.start();

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(machine.getState().getIds(), contains(TestStates.S2));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testAutoStartFlagOn() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();

		assertThat(((SmartLifecycle)machine).isAutoStartup(), is(true));
		assertThat(((SmartLifecycle)machine).isRunning(), is(true));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testAutoStartFlagOff() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY, StateMachineFactory.class);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();

		assertThat(((SmartLifecycle)machine).isAutoStartup(), is(false));
		assertThat(((SmartLifecycle)machine).isRunning(), is(false));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testCustomNamedFactory() {
		context.register(Config4.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory =
				context.getBean("factory1", ObjectStateMachineFactory.class);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();
		machine.start();

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testMultipleCustomNamedFactories() {
		context.register(Config4.class, Config5.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory1 =
				context.getBean("factory1", ObjectStateMachineFactory.class);
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory2 =
				context.getBean("factory2", ObjectStateMachineFactory.class);
		StateMachine<TestStates,TestEvents> machine1 = stateMachineFactory1.getStateMachine();
		StateMachine<TestStates,TestEvents> machine2 = stateMachineFactory2.getStateMachine();

		machine1.start();
		machine2.start();

		assertThat(machine1.getState().getIds(), contains(TestStates.S1));
		assertThat(machine2.getState().getIds(), contains(TestStates.S1));
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

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachineFactory
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.states(EnumSet.allOf(TestStates.class));
		}

	}

	@Configuration
	@EnableStateMachineFactory
	public static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(false);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.states(EnumSet.allOf(TestStates.class));
		}

	}

	@Configuration
	@EnableStateMachineFactory(name = "factory1")
	public static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(false);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.states(EnumSet.allOf(TestStates.class));
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
	@EnableStateMachineFactory(name = "factory2")
	public static class Config5 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(false);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.states(EnumSet.allOf(TestStates.class));
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

}
