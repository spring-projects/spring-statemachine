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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
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

		ensemble.setState(new DefaultStateMachineContext<String, String>("S1","E1", null, null));
		ensemble.setState(new DefaultStateMachineContext<String, String>("S2","E1", null, null));

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

		ensemble1.setState(new DefaultStateMachineContext<String, String>("S1", "E1", null, null));
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

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private class TestEnsembleListener implements EnsembleListeger<String, String> {

		volatile CountDownLatch joinedLatch = new CountDownLatch(1);
		volatile CountDownLatch eventLatch = new CountDownLatch(1);

		@Override
		public void stateMachineJoined(StateMachineContext<String, String> context) {
			joinedLatch.countDown();
		}

		@Override
		public void stateMachineLeft(StateMachineContext<String, String> context) {
		}

		@Override
		public void stateChanged(StateMachineContext<String, String> context) {
			eventLatch.countDown();
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

	}

}
