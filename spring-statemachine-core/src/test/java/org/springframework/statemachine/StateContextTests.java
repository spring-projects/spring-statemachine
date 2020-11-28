/*
 * Copyright 2015-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
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

	@Test
	public void testStartCycles() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);

		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsOnly(States.S0, States.S1, States.S11);
		assertThat(listener.contexts).hasSize(19);
		assertThat(listener.contexts.stream().map(c -> c.getStage()).collect(Collectors.toList())).containsExactly(
			Arrays.asList(
				Stage.TRANSITION_START,
				Stage.EXTENDED_STATE_CHANGED,
				Stage.TRANSITION,
				Stage.STATE_ENTRY,
				Stage.TRANSITION_START,
				Stage.TRANSITION,
				Stage.STATE_ENTRY,
				Stage.TRANSITION_START,
				Stage.TRANSITION,
				Stage.STATE_ENTRY,
				Stage.STATE_CHANGED,
				Stage.STATEMACHINE_START,
				Stage.TRANSITION_END,
				Stage.STATE_CHANGED,
				Stage.STATEMACHINE_START,
				Stage.TRANSITION_END,
				Stage.STATE_CHANGED,
				Stage.STATEMACHINE_START,
				Stage.TRANSITION_END
				).toArray(new Stage[0])
		);

		assertThat(listener.contexts.get(0).getStage()).isEqualTo(Stage.TRANSITION_START);
		assertThat(listener.contexts.get(0).getTransition()).isNotNull();
		assertThat(listener.contexts.get(0).getTransition().getSource()).isNull();
		assertThat(listener.contexts.get(0).getTransition().getTarget()).isNotNull();
		assertThat(listener.contexts.get(0).getTransition().getTarget().getId()).isEqualTo(States.S0);
		assertThat(listener.contexts.get(0).getSource()).isNull();
		assertThat(listener.contexts.get(0).getTarget()).isNotNull();

		assertThat(listener.contexts.get(1).getStage()).isEqualTo(Stage.EXTENDED_STATE_CHANGED);

		assertThat(listener.contexts.get(2).getStage()).isEqualTo(Stage.TRANSITION);
		assertThat(listener.contexts.get(2).getTransition()).isNotNull();
		assertThat(listener.contexts.get(2).getTransition().getSource()).isNull();
		assertThat(listener.contexts.get(2).getTransition().getTarget()).isNotNull();
		assertThat(listener.contexts.get(2).getTransition().getTarget().getId()).isEqualTo(States.S0);
		assertThat(listener.contexts.get(2).getSource()).isNull();
		assertThat(listener.contexts.get(2).getTarget()).isNotNull();


		assertThat(listener.contexts.get(3).getStage()).isEqualTo(Stage.STATE_ENTRY);
		assertThat(listener.contexts.get(3).getTarget()).isNotNull();
		assertThat(listener.contexts.get(3).getTarget().getId()).isEqualTo(States.S0);
		assertThat(listener.contexts.get(3).getTransition()).isNotNull();

		assertThat(listener.contexts.get(4).getStage()).isEqualTo(Stage.TRANSITION_START);

		assertThat(listener.contexts.get(5).getStage()).isEqualTo(Stage.TRANSITION);

		assertThat(listener.contexts.get(6).getStage()).isEqualTo(Stage.STATE_ENTRY);
		assertThat(listener.contexts.get(6).getTarget()).isNotNull();
		assertThat(listener.contexts.get(6).getTarget().getId()).isEqualTo(States.S1);
		assertThat(listener.contexts.get(6).getTransition()).isNotNull();

		assertThat(listener.contexts.get(7).getStage()).isEqualTo(Stage.TRANSITION_START);

		assertThat(listener.contexts.get(8).getStage()).isEqualTo(Stage.TRANSITION);

		assertThat(listener.contexts.get(9).getStage()).isEqualTo(Stage.STATE_ENTRY);
		assertThat(listener.contexts.get(9).getTarget()).isNotNull();
		assertThat(listener.contexts.get(9).getTarget().getId()).isEqualTo(States.S11);
		assertThat(listener.contexts.get(9).getTransition()).isNotNull();

		assertThat(listener.contexts.get(10).getStage()).isEqualTo(Stage.STATE_CHANGED);

		assertThat(listener.contexts.get(11).getStage()).isEqualTo(Stage.STATEMACHINE_START);
		assertThat(listener.contexts.get(11).getTransition()).isNotNull();

		assertThat(listener.contexts.get(12).getStage()).isEqualTo(Stage.TRANSITION_END);

		assertThat(listener.contexts.get(13).getStage()).isEqualTo(Stage.STATE_CHANGED);

		assertThat(listener.contexts.get(14).getStage()).isEqualTo(Stage.STATEMACHINE_START);
		assertThat(listener.contexts.get(14).getTransition()).isNotNull();

		assertThat(listener.contexts.get(15).getStage()).isEqualTo(Stage.TRANSITION_END);

		assertThat(listener.contexts.get(16).getStage()).isEqualTo(Stage.STATE_CHANGED);

		assertThat(listener.contexts.get(17).getStage()).isEqualTo(Stage.STATEMACHINE_START);
		assertThat(listener.contexts.get(17).getTransition()).isNotNull();

		assertThat(listener.contexts.get(18).getStage()).isEqualTo(Stage.TRANSITION_END);
		assertThat(listener.contexts.get(18).getTransition()).isNotNull();
	}

	@Test
	public void testEventNotAccepted() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);

		TestStateMachineListener listener = new TestStateMachineListener();
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		listener.contexts.clear();

		doSendEventAndConsumeAll(machine, Events.J);

		// all nested machines sends these
		assertThat(listener.contexts.stream().map(c -> c.getStage()).collect(Collectors.toList())).containsExactly(
			Arrays.asList(
				Stage.EVENT_NOT_ACCEPTED,
				Stage.EVENT_NOT_ACCEPTED,
				Stage.EVENT_NOT_ACCEPTED
				).toArray(new Stage[0])
		);

		assertThat(listener.contexts.get(0).getStage()).isEqualTo(Stage.EVENT_NOT_ACCEPTED);
		assertThat(listener.contexts.get(0).getTransition()).isNull();
		assertThat(listener.contexts.get(0).getEvent()).isEqualTo(Events.J);
		assertThat(listener.contexts.get(0).getSource()).isNotNull();
		assertThat(listener.contexts.get(0).getSource().getId()).isEqualTo(States.S0);
		assertThat(listener.contexts.get(0).getTarget()).isNull();
	}

	static class TestStateMachineListener extends StateMachineListenerAdapter<States, Events> {

		ArrayList<StateContext<States, Events>> contexts = new ArrayList<>();

		@Override
		public void stateContext(StateContext<States, Events> stateContext) {
			contexts.add(stateContext);
		}
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
