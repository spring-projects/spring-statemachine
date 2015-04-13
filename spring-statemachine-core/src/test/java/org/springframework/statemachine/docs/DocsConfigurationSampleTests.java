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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

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
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
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
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.state(States.S1)
					.and()
					.withStates()
						.parent(States.S1)
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
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.states(EnumSet.allOf(States.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S1).target(States.S2)
					.event(Events.E1)
					.and()
				.withInternal()
					.source(States.S2)
					.event(Events.E2)
					.and()
				.withLocal()
					.source(States.S2).target(States.S3)
					.event(Events.E3);
		}

	}
// end::snippetC[]

// tag::snippetD[]
	@Configuration
	@EnableStateMachine
	public static class Config4 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S1).target(States.S2)
					.event(Events.E1)
					.guard(guard())
					.and()
				.withExternal()
					.source(States.S2).target(States.S3)
					.event(Events.E2)
					.guardExpression("true");

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
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
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
	public static class Config6
		extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.end(States.SF)
					.states(EnumSet.allOf(States.class));
		}

	}
// end::snippetF[]


// tag::snippetG[]
	static class StateMachineApplicationEventListener
		implements ApplicationListener<StateMachineEvent> {

		@Override
		public void onApplicationEvent(StateMachineEvent event) {
		}
	}
// end::snippetG[]

// tag::snippetH[]
	static class StateMachineEventListener
		extends StateMachineListenerAdapter<States, Events> {

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
		}

		@Override
		public void stateEntered(State<States, Events> state) {
		}

		@Override
		public void stateExited(State<States, Events> state) {
		}

		@Override
		public void transition(Transition<States, Events> transition) {
		}

		@Override
		public void transitionStarted(Transition<States, Events> transition) {
		}

		@Override
		public void transitionEnded(Transition<States, Events> transition) {
		}

		@Override
		public void stateMachineStarted(StateMachine<States, Events> stateMachine) {
		}

		@Override
		public void stateMachineStopped(StateMachine<States, Events> stateMachine) {
		}
	}
// end::snippetH[]

// tag::snippetI[]
	@WithStateMachine
	static class Bean1 {

		@OnTransition(source = "S1", target = "S2")
		public void fromS1ToS2() {
		}
	}
// end::snippetI[]

// tag::snippetJ[]
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@OnTransition
	static @interface StatesOnTransition {

		States[] source() default {};

		States[] target() default {};
	}
// end::snippetJ[]

// tag::snippetK[]
	@WithStateMachine
	static class Bean2 {

		@StatesOnTransition(source = States.S1, target = States.S2)
		public void fromS1ToS2() {
		}
	}
// end::snippetK[]

// tag::snippetL[]
	static class Bean3 {

		@Autowired
		StateMachineFactory<States, Events> factory;

		void method() {
			StateMachine<States,Events> stateMachine = factory.getStateMachine();
			stateMachine.start();
		}
	}
// end::snippetL[]

// tag::snippetM[]
	static class Config7 {

		@Autowired
		StateMachine<States, Events> stateMachine;

		@Bean
		public StateMachineEventListener stateMachineEventListener() {
			StateMachineEventListener listener = new StateMachineEventListener();
			stateMachine.addStateListener(listener);
			return listener;
		}

	}
// end::snippetM[]

// tag::snippetN[]
	@Configuration
	@EnableStateMachine(contextEvents = false)
	public static class Config8
		extends EnumStateMachineConfigurerAdapter<States, Events> {
	}

	@Configuration
	@EnableStateMachineFactory(contextEvents = false)
	public static class Config9
		extends EnumStateMachineConfigurerAdapter<States, Events> {
	}
// end::snippetN[]

	static class DummyShowSendEvent {

// tag::snippetO[]
		@Autowired
		StateMachine<States, Events> stateMachine;

		void signalMachine() {
			stateMachine.sendEvent(Events.E1);

			Message<Events> message = MessageBuilder
					.withPayload(Events.E2)
					.setHeader("foo", "bar")
					.build();
			stateMachine.sendEvent(message);
		}
// end::snippetO[]

	}

}
