/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.statemachine.docs;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineMessageHeaders;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class DocsConfigurationSampleTests11 extends AbstractStateMachineTests {

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.stateDoActionPolicy(StateDoActionPolicy.IMMEDIATE_CANCEL);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2", context -> {})
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S3")
					.event("E2");
		}
	}
// end::snippetA[]

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

// tag::snippetB[]
		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.stateDoActionPolicy(StateDoActionPolicy.TIMEOUT_CANCEL)
					.stateDoActionPolicyTimeout(10, TimeUnit.SECONDS);
		}
// end::snippetB[]

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2", context -> {})
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S3")
					.event("E2");
		}
	}

	static class Example1 {

// tag::snippetC[]
		@Autowired
		StateMachine<String, String> stateMachine;

		void sendEventUsingTimeout() {
			stateMachine.sendEvent(MessageBuilder
					.withPayload("E1")
					.setHeader(StateMachineMessageHeaders.HEADER_DO_ACTION_TIMEOUT, 5000)
					.build());

		}
// end::snippetC[]
	}

}
