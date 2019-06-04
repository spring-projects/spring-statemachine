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
import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class ClassAnnotationTests extends AbstractStateMachineTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testClassAnnotations() throws Exception {
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(BaseConfig.class, BeanConfig1.class, FooConfig.class, BarConfig.class);

		ObjectStateMachine<TestStates,TestEvents> fooMachine =
				context.getBean("fooMachine", ObjectStateMachine.class);

		ObjectStateMachine<TestStates,TestEvents> barMachine =
				context.getBean("barMachine", ObjectStateMachine.class);

		assertThat(context.containsBean("fooMachine"), is(true));
		assertThat(context.containsBean("barMachine"), is(true));
		fooMachine.start();
		barMachine.start();

		FooBean fooBean = context.getBean(FooBean.class);
		BarBean barBean = context.getBean(BarBean.class);

		fooBean.resetMethodLatch();

		// this event should cause 'FooBean.fooMethod' to get called
		fooMachine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(fooBean.onFooMethodLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(barBean.onBarMethodLatch.await(2, TimeUnit.SECONDS), is(false));

		fooBean.resetMethodLatch();

		// this event should cause 'BarBean.barMethod' to get called
		barMachine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(fooBean.onFooMethodLatch.await(2, TimeUnit.SECONDS), is(false));
		assertThat(barBean.onBarMethodLatch.await(2, TimeUnit.SECONDS), is(true));

		context.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testClassAnnotationsWithMeta() throws Exception {
		AnnotationConfigApplicationContext context =
				new AnnotationConfigApplicationContext(BaseConfig.class, BeanConfig2.class, JeeConfig.class, FooConfig.class);

		ObjectStateMachine<TestStates,TestEvents> jeeMachine =
				context.getBean("jeeMachine", ObjectStateMachine.class);

		assertThat(context.containsBean("fooMachine"), is(true));
		assertThat(context.containsBean("jeeMachine"), is(true));

		JeeBean jeeBean = context.getBean(JeeBean.class);
		FooBean fooBean = context.getBean(FooBean.class);
		fooBean.resetMethodLatch();
		jeeMachine.start();
		jeeMachine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(jeeBean.onJeeMethodLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(fooBean.onFooMethodLatch.await(2, TimeUnit.SECONDS), is(false));
		context.close();
	}

	@WithStateMachine(name = "fooMachine")
	static class FooBean {

		CountDownLatch onFooMethodLatch;

		public void resetMethodLatch() {
			onFooMethodLatch = new CountDownLatch(1);
		}

		@OnTransition(source = "S1", target = "S2")
		public void fooMethod() {
			onFooMethodLatch.countDown();
		}

	}

	@WithStateMachine(name = "barMachine")
	static class BarBean {

		CountDownLatch onBarMethodLatch = new CountDownLatch(1);

		@OnTransition(source = "S1", target = "S2")
		public void barMethod() {
			onBarMethodLatch.countDown();
		}

	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@WithStateMachine(name = "jeeMachine")
	public @interface WithJeeMachine {
	}

	@WithJeeMachine
	static class JeeBean {

		CountDownLatch onJeeMethodLatch = new CountDownLatch(1);

		@OnTransition(source = "S1", target = "S2")
		public void jeeMethod() {
			onJeeMethodLatch.countDown();
		}

	}

	@Configuration
	static class BeanConfig1 {

		@Bean
		public FooBean fooBean() {
			return new FooBean();
		}

		@Bean
		public BarBean barBean() {
			return new BarBean();
		}

	}

	@Configuration
	static class BeanConfig2 {

		@Bean
		public FooBean fooBean() {
			return new FooBean();
		}

		@Bean
		public JeeBean jeeBean() {
			return new JeeBean();
		}

	}

	@Configuration
	@EnableStateMachine(name = "fooMachine")
	static class FooConfig extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2);
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
		public TestGuard testGuard() {
			return new TestGuard();
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

	}

	@Configuration
	@EnableStateMachine(name = "barMachine")
	static class BarConfig extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2);
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
		public TestGuard testGuard() {
			return new TestGuard();
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

	}

	@Configuration
	@EnableStateMachine(name = "jeeMachine")
	static class JeeConfig extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2);
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
		public TestGuard testGuard() {
			return new TestGuard();
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

	}

}
