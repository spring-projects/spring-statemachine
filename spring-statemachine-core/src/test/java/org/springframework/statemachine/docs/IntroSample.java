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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class IntroSample {

// tag::snippetA[]
	public enum States {
		STATE1, STATE2
	}

	public enum Events {
		EVENT1, EVENT2
	}
// end::snippetA[]

// tag::snippetB[]
	@Configuration
	@EnableStateMachine
	public class Config1 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.STATE1)
					.states(EnumSet.allOf(States.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.STATE1).target(States.STATE2)
					.event(Events.EVENT1)
					.and()
				.withExternal()
					.source(States.STATE2).target(States.STATE1)
					.event(Events.EVENT2);
		}
	}
// end::snippetB[]

// tag::snippetC[]
	@WithStateMachine
	public class MyBean {

		@OnTransition(target = "STATE1")
		void toState1() {
		}

		@OnTransition(target = "STATE2")
		void toState2() {
		}
	}
// end::snippetC[]

// tag::snippetD[]
	public class MyApp {

		@Autowired
		StateMachine<States, Events> stateMachine;

		void doSignals() {
			stateMachine.sendEvent(Events.EVENT1);
			stateMachine.sendEvent(Events.EVENT2);
		}
	}
// end::snippetD[]

	public void testManual() throws Exception {
		StateMachine<States, Events> stateMachine = buildMachine();
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(States.STATE1));
		stateMachine.sendEvent(Events.EVENT1);
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(States.STATE2));
		stateMachine.sendEvent(Events.EVENT2);
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(States.STATE1));
	}

	public StateMachine<States, Events> buildMachine() throws Exception {
		Builder<States, Events> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial(States.STATE1)
				.states(EnumSet.allOf(States.class));
		builder.configureTransitions()
			.withExternal()
				.source(States.STATE1).target(States.STATE2)
				.event(Events.EVENT1)
				.and()
			.withExternal()
				.source(States.STATE2).target(States.STATE1)
				.event(Events.EVENT2);
		return builder.build();
	}
}
