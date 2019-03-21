/*
 * Copyright 2015-2016 the original author or authors.
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

import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Default implementation of a {@link StateContext}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateContext<S, E> implements StateContext<S, E> {

	private final Stage stage;
	private final Message<E> message;
	private final MessageHeaders messageHeaders;
	private final ExtendedState extendedState;
	private final Transition<S,E> transition;
	private final StateMachine<S, E> stateMachine;
	private final State<S, E> source;
	private final State<S, E> target;
	private final Collection<State<S, E>> sources;
	private final Collection<State<S, E>> targets;
	private final Exception exception;

	/**
	 * Instantiates a new default state context.
	 *
	 * @param stage the stage
	 * @param message the message
	 * @param messageHeaders the message headers
	 * @param extendedState the extended state
	 * @param transition the transition
	 * @param stateMachine the state machine
	 * @param source the source
	 * @param target the target
	 * @param exception the exception
	 */
	public DefaultStateContext(Stage stage, Message<E> message, MessageHeaders messageHeaders, ExtendedState extendedState,
			Transition<S, E> transition, StateMachine<S, E> stateMachine, State<S, E> source, State<S, E> target, Exception exception) {
		this.stage = stage;
		this.message = message;
		this.messageHeaders = messageHeaders;
		this.extendedState = extendedState;
		this.transition = transition;
		this.stateMachine = stateMachine;
		this.source = source;
		this.target = target;
		this.exception = exception;
		this.sources = null;
		this.targets = null;
	}

	/**
	 * Instantiates a new default state context.
	 *
	 * @param stage the stage
	 * @param message the message
	 * @param messageHeaders the message headers
	 * @param extendedState the extended state
	 * @param transition the transition
	 * @param stateMachine the state machine
	 * @param source the source
	 * @param target the target
	 * @param sources the sources
	 * @param targets the targets
	 * @param exception the exception
	 */
	public DefaultStateContext(Stage stage, Message<E> message, MessageHeaders messageHeaders, ExtendedState extendedState,
			Transition<S, E> transition, StateMachine<S, E> stateMachine, State<S, E> source, State<S, E> target,
			Collection<State<S, E>> sources, Collection<State<S, E>> targets, Exception exception) {
		this.stage = stage;
		this.message = message;
		this.messageHeaders = messageHeaders;
		this.extendedState = extendedState;
		this.transition = transition;
		this.stateMachine = stateMachine;
		this.source = source;
		this.target = target;
		this.sources = sources;
		this.targets = targets;
		this.exception = exception;
	}

	@Override
	public Stage getStage() {
		return stage;
	}

	@Override
	public E getEvent() {
		return message != null ? message.getPayload() : null;
	}

	@Override
	public Message<E> getMessage() {
		return message;
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

	@Override
	public State<S, E> getSource() {
		return source != null ? source : (transition != null ? transition.getSource() : null);
	}

	@Override
	public Collection<State<S, E>> getSources() {
		return sources;
	}

	@Override
	public State<S, E> getTarget() {
		return target != null ? target : (transition != null ? transition.getTarget() : null);
	}

	@Override
	public Collection<State<S, E>> getTargets() {
		return targets;
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public String toString() {
		return "DefaultStateContext [stage=" + stage + ", message=" + message + ", messageHeaders=" + messageHeaders + ", extendedState="
				+ extendedState + ", transition=" + transition + ", stateMachine=" + stateMachine + ", source=" + source + ", target="
				+ target + ", sources=" + sources + ", targets=" + targets + ", exception=" + exception + "]";
	}
}
