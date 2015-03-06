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
package org.springframework.statemachine.docs;

import java.util.EnumSet;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.guard.Guard;

/**
 * Tests for state machine configuration.
 *
 * @author Janne Valkealahti
 *
 */
public class DocsConfigurationSampleTests extends AbstractStateMachineTests {

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.end(States.SF)
					.states(EnumSet.allOf(States.class));
		}

	}
// end::snippetA[]

// tag::snippetB[]
	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.end(States.SF)
					.states(EnumSet.allOf(States.class))
					.and()
					.withStates()
						.initial(States.S2)
						.state(States.S2);
		}

	}
// end::snippetB[]

// tag::snippetC[]
	@Configuration
	@EnableStateMachine
	public static class Config3 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.end(States.SF)
					.states(EnumSet.allOf(States.class))
					.and()
					.withStates()
						.initial(States.S2)
						.state(States.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
			transitions
				.withExternal()
					.and()
				.withInternal()
					.and()
				.withLocal();
		}

	}
// end::snippetC[]

// tag::snippetD[]
	@Configuration
	@EnableStateMachine
	public static class Config4 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
			transitions
				.withExternal()
					.source(States.S1)
					.target(States.S2)
					.event(Events.E1)
					.guard(guard());
		}

		@Bean
		public Guard<States, Events> guard() {
			return new Guard<States, Events>() {

				@Override
				public boolean evaluate(StateContext<States, Events> context) {
					return true;
				}
			};
		}

	}
// end::snippetD[]

// tag::snippetE[]
	@Configuration
	@EnableStateMachine
	public static class Config5 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
			transitions
				.withExternal()
					.source(States.S1)
					.target(States.S2)
					.event(Events.E1)
					.action(action());
		}

		@Bean
		public Action<States, Events> action() {
			return new Action<States, Events>() {

				@Override
				public void execute(StateContext<States, Events> context) {
					// do something
				}
			};
		}

	}
// end::snippetE[]

// tag::snippetF[]
	@Configuration
	@EnableStateMachineFactory
	public static class Config6 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.end(States.SF)
					.states(EnumSet.allOf(States.class));
		}

	}
// end::snippetF[]


// tag::snippetG[]
	static class StateMachineEventListener implements ApplicationListener<StateMachineEvent> {

		@Override
		public void onApplicationEvent(StateMachineEvent event) {
		}
	}
// end::snippetG[]

}
