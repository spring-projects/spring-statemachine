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
import static org.junit.Assert.assertThat;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.ensemble.StateMachinePersist;
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
				new DefaultStateMachineContext<String, String>("S1", "E1", null, null);
		persist.write(contextOut, new Stat());
		StateMachineContext<String, String> contextIn = persist.read(new Stat());

		assertThat(contextOut.getState(), is(contextIn.getState()));
		assertThat(contextOut.getEvent(), is(contextIn.getEvent()));
	}

}
