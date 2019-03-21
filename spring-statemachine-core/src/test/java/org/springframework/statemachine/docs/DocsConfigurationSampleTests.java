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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.annotation.EventHeaders;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer.History;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.event.OnExtendedStateChanged;
import org.springframework.statemachine.event.OnStateMachineError;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
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
	public class Config1Enums
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
	public class Config1Strings
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
	public class Config2
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
	public class Config3
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
	public class Config4
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

// tag::snippetEA[]
	@Configuration
	@EnableStateMachine
	public class Config51
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
// end::snippetEA[]

// tag::snippetEB[]
		@Configuration
		@EnableStateMachine
		public class Config52
				extends EnumStateMachineConfigurerAdapter<States, Events> {

			@Override
			public void configure(StateMachineStateConfigurer<States, Events> states)
					throws Exception {
				states
					.withStates()
						.initial(States.S1, action())
						.state(States.S1, action(), null)
						.state(States.S2, null, action())
						.state(States.S3, action(), action());
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
// end::snippetEB[]

// tag::snippetFA[]
	@Configuration
	@EnableStateMachineFactory
	public class Config6
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
	StateMachine<String, String> buildMachine1() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial("S1")
				.end("SF")
				.states(new HashSet<String>(Arrays.asList("S1","S2","S3","S4")));
		return builder.build();
	}
// end::snippetFB[]

// tag::snippetFC[]
	StateMachine<String, String> buildMachine2() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureConfiguration()
			.withConfiguration()
				.autoStartup(false)
				.beanFactory(null)
				.taskExecutor(null)
				.taskScheduler(null)
				.listener(null);
		return builder.build();
	}
// end::snippetFC[]

// tag::snippetG[]
	public class StateMachineApplicationEventListener
			implements ApplicationListener<StateMachineEvent> {

		@Override
		public void onApplicationEvent(StateMachineEvent event) {
		}
	}

	@Configuration
	public class ListenerConfig {

		@Bean
		public StateMachineApplicationEventListener contextListener() {
			return new StateMachineApplicationEventListener();
		}
	}
// end::snippetG[]

// tag::snippetGG[]
	@Configuration
	@EnableStateMachine
	public class ManualBuilderConfig {

		@Bean
		public StateMachine<String, String> stateMachine() throws Exception {

			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureStates()
				.withStates()
					.initial("S1")
					.state("S2");
			builder.configureTransitions()
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
			return builder.build();
		}
	}
// end::snippetGG[]

// tag::snippetH[]
	public class StateMachineEventListener
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

		@Override
		public void eventNotAccepted(Message<Events> event) {
		}

		@Override
		public void extendedStateChanged(Object key, Object value) {
		}

		@Override
		public void stateMachineError(StateMachine<States, Events> stateMachine, Exception exception) {
		}
	}
// end::snippetH[]

// tag::snippetI[]
	@WithStateMachine
	public class Bean1 {

		@OnTransition(source = "S1", target = "S2")
		public void fromS1ToS2() {
		}

		@OnTransition
		public void anyTransition() {
		}
	}
// end::snippetI[]

// tag::snippetII[]
	@WithStateMachine
	public class Bean4 {

		@StatesOnTransition(source = States.S1, target = States.S2)
		public void fromS1ToS2(@EventHeaders Map<String, Object> headers, ExtendedState extendedState) {
		}
	}
// end::snippetII[]

// tag::snippetJ[]
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@OnTransition
	public @interface StatesOnTransition {

		States[] source() default {};

		States[] target() default {};
	}
// end::snippetJ[]

// tag::snippetK[]
	@WithStateMachine
	public class Bean2 {

		@StatesOnTransition(source = States.S1, target = States.S2)
		public void fromS1ToS2() {
		}
	}
// end::snippetK[]

// tag::snippetL[]
	public class Bean3 {

		@Autowired
		StateMachineFactory<States, Events> factory;

		void method() {
			StateMachine<States,Events> stateMachine = factory.getStateMachine();
			stateMachine.start();
		}
	}
// end::snippetL[]

// tag::snippetM[]
	public class Config7 {

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
	public class Config8
			extends EnumStateMachineConfigurerAdapter<States, Events> {
	}

	@Configuration
	@EnableStateMachineFactory(contextEvents = false)
	public class Config9
			extends EnumStateMachineConfigurerAdapter<States, Events> {
	}
// end::snippetN[]

	public class DummyShowSendEvent {

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
	public class Config10
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
	public class Config11
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
	public class Config12
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
	public class Config13
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
	public class Config14
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
	public class Config15
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
	public class Config16
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

		public class BaseGuard implements Guard<States, Events> {

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

		public class BaseAction implements Action<States, Events> {

			@Override
			public void execute(StateContext<States, Events> context) {
			}
		}

		public class SpelAction extends SpelExpressionAction<States, Events> {

			public SpelAction(Expression expression) {
				super(expression);
			}
		}

// end::snippetVD[]

	}

// tag::snippetYA[]
		@Configuration
		@EnableStateMachine
		public class Config17
				extends EnumStateMachineConfigurerAdapter<States, Events> {

			@Override
			public void configure(StateMachineConfigurationConfigurer<States, Events> config)
					throws Exception {
				config
					.withConfiguration()
						.autoStartup(true)
						.beanFactory(new StaticListableBeanFactory())
						.taskExecutor(new SyncTaskExecutor())
						.taskScheduler(new ConcurrentTaskScheduler())
						.listener(new StateMachineListenerAdapter<States, Events>());
			}

		}
// end::snippetYA[]

// tag::snippetYB[]
		@Configuration
		@EnableStateMachine
		public class Config18
				extends EnumStateMachineConfigurerAdapter<States, Events> {

			@Override
			public void configure(StateMachineConfigurationConfigurer<States, Events> config)
					throws Exception {
				config
					.withDistributed()
						.ensemble(stateMachineEnsemble());
			}

			@Bean
			public StateMachineEnsemble<States, Events> stateMachineEnsemble()
					throws Exception {
				// naturally not null but should return ensemble instance
				return null;
			}

		}
// end::snippetYB[]

		public class AccessorSamples {

			StateMachine<String, String> stateMachine = null;

			void s1() {
// tag::snippetZA[]
				stateMachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<String,String>>() {

					@Override
					public void apply(StateMachineAccess<String, String> function) {
						function.setRelay(stateMachine);
					}
				});

				stateMachine.getStateMachineAccessor()
					.doWithAllRegions(access -> access.setRelay(stateMachine));
// end::snippetZA[]
			}

			void s2() {
// tag::snippetZB[]
				stateMachine.getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<String,String>>() {

					@Override
					public void apply(StateMachineAccess<String, String> function) {
						function.setRelay(stateMachine);
					}
				});

				stateMachine.getStateMachineAccessor()
					.doWithRegion(access -> access.setRelay(stateMachine));
// end::snippetZB[]
			}

			void s3() {
// tag::snippetZC[]
				for (StateMachineAccess<String, String> access : stateMachine.getStateMachineAccessor().withAllRegions()) {
					access.setRelay(stateMachine);
				}

				stateMachine.getStateMachineAccessor().withAllRegions()
					.stream().forEach(access -> access.setRelay(stateMachine));
// end::snippetZC[]
			}

			void s4() {
// tag::snippetZD[]
				stateMachine.getStateMachineAccessor()
					.withRegion().setRelay(stateMachine);
// end::snippetZD[]
			}

		}

		public class InterceptorSamples {

			StateMachine<String, String> stateMachine = null;

			void s1() {
// tag::snippetZH[]
				stateMachine.getStateMachineAccessor()
					.withRegion().addStateMachineInterceptor(new StateMachineInterceptor<String, String>() {

						@Override
						public StateContext<String, String> preTransition(StateContext<String, String> stateContext) {
							return stateContext;
						}

						@Override
						public void preStateChange(State<String, String> state, Message<String> message,
								Transition<String, String> transition, StateMachine<String, String> stateMachine) {
						}

						@Override
						public StateContext<String, String> postTransition(StateContext<String, String> stateContext) {
							return stateContext;
						}

						@Override
						public void postStateChange(State<String, String> state, Message<String> message,
								Transition<String, String> transition, StateMachine<String, String> stateMachine) {
						}

						@Override
						public Exception stateMachineError(StateMachine<String, String> stateMachine,
								Exception exception) {
							return exception;
						}
					});
// end::snippetZH[]
			}

		}

		@SuppressWarnings("unused")
		private class InterceptorAddExample {

// tag::snippet1[]
			StateMachine<String, String> stateMachine;

			void addInterceptor() {
				stateMachine.getStateMachineAccessor()
					.doWithRegion(new StateMachineFunction<StateMachineAccess<String, String>>() {

					@Override
					public void apply(StateMachineAccess<String, String> function) {
						function.addStateMachineInterceptor(
								new StateMachineInterceptorAdapter<String, String>() {
							@Override
							public Exception stateMachineError(StateMachine<String, String> stateMachine,
									Exception exception) {
								// return null indicating handled error
								return exception;
							}
						});
					}
				});

			}
// end::snippet1[]
		}

// tag::snippet2[]
		public class ErrorStateMachineListener
				extends StateMachineListenerAdapter<String, String> {

			@Override
			public void stateMachineError(StateMachine<String, String> stateMachine, Exception exception) {
	    		// do something with error
			}
		}
// end::snippet2[]

// tag::snippet3[]
		public class GenericApplicationEventListener
				implements ApplicationListener<StateMachineEvent> {

		    @Override
		    public void onApplicationEvent(StateMachineEvent event) {
		    	if (event instanceof OnStateMachineError) {
		    		// do something with error
		    	}
		    }
		}
// end::snippet3[]

// tag::snippet4[]
		public class ErrorApplicationEventListener
				implements ApplicationListener<OnStateMachineError> {

			@Override
			public void onApplicationEvent(OnStateMachineError event) {
	    		// do something with error
			}
		}
// end::snippet4[]

// tag::snippet5[]
		public class ExtendedStateVariableListener
				extends StateMachineListenerAdapter<String, String> {

			@Override
			public void extendedStateChanged(Object key, Object value) {
				// do something with changed variable
			}
		}
// end::snippet5[]

// tag::snippet6[]
		public class ExtendedStateVariableEventListener
				implements ApplicationListener<OnExtendedStateChanged> {

			@Override
			public void onApplicationEvent(OnExtendedStateChanged event) {
				// do something with changed variable
			}
		}
// end::snippet6[]

		public class ExtendedStateVariableActionSample {

// tag::snippet7[]
			public Action<String, String> myVariableAction() {
				return new Action<String, String>() {

					@Override
					public void execute(StateContext<String, String> context) {
						context.getExtendedState()
							.getVariables().put("mykey", "myvalue");
					}
				};
			}
// end::snippet7[]

		}

}
