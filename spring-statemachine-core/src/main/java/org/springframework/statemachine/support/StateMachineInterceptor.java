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
 * Interface which can be registered with a state machine and can be used
 * to intercept and break a state change chain.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineInterceptor<S, E> {

	/**
	 * Called before message is sent to processing. Throwing exception or
	 * returning null will skip the message.
	 *
	 * @param message the message
	 * @param stateMachine the state machine
	 * @return the intercepted message
	 */
	Message<E> preEvent(Message<E> message, StateMachine<S, E> stateMachine);

	/**
	 * Called prior of a state change. Throwing an exception
	 * from this method will stop a state change logic.
	 *
	 * @param state the state
	 * @param message the message
	 * @param transition the transition
	 * @param stateMachine the state machine
	 */
	void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine);

	/**
	 * Called after a state change.
	 *
	 * @param state the state
	 * @param message the message
	 * @param transition the transition
	 * @param stateMachine the state machine
	 */
	void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine);

	/**
	 * Called prior of a start of a transition. Returning
	 * {@code null} from this method will break the transtion
	 * chain.
	 *
	 * @param stateContext the state context
	 * @return the state context
	 */
	StateContext<S, E> preTransition(StateContext<S, E> stateContext);

	/**
	 * Called after of a transition if transition happened.
	 *
	 * @param stateContext the state context
	 * @return the state context
	 */
	StateContext<S, E> postTransition(StateContext<S, E> stateContext);

	/**
	 * State when state machine is about to enter error it can't recover.
	 *
	 * @param stateMachine the state machine
	 * @param exception the exception
	 * @return the exception
	 */
	Exception stateMachineError(StateMachine<S, E> stateMachine, Exception exception);

}
