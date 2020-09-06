/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.statemachine.transition;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.state.State;

import reactor.core.publisher.Mono;

/**
 * {@link Transition} used during a state machine start.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class InitialTransition<S, E> extends AbstractTransition<S, E> {

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the target
	 */
	public InitialTransition(State<S, E> target) {
		super(null, target, null, null, TransitionKind.INITIAL, null, null, null);
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the target
	 * @param action the action
	 */
	public InitialTransition(State<S, E> target, Function<StateContext<S, E>, Mono<Void>> action) {
		super(null, target, action != null ? Collections.singleton(action) : null, null, TransitionKind.INITIAL, null,
				null, null);
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the target
	 * @param actions the actions
	 */
	public InitialTransition(State<S, E> target, Collection<Function<StateContext<S, E>, Mono<Void>>> actions) {
		super(null, target, actions, null, TransitionKind.INITIAL, null, null, null);
	}

	@Override
	public Mono<Boolean> transit(StateContext<S, E> context) {
		// initial itself doesn't cause further changes what
		// returned true might cause.
		return Mono.just(false);
	}
}
