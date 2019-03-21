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
package org.springframework.statemachine.docs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;

public class DocsConfigurationSampleTests3 {

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.enabled(true)
					.event("true")
					.event("ROLE_ANONYMOUS", ComparisonType.ANY);
		}
	}
// end::snippetA[]

// tag::snippetB[]
	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A")
					.secured("ROLE_ANONYMOUS", ComparisonType.ANY)
					.secured("hasTarget('S1')");
		}
	}
// end::snippetB[]


// tag::snippetC[]
	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.enabled(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.action(securedAction())
					.event("A");
		}

		@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
		@Bean
		public Action<String, String> securedAction() {
			return new Action<String, String>() {

				@Secured("ROLE_ANONYMOUS")
				@Override
				public void execute(StateContext<String, String> context) {
				}
			};
		}

	}
// end::snippetC[]

// tag::snippetD[]
		@Configuration
		@EnableStateMachine
		static class Config4 extends StateMachineConfigurerAdapter<String, String> {

			@Override
			public void configure(StateMachineConfigurationConfigurer<String, String> config)
					throws Exception {
				config
					.withSecurity()
						.enabled(true)
						.transitionAccessDecisionManager(null)
						.eventAccessDecisionManager(null);
			}
		}
// end::snippetD[]

// tag::snippetE[]
		@Configuration
		@EnableGlobalMethodSecurity(securedEnabled = true)
		public static class Config5 extends WebSecurityConfigurerAdapter {

			@Autowired
			public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
				auth
					.inMemoryAuthentication()
						.withUser("user").password("password").roles("USER");
			}
		}
// end::snippetE[]

// tag::snippetF[]
		@Configuration
		@EnableStateMachine
		static class Config6 extends StateMachineConfigurerAdapter<String, String> {

			@Override
			public void configure(StateMachineConfigurationConfigurer<String, String> config)
					throws Exception {
				config
					.withSecurity()
						.enabled(true)
						.transition("true")
						.transition("ROLE_ANONYMOUS", ComparisonType.ANY);
			}
		}
// end::snippetF[]

}
