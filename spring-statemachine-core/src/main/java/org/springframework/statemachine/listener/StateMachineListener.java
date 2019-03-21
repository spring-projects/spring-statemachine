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
package org.springframework.statemachine.listener;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * {@code StateMachineListener} for various state machine events.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineListener<S,E> {

	/**
	 * Notified when state is changed.
	 *
	 * @param from the source state
	 * @param to the target state
	 */
	void stateChanged(State<S,E> from, State<S,E> to);

	/**
	 * Notified when state is entered.
	 *
	 * @param state the state
	 */
	void stateEntered(State<S,E> state);

	/**
	 * Notified when state is exited.
	 *
	 * @param state the state
	 */
	void stateExited(State<S,E> state);

	/**
	 * Notified when event was not accepted.
	 *
	 * @param event the event
	 */
	void eventNotAccepted(Message<E> event);

	/**
	 * Notified when transition happened.
	 *
	 * @param transition the transition
	 */
	void transition(Transition<S, E> transition);

	/**
	 * Notified when transition started.
	 *
	 * @param transition the transition
	 */
	void transitionStarted(Transition<S, E> transition);

	/**
	 * Notified when transition ended.
	 *
	 * @param transition the transition
	 */
	void transitionEnded(Transition<S, E> transition);

	/**
	 * Notified when statemachine starts
	 *
	 * @param stateMachine the statemachine
	 */
	void stateMachineStarted(StateMachine<S, E> stateMachine);

	/**
	 * Notified when statemachine stops
	 *
	 * @param stateMachine the statemachine
	 */
	void stateMachineStopped(StateMachine<S, E> stateMachine);

	/**
	 * Notified when statemachine enters error it can't recover from.
	 *
	 * @param stateMachine the state machine
	 * @param exception the exception
	 */
	void stateMachineError(StateMachine<S, E> stateMachine, Exception exception);

	/**
	 * Notified when extended state variable is either added, modified or removed.
	 *
	 * @param key the variable key
	 * @param value the variable value
	 */
	void extendedStateChanged(Object key, Object value);

}
