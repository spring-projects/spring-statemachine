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
package org.springframework.statemachine.docs;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class DocsConfigurationSampleTests10 extends AbstractStateMachineTests {

	@Test
	public void testConfig1() throws Exception {
		context.register(Config1.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder("S1"));
		assertThat(machine.getId(), is("mymachine"));
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Test
	public void testConfig2() throws Exception {
		context.register(Config2.class);
		context.refresh();
		@SuppressWarnings("unchecked")
// tag::snippetB[]
		StateMachineFactory<String, String> factory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> machine = factory.getStateMachine("mymachine");
// end::snippetB[]
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder("S1"));
		assertThat(machine.getId(), is("mymachine"));
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

// tag::snippetA[]
		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withConfiguration()
					.machineId("mymachine");
		}
// end::snippetA[]

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
					.event("E1");
		}
	}

	@Configuration
	@EnableStateMachineFactory
	public static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withConfiguration()
					.machineId("mymachine");
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
					.event("E1");
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
