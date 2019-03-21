/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.monitor;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.transition.Transition;

/**
 * {@code StateMachineMonitor} for various state machine monitoring events.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineMonitor<S, E> {

	/**
	 * Notified duration of a particular transition.
	 *
	 * @param stateMachine the state machine
	 * @param transition the transition
	 * @param duration the transition duration
	 */
	void transition(StateMachine<S, E> stateMachine, Transition<S, E> transition, long duration);

	/**
	 * Notified duration of a particular action.
	 *
	 * @param stateMachine the state machine
	 * @param action the action
	 * @param duration the transition duration
	 */
	void action(StateMachine<S, E> stateMachine, Action<S, E> action, long duration);
}
