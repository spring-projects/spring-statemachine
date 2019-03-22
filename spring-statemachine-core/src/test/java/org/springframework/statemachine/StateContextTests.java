/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.statemachine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.Map;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

public class StateContextTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStartCycles() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<States, Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0, States.S1, States.S11));
		assertThat(listener.contexts, hasSize(19));

		assertThat(listener.contexts, contains(
				hasStage(Stage.TRANSITION_START),
				hasStage(Stage.EXTENDED_STATE_CHANGED),
				hasStage(Stage.TRANSITION),
				hasStage(Stage.STATE_ENTRY),
				hasStage(Stage.TRANSITION_START),
				hasStage(Stage.TRANSITION),
				hasStage(Stage.STATE_ENTRY),
				hasStage(Stage.TRANSITION_START),
				hasStage(Stage.TRANSITION),
				hasStage(Stage.STATE_ENTRY),
				hasStage(Stage.STATE_CHANGED),
				hasStage(Stage.STATEMACHINE_START),
				hasStage(Stage.TRANSITION_END),
				hasStage(Stage.STATE_CHANGED),
				hasStage(Stage.STATEMACHINE_START),
				hasStage(Stage.TRANSITION_END),
				hasStage(Stage.STATE_CHANGED),
				hasStage(Stage.STATEMACHINE_START),
				hasStage(Stage.TRANSITION_END)
		));

		assertThat(listener.contexts.get(0).getStage(), is(Stage.TRANSITION_START));
		assertThat(listener.contexts.get(0).getTransition(), notNullValue());
		assertThat(listener.contexts.get(0).getTransition().getSource(), nullValue());
		assertThat(listener.contexts.get(0).getTransition().getTarget(), notNullValue());
		assertThat(listener.contexts.get(0).getTransition().getTarget().getId(), is(States.S0));
		assertThat(listener.contexts.get(0).getSource(), nullValue());
		assertThat(listener.contexts.get(0).getTarget(), notNullValue());

		assertThat(listener.contexts.get(1).getStage(), is(Stage.EXTENDED_STATE_CHANGED));

		assertThat(listener.contexts.get(2).getStage(), is(Stage.TRANSITION));
		assertThat(listener.contexts.get(2).getTransition(), notNullValue());
		assertThat(listener.contexts.get(2).getTransition().getSource(), nullValue());
		assertThat(listener.contexts.get(2).getTransition().getTarget(), notNullValue());
		assertThat(listener.contexts.get(2).getTransition().getTarget().getId(), is(States.S0));
		assertThat(listener.contexts.get(2).getSource(), nullValue());
		assertThat(listener.contexts.get(2).getTarget(), notNullValue());


		assertThat(listener.contexts.get(3).getStage(), is(Stage.STATE_ENTRY));
		assertThat(listener.contexts.get(3).getTarget(), notNullValue());
		assertThat(listener.contexts.get(3).getTarget().getId(), is(States.S0));
		assertThat(listener.contexts.get(3).getTransition(), notNullValue());

		assertThat(listener.contexts.get(4).getStage(), is(Stage.TRANSITION_START));

		assertThat(listener.contexts.get(5).getStage(), is(Stage.TRANSITION));

		assertThat(listener.contexts.get(6).getStage(), is(Stage.STATE_ENTRY));
		assertThat(listener.contexts.get(6).getTarget(), notNullValue());
		assertThat(listener.contexts.get(6).getTarget().getId(), is(States.S1));
		assertThat(listener.contexts.get(6).getTransition(), notNullValue());

		assertThat(listener.contexts.get(7).getStage(), is(Stage.TRANSITION_START));

		assertThat(listener.contexts.get(8).getStage(), is(Stage.TRANSITION));

		assertThat(listener.contexts.get(9).getStage(), is(Stage.STATE_ENTRY));
		assertThat(listener.contexts.get(9).getTarget(), notNullValue());
		assertThat(listener.contexts.get(9).getTarget().getId(), is(States.S11));
		assertThat(listener.contexts.get(9).getTransition(), notNullValue());

		assertThat(listener.contexts.get(10).getStage(), is(Stage.STATE_CHANGED));

		assertThat(listener.contexts.get(11).getStage(), is(Stage.STATEMACHINE_START));
		assertThat(listener.contexts.get(11).getTransition(), notNullValue());

		assertThat(listener.contexts.get(12).getStage(), is(Stage.TRANSITION_END));

		assertThat(listener.contexts.get(13).getStage(), is(Stage.STATE_CHANGED));

		assertThat(listener.contexts.get(14).getStage(), is(Stage.STATEMACHINE_START));
		assertThat(listener.contexts.get(14).getTransition(), notNullValue());

		assertThat(listener.contexts.get(15).getStage(), is(Stage.TRANSITION_END));

		assertThat(listener.contexts.get(16).getStage(), is(Stage.STATE_CHANGED));

		assertThat(listener.contexts.get(17).getStage(), is(Stage.STATEMACHINE_START));
		assertThat(listener.contexts.get(17).getTransition(), notNullValue());

		assertThat(listener.contexts.get(18).getStage(), is(Stage.TRANSITION_END));
		assertThat(listener.contexts.get(18).getTransition(), notNullValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEventNotAccepted() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<States, Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		machine.start();
		listener.contexts.clear();

		machine.sendEvent(Events.J);

		// all nested machines sends these
		assertThat(listener.contexts, contains(
				hasStage(Stage.EVENT_NOT_ACCEPTED)
		));

		assertThat(listener.contexts.get(0).getStage(), is(Stage.EVENT_NOT_ACCEPTED));
		assertThat(listener.contexts.get(0).getTransition(), nullValue());
		assertThat(listener.contexts.get(0).getEvent(), is(Events.J));
		assertThat(listener.contexts.get(0).getSource(), notNullValue());
		assertThat(listener.contexts.get(0).getSource().getId(), is(States.S0));
		assertThat(listener.contexts.get(0).getTarget(), nullValue());
	}

	static class TestStateMachineListener extends StateMachineListenerAdapter<States, Events> {

		ArrayList<StateContext<States, Events>> contexts = new ArrayList<>();

		@Override
		public void stateContext(StateContext<States, Events> stateContext) {
			contexts.add(stateContext);
		}
	}

	private static Matcher<StateContext<?, ?>> hasStage(final Stage stage) {
		return new FeatureMatcher<StateContext<?, ?>, Stage>(equalTo(stage), "stage", "stage") {
			@Override
			protected Stage featureValueOf(final StateContext<?, ?> actual) {
				return actual.getStage();
			}
		};
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0, fooAction())
					.state(States.S0)
					.and()
					.withStates()
						.parent(States.S0)
						.initial(States.S1)
						.state(States.S1)
						.and()
						.withStates()
							.parent(States.S1)
							.initial(States.S11)
							.state(States.S11)
							.state(States.S12)
							.and()
					.withStates()
						.parent(States.S0)
						.state(States.S2)
						.and()
						.withStates()
							.parent(States.S2)
							.initial(States.S21)
							.state(States.S21)
							.and()
							.withStates()
								.parent(States.S21)
								.initial(States.S211)
								.state(States.S211)
								.state(States.S212);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S1).target(States.S1).event(Events.A)
					.guard(foo1Guard())
					.and()
				.withExternal()
					.source(States.S1).target(States.S11).event(Events.B)
					.and()
				.withExternal()
					.source(States.S21).target(States.S211).event(Events.B)
					.and()
				.withExternal()
					.source(States.S1).target(States.S2).event(Events.C)
					.and()
				.withExternal()
					.source(States.S2).target(States.S1).event(Events.C)
					.and()
				.withExternal()
					.source(States.S1).target(States.S0).event(Events.D)
					.and()
				.withExternal()
					.source(States.S211).target(States.S21).event(Events.D)
					.and()
				.withExternal()
					.source(States.S0).target(States.S211).event(Events.E)
					.and()
				.withExternal()
					.source(States.S1).target(States.S211).event(Events.F)
					.and()
				.withExternal()
					.source(States.S2).target(States.S11).event(Events.F)
					.and()
				.withExternal()
					.source(States.S11).target(States.S211).event(Events.G)
					.and()
				.withExternal()
					.source(States.S211).target(States.S0).event(Events.G)
					.and()
				.withInternal()
					.source(States.S0).event(Events.H)
					.guard(foo0Guard())
					.action(fooAction())
					.and()
				.withInternal()
					.source(States.S2).event(Events.H)
					.guard(foo1Guard())
					.action(fooAction())
					.and()
				.withInternal()
					.source(States.S1).event(Events.H)
					.and()
				.withExternal()
					.source(States.S11).target(States.S12).event(Events.I)
					.and()
				.withExternal()
					.source(States.S211).target(States.S212).event(Events.I)
					.and()
				.withExternal()
					.source(States.S12).target(States.S212).event(Events.I)
					.and()
				.withExternal()
					.source(States.S212).target(States.S211).event(Events.J);

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

	public static enum States {
		S0, S1, S11, S12, S2, S21, S211, S212
	}

	public static enum Events {
		A, B, C, D, E, F, G, H, I, J
	}

	private static class FooAction implements Action<States, Events> {

		@Override
		public void execute(StateContext<States, Events> context) {
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			Integer foo = context.getExtendedState().get("foo", Integer.class);
			if (foo == null) {
				variables.put("foo", 0);
			} else if (foo == 0) {
				variables.put("foo", 1);
			} else if (foo == 1) {
				variables.put("foo", 0);
			}
		}
	}

	private static class FooGuard implements Guard<States, Events> {

		private final int match;

		public FooGuard(int match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<States, Events> context) {
			Object foo = context.getExtendedState().getVariables().get("foo");
			return !(foo == null || !foo.equals(match));
		}
	}

}
