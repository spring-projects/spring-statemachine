/*
 * Copyright 2016 the original author or authors.
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class ExitEntryStateTests extends AbstractStateMachineTests {

	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleEntryExit() {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine, notNullValue());
		machine.start();
		assertThat(machine.getState().getIds(), contains("S1"));
		machine.sendEvent("ENTRY1");
		assertThat(machine.getState().getIds(), contains("S2", "S22"));
		machine.sendEvent("EXIT1");
		assertThat(machine.getState().getIds(), contains("S4"));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			// entry point takes to S22 instead of initial S21
			// exit point takes to S4 instead from S2 to S3
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3")
					.state("S4")
					.state("S5")
					.and()
					.withStates()
						.parent("S2")
						.initial("S21")
						.entry("S2ENTRY1")
						.entry("S2ENTRY2")
						.exit("S2EXIT1")
						.exit("S2EXIT2")
						.state("S22")
						.state("S23");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2").target("S3")
					.event("E2")
					.and()
				.withExternal()
					.source("S1").target("S2ENTRY1")
					.event("ENTRY1")
					.and()
				.withExternal()
					.source("S1").target("S2ENTRY2")
					.event("ENTRY2")
					.and()
				.withExternal()
					.source("S22").target("S2EXIT1")
					.event("EXIT1")
					.and()
				.withExternal()
					.source("S22").target("S2EXIT2")
					.event("EXIT2")
					.and()
				.withEntry()
					.source("S2ENTRY1").target("S22")
					.and()
				.withEntry()
					.source("S2ENTRY2").target("S23")
					.and()
				.withExit()
					.source("S2EXIT1").target("S4")
					.and()
				.withExit()
					.source("S2EXIT2").target("S5");
		}
	}


	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
