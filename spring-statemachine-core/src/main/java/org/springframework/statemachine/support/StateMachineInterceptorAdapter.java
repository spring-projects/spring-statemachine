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

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Adapter helper implementation for {@link StateMachineInterceptor}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineInterceptorAdapter<S, E> implements StateMachineInterceptor<S, E> {

	@Override
	public Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine) {
		return message;
	}

	@Override
	public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine) {
	}

	@Override
	public void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine) {
	}

	@Override
	public StateContext<S, E> preTransition(StateContext<S, E> stateContext) {
		return stateContext;
	}

	@Override
	public StateContext<S, E> postTransition(StateContext<S, E> stateContext) {
		return stateContext;
	}

	@Override
	public Exception stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
		return exception;
	}

}
