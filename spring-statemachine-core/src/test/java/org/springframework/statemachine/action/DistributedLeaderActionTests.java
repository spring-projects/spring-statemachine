/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.action;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.InMemoryStateMachineEnsemble;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;

public class DistributedLeaderActionTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testAction() throws Exception {
		context.register(Config1.class);
		context.refresh();

		TestLeaderAction action = context.getBean("testLeaderAction", TestLeaderAction.class);

		@SuppressWarnings("unchecked")
		StateMachineFactory<String, String> factory = context.getBean(StateMachineFactory.class);

		StateMachine<String, String> machine1 = factory.getStateMachine();
		StateMachine<String, String> machine2 = factory.getStateMachine();
		machine1.sendEvent("E1");
		assertThat(action.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(action.count, is(1));
		action.reset(2);

		machine2.sendEvent("E2");
		assertThat(action.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(action.count, is(1));
	}

	@Configuration
	@EnableStateMachineFactory
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(true)
					.and()
				.withDistributed()
					.ensemble(ensemble());
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2", testDistributedLeaderAction(), null)
					.state("S3", testDistributedLeaderAction(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2").target("S3")
					.event("E2")
					.and()
				.withExternal()
					.source("S3").target("S1")
					.event("E3");
		}

		@Bean
		public StateMachineEnsemble<String, String> ensemble() {
			return new InMemoryStateMachineEnsemble<String, String>();
		}

		@Bean
		public Action<String, String> testLeaderAction() {
			return new TestLeaderAction();
		}

		@Bean
		public Action<String, String> testDistributedLeaderAction() {
			return new DistributedLeaderAction<>(testLeaderAction(), ensemble());
		}

	}

	static class TestLeaderAction implements Action<String, String> {

		CountDownLatch latch = new CountDownLatch(2);
		int count = 0;

		@Override
		public void execute(StateContext<String, String> context) {
			count++;
			latch.countDown();
		}

		public void reset(int a1) {
			count = 0;
			latch = new CountDownLatch(a1);
		}
	}

}
