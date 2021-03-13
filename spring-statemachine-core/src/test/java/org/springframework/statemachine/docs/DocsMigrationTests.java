/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings({ "unused", "deprecation" })
public class DocsMigrationTests {

	StateMachine<String, String> machine;

	interface StateMachineDocs<S, E> extends StateMachine<S, E> {
		// tag::snippetA[]
		Flux<StateMachineEventResult<S, E>> sendEvent(Mono<Message<E>> event);

		Flux<StateMachineEventResult<S, E>> sendEvents(Flux<Message<E>> events);

		Mono<List<StateMachineEventResult<S, E>>> sendEventCollect(Mono<Message<E>> event);
		// end::snippetA[]
	}

	public void sample1() {
		// tag::snippetB1[]
		Message<String> message = MessageBuilder.withPayload("EVENT").build();
		machine.sendEvent(Mono.just(message)).subscribe();
		// end::snippetB1[]
		// tag::snippetB2[]
		machine.sendEvents(Flux.just(message)).subscribe();
		// end::snippetB2[]
	}

	public void sample2() {
		// tag::snippetB3[]
		Mono<Message<String>> mono = Mono.just(MessageBuilder.withPayload("EVENT").build());
		machine.sendEvent(mono)
			.doOnComplete(() -> {
				System.out.println("Event handling complete");
			})
			.subscribe();
		// end::snippetB3[]
	}

	public void sample3() {
		// tag::snippetB4[]
		boolean accepted = machine.sendEvent("EVENT");
		// end::snippetB4[]
	}
}
