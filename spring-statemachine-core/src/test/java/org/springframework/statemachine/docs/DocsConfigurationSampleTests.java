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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer.History;
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

// tag::snippetAA[]
	@Configuration
	@EnableStateMachine
	public static class Config1Enums
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
// end::snippetAA[]

// tag::snippetAB[]
	@Configuration
	@EnableStateMachine
	public static class Config1Strings
			extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S1")
					.end("SF")
					.states(new HashSet<String>(Arrays.asList("S1","S2","S3","S4")));
		}

	}
// end::snippetAB[]

// tag::snippetB[]
	@Configuration
	@EnableStateMachine
	public static class Config2
			extends EnumStateMachineConfigurerAdapter<States, Events> {

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
	public static class Config3
			extends EnumStateMachineConfigurerAdapter<States, Events> {

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
	public static class Config4
			extends EnumStateMachineConfigurerAdapter<States, Events> {

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
	public static class Config5
			extends EnumStateMachineConfigurerAdapter<States, Events> {

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

// tag::snippetFA[]
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
// end::snippetFA[]

// tag::snippetFB[]
	StateMachine<String, String> buildMachine() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial("S1")
				.end("SF")
				.states(new HashSet<String>(Arrays.asList("S1","S2","S3","S4")));
		return builder.build();
	}
// end::snippetFB[]

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

// tag::snippetP[]
	@Configuration
	@EnableStateMachine
	public static class Config10
			extends EnumStateMachineConfigurerAdapter<States2, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States2, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States2.S1)
					.state(States2.S2)
					.and()
					.withStates()
						.parent(States2.S2)
						.initial(States2.S2I)
						.state(States2.S21)
						.end(States2.S2F)
						.and()
					.withStates()
						.parent(States2.S2)
						.initial(States2.S3I)
						.state(States2.S31)
						.end(States2.S3F);
		}

	}
// end::snippetP[]

// tag::snippetQ[]
	@Configuration
	@EnableStateMachine
	public static class Config11
			extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S1, initialAction())
					.end(States.SF)
					.states(EnumSet.allOf(States.class));
		}

		@Bean
		public Action<States, Events> initialAction() {
			return new Action<States, Events>() {

				@Override
				public void execute(StateContext<States, Events> context) {
					// do something initially
				}
			};
		}

	}
// end::snippetQ[]

// tag::snippetR[]
	@Configuration
	@EnableStateMachine
	public static class Config12
			extends EnumStateMachineConfigurerAdapter<States3, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States3, Events> states)
				throws Exception {
			states
			.withStates()
				.initial(States3.S1)
				.state(States3.S2)
				.and()
				.withStates()
					.parent(States3.S2)
					.initial(States3.S2I)
					.state(States3.S21)
					.state(States3.S22)
					.history(States3.SH, History.SHALLOW);
		}

	}
// end::snippetR[]

// tag::snippetS[]
	@Configuration
	@EnableStateMachine
	public static class Config13
			extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.SI)
					.choice(States.S1)
					.end(States.SF)
					.states(EnumSet.allOf(States.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withChoice()
					.source(States.S1)
					.first(States.S2, s2Guard())
					.then(States.S3, s3Guard())
					.last(States.S4);
		}

		@Bean
		public Guard<States, Events> s2Guard() {
			return new Guard<States, Events>() {

				@Override
				public boolean evaluate(StateContext<States, Events> context) {
					return false;
				}
			};
		}

		@Bean
		public Guard<States, Events> s3Guard() {
			return new Guard<States, Events>() {

				@Override
				public boolean evaluate(StateContext<States, Events> context) {
					return true;
				}
			};
		}

	}
// end::snippetS[]

// tag::snippetT[]
	@Configuration
	@EnableStateMachine
	public static class Config14
			extends EnumStateMachineConfigurerAdapter<States2, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States2, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States2.S1)
					.fork(States2.S2)
					.state(States2.S3)
					.and()
					.withStates()
						.parent(States2.S3)
						.initial(States2.S2I)
						.state(States2.S21)
						.state(States2.S22)
						.end(States2.S2F)
						.and()
					.withStates()
						.parent(States2.S3)
						.initial(States2.S3I)
						.state(States2.S31)
						.state(States2.S32)
						.end(States2.S3F);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States2, Events> transitions)
				throws Exception {
			transitions
				.withFork()
					.source(States2.S2)
					.target(States2.S22)
					.target(States2.S32);
		}

	}
// end::snippetT[]

// tag::snippetU[]
	@Configuration
	@EnableStateMachine
	public static class Config15
			extends EnumStateMachineConfigurerAdapter<States2, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States2, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States2.S1)
					.state(States2.S3)
					.join(States2.S4)
					.and()
					.withStates()
						.parent(States2.S3)
						.initial(States2.S2I)
						.state(States2.S21)
						.state(States2.S22)
						.end(States2.S2F)
						.and()
					.withStates()
						.parent(States2.S3)
						.initial(States2.S3I)
						.state(States2.S31)
						.state(States2.S32)
						.end(States2.S3F);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States2, Events> transitions)
				throws Exception {
			transitions
				.withJoin()
					.source(States2.S2F)
					.source(States2.S3F)
					.target(States2.S5);
		}

	}
// end::snippetU[]

	@Configuration
	@EnableStateMachine
	public static class Config16
			extends EnumStateMachineConfigurerAdapter<States, Events> {

// tag::snippetVA[]
		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.SI)
					.state(States.S1, action1(), action2())
					.state(States.S2, action1(), action2())
					.state(States.S3, action1(), action3());
		}
// end::snippetVA[]

// tag::snippetVB[]
		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.SI).target(States.S1)
					.event(Events.E1)
					.guard(guard1())
					.and()
				.withExternal()
					.source(States.S1).target(States.S2)
					.event(Events.E1)
					.guard(guard2())
					.and()
				.withExternal()
					.source(States.S2).target(States.S3)
					.event(Events.E2)
					.guardExpression("extendedState.variables.get('myvar')");
		}
// end::snippetVB[]

// tag::snippetVC[]
		@Bean
		public Guard<States, Events> guard1() {
			return new Guard<States, Events>() {

				@Override
				public boolean evaluate(StateContext<States, Events> context) {
					return true;
				}
			};
		}

		@Bean
		public BaseGuard guard2() {
			return new BaseGuard();
		}

		static class BaseGuard implements Guard<States, Events> {

			@Override
			public boolean evaluate(StateContext<States, Events> context) {
				return false;
			}
		}
// end::snippetVC[]

// tag::snippetVD[]
		@Bean
		public Action<States, Events> action1() {
			return new Action<States, Events>() {

				@Override
				public void execute(StateContext<States, Events> context) {
				}
			};
		}

		@Bean
		public BaseAction action2() {
			return new BaseAction();
		}

		@Bean
		public SpelAction action3() {
			ExpressionParser parser = new SpelExpressionParser();
			return new SpelAction(
					parser.parseExpression(
							"stateMachine.sendEvent(T(org.springframework.statemachine.docs.Events).E1)"));
		}

		static class BaseAction implements Action<States, Events> {

			@Override
			public void execute(StateContext<States, Events> context) {
			}
		}

		static class SpelAction extends SpelExpressionAction<States, Events> {

			public SpelAction(Expression expression) {
				super(expression);
			}
		}

// end::snippetVD[]

	}

}
