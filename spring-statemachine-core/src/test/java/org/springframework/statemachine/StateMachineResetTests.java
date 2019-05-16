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
package org.springframework.statemachine;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

/**
 * Tests for resetting a state machine state and extended variables using a
 * {@link StateMachineContext}.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineResetTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testResetSubStates1() throws Exception {
		context.register(Config1.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<States, Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		Map<Object, Object> variables = new HashMap<Object, Object>();
		variables.put("foo", 1);
		ExtendedState extendedState = new DefaultExtendedState(variables);
		DefaultStateMachineContext<States,Events> stateMachineContext = new DefaultStateMachineContext<States, Events>(States.S12, Events.I, null, extendedState);

		machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<States,Events>>() {

			@Override
			public void apply(StateMachineAccess<States, Events> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0, States.S1, States.S12));
		assertThat((Integer)machine.getExtendedState().getVariables().get("foo"), is(1));
	}

	@Test
	public void testResetSubStates2() throws Exception {
		context.register(Config1.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<States, Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		Map<Object, Object> variables = new HashMap<Object, Object>();
		variables.put("foo", 1);
		ExtendedState extendedState = new DefaultExtendedState(variables);
		DefaultStateMachineContext<States,Events> stateMachineContext = new DefaultStateMachineContext<States, Events>(States.S211, Events.C, null, extendedState);

		machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<States,Events>>() {

			@Override
			public void apply(StateMachineAccess<States, Events> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0, States.S2, States.S21, States.S211));
		assertThat((Integer)machine.getExtendedState().getVariables().get("foo"), is(1));
	}

	@Test
	public void testResetSubStates3() throws Exception {
		context.register(Config1.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<States, Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		Map<Object, Object> variables = new HashMap<Object, Object>();
		variables.put("foo", 1);
		ExtendedState extendedState = new DefaultExtendedState(variables);
		DefaultStateMachineContext<States,Events> stateMachineContext = new DefaultStateMachineContext<States, Events>(States.S2, Events.C, null, extendedState);

		machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<States,Events>>() {

			@Override
			public void apply(StateMachineAccess<States, Events> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0, States.S2, States.S21, States.S211));
		assertThat((Integer)machine.getExtendedState().getVariables().get("foo"), is(1));
	}

	@Test
	public void testResetRegions1() {
		context.register(Config2.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<TestStates, TestEvents> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		DefaultStateMachineContext<TestStates, TestEvents> stateMachineContext1 =
				new DefaultStateMachineContext<TestStates, TestEvents>(TestStates.S21, TestEvents.E2, null, null);
		DefaultStateMachineContext<TestStates, TestEvents> stateMachineContext2 =
				new DefaultStateMachineContext<TestStates, TestEvents>(TestStates.S31, TestEvents.E3, null, null);

		List<StateMachineContext<TestStates, TestEvents>> childs = new ArrayList<StateMachineContext<TestStates,TestEvents>>();
		childs.add(stateMachineContext1);
		childs.add(stateMachineContext2);

		DefaultStateMachineContext<TestStates, TestEvents> stateMachineContext =
				new DefaultStateMachineContext<TestStates, TestEvents>(childs, TestStates.S2, TestEvents.E1, null, null);

		machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<TestStates, TestEvents>>() {

			@Override
			public void apply(StateMachineAccess<TestStates, TestEvents> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S31));
	}

	@Test
	public void testResetRegions2() {
		context.register(Config2.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<TestStates, TestEvents> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		DefaultStateMachineContext<TestStates, TestEvents> stateMachineContext1 =
				new DefaultStateMachineContext<TestStates, TestEvents>(TestStates.S21, null, null, null);
		DefaultStateMachineContext<TestStates, TestEvents> stateMachineContext2 =
				new DefaultStateMachineContext<TestStates, TestEvents>(TestStates.S31, null, null, null);

		List<StateMachineContext<TestStates, TestEvents>> childs = new ArrayList<StateMachineContext<TestStates,TestEvents>>();
		childs.add(stateMachineContext1);
		childs.add(stateMachineContext2);

		DefaultStateMachineContext<TestStates, TestEvents> stateMachineContext =
				new DefaultStateMachineContext<TestStates, TestEvents>(childs, TestStates.S2, null, null, null);

		machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<TestStates, TestEvents>>() {

			@Override
			public void apply(StateMachineAccess<TestStates, TestEvents> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S31));
	}

	@Test
	public void testResetUpdateExtendedStateVariables() {
		context.register(Config3.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<States, Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		assertThat((Integer)machine.getExtendedState().getVariables().get("count"), nullValue());
		machine.sendEvent(Events.A);
		assertThat((Integer)machine.getExtendedState().getVariables().get("count"), is(1));

		machine.stop();
		Map<Object, Object> variables = new HashMap<Object, Object>();
		variables.putAll(machine.getExtendedState().getVariables());
		ExtendedState extendedState = new DefaultExtendedState(variables);
		DefaultStateMachineContext<States,Events> stateMachineContext = new DefaultStateMachineContext<States, Events>(States.S0, null, null, extendedState);

		machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<States,Events>>() {

			@Override
			public void apply(StateMachineAccess<States, Events> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});

		machine.start();
		assertThat((Integer)machine.getExtendedState().getVariables().get("count"), is(1));
		machine.sendEvent(Events.A);
		assertThat((Integer)machine.getExtendedState().getVariables().get("count"), is(2));
	}

	@Test
	public void testResetWithNullContext() throws Exception {
		context.register(Config1.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<States, Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0, States.S1, States.S11));
		assertThat((Integer)machine.getExtendedState().getVariables().get("foo"), is(0));

		machine.sendEvent(Events.I);
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0, States.S1, States.S12));
		assertThat((Integer)machine.getExtendedState().getVariables().get("foo"), is(0));

		machine.stop();
		machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<States,Events>>() {

			@Override
			public void apply(StateMachineAccess<States, Events> function) {
				function.resetStateMachine(null);
			}
		});
		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0, States.S1, States.S11));
		assertThat(machine.getExtendedState().getVariables().size(), is(0));
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
					.source(States.S12).target(States.S212).event(Events.I);

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

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<States, Events> config)
				throws Exception {
			config
				.withConfiguration()
					.autoStartup(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withInternal()
					.source(States.S0)
					.event(Events.A)
					.action(updateAction());
		}

		@Bean
		public Action<States, Events> updateAction() {
			return new Action<States, Events>() {

				@Override
				public void execute(StateContext<States, Events> context) {
					Integer count = context.getExtendedState().get("count", Integer.class);
					if (count == null) {
						context.getExtendedState().getVariables().put("count", 1);
					} else {
						context.getExtendedState().getVariables().put("count", (count + 1));
					}
				}
			};
		}

	}

	public static enum States {
		S0, S1, S11, S12, S2, S21, S211, S212
	}

	public static enum Events {
		A, B, C, D, E, F, G, H, I
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

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.SI)
					.state(TestStates.S2)
					.end(TestStates.SF)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30)
						.state(TestStates.S31);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S30)
					.target(TestStates.S31)
					.event(TestEvents.E3);
		}

	}

}
