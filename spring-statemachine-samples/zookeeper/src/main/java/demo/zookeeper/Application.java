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
package demo.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Bootstrap;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.DistributedStateMachine;
import org.springframework.statemachine.zookeeper.ZookeeperStateMachineEnsemble;

@Configuration
public class Application  {

	@Configuration
	static class ZkConfig {

		@Qualifier("internalStateMachine")
		@Autowired
		StateMachine<String, String> internalMachine;

		@Bean
		public StateMachine<String, String> stateMachine() throws Exception {
			DistributedStateMachine<String, String> machine =
					new DistributedStateMachine<String, String>(ensemble(), internalMachine);
			return machine;
		}

		@Bean
		public ZookeeperStateMachineEnsemble<String, String> ensemble() throws Exception {
			ZookeeperStateMachineEnsemble<String, String> ensemble =
					new ZookeeperStateMachineEnsemble<String, String>(curatorClient(), "/foo");
			return ensemble;
		}

		// for now lets not close it here, we need to let
		// some other framework, ie cloud, to create curator
		@Bean//(destroyMethod = "close")
		public CuratorFramework curatorClient() throws Exception {
			CuratorFramework client = CuratorFrameworkFactory.builder().defaultData(new byte[0])
					.retryPolicy(new ExponentialBackoffRetry(1000, 3))
					.connectString("localhost:2181").build();
			// for testing we start it here, thought initiator
			// is trying to start it if not already done
			client.start();
			return client;
		}

	}


//tag::snippetA[]
	@Configuration
	@EnableStateMachine(name="internalStateMachine")
	static class StateMachineConfig
			extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("LOCKED")
					.state("UNLOCKED");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("LOCKED")
					.target("UNLOCKED")
					.event("COIN")
					.and()
				.withExternal()
					.source("UNLOCKED")
					.target("LOCKED")
					.event("PUSH");
		}

	}
//end::snippetA[]

//tag::snippetB[]
	public static enum States {
	    LOCKED, UNLOCKED
	}
//end::snippetB[]

//tag::snippetC[]
	public static enum Events {
	    COIN, PUSH
	}
//end::snippetC[]

	public static void main(String[] args) throws Exception {
		Bootstrap.main(args);
	}

}
