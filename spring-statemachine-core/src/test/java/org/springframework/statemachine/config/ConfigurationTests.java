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
package org.springframework.statemachine.config;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for state machine configuration.
 * 
 * @author Janne Valkealahti
 *
 */
public class ConfigurationTests extends AbstractStateMachineTests {

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testStates() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config1.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		EnumStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		assertThat(machine, notNullValue());
		TestAction testAction = ctx.getBean("testAction", TestAction.class);
		TestGuard testGuard = ctx.getBean("testGuard", TestGuard.class);
		assertThat(testAction, notNullValue());
		assertThat(testGuard, notNullValue());
		ctx.close();
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
	
}
