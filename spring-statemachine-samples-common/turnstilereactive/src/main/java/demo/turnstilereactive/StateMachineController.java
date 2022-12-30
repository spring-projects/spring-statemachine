/*
 * Copyright 2019 the original author or authors.
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
package demo.turnstilereactive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import demo.turnstilereactive.StateMachineConfig.Events;
import demo.turnstilereactive.StateMachineConfig.States;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class StateMachineController {

	//tag::snippetA[]
	@Autowired
	private StateMachine<States, Events> stateMachine;
	//end::snippetA[]

	//tag::snippetB[]
	@GetMapping("/state")
	public Mono<States> state() {
		return Mono.defer(() -> Mono.justOrEmpty(stateMachine.getState().getId()));
	}
	//end::snippetB[]

	//tag::snippetC[]
	@PostMapping("/events")
	public Flux<EventResult> events(@RequestBody Flux<EventData> eventData) {
		return eventData
			.filter(ed -> ed.getEvent() != null)
			.map(ed -> MessageBuilder.withPayload(ed.getEvent()).build())
			.flatMap(m -> stateMachine.sendEvent(Mono.just(m)))
			.map(EventResult::new);
	}
	//end::snippetC[]

	public static class EventData {
		private Events event;

		public Events getEvent() {
			return event;
		}

		public void setEvent(Events event) {
			this.event = event;
		}
	}

	public static class EventResult {

		private final StateMachineEventResult<States, Events> result;
		
		EventResult(StateMachineEventResult<States, Events> result) {
			this.result = result;
		}

		public ResultType getResultType() {
			return result.getResultType();
		}

		public Events getEvent() {
			return result.getMessage().getPayload();
		}
	}
}
