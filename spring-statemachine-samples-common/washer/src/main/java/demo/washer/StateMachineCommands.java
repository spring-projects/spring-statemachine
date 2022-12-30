/*
 * Copyright 2015-2019 the original author or authors.
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
package demo.washer;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import demo.AbstractStateMachineCommands;
import demo.washer.Application.Events;
import demo.washer.Application.States;
import reactor.core.publisher.Mono;

@Component
public class StateMachineCommands extends AbstractStateMachineCommands<States, Events> {

	@CliCommand(value = "sm event", help = "Sends an event to a state machine")
	public String event(@CliOption(key = { "", "event" }, mandatory = true, help = "The event") final Events event) {
		getStateMachine()
			.sendEvent(Mono.just(MessageBuilder
				.withPayload(event).build()))
			.subscribe();
		return "Event " + event + " send";
	}

}