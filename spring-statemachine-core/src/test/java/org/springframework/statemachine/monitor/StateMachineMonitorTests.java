/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.monitor;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.transition.Transition;

public class StateMachineMonitorTests extends AbstractStateMachineTests {

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testSimpleMonitor() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		TestStateMachineMonitor monitor = context.getBean(TestStateMachineMonitor.class);

		machine.start();
		assertThat(machine.getState().getIds(), contains("S1"));
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), contains("S2"));
		assertThat(monitor.transition, notNullValue());
		assertThat(monitor.duration, notNullValue());
		monitor.reset();
		machine.sendEvent("E2");
		assertThat(machine.getState().getIds(), contains("S1"));
		assertThat(monitor.transition, notNullValue());
		assertThat(monitor.duration, notNullValue());
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withMonitoring()
					.monitor(stateMachineMonitor());
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S1")
					.event("E2");
		}

		@Bean
		public StateMachineMonitor<String, String> stateMachineMonitor() {
			return new TestStateMachineMonitor();
		}

	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private static class TestStateMachineMonitor extends AbstractStateMachineMonitor<String, String> {

		Transition<String, String> transition;
		Long duration;

		@Override
		public void transition(StateMachine<String, String> stateMachine, Transition<String, String> transition, long duration) {
			this.transition = transition;
			this.duration = duration;
		}

		void reset() {
			transition = null;
			duration = null;
		}
	}
}
