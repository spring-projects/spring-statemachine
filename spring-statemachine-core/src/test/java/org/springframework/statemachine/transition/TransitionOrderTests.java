/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.statemachine.transition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

/**
 * Tests for transitions for cases where specific order is assumed.
 *
 * @author Janne Valkealahti
 *
 */
public class TransitionOrderTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testAnonymousTransitionInConfigUseParent1() {
		TestListener listener = new TestListener();
		context.register(Config1.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1);
	}

	@Test
	public void testAnonymousTransitionInConfigUseParent2() {
		TestListener listener = new TestListener();
		context.register(Config2.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1);
	}

	@Test
	public void testAnonymousTransitionInConfigUseChild1() {
		TestListener listener = new TestListener();
		context.register(Config3.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1012, TestStates.S1);
	}

	@Test
	public void testAnonymousTransitionInConfigUseParent3() {
		TestListener listener = new TestListener();
		context.register(Config4.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1);
	}

	@Test
	public void testAnonymousTransitionInConfigUseParent4() {
		TestListener listener = new TestListener();
		context.register(Config5.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1012);
	}

	@Test
	public void testAnonymousTransitionInConfigUseParent5() {
		TestListener listener = new TestListener();
		context.register(Config6.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1);
	}

	@Test
	public void testAnonymousTransitionInConfigUseParent6() {
		TestListener listener = new TestListener();
		context.register(Config7.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1012, TestStates.S2011, TestStates.S1);
	}

	@Test
	public void testAnonymousTransitionInConfigUseParent7() {
		TestListener listener = new TestListener();
		context.register(Config8.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = resolveMachine(context);
		machine.addStateListener(listener);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S1);

		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.statesEntered).containsExactly(TestStates.S1, TestStates.S10, TestStates.S1011, TestStates.S1);
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.PARENT);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.state(TestStates.S1012);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
					// Config1 vs. Config2, next 2 transitions are defined in different order
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012)
					.and()
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S1);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.PARENT);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.state(TestStates.S1012);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
					// Config1 vs. Config2, next 2 transitions are defined in different order
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S1)
					.and()
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.CHILD);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.state(TestStates.S1012);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S1)
					.and()
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.PARENT);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.stateEntry(TestStates.S1011, c -> c.getExtendedState().getVariables().put("error", true))
						.state(TestStates.S1012);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S1)
					.guard(c -> c.getExtendedState().getVariables().containsKey("error"))
					.and()
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config5 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.PARENT);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.state(TestStates.S1012);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S1)
					.guard(c -> c.getExtendedState().getVariables().containsKey("error"))
					.and()
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config6 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.PARENT);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.state(TestStates.S1012)
						.state(TestStates.S2011);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S1)
					.and()
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012)
					.and()
				.withExternal()
					.source(TestStates.S1012)
					.target(TestStates.S2011);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config7 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.CHILD);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.state(TestStates.S1012)
						.state(TestStates.S2011);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S1)
					.and()
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012)
					.and()
				.withExternal()
					.source(TestStates.S1012)
					.target(TestStates.S2011);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config8 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<TestStates, TestEvents> config) throws Exception {
			config
				.withConfiguration()
					.transitionConflictPolicy(TransitionConflictPolicy.PARENT);
		}

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S10)
					.junction(TestStates.SF)
					.and()
					.withStates()
						.parent(TestStates.S10)
						.initial(TestStates.S1011)
						.state(TestStates.S1012)
						.state(TestStates.S2011);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withJunction()
					.source(TestStates.SF)
					.last(TestStates.S1)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S10)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S1011)
					.target(TestStates.S1012)
					.and()
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.SF)
					.and()
				.withExternal()
					.source(TestStates.S1012)
					.target(TestStates.S2011);
		}
	}

	static class TestListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;
		volatile CountDownLatch statesEnteredLatch = new CountDownLatch(1);
		final ArrayList<TestStates> statesEntered = new ArrayList<>();

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void stateEntered(State<TestStates, TestEvents> state) {
			statesEntered.add(state.getId());
			statesEnteredLatch.countDown();
		}

		public void reset(int c1) {
			stateChangedLatch = new CountDownLatch(c1);
			stateChangedCount = 0;
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			stateChangedCount = 0;
			statesEnteredLatch = new CountDownLatch(c2);
			statesEntered.clear();
		}
	}
}
