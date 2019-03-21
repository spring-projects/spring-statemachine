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
package org.springframework.statemachine.security;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;
import org.springframework.statemachine.state.State;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for securing transitions.
 *
 * @author Janne Valkealahti
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class TransitionSecurityExpressionTests extends AbstractStateMachineTests {

	@Test
	@WithMockUser(authorities = { "FOO" })
	public void testAttr() throws Exception {
		TestListener listener = new TestListener();
		StateMachine<States,Events> machine = buildMachine(listener, "ROLE_ANONYMOUS", ComparisonType.ANY, null);
		assertTransitionDenied(machine, listener);
	}

	@Test
	@WithMockUser(roles = { "FOO" })
	public void testExpression() throws Exception {
		TestListener listener = new TestListener();
		StateMachine<States,Events> machine = buildMachine(listener, null, null, "hasRole('FOO')");
		assertTransitionAllowed(machine, listener);
	}

	private static void assertTransitionAllowed(StateMachine<States, Events> machine, TestListener listener) throws Exception {
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0));

		listener.reset(1);
		machine.sendEvent(Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S1));
	}

	private static void assertTransitionDenied(StateMachine<States, Events> machine, TestListener listener) throws Exception {
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0));

		listener.reset(1);
		machine.sendEvent(Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(false));
		assertThat(listener.stateChangedCount, is(0));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0));
	}

	@Configuration
	public static class Config {
	}

	public static enum States {
		S0, S1;
	}

	public static enum Events {
		A;
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

	private static StateMachine<States, Events> buildMachine(TestListener listener, String attributes, ComparisonType match, String expression) throws Exception {
		Builder<States, Events> builder = StateMachineBuilder.<States, Events>builder();

		builder.configureConfiguration()
			.withConfiguration()
				.listener(listener)
				.autoStartup(true)
				.taskExecutor(new SyncTaskExecutor())
				.and()
			.withSecurity()
				.enabled(true);

		builder.configureStates()
			.withStates()
				.initial(States.S0)
				.state(States.S0)
				.state(States.S1);

		ExternalTransitionConfigurer<States, Events> withExternal = builder.configureTransitions()
			.withExternal();
		if (attributes != null) {
			withExternal.secured(attributes, match);
		}
		if (expression != null) {
			withExternal.secured(expression);
		}
		withExternal.source(States.S0);
		withExternal.target(States.S1);
		withExternal.event(Events.A);

		return builder.build();
	}

}
