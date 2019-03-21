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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

/**
 * Tests for state machine configuration.
 *
 * @author Janne Valkealahti
 *
 */
public class ConfigurationTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testStates() {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
		TestAction testAction = context.getBean("testAction", TestAction.class);
		TestGuard testGuard = context.getBean("testGuard", TestGuard.class);
		assertThat(testAction, notNullValue());
		assertThat(testGuard, notNullValue());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testSimpleSubmachine() throws Exception {
		context.register(Config4.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testRegions() throws Exception {
		context.register(Config6.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testSubmachineWithState() throws Exception {
		context.register(Config7.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testSubmachineWithRegion() throws Exception {
		context.register(Config8.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testAutoStartFlagOn() throws Exception {
		context.register(Config9.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine.isAutoStartup(), is(true));
		assertThat(machine.isRunning(), is(true));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testAutoStartFlagOff() throws Exception {
		context.register(Config11.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine.isAutoStartup(), is(false));
		assertThat(machine.isRunning(), is(false));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testRegisterListeners() throws Exception {
		context.register(Config10.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		Object o1 = TestUtils.readField("stateListener", machine);
		Object o2 = TestUtils.readField("listeners", o1);
		Object o3 = TestUtils.readField("list", o2);
		assertThat(((List<?>)o3).size(), is(2));
	}

	@Test
	public void testEnableStateMachineNoAdapter() {
		context.register(Config12.class);
		context.refresh();
	}

	@Test(expected = BeanCreationException.class)
	public void testEnableStateMachineFactoryNoAdapter() {
		context.register(Config13.class);
		context.refresh();
	}

	@Test
	public void testTaskExecutor1() throws Exception {
		// set in builder, no bf or taskExecutor bean registered
		context.register(Config14.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		Object executorFromMachine = TestUtils.readField("taskExecutor", stateMachine);
		Object stateMachineExecutor = TestUtils.readField("stateMachineExecutor", stateMachine);
		Object executorFromExecutor = TestUtils.readField("taskExecutor", stateMachineExecutor);

		assertThat(executorFromMachine, sameInstance(Config14.taskExecutor));
		assertThat(executorFromExecutor, sameInstance(Config14.taskExecutor));

		assertThat(executorFromMachine, notNullValue());
		assertThat(executorFromExecutor, notNullValue());
		assertThat(executorFromMachine, sameInstance(executorFromExecutor));
	}

	@Test
	public void testTaskExecutor2() throws Exception {
		// set as bean, should get from bf
		context.register(BaseConfig.class, Config15.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(context.containsBean(StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME), is(true));

		Object stateMachineExecutor = TestUtils.readField("stateMachineExecutor", stateMachine);

		Object executorFromMachine = TestUtils.callMethod("getTaskExecutor", stateMachine);
		Object executorFromExecutor = TestUtils.callMethod("getTaskExecutor", stateMachineExecutor);

		assertThat(executorFromMachine, notNullValue());
		assertThat(executorFromExecutor, notNullValue());
		assertThat(executorFromMachine, sameInstance(executorFromExecutor));
	}

	@Test
	public void testBeanFactory1() throws Exception {
		// should come from context
		context.register(Config15.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		Object stateMachineExecutor = TestUtils.readField("stateMachineExecutor", stateMachine);

		Object bfFromMachine = TestUtils.callMethod("getBeanFactory", stateMachine);
		Object bfFromExecutor = TestUtils.callMethod("getBeanFactory", stateMachineExecutor);

		assertThat(bfFromMachine, notNullValue());
		assertThat(bfFromExecutor, notNullValue());
		assertThat(bfFromMachine, sameInstance(bfFromExecutor));
	}

	@Test
	public void testBeanFactory2() throws Exception {
		// set bf in builder
		context.register(Config16.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		Object stateMachineExecutor = TestUtils.readField("stateMachineExecutor", stateMachine);

		Object bfFromMachine = TestUtils.callMethod("getBeanFactory", stateMachine);
		Object bfFromExecutor = TestUtils.callMethod("getBeanFactory", stateMachineExecutor);

		assertThat(bfFromMachine, notNullValue());
		assertThat(bfFromExecutor, notNullValue());
		assertThat(bfFromMachine, sameInstance(Config16.beanFactory));
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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
					.guard(testGuard())
					.action(testAction());
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

		@Bean
		public TestGuard testGuard() {
			return new TestGuard();
		}

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.states(EnumSet.allOf(TestStates.class));
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.end(TestStates.SF)
					.states(EnumSet.allOf(TestStates.class));
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.and()
					.withStates()
						.parent(TestStates.S1)
						.initial(TestStates.S2)
						.end(TestStates.SF)
						.state(TestStates.SI)
						.state(TestStates.S2)
						.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S1)
					.event(TestEvents.E1)
					.and()
				.withLocal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E2)
					.and()
				.withInternal()
					.source(TestStates.S2)
					.event(TestEvents.E3);
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config5 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			TestEntryAction action1 = action1();
			Collection<TestEntryAction> actions1 = new ArrayList<TestEntryAction>();
			actions1.add(action1);
			Collection<Action<TestStates, TestEvents>> actions3 = new ArrayList<Action<TestStates,TestEvents>>();
			actions3.add(action3());
			states
				.withStates()
					.initial(TestStates.S11)
					.state(TestStates.S11, actions1, Arrays.asList(action2()))
					.state(TestStates.S12, action1(), action2())
					.and()
					.withStates()
						.parent(TestStates.S11)
						.initial(TestStates.S111)
						.state(TestStates.S111, actions3, Arrays.asList(action4()));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S111)
					.target(TestStates.S1)
					.event(TestEvents.E1);
		}

		public TestEntryAction action1() {
			return new TestEntryAction();
		}

		public Action<TestStates, TestEvents> action2() {
			return new TestExitAction();
		}

		public Action<TestStates, TestEvents> action3() {
			return new TestEntryAction();
		}

		public TestExitAction action4() {
			return new TestExitAction();
		}

	}
/*
//              +-----------------------------------------------------------------------------------------------------------------+
//              |                                                       SM                                                        |
//              +-----------------------------------------------------------------------------------------------------------------+
//              |                                                        |                                                        |
//              |       +---------------------------------------+        |       +---------------------------------------+        |
//              |   *-->|                    S10                |        |   *-->|                    S20                |        |
//              |       +---------------------------------------+        |       +---------------------------------------+        |
//              |       | entry/                                |        |       | entry/                                |        |
//              |       | exit/                                 |        |       | exit/                                 |        |
//              |       |        +--------------------------+   |        |       |        +--------------------------+   |        |
//              |       |    *-->|           S101           |   |        |       |    *-->|           S201           |   |        |
//              |       |        +--------------------------+   |        |       |        +--------------------------+   |        |
//              |       |        | entry/                   |   |        |       |        | entry/                   |   |        |
//              |       |        | exit/                    |   |        |       |        | exit/                    |   |        |
//              |       |        |        +-----------+     |   |        |       |        |        +-----------+     |   |        |
//              |       |        |    *-->|   S1011   |     |   |        |       |        |    *-->|   S2011   |     |   |        |
//              |       |        |        +-----------+     |   |        |       |        |        +-----------+     |   |        |
//              |       |        |        | entry/    |     |   |        |       |        |        | entry/    |     |   |        |
//              |       |        |        | exit/     |     |   |        |       |        |        | exit/     |     |   |        |
//              |       |        |        |           |     |   |        |       |        |        |           |     |   |        |
//              |       |        |        +-----------+     |   |        |       |        |        +-----------+     |   |        |
//              |       |        |                          |   |        |       |        |                          |   |        |
//              |       |        |                          |   |        |       |        |                          |   |        |
//              |       |        |                          |   |        |       |        |                          |   |        |
//              |       |        |        +-----------+     |   |        |       |        |        +-----------+     |   |        |
//              |       |        |        |   S1012   |     |   |        |       |        |        |   S2012   |     |   |        |
//              |       |        |        +-----------+     |   |        |       |        |        +-----------+     |   |        |
//              |       |        |        | entry/    |     |   |        |       |        |        | entry/    |     |   |        |
//              |       |        |        | exit/     |     |   |        |       |        |        | exit/     |     |   |        |
//              |       |        |        |           |     |   |        |       |        |        |           |     |   |        |
//              |       |        |        +-----------+     |   |        |       |        |        +-----------+     |   |        |
//              |       |        |                          |   |        |       |        |                          |   |        |
//              |       |        +--------------------------+   |        |       |        +--------------------------+   |        |
//              |       |                                       |        |       |                                       |        |
//              |       |                                       |        |       |                                       |        |
//              |       +---------------------------------------+        |       +---------------------------------------+        |
//              |                                                        |                                                        |
//              |       +---------------------------------------+        |       +---------------------------------------+        |
//              |       |                    S11                |        |       |                    S21                |        |
//              |       +---------------------------------------+        |       +---------------------------------------+        |
//              |       |  entry/                               |        |       |  entry/                               |        |
//              |       |  exit/                                |        |       |  exit/                                |        |
//              |       |                                       |        |       |                                       |        |
//              |       +---------------------------------------+        |       +---------------------------------------+        |
//              |                                                        |                                                        |
//              +--------------------------------------------------------+--------------------------------------------------------+
 */

	@Configuration
	@EnableStateMachine
	static class Config6 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S10)
					.state(TestStates.S10)
					.state(TestStates.S11)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S101)
						.state(TestStates.S101)
						.and()
						.withStates()
							.parent(TestStates.S101)
							.initial(TestStates.S1011)
							.state(TestStates.S1011)
							.state(TestStates.S1012)
							.and()
				.withStates()
					.initial(TestStates.S20)
					.state(TestStates.S20)
					.state(TestStates.S21)
					.and()
					.withStates()
						.parent(TestStates.S20)
						.initial(TestStates.S201)
						.state(TestStates.S201)
						.and()
						.withStates()
							.parent(TestStates.S201)
							.initial(TestStates.S2011)
							.state(TestStates.S2011)
							.state(TestStates.S2012);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S11)
					.event(TestEvents.E1);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config7 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
			.withStates()
				.initial(TestStates.S10)
				.state(TestStates.S10)
				.state(TestStates.S11)
				.and()
				.withStates()
					.parent(TestStates.S10)
					.initial(TestStates.S101)
					.state(TestStates.S101)
					.and()
					.withStates()
						.parent(TestStates.S101)
						.initial(TestStates.S1011)
						.state(TestStates.S1011)
						.state(TestStates.S1012);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S11)
					.event(TestEvents.E1);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config8 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S10)
					.state(TestStates.S10)
					.state(TestStates.S11)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S101)
						.state(TestStates.S101)
						.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S111)
						.state(TestStates.S111);
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config9 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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
	@EnableStateMachine
	public static class Config10 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.listener(new StateMachineListenerAdapter<TestStates, TestEvents>())
					.listener(new StateMachineListenerAdapter<TestStates, TestEvents>());
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
	@EnableStateMachine
	public static class Config11 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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
	@EnableStateMachine
	public static class Config12 {
	}

	@Configuration
	@EnableStateMachineFactory
	public static class Config13 {
	}

	@Configuration
	public static class Config14 {

		public static TaskExecutor taskExecutor = new SyncTaskExecutor();

		@Bean
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(false)
					.taskExecutor(taskExecutor);
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

	}

	@Configuration
	public static class Config15 {

		@Bean
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(false);
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

	}

	@Configuration
	public static class Config16 {

		public static BeanFactory beanFactory = new DefaultListableBeanFactory();

		@Bean
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(false)
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
			return stateMachine;
		}

	}

}
