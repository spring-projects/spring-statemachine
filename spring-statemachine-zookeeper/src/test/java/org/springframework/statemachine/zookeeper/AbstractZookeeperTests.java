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

import java.io.IOException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public abstract class AbstractZookeeperTests {

	protected AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		context = buildContext();
	}

	@After
	public void clean() {
		if (context != null) {
			context.close();
		}
	}

	protected AnnotationConfigApplicationContext buildContext() {
		return null;
	}

	@Configuration
	protected static class ZkServerConfig {

		@Bean
		public TestingServerWrapper testingServerWrapper() throws Exception {
			return new TestingServerWrapper();
		}

	}

	@Configuration
	protected static class BaseConfig {

		@Autowired
		TestingServerWrapper testingServerWrapper;

		@Bean(destroyMethod = "close")
		public CuratorFramework curatorClient() throws Exception {
			CuratorFramework client = CuratorFrameworkFactory.builder().defaultData(new byte[0])
					.retryPolicy(new ExponentialBackoffRetry(1000, 3))
					.connectString("localhost:" + testingServerWrapper.getPort()).build();
			// for testing we start it here, thought initiator
			// is trying to start it if not already done
			client.start();
			return client;
		}

	}

	protected static class TestingServerWrapper implements DisposableBean {

		TestingServer testingServer;

		public TestingServerWrapper() throws Exception {
			this.testingServer = new TestingServer(true);
		}

		@Override
		public void destroy() throws Exception {
			try {
				testingServer.close();
			}
			catch (IOException e) {
			}
		}

		public int getPort() {
			return testingServer.getPort();
		}

	}

}
