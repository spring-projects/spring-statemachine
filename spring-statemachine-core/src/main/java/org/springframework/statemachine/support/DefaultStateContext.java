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
package org.springframework.statemachine.support;

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.transition.Transition;

public class DefaultStateContext<S, E> implements StateContext<S, E> {

	private final E event;
	
	private final MessageHeaders messageHeaders;

	private final ExtendedState extendedState;

	private final Transition<S,E> transition;

	private final StateMachine<S, E> stateMachine;

	public DefaultStateContext(E event, MessageHeaders messageHeaders, ExtendedState extendedState, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		this.event = event;
		this.messageHeaders = messageHeaders;
		this.extendedState = extendedState;
		this.transition = transition;
		this.stateMachine = stateMachine;
	}
	
	@Override
	public E getEvent() {
		return event;
	}

	@Override
	public MessageHeaders getMessageHeaders() {
		return messageHeaders;
	}

	@Override
	public Object getMessageHeader(Object header) {
		if (header instanceof String) {
			return messageHeaders.get((String)header);
		} else if (header instanceof Enum<?>) {
			return messageHeaders.get(((Enum<?>)header).toString());
		}
		return null;
	}

	@Override
	public ExtendedState getExtendedState() {
		return extendedState;
	}

	@Override
	public Transition<S, E> getTransition() {
		return transition;
	}

	@Override
	public StateMachine<S, E> getStateMachine() {
		return stateMachine;
	}

}
