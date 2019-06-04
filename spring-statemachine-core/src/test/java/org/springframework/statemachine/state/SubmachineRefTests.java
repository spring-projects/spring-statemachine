/*
 * Copyright 2016-2019 the original author or authors.
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
package org.springframework.statemachine.state;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveFactory;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for submachine references.
 *
 * @author Janne Valkealahti
 *
 */
public class SubmachineRefTests extends AbstractStateMachineTests {

	@Test
	public void testSubmachineRef() throws Exception {
		context.register(Config2.class, Config1.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		assertThat(machine, notNullValue());
		doStartAndAssert(machine);
		assertThat(machine.getState().getIds(), containsInAnyOrder("S1"));
		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S20"));
		doSendEventAndConsumeAll(machine, "E2");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21", "S30"));
		doSendEventAndConsumeAll(machine, "E3");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21", "S31"));
	}

	@Test
	public void testSubmachineRefWithFactory() throws Exception {
		context.register(Config4.class, Config3.class);
		context.refresh();
		StateMachineFactory<String, String> factory = resolveFactory(context);
		StateMachine<String, String> machine = factory.getStateMachine();
		assertThat(machine, notNullValue());
		doStartAndAssert(machine);
		assertThat(machine.getState().getIds(), containsInAnyOrder("S1"));
		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S20"));
		doSendEventAndConsumeAll(machine, "E2");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21", "S30"));
		doSendEventAndConsumeAll(machine, "E3");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2", "S21", "S31"));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		@Qualifier("subStateMachine")
		private StateMachine<String, String> subStateMachine;

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2", subStateMachine);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2").event("E1");
		}

	}

	@Configuration
	@EnableStateMachine(name = "subStateMachine")
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S20")
					.state("S21")
					.and()
					.withStates()
						.parent("S21")
						.initial("S30")
						.state("S31");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S20").target("S21").event("E2").and()
				.withExternal()
					.source("S30").target("S31").event("E3");
		}

	}

	@Configuration
	@EnableStateMachineFactory
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		@Qualifier("subStateMachineFactory")
		private StateMachineFactory<String, String> subStateMachineFactory;

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2", subStateMachineFactory);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2").event("E1");
		}

	}

	@Configuration
	@EnableStateMachineFactory(name = "subStateMachineFactory")
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S20")
					.state("S21")
					.and()
					.withStates()
						.parent("S21")
						.initial("S30")
						.state("S31");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S20").target("S21").event("E2").and()
				.withExternal()
					.source("S30").target("S31").event("E3");
		}

	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
