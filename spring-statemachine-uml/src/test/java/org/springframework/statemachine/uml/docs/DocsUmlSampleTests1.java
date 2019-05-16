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
package org.springframework.statemachine.uml.docs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

public class DocsUmlSampleTests1 {

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/docs/simple-machine.uml");
		}
	}
// end::snippetA[]

// tag::snippetB[]
	@Configuration
	@EnableStateMachine
	public static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			UmlStateMachineModelFactory factory = new UmlStateMachineModelFactory(
					"classpath:org/springframework/statemachine/uml/docs/simple-machine.uml");
			factory.setStateMachineComponentResolver(stateMachineComponentResolver());
			return factory;
		}

		@Bean
		public StateMachineComponentResolver<String, String> stateMachineComponentResolver() {
			DefaultStateMachineComponentResolver<String, String> resolver = new DefaultStateMachineComponentResolver<>();
			resolver.registerAction("myAction", myAction());
			resolver.registerGuard("myGuard", myGuard());
			return resolver;
		}

		public Action<String, String> myAction() {
			return new Action<String, String>() {

				@Override
				public void execute(StateContext<String, String> context) {
				}
			};
		}

		public Guard<String, String> myGuard() {
			return new Guard<String, String>() {

				@Override
				public boolean evaluate(StateContext<String, String> context) {
					return false;
				}
			};
		}
	}
// end::snippetB[]
}
