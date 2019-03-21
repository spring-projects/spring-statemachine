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
 * Adapter implementation of {@link StateMachineListener} implementing all
 * methods which extended implementation can override.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineListenerAdapter<S, E> implements StateMachineListener<S, E> {

	@Override
	public void stateChanged(State<S, E> from, State<S, E> to) {
	}

	@Override
	public void stateEntered(State<S, E> state) {
	}

	@Override
	public void stateExited(State<S, E> state) {
	}

	@Override
	public void eventNotAccepted(Message<E> event) {
	}

	@Override
	public void transition(Transition<S, E> transition) {
	}

	@Override
	public void transitionStarted(Transition<S, E> transition) {
	}

	@Override
	public void transitionEnded(Transition<S, E> transition) {
	}

	@Override
	public void stateMachineStarted(StateMachine<S, E> stateMachine) {
	}

	@Override
	public void stateMachineStopped(StateMachine<S, E> stateMachine) {
	}

	@Override
	public void stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
	}

	@Override
	public void extendedStateChanged(Object key, Object value) {
	}

}
