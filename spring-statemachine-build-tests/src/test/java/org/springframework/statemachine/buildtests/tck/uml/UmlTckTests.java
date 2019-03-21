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
package org.springframework.statemachine.buildtests.tck.uml;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.buildtests.tck.AbstractTckTests;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

/**
 * Tck tests for machine configs build from uml.
 *
 * @author Janne Valkealahti
 *
 */
public class UmlTckTests extends AbstractTckTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Override
	protected StateMachine<String, String> getSimpleMachine() {
		context.register(SimpleMachineConfig.class);
		context.refresh();
		return getStateMachineFromContext();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		context.register(SimpleSubMachineConfig.class);
		context.refresh();
		return getStateMachineFromContext();
	}

	@Override
	protected StateMachine<String, String> getShowcaseMachine() throws Exception {
		context.register(ShowcaseMachineConfig.class);
		context.refresh();
		return getStateMachineFromContext();
	}

	@Configuration
	@EnableStateMachine
	public static class SimpleMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/tck/uml/SimpleMachine.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class SimpleSubMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/tck/uml/SimpleSubMachine.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class ShowcaseMachineConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/tck/uml/ShowcaseMachine.uml");
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
