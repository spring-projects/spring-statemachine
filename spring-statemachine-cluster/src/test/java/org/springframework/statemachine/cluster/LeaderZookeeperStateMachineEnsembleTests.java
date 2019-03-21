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
package org.springframework.statemachine.cluster;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.EnsembleListenerAdapter;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;

public class LeaderZookeeperStateMachineEnsembleTests extends AbstractZookeeperTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLeader() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class, Config1.class);
		context.refresh();

		StateMachineFactory<String, String> factory = context.getBean(StateMachineFactory.class);
		StateMachineEnsemble<String, String> stateMachineEnsemble = context.getBean(StateMachineEnsemble.class);
		TestEnsembleListener listener = context.getBean(TestEnsembleListener.class);

		StateMachine<String, String> machine1 = factory.getStateMachine();
		assertThat(machine1.getState().getIds(), contains("S1"));
		assertThat(listener.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachineEnsemble.getLeader(), is(machine1));

		listener.reset(1);
		StateMachine<String, String> machine2 = factory.getStateMachine();
		stateMachineEnsemble.leave(machine1);
		assertThat(listener.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachineEnsemble.getLeader(), is(machine2));
	}

	@Configuration
	@EnableStateMachineFactory
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private CuratorFramework curatorClient;

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(true)
					.and()
				.withDistributed()
					.ensemble(stateMachineEnsemble());
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3");
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
		public StateMachineEnsemble<String, String> stateMachineEnsemble() throws Exception {
			LeaderZookeeperStateMachineEnsemble<String,String> ensemble = new LeaderZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
			ensemble.addEnsembleListener(testEnsembleListener());
			return ensemble;
		}

		@Bean
		public TestEnsembleListener testEnsembleListener() {
			return new TestEnsembleListener();
		}

	}

	static class TestEnsembleListener extends EnsembleListenerAdapter<String, String> {
		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void ensembleLeaderGranted(StateMachine<String, String> stateMachine) {
			latch.countDown();
		}

		void reset(int a1) {
			latch = new CountDownLatch(a1);
		}
	}

}
