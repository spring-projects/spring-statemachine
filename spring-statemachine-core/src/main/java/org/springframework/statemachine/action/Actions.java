/*
 * Copyright 2016-2020 the original author or authors.
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

package org.springframework.statemachine.action;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.support.DefaultStateContext;

import reactor.core.publisher.Mono;

/**
 * Action Utilities.
 *
 * @author Janne Valkealahti
 *
 */
public final class Actions {

	private Actions() {
		// This helper class should not be instantiated.
	}

	/**
	 * Builds a noop {@link Action}.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @return an empty (Noop) Action.
	 */
	public static <S, E> Action<S, E> emptyAction() {
		return new Action<S, E>() {
			@Override
			public void execute(final StateContext<S, E> context) {
				// Nothing to do;
			}
		};
	}

	/**
	 * Builds an error calling action {@link Action}.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param action the action
	 * @param errorAction the error action
	 * @return the error calling action
	 */
	public static <S, E> Action<S, E> errorCallingAction(final Action<S, E> action, final Action<S, E> errorAction) {
		return new Action<S, E>() {
			@Override
			public void execute(final StateContext<S, E> context) {
				try {
					action.execute(context);
				}
				catch (Exception exception) {
					// notify something wrong is happening in actions execution.
					try {
						errorAction.execute(new DefaultStateContext<>(context.getStage(), context.getMessage(), context.getMessageHeaders(),
								context.getExtendedState(), context.getTransition(), context.getStateMachine(), context.getSource(),
								context.getTarget(), context.getSources(), context.getTargets(), exception));
					} catch (Exception e) {
						// not interested
					}
					throw exception;
				}
			}
		};
	}

	/**
	 * Builds a {@link Function} from an {@link Action}.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param action the action
	 * @return the function
	 */
	public static <S, E> Function<StateContext<S, E>, Mono<Void>> from(Action<S, E> action) {
		if (action != null) {
			return context -> Mono.fromRunnable(() -> action.execute(context));
		} else {
			return null;
		}
	}


	/**
	 * Builds a {@link Collection} of {@link Function}s from a {@link Collection} of an {@link Action}s.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param actions the actions
	 * @return the function
	 */
	public static <S, E> Collection<Function<StateContext<S, E>, Mono<Void>>> from(Collection<Action<S, E>> actions) {
		if (actions != null) {
			return actions.stream().map(action -> from(action)).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}
}
