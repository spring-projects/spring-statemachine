/*
 * Copyright 2016-2023 the original author or authors.
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
package org.springframework.statemachine.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.model.MalformedConfigurationException;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.verifier.StateMachineModelVerifier;

public class ConfigurationErrorTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testInitialStateNotSet1() {
		assertThatThrownBy(() -> {
			context.register(Config1.class);
			context.refresh();
		}).satisfies(e -> {
			assertThat(e.getCause()).isInstanceOf(MalformedConfigurationException.class);
		});
	}

	@Test
	public void testInitialStateNotSet2() throws Exception {
		assertThatThrownBy(() -> {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder
				.configureStates()
				.withStates()
				.state("S1")
				.state("S2");
			builder
				.configureTransitions()
				.withExternal()
				.source("S1")
				.target("S2")
				.event("E1");
			builder.build();
		}).satisfies(e -> {
			assertThat(e.getCause()).isInstanceOf(MalformedConfigurationException.class);
		});
	}

	@Test
	public void testNoTransitions() throws Exception {
		assertThatThrownBy(() -> {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder
				.configureStates()
				.withStates()
				.initial("S1")
				.state("S1")
				.state("S2");
			builder.build();
		}).satisfies(e -> {
			assertThat(e.getCause()).isInstanceOf(MalformedConfigurationException.class);
		});
	}

	@Test
	public void testVerifierDisabled() throws Exception {
		// should fail for no transitions but verifier is disabled
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder
			.configureConfiguration()
				.withVerifier()
					.enabled(false);
		builder
			.configureStates()
				.withStates()
					.initial("S1")
					.state("S1")
					.state("S2");
		builder.build();
	}

	@Test
	public void testCustomVerifier() throws Exception {
		TestStateMachineModelVerifier verifier = new TestStateMachineModelVerifier();
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder
			.configureConfiguration()
				.withVerifier()
					.verifier(verifier);
		builder
			.configureStates()
				.withStates()
					.initial("S1")
					.state("S1")
					.state("S2");
		builder.build();
		assertThat(verifier.latch.await(2, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	public void testPseudostateNoTransition() throws Exception {
		assertThatThrownBy(() -> {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureStates()
				.withStates()
					.initial("S1")
					.state("S1")
					.choice("S2")
					.state("S3");
			builder.configureTransitions()
				.withExternal()
					.source("S1")
					.target("S2");
			builder.build();
		}).satisfies(e -> {
			assertThat(e.getCause()).isInstanceOf(MalformedConfigurationException.class);
			assertThat(e.getCause().getLocalizedMessage()).contains("No transitions for state S2");
		});
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {
		// initial state not set

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					//.initial("S1")
					.state("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
		}
	}

	static class TestStateMachineModelVerifier implements StateMachineModelVerifier<String, String> {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void verify(StateMachineModel<String, String> model) {
			latch.countDown();
		}
	}

}
