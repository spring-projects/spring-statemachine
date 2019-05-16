/*
 * Copyright 2015-2018 the original author or authors.
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
package org.springframework.statemachine.processor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.transaction.annotation.Transactional;

public class StateMachineAnnotationPostProcessorTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testWithNormalAnnotation() {
		context.register(Config1.class, BeanConfig1.class);
		context.refresh();
		assertThat(context.getBeansOfType(StateMachineHandler.class).size(), is(2));
	}

	@Test
	public void testWithNormalAnnotationWithTransactional() {
		context.register(Config1.class, BeanConfig2.class);
		context.refresh();
		assertThat(context.getBeansOfType(StateMachineHandler.class).size(), is(1));
	}

	@WithStateMachine
	static class Bean1 {

		CountDownLatch onMethod1Latch = new CountDownLatch(1);
		CountDownLatch onOnTransitionFromS2ToS3Latch = new CountDownLatch(1);

		@OnTransition(source = "S1", target = "S2")
		public void method1() {
			onMethod1Latch.countDown();
		}

		@OnTransition
		public void onTransitionFromS2ToS3() {
			onOnTransitionFromS2ToS3Latch.countDown();
		}

		@Bean
		public String dummy() {
			return "dummy";
		}

	}

	@WithStateMachine
	static class Bean2 {

		CountDownLatch onMethod1Latch = new CountDownLatch(1);
		CountDownLatch onOnTransitionFromS2ToS3Latch = new CountDownLatch(1);

		@OnTransition(source = "S1", target = "S2")
		@Transactional
		public void method1() {
			onMethod1Latch.countDown();
		}

		@Bean
		public String dummy() {
			return "dummy";
		}

	}

	@Configuration
	static class BeanConfig1 {

		@Bean
		public Bean1 bean1() {
			return new Bean1();
		}

	}

	@Configuration
	static class BeanConfig2 {

		@Bean
		public Bean2 bean2() {
			return new Bean2();
		}

	}

	@Configuration
	@EnableStateMachine(name = {StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, "fooMachine"})
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
}
