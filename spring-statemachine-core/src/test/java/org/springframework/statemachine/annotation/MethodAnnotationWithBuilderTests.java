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
package org.springframework.statemachine.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableWithStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.configuration.StateMachineAnnotationPostProcessorConfiguration;
import org.springframework.statemachine.config.configurers.ConfigurationConfigurer;

@SuppressWarnings("unchecked")
public class MethodAnnotationWithBuilderTests extends AbstractStateMachineTests {

	@Test
	public void testMethodAnnotations1() throws Exception {
		context.register(BeanConfig1.class, StateMachineAnnotationPostProcessorConfiguration.class);
		context.refresh();

		Bean1 bean1 = context.getBean(Bean1.class);

		StateMachine<TestStates,TestEvents> machine = context.getBean(StateMachine.class);
		machine.start();

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates.S2));
		assertThat(bean1.onStateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
	}

	@Test
	public void testMethodAnnotations2() throws Exception {
		context.register(BeanConfig2.class);
		context.refresh();

		Bean1 bean1 = context.getBean(Bean1.class);

		StateMachine<TestStates,TestEvents> machine = context.getBean(StateMachine.class);
		machine.start();

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates.S2));
		assertThat(bean1.onStateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
	}

	@Test
	public void testMethodAnnotations3() throws Exception {
		context.register(BeanConfig1.class, StateMachineAnnotationPostProcessorConfiguration.class);
		context.refresh();

		Bean1 bean1 = context.getBean(Bean1.class);

		StateMachine<TestStates,TestEvents> machine = buildMachine(context);
		machine.start();

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates.S2));
		assertThat(bean1.onStateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
	}

	@Test
	public void testMethodAnnotations4() throws Exception {
		context.register(BeanConfig3.class);
		context.refresh();

		Bean2 bean2 = context.getBean(Bean2.class);

		StateMachine<TestStates,TestEvents> machine = buildMachine(context);
		machine.start();

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), contains(TestStates.S2));
		assertThat(bean2.onStateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
	}

	@WithStateMachine(id = "xxx")
	static class Bean1 {

		CountDownLatch onStateChangedLatch = new CountDownLatch(1);

		@OnStateChanged
		public void onStateChanged() {
			onStateChangedLatch.countDown();
		}
	}

	@WithStateMachine(name = "", id = "xxx")
	static class Bean2 {

		CountDownLatch onStateChangedLatch = new CountDownLatch(1);

		@OnStateChanged
		public void onStateChanged() {
			onStateChangedLatch.countDown();
		}
	}

	@Configuration
	static class BeanConfig1 {

		@Bean
		public Bean1 bean1() {
			return new Bean1();
		}

		@Bean
		public StateMachine<TestStates,TestEvents> stateMachine() throws Exception {
			return buildMachine(null);
		}
	}

	@EnableWithStateMachine
	static class BeanConfig2 {

		@Bean
		public Bean1 bean1() {
			return new Bean1();
		}

		@Bean
		public StateMachine<TestStates,TestEvents> stateMachine() throws Exception {
			return buildMachine(null);
		}
	}

	@EnableWithStateMachine
	static class BeanConfig3 {

		@Bean
		public Bean2 bean2() {
			return new Bean2();
		}
	}

	private static StateMachine<TestStates,TestEvents> buildMachine(BeanFactory beanFactory) throws Exception {
		Builder<TestStates,TestEvents> builder = StateMachineBuilder.builder();

		ConfigurationConfigurer<TestStates, TestEvents> withConfiguration = builder.configureConfiguration().withConfiguration();
		withConfiguration.machineId("xxx");
		if (beanFactory != null) {
			withConfiguration.beanFactory(beanFactory);
		}

		builder.configureStates()
			.withStates()
				.initial(TestStates.S1)
				.states(EnumSet.allOf(TestStates.class));

		builder.configureTransitions()
			.withExternal()
				.source(TestStates.S1)
				.target(TestStates.S2)
				.event(TestEvents.E1);

		return builder.build();
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
