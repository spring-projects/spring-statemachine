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

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.support.StateMachineInterceptorList;
import org.springframework.statemachine.transition.Transition;

/**
 * Generic security config tests.
 *
 * @author Janne Valkealahti
 *
 */
public class SecurityConfigTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testSecurityEnabledWithTrue() throws Exception {
		context.register(Config1.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		StateMachineInterceptorList<?, ?> ilist = TestUtils.readField("interceptors", machine);
		List<StateMachineInterceptor<?, ?>> interceptors = TestUtils.readField("interceptors", ilist);
		assertThat(interceptors, notNullValue());
		assertThat(interceptors.size(), is(1));
		assertThat(interceptors.get(0), instanceOf(StateMachineSecurityInterceptor.class));
		Object adm = TestUtils.readField("transitionAccessDecisionManager", interceptors.get(0));
		assertThat(adm, nullValue());
	}

	@Test
	public void testSecurityDisabledWithFalse() throws Exception {
		context.register(Config2.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		StateMachineInterceptorList<?, ?> ilist = TestUtils.readField("interceptors", machine);
		List<StateMachineInterceptor<?, ?>> interceptors = TestUtils.readField("interceptors", ilist);
		assertThat(interceptors, notNullValue());
		assertThat(interceptors.size(), is(0));
	}

	@Test
	public void testSecurityEnabledWithJustWith() throws Exception {
		context.register(Config3.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		StateMachineInterceptorList<?, ?> ilist = TestUtils.readField("interceptors", machine);
		List<StateMachineInterceptor<?, ?>> interceptors = TestUtils.readField("interceptors", ilist);
		assertThat(interceptors.size(), is(1));
		assertThat(interceptors.get(0), instanceOf(StateMachineSecurityInterceptor.class));
		Object adm = TestUtils.readField("transitionAccessDecisionManager", interceptors.get(0));
		assertThat(adm, nullValue());
	}

	@Test
	public void testSecurityDisabledNoSecurityConfigurer() throws Exception {
		context.register(Config4.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		StateMachineInterceptorList<?, ?> ilist = TestUtils.readField("interceptors", machine);
		List<StateMachineInterceptor<?, ?>> interceptors = TestUtils.readField("interceptors", ilist);
		assertThat(interceptors, notNullValue());
		assertThat(interceptors.size(), is(0));
	}

	@Test
	public void testCustomAccessDecisionManager() throws Exception {
		context.register(Config5.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		StateMachineInterceptorList<?, ?> ilist = TestUtils.readField("interceptors", machine);
		List<StateMachineInterceptor<?, ?>> interceptors = TestUtils.readField("interceptors", ilist);
		assertThat(interceptors, notNullValue());
		assertThat(interceptors.size(), is(1));
		assertThat(interceptors.get(0), instanceOf(StateMachineSecurityInterceptor.class));
		Object adm = TestUtils.readField("transitionAccessDecisionManager", interceptors.get(0));
		assertThat(adm, notNullValue());
		assertThat(adm, instanceOf(MockAccessDecisionManager.class));
	}

	@Test
	public void testTransitionExplicit() throws Exception {
		context.register(Config6.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		Transition<String, String> transition = machine.getTransitions().iterator().next();
		assertThat(transition.getSecurityRule(), notNullValue());
	}

	@Test
	public void testTransitionGlobal() throws Exception {
		context.register(Config8.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		Transition<String, String> transition = machine.getTransitions().iterator().next();
		assertThat(transition.getSecurityRule(), notNullValue());
	}

	@Test
	public void testEventRule() throws Exception {
		context.register(Config7.class);
		context.refresh();
		assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		@SuppressWarnings("unchecked")
		ObjectStateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine, notNullValue());

		StateMachineInterceptorList<?, ?> ilist = TestUtils.readField("interceptors", machine);
		List<StateMachineInterceptor<?, ?>> interceptors = TestUtils.readField("interceptors", ilist);
		assertThat(interceptors, notNullValue());
		assertThat(interceptors.size(), is(1));
		assertThat(interceptors.get(0), instanceOf(StateMachineSecurityInterceptor.class));
		Object adm = TestUtils.readField("eventSecurityRule", interceptors.get(0));
		assertThat(adm, notNullValue());
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.enabled(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.enabled(false);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity();
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.eventAccessDecisionManager(new MockAccessDecisionManager())
					.transitionAccessDecisionManager(new MockAccessDecisionManager());
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config6 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.enabled(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A")
					.secured("expression")
					.secured("FOO", ComparisonType.ALL);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config7 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.enabled(true)
					.event("expression")
					.event("FOO", ComparisonType.ALL);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A");
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config8 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withSecurity()
					.enabled(true)
					.transition("expression")
					.transition("FOO", ComparisonType.ALL);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S0")
					.state("S1");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S0")
					.target("S1")
					.event("A");
		}

	}

	private static class MockAccessDecisionManager implements AccessDecisionManager {

		@Override
		public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
				throws AccessDeniedException, InsufficientAuthenticationException {
		}

		@Override
		public boolean supports(ConfigAttribute attribute) {
			return false;
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return false;
		}
	}

}
