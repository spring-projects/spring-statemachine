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
package demo.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Bootstrap;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler;

@SpringBootApplication
public class Application  {

//tag::snippetA[]
	@Configuration
	@EnableStateMachine
	static class StateMachineConfig
			extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("PLACED")
					.state("PROCESSING")
					.state("SENT")
					.state("DELIVERED");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("PLACED").target("PROCESSING")
					.event("PROCESS")
					.and()
				.withExternal()
					.source("PROCESSING").target("SENT")
					.event("SEND")
					.and()
				.withExternal()
					.source("SENT").target("DELIVERED")
					.event("DELIVER");
		}

	}
//end::snippetA[]

//tag::snippetB[]
	@Configuration
	static class PersistHandlerConfig {

		@Autowired
		private StateMachine<String, String> stateMachine;

		@Bean
		public Persist persist() {
			return new Persist(persistStateMachineHandler());
		}

		@Bean
		public PersistStateMachineHandler persistStateMachineHandler() {
			return new PersistStateMachineHandler(stateMachine);
		}

	}
//end::snippetB[]

//tag::snippetC[]
	public static class Order {
		int id;
		String state;

		public Order(int id, String state) {
			this.id = id;
			this.state = state;
		}

		@Override
		public String toString() {
			return "Order [id=" + id + ", state=" + state + "]";
		}

	}
//end::snippetC[]

	public static void main(String[] args) throws Exception {
		Bootstrap.main(args);
	}

}
