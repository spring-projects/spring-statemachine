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
package org.springframework.statemachine.ensemble;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;

/**
 * {@code EnsembleListeger} for various ensemble events.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface EnsembleListeger<S, E> {

	/**
	 * Called when state machine joined an ensemble.
	 *
	 * @param stateMachine the state machine
	 * @param context the state machine context
	 */
	void stateMachineJoined(StateMachine<S, E> stateMachine, StateMachineContext<S, E> context);

	/**
	 * Called when state machine left an ensemble.
	 *
	 * @param stateMachine the state machine
	 * @param context the state machine context
	 */
	void stateMachineLeft(StateMachine<S, E> stateMachine, StateMachineContext<S, E> context);

	/**
	 * Called when ensemble is discovering a state change.
	 *
	 * @param context the state machine context
	 */
	void stateChanged(StateMachineContext<S, E> context);

	/**
	 * Called when {@link StateMachineEnsemble} resulted an error.
	 *
	 * @param exception the exception
	 */
	void ensembleError(StateMachineEnsembleException exception);

}
