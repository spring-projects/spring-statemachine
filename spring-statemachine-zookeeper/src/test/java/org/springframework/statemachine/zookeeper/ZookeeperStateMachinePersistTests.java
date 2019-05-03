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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

public class ZookeeperStateMachinePersistTests extends AbstractZookeeperTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testStateEvent() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);
		curatorClient.create().forPath("/KryoStateMachinePersistTests");

		StateMachinePersist<String, String, Stat> persist = new ZookeeperStateMachinePersist<String, String>(
				curatorClient, "/KryoStateMachinePersistTests");

		StateMachineContext<String, String> contextOut =
				new DefaultStateMachineContext<String, String>("S1", "E1", new HashMap<String, Object>(), new DefaultExtendedState());
		persist.write(contextOut, new Stat());
		StateMachineContext<String, String> contextIn = persist.read(new Stat());

		assertThat(contextOut.getState(), is(contextIn.getState()));
		assertThat(contextOut.getEvent(), is(contextIn.getEvent()));
	}

	@Test
	public void testLogs() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);
		curatorClient.create().forPath("/KryoStateMachinePersistTests");

		ZookeeperStateMachinePersist<String, String> persist = new ZookeeperStateMachinePersist<String, String>(
				curatorClient, "/KryoStateMachinePersistTests", "/KryoStateMachinePersistTestsLogs", 32);

		for (int i = 0; i < 10; i++) {
			curatorClient.create().creatingParentsIfNeeded().forPath("/KryoStateMachinePersistTestsLogs/" + i);
		}

		for (int i = 0; i < 10; i++) {
			Stat stat = new Stat();
			stat.setVersion(i);
			StateMachineContext<String, String> contextOut =
					new DefaultStateMachineContext<String, String>("S" + i, "E" + i, new HashMap<String, Object>(), new DefaultExtendedState());
			persist.write(contextOut, stat);
		}

		for (int i = 0; i < 10; i++) {
			StateMachineContext<String, String> contextIn = persist.readLog(i, new Stat());
			assertThat(contextIn.getState(), is("S" + i));
			assertThat(contextIn.getEvent(), is("E" + i));
		}
	}

	@Test
	public void testEventHeaders() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);
		curatorClient.create().forPath("/KryoStateMachinePersistTests");

		StateMachinePersist<String, String, Stat> persist = new ZookeeperStateMachinePersist<String, String>(
				curatorClient, "/KryoStateMachinePersistTests");

		HashMap<String, Object> eventHeaders = new HashMap<String, Object>();
		eventHeaders.put("foo", "jee");

		StateMachineContext<String, String> contextOut =
				new DefaultStateMachineContext<String, String>("S1", "E1", eventHeaders, new DefaultExtendedState());
		persist.write(contextOut, new Stat());
		StateMachineContext<String, String> contextIn = persist.read(new Stat());

		assertThat(contextOut.getState(), is(contextIn.getState()));
		assertThat(contextOut.getEvent(), is(contextIn.getEvent()));
		assertThat(contextOut.getEventHeaders().get("foo"), is(contextIn.getEventHeaders().get("foo")));
	}

	@Test
	public void testEventHeadersAsMessageHeaders() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);
		curatorClient.create().forPath("/KryoStateMachinePersistTests");

		StateMachinePersist<String, String, Stat> persist = new ZookeeperStateMachinePersist<String, String>(
				curatorClient, "/KryoStateMachinePersistTests");

		HashMap<String, Object> eventHeaders = new HashMap<String, Object>();
		eventHeaders.put("foo", "jee");
		MessageHeaders messageHeaders = new MessageHeaders(eventHeaders);

		StateMachineContext<String, String> contextOut =
				new DefaultStateMachineContext<String, String>("S1", "E1", messageHeaders, new DefaultExtendedState());
		persist.write(contextOut, new Stat());
		StateMachineContext<String, String> contextIn = persist.read(new Stat());

		assertThat(contextOut.getState(), is(contextIn.getState()));
		assertThat(contextOut.getEvent(), is(contextIn.getEvent()));
		assertThat(contextOut.getEventHeaders().get("foo"), is(contextIn.getEventHeaders().get("foo")));
	}

	@Test
	public void testExtendedState() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);
		curatorClient.create().forPath("/KryoStateMachinePersistTests");

		StateMachinePersist<String, String, Stat> persist = new ZookeeperStateMachinePersist<String, String>(
				curatorClient, "/KryoStateMachinePersistTests");

		HashMap<String, Object> eventHeaders = new HashMap<String, Object>();
		HashMap<Object, Object> variables = new HashMap<Object, Object>();
		variables.put("foo", "jee");

		StateMachineContext<String, String> contextOut =
				new DefaultStateMachineContext<String, String>("S1", "E1", eventHeaders, new DefaultExtendedState(variables));
		persist.write(contextOut, new Stat());
		StateMachineContext<String, String> contextIn = persist.read(new Stat());

		assertThat(contextOut.getState(), is(contextIn.getState()));
		assertThat(contextOut.getEvent(), is(contextIn.getEvent()));
		assertThat(contextOut.getExtendedState().getVariables().get("foo"), is(contextIn.getExtendedState().getVariables().get("foo")));
	}

	@Test
	public void testChilds() throws Exception {
		context.register(ZkServerConfig.class, BaseConfig.class);
		context.refresh();

		CuratorFramework curatorClient =
				context.getBean("curatorClient", CuratorFramework.class);
		curatorClient.create().forPath("/KryoStateMachinePersistTests");

		StateMachinePersist<String, String, Stat> persist = new ZookeeperStateMachinePersist<String, String>(
				curatorClient, "/KryoStateMachinePersistTests");

		StateMachineContext<String, String> child =
				new DefaultStateMachineContext<String, String>("S2", "E2", new HashMap<String, Object>(), new DefaultExtendedState());
		List<StateMachineContext<String, String>> childs = new ArrayList<StateMachineContext<String, String>>();
		childs.add(child);
		StateMachineContext<String, String> contextOut =
				new DefaultStateMachineContext<String, String>(childs, "S1", "E1", new HashMap<String, Object>(), new DefaultExtendedState());
		persist.write(contextOut, new Stat());
		StateMachineContext<String, String> contextIn = persist.read(new Stat());

		assertThat(contextOut.getState(), is(contextIn.getState()));
		assertThat(contextOut.getEvent(), is(contextIn.getEvent()));

		assertThat(contextIn.getChilds().size(), is(1));
		assertThat(contextIn.getChilds().get(0).getEvent(), is("E2"));
	}

}
