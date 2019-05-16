/*
 * Copyright 2017 the original author or authors.
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
import static org.junit.Assert.assertThat;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;

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
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class CleanTimerTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachineStopStopsTriggering() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = context.getBean(StateMachine.class);
		TestTimerAction action = context.getBean("testTimerAction", TestTimerAction.class);

		Object stateMachineExecutor = TestUtils.readField("stateMachineExecutor", machine);
		Queue<?> triggerQueue = TestUtils.readField("triggerQueue", stateMachineExecutor);

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));
		machine.sendEvent(TestEvents.E1);

		Thread.sleep(100);
		machine.stop();

		int tqize1 = triggerQueue.size();
		Thread.sleep(100);
		int tqsize2 = triggerQueue.size();
		assertThat(tqsize2, is(tqize1));

		int asize1 = action.count;
		Thread.sleep(100);
		int asize2 = action.count;
		assertThat(asize2, is(asize1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMoveToOtherStateStopsTriggering() throws Exception {
		context.register(BaseConfig.class, Config1.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> machine = context.getBean(StateMachine.class);
		TestTimerAction action = context.getBean("testTimerAction", TestTimerAction.class);

		Object stateMachineExecutor = TestUtils.readField("stateMachineExecutor", machine);
		Queue<?> triggerQueue = TestUtils.readField("triggerQueue", stateMachineExecutor);

		machine.start();
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));

		machine.sendEvent(TestEvents.E1);

		Thread.sleep(100);
		machine.sendEvent(TestEvents.E2);
		Thread.sleep(100);

		int tqize1 = triggerQueue.size();
		Thread.sleep(100);
		int tqsize2 = triggerQueue.size();
		assertThat(tqsize2, is(tqize1));

		int asize1 = action.count;
		Thread.sleep(100);
		int asize2 = action.count;
		assertThat(asize2, is(asize1));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3);
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
					.timer(10)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
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
	}
}
