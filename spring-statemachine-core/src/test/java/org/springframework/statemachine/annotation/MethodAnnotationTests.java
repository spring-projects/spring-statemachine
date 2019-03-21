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
package org.springframework.statemachine.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class MethodAnnotationTests extends AbstractStateMachineTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class, BeanConfig1.class, Config1.class);

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		Bean1 bean1 = context.getBean(Bean1.class);
		machine.start();

		assertThat(bean1.onMethod1Latch.await(2, TimeUnit.SECONDS), is(false));
		assertThat(bean1.onOnTransitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onMethod1Count, is(0));
		assertThat(bean1.onOnTransitionCount, is(1));

		bean1.reset(1, 1);

		// this event should cause 'method1' to get called
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(bean1.onMethod1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onOnTransitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onMethod1Count, is(1));
		assertThat(bean1.onOnTransitionCount, is(1));

		context.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations2() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class, BeanConfig2.class, Config1.class);

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		machine.start();

		Bean2 bean2 = context.getBean(Bean2.class);

		// this event should cause 'method1' to get called
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "jee").build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());

		assertThat(bean2.onMethod1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean2.headers, notNullValue());
		assertThat((String)bean2.headers.get("foo"), is("jee"));
		assertThat(bean2.extendedState, notNullValue());

		assertThat(bean2.onMethod2Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean2.variable, notNullValue());
		assertThat((String)bean2.variable, is("jee"));

		context.close();
	}

	@WithStateMachine
	static class Bean1 {

		CountDownLatch onMethod1Latch = new CountDownLatch(1);
		CountDownLatch onOnTransitionLatch = new CountDownLatch(1);
		int onMethod1Count;
		int onOnTransitionCount;

		@OnTransition(source = "S1", target = "S2")
		public void method1() {
			onMethod1Count++;
			onMethod1Latch.countDown();
		}

		@OnTransition
		public void onTransition() {
			onOnTransitionCount++;
			onOnTransitionLatch.countDown();
		}

		public void reset(int a1, int a2) {
			onMethod1Latch = new CountDownLatch(a1);
			onOnTransitionLatch = new CountDownLatch(a2);
			onMethod1Count = 0;
			onOnTransitionCount = 0;
		}

	}

	@WithStateMachine
	static class Bean2 {

		CountDownLatch onMethod1Latch = new CountDownLatch(1);
		CountDownLatch onMethod2Latch = new CountDownLatch(1);
		Map<String, Object> headers;
		ExtendedState extendedState;
		Object variable;

		@OnTransition(source = "S1", target = "S2")
		public void method1(@EventHeaders Map<String, Object> headers, ExtendedState extendedState) {
			this.headers = headers;
			extendedState.getVariables().put("foo", "jee");
			this.extendedState = extendedState;
			onMethod1Latch.countDown();
		}

		@OnTransition(source = "S2", target = "S3")
		public void method2(@EventHeaders Map<String, Object> headers, ExtendedState extendedState) {
			variable = extendedState.getVariables().get("foo");
			onMethod2Latch.countDown();
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
					.event(TestEvents.E1)
					.guard(testGuard())
					.action(testAction())
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3);
		}

		@Bean
		public TestGuard testGuard() {
			return new TestGuard();
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

	}

}
