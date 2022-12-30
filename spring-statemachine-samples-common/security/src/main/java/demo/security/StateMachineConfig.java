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
package demo.security;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;

@Configuration
public class StateMachineConfig {

	private static final Log log = LogFactory.getLog(StateMachineConfig.class);

//tag::snippetE[]
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(securedEnabled = true)
	static class SecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
			auth
				.inMemoryAuthentication()
					.withUser("user")
						.password("password")
						.roles("USER")
						.and()
					.withUser("admin")
						.password("password")
						.roles("USER", "ADMIN");
		}
	}
//end::snippetE[]

	@Configuration
	@EnableStateMachine
	static class Config
			extends EnumStateMachineConfigurerAdapter<States, Events> {

//tag::snippetA[]
		@Override
		public void configure(StateMachineConfigurationConfigurer<States, Events> config)
				throws Exception {
			config
				.withConfiguration()
					.autoStartup(true)
					.and()
				.withSecurity()
					.enabled(true)
					.event("hasRole('USER')");
		}
//end::snippetA[]

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0)
					.states(EnumSet.allOf(States.class));
		}

//tag::snippetB[]
		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S0).target(States.S1).event(Events.A)
					.and()
				.withExternal()
					.source(States.S1).target(States.S2).event(Events.B)
					.and()
				.withExternal()
					.source(States.S2).target(States.S0).event(Events.C)
					.and()
				.withExternal()
					.source(States.S2).target(States.S3).event(Events.E)
					.secured("ROLE_ADMIN", ComparisonType.ANY)
					.and()
				.withExternal()
					.source(States.S3).target(States.S0).event(Events.C)
					.and()
				.withInternal()
					.source(States.S0).event(Events.D)
					.action(adminAction())
					.and()
				.withInternal()
					.source(States.S1).event(Events.F)
					.action(transitionAction())
					.secured("ROLE_ADMIN", ComparisonType.ANY);
		}
//end::snippetB[]

//tag::snippetC[]
		@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
		@Bean
		public Action<States, Events> adminAction() {
			return new Action<States, Events>() {

				@Secured("ROLE_ADMIN")
				@Override
				public void execute(StateContext<States, Events> context) {
					log.info("Executed only for admin role");
				}
			};
		}
//end::snippetC[]

//tag::snippetD[]
		@Bean
		public Action<States, Events> transitionAction() {
			return new Action<States, Events>() {

				@Override
				public void execute(StateContext<States, Events> context) {
					log.info("Executed only for admin role");
				}
			};
		}
//end::snippetD[]
	}

	@Bean
	public String stateChartModel() throws IOException {
		ClassPathResource model = new ClassPathResource("statechartmodel.txt");
		InputStream inputStream = model.getInputStream();
		Scanner scanner = new Scanner(inputStream);
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return content;
	}

	public enum States {
		S0, S1, S2, S3;
	}

	public enum Events {
		A, B, C, D, E, F;
	}

}
