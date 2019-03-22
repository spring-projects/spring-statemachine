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
package org.springframework.statemachine.zookeeper.docs;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.zookeeper.ZookeeperStateMachineEnsemble;

public class DocsZookeeperSampleTests {

	public void sample1() {
	}

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public class Config
			extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withDistributed()
					.ensemble(stateMachineEnsemble())
					.and()
				.withConfiguration()
					.autoStartup(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			// config states
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			// config transitions
		}

		@Bean
		public StateMachineEnsemble<String, String> stateMachineEnsemble()
				throws Exception {
			return new ZookeeperStateMachineEnsemble<String, String>(curatorClient(), "/zkpath");
		}

		@Bean
		public CuratorFramework curatorClient()
				throws Exception {
			CuratorFramework client = CuratorFrameworkFactory
					.builder()
					.defaultData(new byte[0])
					.connectString("localhost:2181").build();
			client.start();
			return client;
		}

	}
// end::snippetA[]

}
