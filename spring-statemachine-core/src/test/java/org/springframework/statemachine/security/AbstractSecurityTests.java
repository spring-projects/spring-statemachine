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

import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.SecurityConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;
import org.springframework.statemachine.state.State;

public abstract class AbstractSecurityTests extends AbstractStateMachineTests {

	protected static void assertTransitionAllowed(StateMachine<States, Events> machine, TestListener listener) throws Exception {
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0));

		listener.reset(1);
		machine.sendEvent(Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S1));
	}

	protected static void assertTransitionDenied(StateMachine<States, Events> machine, TestListener listener) throws Exception {
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0));

		listener.reset(1);
		machine.sendEvent(Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(false));
		assertThat(listener.stateChangedCount, is(0));
		assertThat(machine.getState().getIds(), containsInAnyOrder(States.S0));
	}

	protected static enum States {
		S0, S1;
	}

	protected static enum Events {
		A;
	}

	protected static class TestListener extends StateMachineListenerAdapter<States, Events> {

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

	protected static StateMachine<States, Events> buildMachine(TestListener listener, String attributes, ComparisonType match, String expression) throws Exception {
		return buildMachine(listener, attributes, match, expression, null, null, null);
	}

	protected static StateMachine<States, Events> buildMachine(TestListener listener, String attributes, ComparisonType match,
			String expression, String eventAttributes, ComparisonType eventMatch, String eventExpression) throws Exception {
		Builder<States, Events> builder = StateMachineBuilder.<States, Events>builder();

		StateMachineConfigurationConfigurer<States, Events> configureConfiguration = builder.configureConfiguration();
		configureConfiguration.withConfiguration()
				.listener(listener)
				.autoStartup(true)
				.taskExecutor(new SyncTaskExecutor());

		SecurityConfigurer<States, Events> withSecurity = configureConfiguration.withSecurity();
		withSecurity.enabled(true);

		if (eventAttributes != null && eventMatch != null) {
			withSecurity.event(eventAttributes, eventMatch);
		}

		if (eventExpression != null) {
			withSecurity.event(eventExpression);
		}


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
