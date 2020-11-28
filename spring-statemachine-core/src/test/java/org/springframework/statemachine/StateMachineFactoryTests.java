/*
 * Copyright 2015-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveFactory;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class StateMachineFactoryTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	/**
	 * {@link org.springframework.statemachine.config.ManualBuilderTests#testManualBuildConcept()}
	 */
	@Test
	public void testCreate() throws Exception{
		StateMachineConfigBuilder<TestStates, TestEvents> builder = new StateMachineConfigBuilder<>();
		Config1 config = new Config1();
		builder.apply(config);

		StateMachineFactory<TestStates,TestEvents> factory = StateMachineFactory.create(builder);
		StateMachine<TestStates,TestEvents> machine = factory.getStateMachine();
		doStartAndAssert(machine);

		assertThat(machine).isNotNull();
		assertThat(machine.getState().getId()).isEqualTo(TestStates.S1);
	}

	@Test
	public void testMachineFromFactory() {
		context.register(Config1.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();
		doStartAndAssert(machine);

		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S2);
	}

	@Test
	public void testAutoStartFlagOn() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();

		assertThat(((SmartLifecycle)machine).isAutoStartup()).isTrue();
		assertThat(((SmartLifecycle)machine).isRunning()).isTrue();
	}

	@Test
	public void testAutoStartFlagOff() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();

		assertThat(((SmartLifecycle)machine).isAutoStartup()).isFalse();
		assertThat(((SmartLifecycle)machine).isRunning()).isFalse();
	}

	@Test
	public void testCustomNamedFactory() {
		context.register(Config4.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory("factory1", context);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();
		doStartAndAssert(machine);

		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);
	}

	@Test
	public void testMultipleCustomNamedFactories() {
		context.register(Config4.class, Config5.class);
		context.refresh();
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory1 = resolveFactory("factory1", context);
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory2 = resolveFactory("factory2", context);
		StateMachine<TestStates,TestEvents> machine1 = stateMachineFactory1.getStateMachine();
		StateMachine<TestStates,TestEvents> machine2 = stateMachineFactory2.getStateMachine();

		doStartAndAssert(machine1);
		doStartAndAssert(machine2);

		assertThat(machine1.getState().getIds()).containsExactly(TestStates.S1);
		assertThat(machine2.getState().getIds()).containsExactly(TestStates.S1);
	}

	@Test
	public void testMachineFromFactoryWithAsyncExecutorAutoStart() throws Exception {
		context.register(Config6.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);
		StateMachine<TestStates,TestEvents> machine = stateMachineFactory.getStateMachine();

		// factory waits machine to get started so we
		// should have state immediately
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		// still need to listen state chance manually before
		// checking state as execution happens in a thread
		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S2);
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
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withVerifier()
					.enabled(false)
					.and()
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
				.withVerifier()
					.enabled(false)
					.and()
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

	@Configuration
	@EnableStateMachineFactory
	static class Config6 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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

	static class TestStateMachineListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			latch.countDown();
		}
	}

}
