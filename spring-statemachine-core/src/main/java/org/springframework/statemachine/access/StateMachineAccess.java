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
package org.springframework.statemachine.access;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;

/**
 * Functional interface exposing {@link StateMachine} internals.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineAccess<S, E> {

	/**
	 * Sets the relay state machine.
	 *
	 * @param stateMachine the state machine
	 */
	void setRelay(StateMachine<S, E> stateMachine);

	/**
	 * Reset state machine.
	 *
	 * @param stateMachineContext the state machine context
	 */
	void resetStateMachine(StateMachineContext<S, E> stateMachineContext);

	/**
	 * Adds the state machine interceptor.
	 *
	 * @param interceptor the interceptor
	 */
	void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor);

	/**
	 * Sets if initial state is enabled when a state machine is
	 * using sub states.
	 *
	 * @param enabled the new initial enabled
	 */
	void setInitialEnabled(boolean enabled);

	/**
	 * Set initial forwarded event which is used for passing in
	 * event and its headers for actions executed when sub state
	 * is entered via initial transition.
	 *
	 * @param message the forwarded message
	 */
	void setForwardedInitialEvent(Message<E> message);

}
