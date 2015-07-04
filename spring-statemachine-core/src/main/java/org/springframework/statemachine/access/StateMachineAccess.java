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
package org.springframework.statemachine.access;

import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.StateChangeInterceptor;

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
	 * Reset state.
	 *
	 * @param state the state
	 */
	void resetState(S state);

	/**
	 * Sets the extended state.
	 *
	 * @param extendedState the new extended state
	 */
	void setExtendedState(ExtendedState extendedState);

	/**
	 * Adds the state change interceptor.
	 *
	 * @param interceptor the interceptor
	 */
	void addStateChangeInterceptor(StateChangeInterceptor<S, E> interceptor);

	/**
	 * Sets if initial state is enabled when a state machine is
	 * using sub states.
	 *
	 * @param enabled the new initial enabled
	 */
	void setInitialEnabled(boolean enabled);

}
