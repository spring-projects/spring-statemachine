/*
 * Copyright 2015-2016 the original author or authors.
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
	 * Request a join to a state machine ensemble. This method
	 * is a request to join an ensemble and doesn't guarantee
	 * a requester will eventually successfully join. Join operation
	 * needs to be used together with {@link EnsembleListener} and
	 * {@link EnsembleListener#stateMachineJoined(StateMachine, StateMachineContext)}
	 * is called with a {@link StateMachine} instance for successful join.
	 *
	 * @param stateMachine the state machine
	 */
	void join(StateMachine<S, E> stateMachine);

	/**
	 * Request a leave from an ensemble. This method is a request to
	 * leave an ensemble. After this method is called no further processing
	 * is done for a instance of {@link StateMachine}. Additionally
	 * {@link EnsembleListener#stateMachineLeft(StateMachine, StateMachineContext)}
	 * is called when leave request is fully processed.
	 *
	 * @param stateMachine the state machine
	 */
	void leave(StateMachine<S, E> stateMachine);

	/**
	 * Adds the ensemble listener.
	 *
	 * @param listener the listener
	 */
	void addEnsembleListener(EnsembleListener<S, E> listener);

	/**
	 * Removes the ensemble listener.
	 *
	 * @param listener the listener
	 */
	void removeEnsembleListener(EnsembleListener<S, E> listener);

	/**
	 * Sets the state as a {@link StateMachineContext}.
	 *
	 * @param context the state machine context
	 */
	void setState(StateMachineContext<S, E> context);

	/**
	 * Gets the state as a {@link StateMachineContext}.
	 *
	 * @return the state machine context
	 */
	StateMachineContext<S, E> getState();

	/**
	 * Gets the ensemble leader. If returned machine
	 * is {@code NULL} it indicates that this ensemble
	 * doesn't know any leader.
	 *
	 * @return the ensemble leader
	 */
	StateMachine<S, E> getLeader();
}
