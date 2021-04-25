/*
 * Copyright 2017-2020 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveFactory;

public class MethodAnnotationWithDefaultsWithFactoryTests extends AbstractStateMachineTests {

	@Test
	public void testMethodAnnotations() throws Exception {
		context.register(BeanConfig1.class, Config1.class);
		context.refresh();

		Bean1 bean1 = context.getBean(Bean1.class);
		Bean2 bean2 = context.getBean(Bean2.class);

		StateMachineFactory<TestStates,TestEvents> factory = resolveFactory(context);
		StateMachine<TestStates,TestEvents> machine = factory.getStateMachine();
		doStartAndAssert(machine);

		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S2);
		assertThat(bean1.counter).isEqualTo(1);
		assertThat(bean2.onStateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	public void testMethodAnnotationsWithDynamicId() throws Exception {
		context.register(BeanConfig1.class, Config1.class);
		context.refresh();

		Bean1 bean1 = context.getBean(Bean1.class);
		Bean2 bean2 = context.getBean(Bean2.class);

		String id = String.valueOf(System.currentTimeMillis());

		StateMachineFactory<TestStates,TestEvents> factory = resolveFactory(context);
		StateMachine<TestStates,TestEvents> machine = factory.getStateMachine(id);
		doStartAndAssert(machine);

		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S2);
		assertThat(bean1.counter).isEqualTo(1);
		assertThat(bean2.onStateChangedLatch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@WithStateMachine
	static class Bean1 {

		int counter = 0;

		@OnStateChanged (source = "S1", target = "S2")
		public void onStateChanged() {
			counter++;
		}
	}

	@WithStateMachine(id = StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)
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
		public Bean2 bean2() {
			return new Bean2();
		}
	}

	@Configuration
	@EnableStateMachineFactory
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
