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
package org.springframework.statemachine.zookeeper;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.DistributedStateMachine;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.statemachine.transition.Transition;

public class ZookeeperStateMachineTests extends AbstractZookeeperTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStateChangesManualSetup() throws Exception {
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
	public void testStateChangesManualSetupSendDifferentMachines() throws Exception {
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
		machine2s.sendEvent("E2");
		assertThat(listener1.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener1.stateChangedCount, is(1));
		assertThat(listener2.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener2.stateChangedCount, is(1));
		assertThat(machine1.getState().getIds(), containsInAnyOrder("S2"));
		assertThat(machine2.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLifecycle() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class, Config3.class, Config4.class);
		context.refresh();

		StateMachine<String, String> machine1 =
				context.getBean("sm1", StateMachine.class);
		StateMachine<String, String> machine2 =
				context.getBean("sm2", StateMachine.class);

		assertThat(((SmartLifecycle)machine1).isAutoStartup(), is(false));
		assertThat(((SmartLifecycle)machine1).isRunning(), is(false));
		assertThat(((SmartLifecycle)machine2).isAutoStartup(), is(false));
		assertThat(((SmartLifecycle)machine2).isRunning(), is(false));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStateChangesConfigSetup() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class, Config3.class, Config4.class);
		context.refresh();

		StateMachine<String, String> machine1 =
				context.getBean("sm1", StateMachine.class);
		StateMachine<String, String> machine2 =
				context.getBean("sm2", StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(machine1)
					.stateMachine(machine2)
					.step().expectState("SI").and()
					.step().sendEvent("E1").expectStateChanged(1).expectState("S1").and()
					.step().sendEvent("E2").expectStateChanged(1).expectState("S2").and()
					.build();

		plan.test();
	}

	@Test
	public void testVariousChangesInShowcase() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		StateMachine<String, String> machine1 =
				buildTestStateMachine(curatorClient);

		StateMachine<String, String> machine2 =
				buildTestStateMachine(curatorClient);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.stateMachine(machine2)
					.step()
						.expectStates("S0", "S1", "S11")
						.expectVariable("foo")
						.expectVariable("foo", 0)
						.and()
					.step()
						.sendEvent("C", machine1)
						.expectStateChanged(3)
						.expectStates("S0", "S2", "S21", "S211")
						.and()
					.step()
						.sendEvent("C", machine2)
						.expectStateChanged(2)
						.expectStates("S0", "S1", "S11")
						.and()
					.build();

		plan.test();
	}

	@Test
	public void testShouldHaveCasErrorDoesNotBreakMachines() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		StateMachine<String, String> machine1 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine2 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine3 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine4 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine5 =
				buildTestStateMachine2(curatorClient);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.stateMachine(machine2)
					.stateMachine(machine3)
					.stateMachine(machine4)
					.stateMachine(machine5)
					.step()
						.expectStates("SI")
						.and()
					.step()
						.sendEvent("E1", true)
						.expectStateChanged(1)
						.expectStates("S1")
						.and()
					.step()
						.sendEvent("E2", true)
						.expectStateChanged(1)
						.expectStates("S2")
						.and()
					.build();

		plan.test();
	}

	@Test
	public void testParallelEvents() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		StateMachine<String, String> machine1 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine2 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine3 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine4 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine5 =
				buildTestStateMachine2(curatorClient);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.stateMachine(machine2)
					.stateMachine(machine3)
					.stateMachine(machine4)
					.stateMachine(machine5)
					.step()
						.expectStates("SI")
						.and()
					.step()
						.sendEvent("E1", true)
						.expectStateChanged(1)
						.expectStates("S1")
						.and()
					.step()
						.sendEvent("E2", true, true)
						.expectStateChanged(1)
						.expectStates("S2")
						.and()
					.step()
						.sendEvent("E3", true, true)
						.expectStateChanged(1)
						.expectStates("S1")
						.and()
					.step()
						.sendEvent("E2", true, true)
						.expectStateChanged(1)
						.expectStates("S2")
						.and()
					.step()
						.sendEvent("E3", true, true)
						.expectStateChanged(1)
						.expectStates("S1")
						.and()
					.step()
						.sendEvent("E2", true, true)
						.expectStateChanged(1)
						.expectStates("S2")
						.and()
					.step()
						.sendEvent("E3", true, true)
						.expectStateChanged(1)
						.expectStates("S1")
						.and()
					.build();

		plan.test();
	}

	@Test
	public void testExtendedStateVariables1() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		StateMachine<String, String> machine1 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine2 =
				buildTestStateMachine2(curatorClient);

		Message<String> message = MessageBuilder
				.withPayload("EV")
				.setHeader("testVariable", "x1")
				.build();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.stateMachine(machine2)
					.step()
						.expectStates("SI")
						.and()
					.step()
						.sendEvent(message, machine1)
						.expectTransition(1)
						.expectVariable("testVariable", "x1")
						.and()
					.build();

		plan.test();
	}

	@Test
	public void testExtendedStateVariables2() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		StateMachine<String, String> machine1 =
				buildTestStateMachine2(curatorClient);
		StateMachine<String, String> machine2 =
				buildTestStateMachine2(curatorClient);

		Message<String> message = MessageBuilder
				.withPayload("EV")
				.setHeader("testVariable", "x1")
				.build();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.stateMachine(machine2)
					.step()
						.expectStates("SI")
						.and()
					.step()
						.sendEvent("E1", machine1)
						.expectStateChanged(1)
						.expectStates("S1")
						.and()
					.step()
						.sendEvent(message, machine1)
						.expectTransition(1)
						.expectExtendedStateChanged(1)
						.expectVariable("testVariable", "x1")
						.and()
					.build();

		plan.test();
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

	@Test
	public void testConnectionLoss1() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		StateMachine<String, String> machine1 =
				buildTestStateMachine(curatorClient);

		StateMachineTestPlan<String, String> plan1 =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.step()
						.expectStates("S0", "S1", "S11")
						.and()
					.step()
						.sendEvent("C", machine1)
						.expectStateChanged(3)
						.expectStates("S0", "S2", "S21", "S211")
						.and()
					.build();
		plan1.test();

		Object ensemble = TestUtils.readField("ensemble", machine1);
		try {
			TestUtils.callMethod("handleZkDisconnect", ensemble);
		} catch (Exception e) {
		}
		TestUtils.callMethod("handleZkConnect", ensemble);

		StateMachineTestPlan<String, String> plan2 =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.step()
						.expectStates("S0", "S2", "S21", "S211")
						.and()
					.step()
						.sendEvent("C", machine1)
						.expectStateChanged(2)
						.expectStates("S0", "S1", "S11")
						.and()
					.build();
		plan2.test();
	}

	@Test
	public void testConnectionLoss2() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		StateMachine<String, String> machine1 =
				buildTestStateMachine(curatorClient);
		StateMachine<String, String> machine2 =
				buildTestStateMachine(curatorClient);
		StateMachine<String, String> machine3 =
				buildTestStateMachine(curatorClient);
		StateMachine<String, String> machine4 =
				buildTestStateMachine(curatorClient);
		StateMachine<String, String> machine5 =
				buildTestStateMachine(curatorClient);

		StateMachineTestPlan<String, String> plan1 =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.stateMachine(machine2)
					.stateMachine(machine3)
					.stateMachine(machine4)
					.stateMachine(machine5)
					.step()
						.expectStates("S0", "S1", "S11")
						.and()
					.step()
						.sendEvent("C", machine1)
						.expectStateChanged(3)
						.expectStates("S0", "S2", "S21", "S211")
						.and()
					.build();
		plan1.test();

		Object ensemble2 = TestUtils.readField("ensemble", machine2);
		Object ensemble3 = TestUtils.readField("ensemble", machine3);
		Object ensemble4 = TestUtils.readField("ensemble", machine4);
		Object ensemble5 = TestUtils.readField("ensemble", machine5);
		try {
			TestUtils.callMethod("handleZkDisconnect", ensemble2);
			TestUtils.callMethod("handleZkDisconnect", ensemble3);
			TestUtils.callMethod("handleZkDisconnect", ensemble4);
			TestUtils.callMethod("handleZkDisconnect", ensemble5);
		} catch (Exception e) {
		}
		TestUtils.callMethod("handleZkConnect", ensemble2);
		TestUtils.callMethod("handleZkConnect", ensemble3);
		TestUtils.callMethod("handleZkConnect", ensemble4);
		TestUtils.callMethod("handleZkConnect", ensemble5);

		StateMachineTestPlan<String, String> plan2 =
				StateMachineTestPlanBuilder.<String, String>builder()
					.defaultAwaitTime(2)
					.stateMachine(machine1)
					.stateMachine(machine2)
					.stateMachine(machine3)
					.stateMachine(machine4)
					.stateMachine(machine5)
					.step()
						.expectStates("S0", "S2", "S21", "S211")
						.and()
					.step()
						.sendEvent("C", machine1)
						.expectStateChanged(2)
						.expectStates("S0", "S1", "S11")
						.and()
					.step()
						.sendEvent("C", machine2)
						.expectStateChanged(3)
						.expectStates("S0", "S2", "S21", "S211")
						.and()
					.step()
						.sendEvent("C", machine3)
						.expectStateChanged(2)
						.expectStates("S0", "S1", "S11")
						.and()
					.step()
						.sendEvent("C", machine4)
						.expectStateChanged(3)
						.expectStates("S0", "S2", "S21", "S211")
						.and()
					.step()
						.sendEvent("C", machine5)
						.expectStateChanged(2)
						.expectStates("S0", "S1", "S11")
						.and()
					.build();
		plan2.test();
	}

	@Configuration
	@EnableStateMachine(name = "sm1")
	static class Config1 extends SharedConfig1 {
	}

	@Configuration
	@EnableStateMachine(name = "sm2")
	static class Config2 extends SharedConfig1 {
	}

	@Configuration
	@EnableStateMachine(name = "sm1")
	static class Config3 extends SharedConfig2 {

		@Autowired
		private CuratorFramework curatorClient;

		@Override
		@Bean(name = "listener1")
		public TestListener stateMachineListener() {
			return new TestListener();
		}

		@Override
		@Bean(name = "ensemble1")
		public StateMachineEnsemble<String, String> stateMachineEnsemble() throws Exception {
			return new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		}
	}

	@Configuration
	@EnableStateMachine(name = "sm2")
	static class Config4 extends SharedConfig2 {

		@Autowired
		private CuratorFramework curatorClient;

		@Override
		@Bean(name = "listener2")
		public TestListener stateMachineListener() {
			return new TestListener();
		}

		@Override
		@Bean(name = "ensemble2")
		public StateMachineEnsemble<String, String> stateMachineEnsemble() throws Exception {
			return new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		}
	}

	public abstract static class SharedConfig2 extends SharedConfig1 {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withDistributed()
					.ensemble(stateMachineEnsemble())
					.and()
				.withConfiguration()
					.listener(stateMachineListener())
					// TODO: false, really? testStateChangesConfigSetup() will fail if true
					// maybe it's due to dist needs to be reseted!
					// previously setting it true, didn't actually enable autostart.
					.autoStartup(false);
		}

		public abstract StateMachineEnsemble<String, String> stateMachineEnsemble() throws Exception;
		public abstract TestListener stateMachineListener();

	}

	static class SharedConfig1 extends StateMachineConfigurerAdapter<String, String> {

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
		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);

		@Override
		public void stateMachineStarted(StateMachine<String, String> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

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

	private static StateMachineEnsemble<String, String> stateMachineEnsemble(CuratorFramework curatorClient) {
		ZookeeperStateMachineEnsemble<String, String> ensemble = new ZookeeperStateMachineEnsemble<String, String>(
				curatorClient, "/foo");
		ensemble.afterPropertiesSet();
		ensemble.start();
		return ensemble;
	}

	private StateMachine<String, String> buildTestStateMachine(CuratorFramework curatorClient)
			throws Exception {
		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.taskExecutor(new SyncTaskExecutor())
				.autoStartup(true)
				.and()
			.withDistributed()
				.ensemble(stateMachineEnsemble(curatorClient));

		builder.configureStates()
				.withStates()
					.initial("S0", fooAction())
					.state("S0")
					.and()
					.withStates()
						.parent("S0")
						.initial("S1")
						.state("S1")
						.and()
						.withStates()
							.parent("S1")
							.initial("S11")
							.state("S11")
							.state("S12")
							.and()
					.withStates()
						.parent("S0")
						.state("S2")
						.and()
						.withStates()
							.parent("S2")
							.initial("S21")
							.state("S21")
							.and()
							.withStates()
								.parent("S21")
								.initial("S211")
								.state("S211")
								.state("S212");

		builder.configureTransitions()
				.withExternal()
					.source("S1").target("S1").event("A")
					.guard(foo1Guard())
					.and()
				.withExternal()
					.source("S1").target("S11").event("B")
					.and()
				.withExternal()
					.source("S21").target("S211").event("B")
					.and()
				.withExternal()
					.source("S1").target("S2").event("C")
					.and()
				.withExternal()
					.source("S2").target("S1").event("C")
					.and()
				.withExternal()
					.source("S1").target("S0").event("D")
					.and()
				.withExternal()
					.source("S211").target("S21").event("D")
					.and()
				.withExternal()
					.source("S0").target("S211").event("E")
					.and()
				.withExternal()
					.source("S1").target("S211").event("F")
					.and()
				.withExternal()
					.source("S2").target("S11").event("F")
					.and()
				.withExternal()
					.source("S11").target("S211").event("G")
					.and()
				.withExternal()
					.source("S211").target("S0").event("G")
					.and()
				.withInternal()
					.source("S0").event("H")
					.guard(foo0Guard())
					.action(fooAction())
					.and()
				.withInternal()
					.source("S2").event("H")
					.guard(foo1Guard())
					.action(fooAction())
					.and()
				.withInternal()
					.source("S1").event("H")
					.and()
				.withExternal()
					.source("S11").target("S12").event("I")
					.and()
				.withExternal()
					.source("S211").target("S212").event("I")
					.and()
				.withExternal()
					.source("S12").target("S212").event("I");

		return builder.build();
	}

	private StateMachine<String, String> buildTestStateMachine2(CuratorFramework curatorClient)
			throws Exception {
		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.taskExecutor(new SyncTaskExecutor())
				.autoStartup(true)
				.and()
			.withDistributed()
				.ensemble(stateMachineEnsemble(curatorClient));

		builder.configureStates()
				.withStates()
					.initial("SI")
					.state("S1")
					.state("S2");

		builder.configureTransitions()
				.withExternal()
					.source("SI").target("S1").event("E1")
					.and()
				.withExternal()
					.source("S1").target("S2").event("E2")
					.and()
				.withExternal()
					.source("S2").target("S1").event("E3")
					.and()
				.withInternal()
					.source("SI").event("EV")
					.action(setVariableAction())
					.and()
				.withInternal()
					.source("S1").event("EV")
					.action(setVariableAction());

		return builder.build();
	}

	private static FooGuard foo0Guard() {
		return new FooGuard(0);
	}

	private static FooGuard foo1Guard() {
		return new FooGuard(1);
	}

	private static FooAction fooAction() {
		return new FooAction();
	}

	private static SetVariableAction setVariableAction() {
		return new SetVariableAction();
	}

	private static class FooGuard implements Guard<String, String> {

		private final int match;

		public FooGuard(int match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			Object foo = context.getExtendedState().getVariables().get("foo");
			return !(foo == null || !foo.equals(match));
		}
	}

	private static class FooAction implements Action<String, String> {

		@Override
		public void execute(StateContext<String, String> context) {
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			Integer foo = context.getExtendedState().get("foo", Integer.class);
			if (foo == null) {
				variables.put("foo", 0);
			} else if (foo == 0) {
				variables.put("foo", 1);
			} else if (foo == 1) {
				variables.put("foo", 0);
			}
		}
	}

	private static class SetVariableAction implements Action<String, String> {

		@Override
		public void execute(StateContext<String, String> context) {
			String testVariable = context.getMessageHeaders().get("testVariable", String.class);
			if (testVariable != null) {
				context.getExtendedState().getVariables().put("testVariable", testVariable);
			}
		}

	}

}
