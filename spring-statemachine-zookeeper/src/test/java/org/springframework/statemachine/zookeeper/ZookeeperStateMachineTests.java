/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.zookeeper;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.DistributedStateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class ZookeeperStateMachineTests extends AbstractZookeeperTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStateChanges() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class, Config1.class, Config2.class);
		context.refresh();

		StateMachine<String, String> machine1 =
				context.getBean("sm1", StateMachine.class);
		StateMachine<String, String> machine2 =
				context.getBean("sm2", StateMachine.class);

		TestListener listener1 = new TestListener();
		TestListener listener2 = new TestListener();
		machine1.addStateListener(listener1);
		machine2.addStateListener(listener2);

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble1 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ZookeeperStateMachineEnsemble<String, String> ensemble2 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ensemble1.afterPropertiesSet();
		ensemble2.afterPropertiesSet();
		ensemble1.start();
		ensemble2.start();

		DistributedStateMachine<String, String> machine1s =
				new DistributedStateMachine<String, String>(ensemble1, machine1);

		DistributedStateMachine<String, String> machine2s =
				new DistributedStateMachine<String, String>(ensemble2, machine2);

		machine1s.afterPropertiesSet();
		machine2s.afterPropertiesSet();

		machine1s.start();
		machine2s.start();

		listener1.reset(1);
		listener2.reset(1);
		machine1s.sendEvent("E1");
		assertThat(listener1.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener1.stateChangedCount, is(1));
		assertThat(listener2.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener2.stateChangedCount, is(1));
		assertThat(machine1.getState().getIds(), containsInAnyOrder("S1"));
		assertThat(machine2.getState().getIds(), containsInAnyOrder("S1"));

		listener1.reset(1);
		listener2.reset(1);
		machine1s.sendEvent("E2");
		assertThat(listener1.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener1.stateChangedCount, is(1));
		assertThat(listener2.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener2.stateChangedCount, is(1));
		assertThat(machine1.getState().getIds(), containsInAnyOrder("S2"));
		assertThat(machine2.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoinLaterShouldSyncState() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class, Config1.class, Config2.class);
		context.refresh();

		StateMachine<String, String> machine1 =
				context.getBean("sm1", StateMachine.class);
		StateMachine<String, String> machine2 =
				context.getBean("sm2", StateMachine.class);

		TestListener listener1 = new TestListener();
		TestListener listener2 = new TestListener();
		machine1.addStateListener(listener1);
		machine2.addStateListener(listener2);

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble1 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ensemble1.afterPropertiesSet();
		ensemble1.start();

		DistributedStateMachine<String, String> machine1s =
				new DistributedStateMachine<String, String>(ensemble1, machine1);


		machine1s.afterPropertiesSet();
		machine1s.start();

		listener1.reset(1);
		machine1s.sendEvent("E1");
		assertThat(listener1.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener1.stateChangedCount, is(1));
		assertThat(machine1.getState().getIds(), containsInAnyOrder("S1"));

		ZookeeperStateMachineEnsemble<String, String> ensemble2 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		ensemble2.afterPropertiesSet();
		ensemble2.start();
		DistributedStateMachine<String, String> machine2s =
				new DistributedStateMachine<String, String>(ensemble2, machine2);
		machine2s.afterPropertiesSet();
		machine2s.start();
		assertThat(machine2.getState().getIds(), containsInAnyOrder("S1"));
	}

	@Configuration
	@EnableStateMachine(name = "sm1")
	static class Config1 extends SharedConfig {
	}

	@Configuration
	@EnableStateMachine(name = "sm2")
	static class Config2 extends SharedConfig {
	}

	static class SharedConfig extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.state("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1")
					.and()
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E2");
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<String, String> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;

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
