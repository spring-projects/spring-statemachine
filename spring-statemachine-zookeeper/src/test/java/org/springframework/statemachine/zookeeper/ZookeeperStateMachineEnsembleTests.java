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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.ensemble.EnsembleListener;
import org.springframework.statemachine.ensemble.StateMachineEnsembleException;
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
	public void testReadStateFromOther() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

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

		assertThat(curatorClient.checkExists().forPath("/foo/data/current"), notNullValue());
		assertThat(curatorClient.getData().forPath("/foo/data/current").length, is(0));

		ensemble1.setState(new DefaultStateMachineContext<String, String>("S1","E1", new HashMap<String, Object>(), new DefaultExtendedState()));
		assertThat(curatorClient.getData().forPath("/foo/data/current").length, greaterThan(0));

		StateMachineContext<String, String> context = ensemble2.getState();
		assertThat(context, notNullValue());
		assertThat(context.getState(), is("S1"));
		assertThat(context.getEvent(), is("E1"));
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
		ensemble.join(new TestStateMachine());
		assertThat(listener.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		listener.reset(0, 10);

		for (int i = 0; i < 10; i++) {
			ensemble.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
		}

		assertThat(listener.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener.events.size(), is(10));

		for (int i = 0; i < 10; i++) {
			assertThat(listener.events.get(i).getEvent(), is("E" + i));
		}
	}

	@Test
	public void testContextEventsNotMissedBurstNoOverflow2() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble1 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ZookeeperStateMachineEnsemble<String, String> ensemble2 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		TestEnsembleListener listener1 = new TestEnsembleListener();
		ensemble1.addEnsembleListener(listener1);
		TestEnsembleListener listener2 = new TestEnsembleListener();
		ensemble2.addEnsembleListener(listener2);

		ensemble1.afterPropertiesSet();
		ensemble1.start();
		ensemble2.afterPropertiesSet();
		ensemble2.start();

		ensemble1.join(new TestStateMachine());
		assertThat(listener1.joinedLatch.await(3, TimeUnit.SECONDS), is(true));
		ensemble2.join(new TestStateMachine());
		assertThat(listener2.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		listener1.reset(0, 10);
		listener2.reset(0, 10);

		for (int i = 0; i < 10; i++) {
			ensemble1.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
		}

		assertThat(listener1.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener1.events.size(), is(10));
		assertThat(listener2.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener2.events.size(), is(10));

		for (int i = 0; i < 10; i++) {
			assertThat(listener1.events.get(i).getEvent(), is("E" + i));
			assertThat(listener2.events.get(i).getEvent(), is("E" + i));
		}
	}

	@Test
	public void testContextEventsNotMissedBurstNoOverflow3() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble1 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ZookeeperStateMachineEnsemble<String, String> ensemble2 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		TestEnsembleListener listener1 = new TestEnsembleListener();
		ensemble1.addEnsembleListener(listener1);
		TestEnsembleListener listener2 = new TestEnsembleListener();
		ensemble2.addEnsembleListener(listener2);

		ensemble1.afterPropertiesSet();
		ensemble1.start();
		ensemble2.afterPropertiesSet();
		ensemble2.start();

		ensemble1.join(new TestStateMachine());
		assertThat(listener1.joinedLatch.await(3, TimeUnit.SECONDS), is(true));
		ensemble2.join(new TestStateMachine());
		assertThat(listener2.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		listener1.reset(0, 10);
		listener2.reset(0, 10);

		Exception e = null;
		try {
			for (int i = 0; i < 10; i++) {
				if (((i % 2) == 0)) {
					ensemble1.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
							new HashMap<String, Object>(), new DefaultExtendedState()));
				} else {
					ensemble2.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
							new HashMap<String, Object>(), new DefaultExtendedState()));
				}
			}
		} catch (Exception ee) {
			e = ee;
		}

		if (e != null) {
			assertThat(e, instanceOf(StateMachineException.class));
			assertThat(((StateMachineException)e).contains(KeeperException.BadVersionException.class), is(true));
		} else {
			// miracle happened and no cas error, well then check events
			assertThat(listener1.eventLatch.await(10, TimeUnit.SECONDS), is(true));
			assertThat(listener1.events.size(), is(10));
			assertThat(listener2.eventLatch.await(10, TimeUnit.SECONDS), is(true));
			assertThat(listener2.events.size(), is(10));

			for (int i = 0; i < 10; i++) {
				assertThat(listener1.events.get(i).getEvent(), is("E" + i));
				assertThat(listener2.events.get(i).getEvent(), is("E" + i));
			}
		}
	}

	@Test
	public void testContextEventsNotMissedBurstNoOverflow4() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);

		ZookeeperStateMachineEnsemble<String, String> ensemble1 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");
		ZookeeperStateMachineEnsemble<String, String> ensemble2 =
				new ZookeeperStateMachineEnsemble<String, String>(curatorClient, "/foo");

		TestEnsembleListener listener1 = new TestEnsembleListener();
		ensemble1.addEnsembleListener(listener1);
		TestEnsembleListener listener2 = new TestEnsembleListener();
		ensemble2.addEnsembleListener(listener2);

		ensemble1.afterPropertiesSet();
		ensemble1.start();
		ensemble2.afterPropertiesSet();
		ensemble2.start();

		ensemble1.join(new TestStateMachine());
		assertThat(listener1.joinedLatch.await(3, TimeUnit.SECONDS), is(true));
		ensemble2.join(new TestStateMachine());
		assertThat(listener2.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		for (int i = 0; i < 10; i++) {
			listener1.reset(0, 1);
			listener2.reset(0, 1);
			if (((i % 2) == 0)) {
				ensemble1.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
						new HashMap<String, Object>(), new DefaultExtendedState()));
			} else {
				ensemble2.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
						new HashMap<String, Object>(), new DefaultExtendedState()));
			}
			assertThat(listener1.eventLatch.await(10, TimeUnit.SECONDS), is(true));
			assertThat(listener1.events.size(), is(1));
			assertThat(listener2.eventLatch.await(10, TimeUnit.SECONDS), is(true));
			assertThat(listener2.events.size(), is(1));
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

		ensemble.join(new TestStateMachine());
		assertThat(listener.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		listener.reset(0, 10);

		for (int i = 0; i < 10; i++) {
			ensemble.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
			Thread.sleep(500);
		}

		assertThat(listener.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener.events.size(), is(10));

		for (int i = 0; i < 10; i++) {
			assertThat(listener.events.get(i).getEvent(), is("E" + i));
		}
	}

	@Test
	public void testContextEventsNotMissedSlowNoOverflow2() throws Exception {
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

		ensemble1.join(new TestStateMachine());
		assertThat(listener1.joinedLatch.await(3, TimeUnit.SECONDS), is(true));
		ensemble2.join(new TestStateMachine());
		assertThat(listener2.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		listener1.reset(0, 10);
		listener2.reset(0, 10);

		for (int i = 0; i < 10; i++) {
			ensemble1.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
			Thread.sleep(500);
		}

		assertThat(listener1.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener1.events.size(), is(10));
		assertThat(listener2.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener2.events.size(), is(10));

		for (int i = 0; i < 10; i++) {
			assertThat(listener1.events.get(i).getEvent(), is("E" + i));
			assertThat(listener2.events.get(i).getEvent(), is("E" + i));
		}
	}

	@Test
	public void testDoesNotThrowCasError() throws Exception {
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

		ensemble1.join(new TestStateMachine());
		assertThat(listener1.joinedLatch.await(3, TimeUnit.SECONDS), is(true));
		ensemble2.join(new TestStateMachine());
		assertThat(listener2.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		listener1.reset(0, 9);
		listener2.reset(0, 9);

		for (int i = 0; i < 9; i++) {
			ensemble1.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
		}

		assertThat(listener1.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener1.events.size(), is(9));
		assertThat(listener2.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener2.events.size(), is(9));

		for (int i = 0; i < 9; i++) {
			assertThat(listener1.events.get(i).getEvent(), is("E" + i));
			assertThat(listener2.events.get(i).getEvent(), is("E" + i));
		}

		listener1.reset(0, 1);
		listener2.reset(0, 1);

		// should not throw BadVersionException when we immediately
		for (int i = 9; i < 10; i++) {
			ensemble2.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
		}

		assertThat(listener1.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener1.events.size(), is(1));
		assertThat(listener2.eventLatch.await(10, TimeUnit.SECONDS), is(true));
		assertThat(listener2.events.size(), is(1));

		for (int i = 0; i < 1; i++) {
			assertThat(listener1.events.get(i).getEvent(), is("E" + (i+9)));
			assertThat(listener2.events.get(i).getEvent(), is("E" + (i+9)));
		}
	}

	@Test
	public void testEventsOverflow() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();
		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);
		OverflowControlZookeeperStateMachineEnsemble ensemble =
				new OverflowControlZookeeperStateMachineEnsemble(curatorClient, "/foo", true, 4);

		TestEnsembleListener listener = new TestEnsembleListener();
		ensemble.addEnsembleListener(listener);

		ensemble.afterPropertiesSet();
		ensemble.start();

		ensemble.join(new TestStateMachine());
		assertThat(listener.joinedLatch.await(3, TimeUnit.SECONDS), is(true));

		listener.reset(0, 10, 1);

		// this is a bit of a hack to test things like this
		// not sure if this is totally reliable way
		// we've hacked to disable znode event registration so
		// should not get any errors until it's re-enabled and
		// we write again
		for (int i = 0; i < 10; i++) {
			ensemble.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
		}
		assertThat(listener.errorLatch.await(2, TimeUnit.SECONDS), is(false));

		ensemble.enabled = true;

		// logging error if this fails
		TestUtils.callMethod("registerWatcherForStatePath", ensemble);
		String reason = "";
		if (listener.errors.size() > 0) {
			reason = listener.errors.get(0).toString();
		}
		assertThat(reason, listener.errors.size(), is(0));

		// this should actually cause ensemble to fail
		for (int i = 10; i < 11; i++) {
			ensemble.setState(new DefaultStateMachineContext<String, String>("S" + i, "E" + i,
					new HashMap<String, Object>(), new DefaultExtendedState()));
		}
		assertThat(listener.errorLatch.await(2, TimeUnit.SECONDS), is(true));
	}

	private class OverflowControlZookeeperStateMachineEnsemble extends ZookeeperStateMachineEnsemble<String, String> {

		boolean enabled = false;

		public OverflowControlZookeeperStateMachineEnsemble(CuratorFramework curatorClient, String basePath,
				boolean cleanState, int logSize) {
			super(curatorClient, basePath, cleanState, logSize);
		}

		@Override
		protected void registerWatcherForStatePath() {
			if (enabled) {
				super.registerWatcherForStatePath();
			}
		}

	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private class TestEnsembleListener implements EnsembleListener<String, String> {

		volatile CountDownLatch joinedLatch = new CountDownLatch(1);
		volatile CountDownLatch eventLatch = new CountDownLatch(1);
		volatile CountDownLatch errorLatch = new CountDownLatch(1);
		volatile List<Exception> errors = new ArrayList<Exception>();
		volatile List<StateMachineContext<String, String>> events = new ArrayList<StateMachineContext<String,String>>();

		@Override
		public void stateMachineJoined(StateMachine<String, String> stateMachine, StateMachineContext<String, String> context) {
			joinedLatch.countDown();
		}

		@Override
		public void stateMachineLeft(StateMachine<String, String> stateMachine, StateMachineContext<String, String> context) {
		}

		@Override
		public void stateChanged(StateMachineContext<String, String> context) {
			events.add(context);
			eventLatch.countDown();
		}

		@Override
		public void ensembleError(StateMachineEnsembleException exception) {
			errors.add(exception);
			errorLatch.countDown();
		}

		@Override
		public void ensembleLeaderGranted(StateMachine<String, String> stateMachine) {
		}

		@Override
		public void ensembleLeaderRevoked(StateMachine<String, String> stateMachine) {
		}

		public void reset(int c1, int c2) {
			reset(c1, c2, 0);
		}

		public void reset(int c1, int c2, int c3) {
			joinedLatch = new CountDownLatch(c1);
			eventLatch = new CountDownLatch(c2);
			errorLatch = new CountDownLatch(c3);
			events.clear();
			errors.clear();
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
		public void setStateMachineError(Exception exception) {
		}

		@Override
		public boolean hasStateMachineError() {
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
		public UUID getUuid() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

	}

}
