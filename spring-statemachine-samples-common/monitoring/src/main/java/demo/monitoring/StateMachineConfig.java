/*
 * Copyright 2016 the original author or authors.
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
package demo.monitoring;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
public class StateMachineConfig {

//tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public static class Config extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2", null, (c) -> {System.out.println("hello");})
					.state("S3", (c) -> {System.out.println("hello");}, null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2").event("E1")
					.action((c) -> {System.out.println("hello");})
					.and()
				.withExternal()
					.source("S2").target("S3").event("E2");
		}
	}
//end::snippetA[]
}
