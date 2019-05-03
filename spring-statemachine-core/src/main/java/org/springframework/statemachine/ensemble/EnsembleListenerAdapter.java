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
package org.springframework.statemachine.ensemble;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;

/**
 * Adapter implementation of {@link EnsembleListener} implementing all
 * methods which extended implementation can override.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class EnsembleListenerAdapter<S, E> implements EnsembleListener<S, E> {

	@Override
	public void stateMachineJoined(StateMachine<S, E> stateMachine, StateMachineContext<S, E> context) {
	}

	@Override
	public void stateMachineLeft(StateMachine<S, E> stateMachine, StateMachineContext<S, E> context) {
	}

	@Override
	public void stateChanged(StateMachineContext<S, E> context) {
	}

	@Override
	public void ensembleError(StateMachineEnsembleException exception) {
	}

	@Override
	public void ensembleLeaderGranted(StateMachine<S, E> stateMachine) {
	}

	@Override
	public void ensembleLeaderRevoked(StateMachine<S, E> stateMachine) {
	}
}
