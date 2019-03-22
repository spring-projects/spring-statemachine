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
package org.springframework.statemachine.event;

import org.springframework.messaging.Message;

/**
 * Generic event representing that a state machine did not
 * accept an event.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("serial")
public class OnEventNotAcceptedEvent extends StateMachineEvent {

	private final Message<?> event;

	/**
	 * Instantiates a new on event not accepted event.
	 *
	 * @param source the source
	 * @param event the event
	 */
	public OnEventNotAcceptedEvent(Object source, Message<?> event) {
		super(source);
		this.event = event;
	}

	/**
	 * Gets the event.
	 *
	 * @return the event
	 */
	public Message<?> getEvent() {
		return event;
	}

	@Override
	public String toString() {
		return "OnEventNotAcceptedEvent [event=" + event + "]";
	}

}
