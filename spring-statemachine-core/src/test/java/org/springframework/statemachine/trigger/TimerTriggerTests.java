/*
 * Copyright 2015-2016 the original author or authors.
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
package org.springframework.statemachine.trigger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class TimerTriggerTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testGetter() {
		final long period = 1000;
		final int count = 1;
		final TimerTrigger timerTrigger = new TimerTrigger(period, count);
		assertEquals(period, timerTrigger.getPeriod());
		assertEquals(count, timerTrigger.getCount());
	}

	@Test
	public void testListenerEvents() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();

		final CountDownLatch latch = new CountDownLatch(2);
		@SuppressWarnings("rawtypes")
		TimerTrigger timerTrigger = context.getBean(TimerTrigger.class);
		timerTrigger.addTriggerListener(new TriggerListener() {

			@Override
			public void triggered() {
				latch.countDown();
			}
		});
		timerTrigger.afterPropertiesSet();
		timerTrigger.start();

		assertThat(latch.await(1, TimeUnit.SECONDS), is(true));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTimerTransitions() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = context.getBean(StateMachine.class);
		TestTimerAction action = context.getBean("testTimerAction", TestTimerAction.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));

		listener.reset(1);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2100, TimeUnit.MILLISECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2));

		Thread.sleep(1000);
		// we should have 100, just test 80 due to timing
		assertThat(action.count, greaterThan(80));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTimerExternalTransitions() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> machine = context.getBean(StateMachine.class);
		TestTimerAction2 action = context.getBean("testTimerAction2", TestTimerAction2.class);
		TestListener2 listener = new TestListener2();
		machine.addStateListener(listener);

		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder("READY"));

		for (int i = 0; i < 4; i++) {
			listener.reset(2);
			action.reset();
			machine.sendEvent("SWITCH_TO_RUNNING");
			assertThat(action.latch.await(5, TimeUnit.SECONDS), is(true));
			assertThat(action.count, is(1));
			assertThat(listener.stateChangedLatch.await(5, TimeUnit.SECONDS), is(true));
			assertThat(listener.stateChangedCount, is(2));
			assertThat(machine.getState().getIds(), containsInAnyOrder("RUNNING_TESTING"));
		}
	}

	private class TestTriggerListener implements TriggerListener {

		AtomicInteger count = new AtomicInteger();
		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void triggered() {
			count.incrementAndGet();
			latch.countDown();
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testTimerDelayFireOnlyOnState() throws Exception {
		context.register(BaseConfig.class, Config4.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = context.getBean(StateMachine.class);
		TestTimerAction action = context.getBean("testTimerAction", TestTimerAction.class);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);

		TimerTrigger<?, ?> trigger = null;
		Map<Trigger<?, ?>, Transition<?, ?>> triggerToTransitionMap = TestUtils.readField("triggerToTransitionMap", machine);
		for (Entry<Trigger<?, ?>, Transition<?, ?>> entry : triggerToTransitionMap.entrySet()) {
			if (entry.getKey() instanceof TimerTrigger) {
				trigger = (TimerTrigger<?, ?>) entry.getKey();
				continue;
			}
		}
		assertThat(trigger, notNullValue());
		TestTriggerListener tlistener = new TestTriggerListener();
		trigger.addTriggerListener(tlistener);

		machine.start();
		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));

		assertThat(tlistener.latch.await(2, TimeUnit.SECONDS), is(false));

		listener.reset(1);
		machine.sendEvent(TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S2));

		assertThat(tlistener.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(tlistener.count.get(), is(0));
		assertThat(tlistener.latch.await(4, TimeUnit.SECONDS), is(true));
		assertThat(tlistener.count.get(), is(1));

		assertThat(action.latch.await(2, TimeUnit.SECONDS), is(true));
		action.reset(1);
		assertThat(action.latch.await(2, TimeUnit.SECONDS), is(false));
	}

	static class Config1 {

		@Bean
		public TimerTrigger<TestStates, TestEvents> timerTrigger() {
			return new TimerTrigger<TestStates, TestEvents>(100);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withInternal()
					.source(TestStates.S2)
					.action(testTimerAction())
					.timer(10);
		}

		@Bean
		public TestTimerAction testTimerAction() {
			return new TestTimerAction();
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("READY")
					.state("RUNNING")
					.state("RUNNING_TESTING")
					.end("SHUTDOWN");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("READY")
					.target("RUNNING")
					.event("SWITCH_TO_RUNNING")
					.and()
				.withExternal()
					.source("RUNNING")
					.target("RUNNING_TESTING")
					.timer(1000)
					.action(testTimerAction2())
					.and()
				.withExternal()
					.source("RUNNING_TESTING")
					.target("RUNNING")
					.event("SWITCH_TO_RUNNING")
					.and()
				.withExternal()
					.source("RUNNING")
					.target("SHUTDOWN")
					.event("SWITCH_TO_SHUTDOWN")
					.and()
				.withExternal()
					.source("RUNNING_TESTING")
					.target("SHUTDOWN")
					.event("SWITCH_TO_SHUTDOWN");
		}

		@Bean
		public TestTimerAction2 testTimerAction2() {
			return new TestTimerAction2();
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withInternal()
					.source(TestStates.S2)
					.action(testTimerAction())
					.timerOnce(3000);
		}

		@Bean
		public TestTimerAction testTimerAction() {
			return new TestTimerAction();
		}

	}

	private static class TestTimerAction implements Action<TestStates, TestEvents> {

		int count = 0;
		volatile CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void execute(StateContext<TestStates, TestEvents> context) {
			count++;
			latch.countDown();
		}

		void reset(int a) {
			latch = new CountDownLatch(a);
			count = 0;
		}
	}

	private static class TestTimerAction2 implements Action<String, String> {

		int count = 0;
		volatile CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void execute(StateContext<String, String> context) {
			count++;
			latch.countDown();
		}

		public void reset() {
			count = 0;
			latch = new CountDownLatch(1);
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;

		@Override
		public void stateMachineStarted(StateMachine<TestStates, TestEvents> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<TestStates, TestEvents> transition) {
			transitionLatch.countDown();
		}

		public void reset(int c1) {
			reset(c1, 0);
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			transitionLatch = new CountDownLatch(c2);
			stateChangedCount = 0;
		}

	}

	private static class TestListener2 extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;

		@Override
		public void stateMachineStarted(StateMachine<String, String> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<String, String> transition) {
			transitionLatch.countDown();
		}

		public void reset(int c1) {
			reset(c1, 0);
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			transitionLatch = new CountDownLatch(c2);
			stateChangedCount = 0;
		}

	}

}
