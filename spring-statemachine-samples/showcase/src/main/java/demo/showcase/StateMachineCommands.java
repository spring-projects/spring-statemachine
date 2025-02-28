/*
 * Copyright 2015-2025 the original author or authors.
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
package demo.showcase;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import demo.AbstractStateMachineCommands;
import demo.showcase.Application.Events;
import demo.showcase.Application.States;
import reactor.core.publisher.Mono;

@Command
public class StateMachineCommands extends AbstractStateMachineCommands<States, Events> {

	@Command(command = "sm event", description = "Sends an event to a state machine")
	public String event(@Option(longNames = { "", "event" }, required = true, description = "The event") final Events event) {
		getStateMachine()
			.sendEvent(Mono.just(MessageBuilder
				.withPayload(event).build()))
			.subscribe();
		return "Event " + event + " send";
	}
}
