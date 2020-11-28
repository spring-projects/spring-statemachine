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

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

public class EventDeferTests extends AbstractStateMachineTests {

	private static final Log log = LogFactory.getLog(EventDeferTests.class);

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testDeferWithFlat() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);
		doSendEventAndConsumeAll(machine, "E3");
		doSendEventAndConsumeAll(machine, "E1");
		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).hasSize(1);
		doSendEventAndConsumeAll(machine, "E2");
		assertThat(readField).hasSize(2);
	}

	@Test
	@Tag("smoke")
	@Timeout(value = 20, unit = TimeUnit.SECONDS)
	public void testDeferSmokeExecutorConcurrentModification() throws Exception {
		context.register(Config5.class);
		context.refresh();

		StateMachine<String, String> machine = resolveMachine(context);
		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsExactly("S1");

		AtomicReference<Throwable> error = new AtomicReference<>();
		AtomicInteger i1 = new AtomicInteger();
		Thread t1 = new Thread(() -> {
			while(i1.incrementAndGet() < 200) {
				try {
					doSendEventAndConsumeAll(machine, "E1");
					doSendEventAndConsumeAll(machine, "E2");
				} catch (Throwable e) {
					log.error("T1: error " + e);
					error.set(e);
					break;
				}
			}
		});
		AtomicInteger i2 = new AtomicInteger();
		Thread t2 = new Thread(() -> {
			while(i2.incrementAndGet() < 200) {
				try {
					doSendEventAndConsumeAll(machine, "E1");
					doSendEventAndConsumeAll(machine, "E2");
				} catch (Throwable e) {
					log.error("T2: error " + e);
					error.set(e);
					break;
				}
			}
		});
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		assertThat(error.get()).isNull();
	}

	@Test
	public void testDeferWithSubsSyncExecutor() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);

		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedLatch.await(3, TimeUnit.SECONDS)).isTrue();

		listener.reset(0, 0, 0, 1);
		doSendEventAndConsumeAll(machine, "E3");
		assertThat(listener.sub3readyStateEnteredLatch.await(3, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.sub3readyStateEnteredCount).isEqualTo(1);

		doSendEventAndConsumeAll(machine, "E1");

		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).hasSize(1);

		listener.reset(0, 0, 2, 0);
		doSendEventAndConsumeAll(machine, "E4");
		assertThat(listener.readyStateEnteredLatch.await(3, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.readyStateEnteredCount).isEqualTo(2);

		assertThat(machine.getState().getIds()).containsExactly("READY");
	}

	@Test
	public void testSubNotDeferOverrideSuperTransition() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS)).isTrue();

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds()).containsExactly("SUB1", "SUB11");

		// sub doesn't defer
		doSendEventAndConsumeAll(machine, "E15");
		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).isEmpty();

		assertThat(machine.getState().getIds()).containsExactly("SUB5");
	}

	@Test
	public void testSubDeferOverrideSuperTransition() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS)).isTrue();

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds()).containsExactly("SUB1", "SUB11");

		doSendEventAndConsumeAll(machine, "E1112");
		assertThat(machine.getState().getIds()).containsExactly("SUB1", "SUB12");

		// sub defers
		doSendEventAndConsumeAll(machine, "E15");
		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).hasSize(1);

		assertThat(machine.getState().getIds()).containsExactly("SUB1", "SUB12");

		// from SUB12 to SUB11 should then cause E15 to fire in root
		// causing SUB1 to SUB5
		doSendEventAndConsumeAll(machine, "E1211");
		assertThat(machine.getState().getIds()).containsExactly("SUB5");
	}

	@Test
	public void testRegionOneDeferTransition() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS)).isTrue();

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds()).containsOnly("SUB111", "SUB1", "SUB121");

		doSendEventAndConsumeAll(machine, "E5");
		assertThat(machine.getState().getIds()).containsOnly("SUB112", "SUB1", "SUB121");

		// regions defers
		doSendEventAndConsumeAll(machine, "E3");
		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).isEmpty();
	}

	@Test
	public void testRegionAllDeferTransition() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS)).isTrue();

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds()).containsOnly("SUB111", "SUB1", "SUB121");

		doSendEventAndConsumeAll(machine, "E5");
		assertThat(machine.getState().getIds()).containsOnly("SUB112", "SUB1", "SUB121");

		doSendEventAndConsumeAll(machine, "E8");
		assertThat(machine.getState().getIds()).containsOnly("SUB112", "SUB1", "SUB122");

		// regions defers
		doSendEventAndConsumeAll(machine, "E3");
		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).hasSize(1);
	}

	@Test
	public void testRegionNotDeferTransition() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);
		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		doStartAndAssert(machine);
		assertThat(listener.stateMachineStartedLatch.await(3, TimeUnit.SECONDS)).isTrue();

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds()).containsOnly("SUB111", "SUB1", "SUB121");

		// regions doesn't defer
		doSendEventAndConsumeAll(machine, "E3");
		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).isEmpty();

		assertThat(machine.getState().getIds()).containsExactly("SUB2");
	}

	@Test
	public void testDeferEventCleared() throws Exception {
		context.register(Config5.class);
		context.refresh();
		StateMachine<String, String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		assertThat(machine.getState().getIds()).containsOnly("S1");

		doSendEventAndConsumeAll(machine, "E2");
		assertThat(machine.getState().getIds()).containsOnly("S1");
		Object executor = TestUtils.readField("stateMachineExecutor", machine);
		Collection<?> readField = TestUtils.readField("deferList", executor);
		assertThat(readField).hasSize(1);

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds()).containsOnly("S1");
		readField = TestUtils.readField("deferList", executor);
		assertThat(readField).isEmpty();

		// deferred event handled so should not get back to S1
		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getIds()).containsOnly("S2");
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("READY")
					.state("SUB1")
					.state("SUB2", "E1", "E2")
					.state("SUB3", "E1", "E2")
					.and()
					.withStates()
						.parent("SUB1")
						.initial("SUB1READY")
						.and()
					.withStates()
						.parent("SUB2")
						.initial("SUB2READY")
						.and()
					.withStates()
						.parent("SUB3")
						.initial("SUB3READY");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("READY").target("SUB1")
					.event("E1")
					.and()
				.withExternal()
					.source("READY").target("SUB2")
					.event("E2")
					.and()
				.withExternal()
					.source("READY").target("SUB3")
					.event("E3")
					.and()
				.withExternal()
					.source("SUB1READY").target("READY")
					.and()
				.withExternal()
					.source("SUB2READY").target("READY")
					.and()
				.withExternal()
					.source("SUB3").target("READY")
					.event("NOTUSED")
					.and()
				.withExternal()
					.source("SUB3READY").target("READY")
					.event("E4");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("READY")
					.state("S1")
					.state("S2")
					.state("S3", "E1", "E2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("READY").target("S1")
					.event("E1")
					.and()
				.withExternal()
					.source("READY").target("S2")
					.event("E2")
					.and()
				.withExternal()
					.source("READY").target("S3")
					.event("E3")
					.and()
				.withExternal()
					.source("S3").target("S1")
					.event("E4")
					.and()
				.withExternal()
					.source("S3").target("S2")
					.event("E5")
					.and()
				.withExternal()
					.source("S3").target("READY")
					.event("E6");
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
					.state("SUB1")
					.state("SUB2")
					.state("SUB3")
					.state("SUB4")
					.state("SUB5")
					.and()
					.withStates()
						.parent("SUB1")
						.initial("SUB11")
						.state("SUB12", "E15")
						.and()
					.withStates()
						.parent("SUB2")
						.initial("SUB21")
						.state("SUB22")
						.and()
					.withStates()
						.parent("SUB3")
						.initial("SUB31")
						.state("SUB32");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("READY").target("SUB1")
					.event("E1")
					.and()
				.withExternal()
					.source("READY").target("SUB2")
					.event("E2")
					.and()
				.withExternal()
					.source("READY").target("SUB3")
					.event("E3")
					.and()
				.withExternal()
					.source("READY").target("SUB4")
					.event("E4")
					.and()
				.withExternal()
					.source("READY").target("SUB5")
					.event("E5")
					.and()
				.withExternal()
					.source("SUB1").target("SUB5")
					.event("E15")
					.and()
				.withExternal()
					.source("SUB5").target("SUB1")
					.event("E51")
					.and()
				.withExternal()
					.source("SUB11").target("SUB12")
					.event("E1112")
					.and()
				.withExternal()
					.source("SUB12").target("SUB11")
					.event("E1211");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("READY")
					.state("SUB1")
					.state("SUB2")
					.and()
					.withStates()
						.parent("SUB1")
						.initial("SUB111")
						.state("SUB112", "E3", "E6")
						.and()
					.withStates()
						.parent("SUB1")
						.initial("SUB121")
						.state("SUB122", "E3", "E7");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("READY").target("SUB1")
					.event("E1")
					.and()
				.withExternal()
					.source("SUB1").target("SUB2")
					.event("E2")
					.and()
				.withExternal()
					.source("SUB1").target("SUB2")
					.event("E3")
					.and()
				.withExternal()
					.source("SUB1").target("SUB2")
					.event("E4")
					.and()
				.withExternal()
					.source("SUB111").target("SUB112")
					.event("E5")
					.and()
				.withExternal()
					.source("SUB121").target("SUB122")
					.event("E8");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1", "E2")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2").target("S1")
					.event("E2");
		}
	}

	static class TestListener extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch readyStateEnteredLatch = new CountDownLatch(1);
		volatile CountDownLatch sub3readyStateEnteredLatch = new CountDownLatch(1);
		volatile int readyStateEnteredCount = 0;
		volatile int sub3readyStateEnteredCount = 0;

		@Override
		public void stateChanged(State<String, String> from, State<String, String> to) {
			stateChangedLatch.countDown();
		}

		@Override
		public void stateEntered(State<String, String> state) {
			if (state.getId().equals("READY")) {
				readyStateEnteredCount++;
				readyStateEnteredLatch.countDown();
			} else if (state.getId().equals("SUB3READY")) {
				sub3readyStateEnteredCount++;
				sub3readyStateEnteredLatch.countDown();
			}
		}

		@Override
		public void stateMachineStarted(StateMachine<String, String> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		public void reset(int c1, int c2, int c3, int c4) {
			stateChangedLatch = new CountDownLatch(c1);
			stateMachineStartedLatch = new CountDownLatch(c2);
			readyStateEnteredLatch = new CountDownLatch(c3);
			sub3readyStateEnteredLatch = new CountDownLatch(c4);
			readyStateEnteredCount = 0;
			sub3readyStateEnteredCount = 0;
		}

	}


}
