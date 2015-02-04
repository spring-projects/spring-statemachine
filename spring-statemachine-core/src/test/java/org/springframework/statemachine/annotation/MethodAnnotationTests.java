/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.processor.StateMachineAnnotationPostProcessor;

public class MethodAnnotationTests extends AbstractStateMachineTests {
	
	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations() throws Exception {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(BaseConfig.class, AnnoConfig.class, BeanConfig1.class, Config1.class);

		EnumStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));

		Bean1 bean1 = context.getBean(Bean1.class);
		
		// this event should cause 'method1' to get called
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		
		assertThat(bean1.onMethod1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onOnTransitionFromS2ToS3Latch.await(2, TimeUnit.SECONDS), is(false));
		
		context.close();
	}
		
	@WithStateMachine(name = StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)
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
		
		
	}

	@Configuration
	static class BeanConfig1 {

		@Bean
		public Bean1 bean1() {
			return new Bean1();
		}
		
	}
	
	
	@Configuration
	static class AnnoConfig {
		
		@Bean(name="org.springframework.statemachine.internal.springStateMachineAnnotationPostProcessor")
		public StateMachineAnnotationPostProcessor springStateMachineAnnotationPostProcessor() {
			return new StateMachineAnnotationPostProcessor();
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
					.event(TestEvents.E2);
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
