/*
 * Copyright 2015-2023 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.security.ActionSecurityTests.Config1;
import org.springframework.statemachine.security.ActionSecurityTests.Config2;
import org.springframework.statemachine.state.State;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests for securing actions.
 *
 * @author Janne Valkealahti
 */
@Disabled("TODO: REACTOR rethink security things")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {Config1.class, Config2.class})
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class ActionSecurityTests extends AbstractStateMachineTests {

	@Autowired
	StateMachine<States, Events> machine;

	@Autowired
	TestListener listener;

	@Autowired
	TestSecAction action1;

	@Test
	@WithMockUser(roles = { "FOO" })
	public void testActionExecutionDenied() throws Exception {
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S1);
		assertThat(action1.getCount()).isZero();
	}

	@Test
	@WithMockUser
	public void testActionExecutionAllowed() throws Exception {
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S0);

		listener.reset(1);
		doSendEventAndConsumeAll(machine, Events.A);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(States.S1);
		assertThat(action1.getCount()).isEqualTo(1);
	}

	@Configuration
	public static class Config1 {

		@Bean
		public InMemoryUserDetailsManager userDetailsService() {
			UserDetails user = User.withDefaultPasswordEncoder()
					.username("user")
					.password("password")
					.roles("USER")
					.build();
			return new InMemoryUserDetailsManager(user);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<States, Events> config)
				throws Exception {
			config
				.withConfiguration()
					.listener(testListener())
					.autoStartup(true)
					.and()
				.withSecurity()
					.enabled(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S0)
					.state(States.S0)
					.state(States.S1, action1(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S0)
					.target(States.S1)
					.event(Events.A);

		}

		@Bean
		public TestListener testListener() {
			return new TestListener();
		}

		@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
		@Bean
		public TestSecAction action1() {
			return new TestSecAction();
		}

	}

	public static enum States {
	    S0, S1;
	}

	public static enum Events {
	    A;
	}

	private static class TestSecAction implements Action<States, Events> {

		int count;

		@Secured("ROLE_USER")
		@Override
		public void execute(StateContext<States, Events> context) {
			count++;
		}

		public int getCount() {
			return count;
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

}
