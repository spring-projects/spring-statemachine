/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.support;

import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;

/**
 * Default implementation of a {@link StateMachineContext}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateMachineContext<S, E> implements StateMachineContext<S, E> {

	private final StateMachine<S, E> stateMachine;
	private final S state;
	private final E event;
	private final Map<String, Object> eventHeaders;
	private final ExtendedState extendedState;

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param stateMachine the state machine
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 */
	public DefaultStateMachineContext(StateMachine<S, E> stateMachine, S state, E event, Map<String, Object> eventHeaders, ExtendedState extendedState) {
		this.stateMachine = stateMachine;
		this.state = state;
		this.event = event;
		this.eventHeaders = eventHeaders;
		this.extendedState = extendedState;
	}

	@Override
	public StateMachine<S, E> getStateMachine() {
		return stateMachine;
	}

	@Override
	public S getState() {
		return state;
	}

	@Override
	public E getEvent() {
		return event;
	}

	@Override
	public Map<String, Object> getEventHeaders() {
		return eventHeaders;
	}

	@Override
	public ExtendedState getExtendedState() {
		return extendedState;
	}

}
