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

import demo.BasicCommand;
import demo.Command;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;

import demo.AbstractStateMachineCommands;
import demo.showcase.Application.Events;
import demo.showcase.Application.States;
import reactor.core.publisher.Mono;

@Configuration
public class StateMachineCommands extends AbstractStateMachineCommands<States, Events> {

	@Bean
	public Command event() {
		return new BasicCommand("event", "Sends an event to a state machine") {
			@Override
			public String execute(String[] args) {
				Events event = Events.valueOf(args[0]);
				getStateMachine()
						.sendEvent(Mono.just(MessageBuilder
								.withPayload(event).build()))
						.subscribe();
				return "Event " + event + " sent";
			}
		};
	}
}
