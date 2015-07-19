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
 * {@code StateMachineEnsemble} is a contract between a {@link StateMachine} and
 * arbitrary ensemble of other {@link StateMachine}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineEnsemble<S, E> {

	/**
	 * Request a join to a state machine ensemble.
	 *
	 * @param stateMachine the state machine
	 */
	void join(StateMachine<S, E> stateMachine);

	/**
	 * Request a leave from an ensemble.
	 *
	 * @param stateMachine the state machine
	 */
	void leave(StateMachine<S, E> stateMachine);

	/**
	 * Adds the ensemble listener.
	 *
	 * @param listener the listener
	 */
	void addEnsembleListener(EnsembleListeger<S, E> listener);

	/**
	 * Removes the ensemble listener.
	 *
	 * @param listener the listener
	 */
	void removeEnsembleListener(EnsembleListeger<S, E> listener);

	/**
	 * Sets the state.
	 *
	 * @param context the context
	 */
	void setState(StateMachineContext<S, E> context);

	StateMachineContext<S, E> getState();

}
