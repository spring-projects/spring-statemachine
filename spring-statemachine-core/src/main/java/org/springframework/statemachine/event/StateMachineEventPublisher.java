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
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Interface for publishing state machine based application events.
 *
 * @author Janne Valkealahti
 *
 */
public interface StateMachineEventPublisher {

	/**
	 * Publish a state changed event.
	 *
	 * @param source the component generated this event
	 * @param sourceState the source state
	 * @param targetState the target state
	 */
	void publishStateChanged(Object source, State<?, ?> sourceState, State<?, ?> targetState);

	/**
	 * Publish a state entered event.
	 *
	 * @param source the source
	 * @param state the state
	 */
	void publishStateEntered(Object source, State<?, ?> state);

	/**
	 * Publish a state exited event.
	 *
	 * @param source the source
	 * @param state the state
	 */
	void publishStateExited(Object source, State<?, ?> state);

	/**
	 * Publish event not accepted event.
	 *
	 * @param source the source
	 * @param event the event
	 */
	void publishEventNotAccepted(Object source, Message<?> event);

	/**
	 * Publish a transition start event.
	 *
	 * @param source the source
	 * @param transition the transition
	 */
	void publishTransitionStart(Object source, Transition<?, ?> transition);

	/**
	 * Publish a transition end event.
	 *
	 * @param source the source
	 * @param transition the transition
	 */
	void publishTransitionEnd(Object source, Transition<?, ?> transition);

	/**
	 * Publish a transition event.
	 *
	 * @param source the source
	 * @param transition the transition
	 */
	void publishTransition(Object source, Transition<?, ?> transition);

	/**
	 * Publish a statemachine start event.
	 *
	 * @param source the source
	 * @param stateMachine the statemachine
	 */
	void publishStateMachineStart(Object source, StateMachine<?, ?> stateMachine);

	/**
	 * Publish a statemachine stop event.
	 *
	 * @param source the source
	 * @param stateMachine the statemachine
	 */
	void publishStateMachineStop(Object source, StateMachine<?, ?> stateMachine);

	/**
	 * Publish a state machine error.
	 *
	 * @param source the source
	 * @param stateMachine the state machine
	 * @param exception the exception
	 */
	void publishStateMachineError(Object source, StateMachine<?, ?> stateMachine, Exception exception);

	/**
	 * Publish extended state changed.
	 *
	 * @param source the source
	 * @param key the key
	 * @param value the value
	 */
	void publishExtendedStateChanged(Object source, Object key, Object value);

}
