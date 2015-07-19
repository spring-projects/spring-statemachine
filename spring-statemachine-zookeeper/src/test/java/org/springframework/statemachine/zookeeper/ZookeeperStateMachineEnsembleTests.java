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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.ensemble.EnsembleListeger;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.transition.Transition;

public class ZookeeperStateMachineEnsembleTests extends AbstractZookeeperTests {

	@Test
	public void testInitStart() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		ensemble.afterPropertiesSet();

		assertThat(curatorClient.checkExists().forPath("/foo/data/current"), notNullValue());
		assertThat(curatorClient.checkExists().forPath("/foo/data/log"), notNullValue());

		ensemble.start();
	}

	@Test
	public void testPersist() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		ensemble.afterPropertiesSet();

		assertThat(curatorClient.checkExists().forPath("/foo/data/current"), notNullValue());
		assertThat(curatorClient.getData().forPath("/foo/data/current").length, is(0));

		ensemble.setState(new DefaultStateMachineContext<String, String>("S1","E1", new HashMap<String, Object>(), new DefaultExtendedState()));
		assertThat(curatorClient.getData().forPath("/foo/data/current").length, greaterThan(0));

		ensemble.setState(new DefaultStateMachineContext<String, String>("S2","E1", new HashMap<String, Object>(), new DefaultExtendedState()));
	}

	@Test
	public void testReceiveEvents() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble1 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ZookeeperStateMachineEnsemble<String, String> ensemble2 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		TestEnsembleListener listener1 = new TestEnsembleListener();
		TestEnsembleListener listener2 = new TestEnsembleListener();
		ensemble1.addEnsembleListener(listener1);
		ensemble2.addEnsembleListener(listener2);

		ensemble1.afterPropertiesSet();
		ensemble1.start();
		ensemble2.afterPropertiesSet();
		ensemble2.start();

		TestStateMachine stateMachine1 = new TestStateMachine();
		TestStateMachine stateMachine2 = new TestStateMachine();

		ensemble1.join(stateMachine1);
		ensemble2.join(stateMachine2);
		assertThat(listener1.joinedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener2.joinedLatch.await(2, TimeUnit.SECONDS), is(true));

		ensemble1.setState(new DefaultStateMachineContext<String, String>("S1", "E1", new HashMap<String, Object>(), new DefaultExtendedState()));
		assertThat(listener2.eventLatch.await(2, TimeUnit.SECONDS), is(true));
	}

	@Test
	public void testClearExistingStatePaths() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		// members base path need to exist for delete to happen
		curatorClient.create().creatingParentsIfNeeded().forPath("/foo/members");
		curatorClient.create().creatingParentsIfNeeded().forPath("/foo/data/log", new byte[10]);

		ZookeeperStateMachineEnsemble<String, String> ensemble1 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ensemble1.afterPropertiesSet();
		ensemble1.start();

		// we assume that if data is 0, it's re-created
		assertThat(curatorClient.getData().forPath("/foo/data/log").length, is(0));
	}

	@Test
	public void testLogs() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo", true, 4);

		ensemble.afterPropertiesSet();
		ensemble.start();

		assertThat(curatorClient.checkExists().forPath("/foo/data/log"), notNullValue());
		assertThat(curatorClient.checkExists().forPath("/foo/data/log/0"), notNullValue());
		assertThat(curatorClient.checkExists().forPath("/foo/data/log/1"), notNullValue());
		assertThat(curatorClient.checkExists().forPath("/foo/data/log/2"), notNullValue());
		assertThat(curatorClient.checkExists().forPath("/foo/data/log/3"), notNullValue());
		assertThat(curatorClient.checkExists().forPath("/foo/data/log/4"), nullValue());
		assertThat(curatorClient.getData().forPath("/foo/data/log/0").length, is(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/1").length, is(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/2").length, is(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/3").length, is(0));

		ensemble.setState(new DefaultStateMachineContext<String, String>("S1","E1", new HashMap<String, Object>(), new DefaultExtendedState()));
		assertThat(curatorClient.getData().forPath("/foo/data/log/0").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/1").length, is(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/2").length, is(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/3").length, is(0));

		ensemble.setState(new DefaultStateMachineContext<String, String>("S2","E1", new HashMap<String, Object>(), new DefaultExtendedState()));
		assertThat(curatorClient.getData().forPath("/foo/data/log/0").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/1").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/2").length, is(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/3").length, is(0));

		ensemble.setState(new DefaultStateMachineContext<String, String>("S3","E1", new HashMap<String, Object>(), new DefaultExtendedState()));
		assertThat(curatorClient.getData().forPath("/foo/data/log/0").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/1").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/2").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/3").length, is(0));

		ensemble.setState(new DefaultStateMachineContext<String, String>("S4","E1", new HashMap<String, Object>(), new DefaultExtendedState()));
		assertThat(curatorClient.getData().forPath("/foo/data/log/0").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/1").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/2").length, greaterThan(0));
		assertThat(curatorClient.getData().forPath("/foo/data/log/3").length, greaterThan(0));
	}

	@Test(expected = IllegalStateException.class)
	public void testIllegalLogSize() throws Exception {
				new ZookeeperStateMachineEnsemble<String, String>(null, "/foo", true, 3);
	}

	@Test
	public void testContextEventsNotMissedBurstNoOverflow() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		TestEnsembleListener listener = new TestEnsembleListener();
		ensemble.addEnsembleListener(listener);

		ensemble.afterPropertiesSet();
		ensemble.start();
		listener.reset(0, 10);

		for (int i = 0; i < 10; i++) {
			ensemble.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
		}

		assertThat(listener.eventLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.events.size(), is(10));

		for (int i = 0; i < 10; i++) {
			assertThat(listener.events.get(i).getEvent(), is("E" + i));
		}
	}

	@Test
	public void testContextEventsNotMissedSlowNoOverflow() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		TestEnsembleListener listener = new TestEnsembleListener();
		ensemble.addEnsembleListener(listener);

		ensemble.afterPropertiesSet();
		ensemble.start();
		listener.reset(0, 10);

		for (int i = 0; i < 10; i++) {
			ensemble.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
			Thread.sleep(500);
		}

		assertThat(listener.eventLatch.await(3, TimeUnit.SECONDS), is(true));
		assertThat(listener.events.size(), is(10));

		for (int i = 0; i < 10; i++) {
			assertThat(listener.events.get(i).getEvent(), is("E" + i));
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private class TestEnsembleListener implements EnsembleListeger<String, String> {

		volatile CountDownLatch joinedLatch = new CountDownLatch(1);
		volatile CountDownLatch eventLatch = new CountDownLatch(1);
		volatile List<StateMachineContext<String, String>> events = new ArrayList<StateMachineContext<String,String>>();

		@Override
		public void stateMachineJoined(StateMachineContext<String, String> context) {
			joinedLatch.countDown();
		}

		@Override
		public void stateMachineLeft(StateMachineContext<String, String> context) {
		}

		@Override
		public void stateChanged(StateMachineContext<String, String> context) {
			events.add(context);
			eventLatch.countDown();
		}

		public void reset(int c1, int c2) {
			joinedLatch = new CountDownLatch(c1);
			eventLatch = new CountDownLatch(c2);
			events.clear();
		}

	}

	private class TestStateMachine implements StateMachine<String, String> {

		@Override
		public StateMachineAccessor<String, String> getStateMachineAccessor() {
			return null;
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}

		@Override
		public boolean sendEvent(Message<String> event) {
			return false;
		}

		@Override
		public boolean sendEvent(String event) {
			return false;
		}

		@Override
		public State<String, String> getState() {
			return null;
		}

		@Override
		public Collection<State<String, String>> getStates() {
			return null;
		}

		@Override
		public Collection<Transition<String, String>> getTransitions() {
			return null;
		}

		@Override
		public boolean isComplete() {
			return false;
		}

		@Override
		public void addStateListener(StateMachineListener<String, String> listener) {
		}

		@Override
		public void removeStateListener(StateMachineListener<String, String> listener) {
		}

		@Override
		public State<String, String> getInitialState() {
			return null;
		}

		@Override
		public ExtendedState getExtendedState() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

	}

}
