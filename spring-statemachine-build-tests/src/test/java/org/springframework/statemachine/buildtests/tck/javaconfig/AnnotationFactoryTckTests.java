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
package org.springframework.statemachine.buildtests.tck.javaconfig;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.buildtests.tck.AbstractTckTests;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tck tests for annotation factory based javaconfig.
 *
 * @author Janne Valkealahti
 *
 */
public class AnnotationFactoryTckTests extends AbstractTckTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Override
	protected StateMachine<String, String> getSimpleMachine() {
		context.register(SimpleMachineConfig.class);
		context.refresh();
		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		context.register(SimpleSubMachineConfig.class);
		context.refresh();
		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getShowcaseMachine() throws Exception {
		context.register(ShowcaseMachineConfig.class);
		context.refresh();
		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Configuration
	@EnableStateMachineFactory
	static class SimpleMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3");
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
					.event("E2");
		}
	}

	@Configuration
	@EnableStateMachineFactory
	static class SimpleSubMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3")
					.and()
					.withStates()
						.parent("S2")
						.initial("S21")
						.state("S22");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S21").target("S22")
					.event("E2")
					.and()
				.withExternal()
					.source("S2").target("S3")
					.event("E3");
		}
	}

	@Configuration
	@EnableStateMachineFactory
	static class ShowcaseMachineConfig extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0", fooAction())
					.state("S0")
					.and()
					.withStates()
						.parent("S0")
						.initial("S1")
						.state("S1")
						.and()
						.withStates()
							.parent("S1")
							.initial("S11")
							.state("S11")
							.state("S12")
							.and()
					.withStates()
						.parent("S0")
						.state("S2")
						.and()
						.withStates()
							.parent("S2")
							.initial("S21")
							.state("S21")
							.and()
							.withStates()
								.parent("S21")
								.initial("S211")
								.state("S211")
								.state("S212");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S1").event("A")
					.guard(foo1Guard())
					.and()
				.withExternal()
					.source("S1").target("S11").event("B")
					.and()
				.withExternal()
					.source("S21").target("S211").event("B")
					.and()
				.withExternal()
					.source("S1").target("S2").event("C")
					.and()
				.withExternal()
					.source("S2").target("S1").event("K")
					.and()
				.withExternal()
					.source("S1").target("S0").event("D")
					.and()
				.withExternal()
					.source("S211").target("S21").event("D")
					.and()
				.withExternal()
					.source("S0").target("S211").event("E")
					.and()
				.withExternal()
					.source("S1").target("S211").event("F")
					.and()
				.withExternal()
					.source("S2").target("S11").event("F")
					.and()
				.withExternal()
					.source("S11").target("S211").event("G")
					.and()
				.withExternal()
					.source("S211").target("S0").event("G")
					.and()
				.withInternal()
					.source("S0").event("H")
					.guard(foo0Guard())
					.action(fooAction())
					.and()
				.withInternal()
					.source("S2").event("H")
					.guard(foo1Guard())
					.action(fooAction())
					.and()
				.withInternal()
					.source("S1").event("H")
					.and()
				.withExternal()
					.source("S11").target("S12").event("I")
					.and()
				.withExternal()
					.source("S211").target("S212").event("I")
					.and()
				.withExternal()
					.source("S12").target("S212").event("I")
					.and()
				.withInternal()
					.source("S11").event("J");
		}

		@Bean
		public FooGuard foo0Guard() {
			return new FooGuard(0);
		}

		@Bean
		public FooGuard foo1Guard() {
			return new FooGuard(1);
		}

		@Bean
		public FooAction fooAction() {
			return new FooAction();
		}
	}
}
