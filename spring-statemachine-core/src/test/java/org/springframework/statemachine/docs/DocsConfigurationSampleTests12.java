/*
 * Copyright 2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class DocsConfigurationSampleTests12 {


// tag::snippetA1[]
	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("SI")
					.stateEntry("S1", (context) -> {
						throw new RuntimeException("example error");
					});
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("SI")
					.target("S1")
					.event("E1");
		}
	}
// end::snippetA1[]

// tag::snippetA2[]
	@Autowired
	private StateMachine<String, String> machine;

	@Test
	public void testActionEntryErrorWithEvent() throws Exception {
		StepVerifier.create(machine.startReactively()).verifyComplete();
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder("SI");

		StepVerifier.create(machine.sendEvent(Mono.just(MessageBuilder.withPayload("E1").build())))
			.consumeNextWith(result -> {
				StepVerifier.create(result.complete()).consumeErrorWith(e -> {
					assertThat(e).isInstanceOf(StateMachineException.class).hasMessageContaining("example error");
				}).verify();
			})
			.verifyComplete();

		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder("S1");
	}
// end::snippetA2[]
}
