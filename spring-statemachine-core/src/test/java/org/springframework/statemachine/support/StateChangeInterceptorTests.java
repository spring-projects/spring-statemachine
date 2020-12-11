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
package org.springframework.statemachine.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class StateChangeInterceptorTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testIntercept() throws InterruptedException {
		context.register(Config1.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		TestStateChangeInterceptor interceptor = new TestStateChangeInterceptor();

		machine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(interceptor));


		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(3);
		assertThat(machine.getState().getIds()).containsOnly(States.S0, States.S1, States.S11);
		assertThat((Integer)machine.getExtendedState().getVariables().get("foo")).isZero();

		listener.reset(3);
		interceptor.reset(1);
		doSendEventAndConsumeAll(machine, Events.C);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(3);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0, States.S2, States.S21, States.S211);
		doSendEventAndConsumeAll(machine, Events.H);
		assertThat(machine.getState().getIds()).containsOnly(States.S0, States.S2, States.S21, States.S211);
		assertThat((Integer)machine.getExtendedState().getVariables().get("foo")).isEqualTo(1);
	}

	@Test
	public void testIntercept2() throws InterruptedException {
		context.register(Config2.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		TestStateChangeInterceptor interceptor = new TestStateChangeInterceptor();

		machine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(interceptor));

		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S1);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.B);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S2);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.C);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);
	}

	@Test
	public void testIntercept3() throws InterruptedException {
		context.register(Config3.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		TestStateChangeInterceptor interceptor = new TestStateChangeInterceptor();

		machine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(interceptor));

		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S2);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);
	}

	@Test
	public void testIntercept4() throws InterruptedException {
		context.register(Config4.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		TestStateChangeInterceptor interceptor = new TestStateChangeInterceptor();

		machine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(interceptor));

		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S2);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);
		assertThat(interceptor.postStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.postStateChangeCount2).isEqualTo(1);
		assertThat(interceptor.preStateChangeStates2).hasSize(1);
		assertThat(interceptor.postStateChangeStates2).hasSize(1);
		assertThat(interceptor.preStateChangeStates2.get(0).getId()).isEqualTo(interceptor.postStateChangeStates2.get(0).getId());
	}

	@Test
	public void testIntercept5() throws InterruptedException {
		context.register(Config4.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		TestStateChangeInterceptor interceptor = new TestStateChangeInterceptor();

		machine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(interceptor));

		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, MessageBuilder.withPayload(Events.A).setHeader("test", "exists").build());
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S3);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);
		assertThat(interceptor.postStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.postStateChangeCount2).isEqualTo(1);
		assertThat(interceptor.preStateChangeStates2).hasSize(1);
		assertThat(interceptor.postStateChangeStates2).hasSize(1);
		assertThat(interceptor.preStateChangeStates2.get(0).getId()).isEqualTo(interceptor.postStateChangeStates2.get(0).getId());
	}

	@Test
	public void testIntercept6() throws InterruptedException {
		context.register(Config5.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		TestStateChangeInterceptor interceptor = new TestStateChangeInterceptor();

		machine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(interceptor));

		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S1);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);

		interceptor.reset(1);
		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.E);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S2);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(1);
	}

	@Test
	public void testIntercept7() throws InterruptedException {
		context.register(Config6.class);
		context.refresh();
		StateMachine<States, Events> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		TestStateChangeInterceptor interceptor = new TestStateChangeInterceptor();

		machine.getStateMachineAccessor().doWithRegion(function -> function.addStateMachineInterceptor(interceptor));

		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		interceptor.reset(2);
		listener.reset(2);
		doSendEventAndConsumeAll(machine, Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsOnly(States.S2);
		assertThat(interceptor.preStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.preStateChangeCount2).isEqualTo(2);
		assertThat(interceptor.postStateChangeLatch2.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(interceptor.postStateChangeCount2).isEqualTo(2);

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
	static class Config2 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0)
					.state(States.S1)
					.state(States.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S0).target(States.S1)
					.event(Events.A)
					.and()
				.withExternal()
					.source(States.S1).target(States.S2)
					.event(Events.B)
					.and()
				.withExternal()
					.source(States.S2).target(States.S0)
					.event(Events.C);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0)
					.choice(States.S1)
					.state(States.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S0).target(States.S1)
					.event(Events.A)
					.and()
				.withChoice()
					.source(States.S1)
					.last(States.S2);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0)
					.choice(States.S1)
					.state(States.S2)
					.state(States.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S0).target(States.S1)
					.event(Events.A)
					.and()
				.withChoice()
					.source(States.S1)
					.first(States.S3, guard())
					.last(States.S2);
		}

		@Bean
		public EventHeaderGuard guard() {
			return new EventHeaderGuard("test");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config5 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
					.withStates()
					.initial(States.S0)
					.state(States.S1)
					.end(States.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
					.withExternal()
					.source(States.S0).target(States.S1)
					.event(Events.A)
					.and()
					.withExternal()
					.source(States.S1).target(States.S2)
					.event(Events.E);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config6 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0)
					.state(States.S1)
					.state(States.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S0).target(States.S1)
					.event(Events.A)
					.and()
				.withExternal()
					.source(States.S1).target(States.S2)
					.and()
				.withExternal()
					.source(States.S2).target(States.S0)
					.event(Events.C);
		}
	}

	public static enum States {
		S0, S1, S11, S12, S2, S21, S211, S212, S3;
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

	private static class EventHeaderGuard implements Guard<States, Events> {

		private final String header;

		public EventHeaderGuard(String header) {
			this.header = header;
		}

		@Override
		public boolean evaluate(StateContext<States, Events> context) {
			return context.getMessageHeader(header) != null;
		}
	}

	private static class TestListener extends StateMachineListenerAdapter<States, Events> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile int stateChangedCount = 0;

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		public void reset(int c1) {
			stateChangedLatch = new CountDownLatch(c1);
			stateChangedCount = 0;
		}

	}

	private static class TestStateChangeInterceptor implements StateMachineInterceptor<States, Events> {

		volatile CountDownLatch preStateChangeLatch2 = new CountDownLatch(1);
		volatile CountDownLatch postStateChangeLatch2 = new CountDownLatch(1);
		volatile int preStateChangeCount2 = 0;
		volatile int postStateChangeCount2 = 0;
		ArrayList<State<States, Events>> preStateChangeStates2 = new ArrayList<>();
		ArrayList<State<States, Events>> postStateChangeStates2 = new ArrayList<>();

		@Override
		public Message<Events> preEvent(Message<Events> message, StateMachine<States, Events> stateMachine) {
			return message;
		}

		@Override
		public void preStateChange(State<States, Events> state, Message<Events> message,
				Transition<States, Events> transition, StateMachine<States, Events> stateMachine,
				StateMachine<States, Events> rootStateMachine) {
			preStateChangeStates2.add(state);
			preStateChangeCount2++;
			preStateChangeLatch2.countDown();
		}

		@Override
		public void postStateChange(State<States, Events> state, Message<Events> message,
				Transition<States, Events> transition, StateMachine<States, Events> stateMachine,
				StateMachine<States, Events> rootStateMachine) {
			postStateChangeStates2.add(state);
			postStateChangeCount2++;
			postStateChangeLatch2.countDown();
		}

		@Override
		public StateContext<States, Events> preTransition(StateContext<States, Events> stateContext) {
			return stateContext;
		}

		@Override
		public StateContext<States, Events> postTransition(StateContext<States, Events> stateContext) {
			return stateContext;
		}

		public void reset(int c1) {
			preStateChangeLatch2 = new CountDownLatch(c1);
			preStateChangeCount2 = 0;
			postStateChangeLatch2 = new CountDownLatch(c1);
			postStateChangeCount2 = 0;
			preStateChangeStates2.clear();
			postStateChangeStates2.clear();
		}

		@Override
		public Exception stateMachineError(StateMachine<States, Events> stateMachine, Exception exception) {
			return exception;
		}

	}

}
