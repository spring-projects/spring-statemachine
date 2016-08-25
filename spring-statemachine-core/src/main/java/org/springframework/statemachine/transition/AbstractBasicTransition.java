/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.statemachine.transition;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.state.State;

import java.util.Arrays;
import java.util.Collection;

public abstract class AbstractBasicTransition<S, E> {
	protected final State<S, E> target;
	protected final Collection<Action<S, E>> actions;
	protected final Action<S, E> errorAction;

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 */
	public AbstractBasicTransition(State<S, E> target) {
		this(target, null, null);
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 * @param action the initial action
	 */
	public AbstractBasicTransition(State<S, E> target, Action<S, E> action) {
		this(target, action == null ? null : Arrays.asList(action), null);
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 * @param actions the initial actions
	 * @param errorAction the error action.
	 */
	public AbstractBasicTransition(State<S, E> target, Collection<Action<S, E>> actions, Action<S, E> errorAction) {
		this.target = target;
		this.actions = actions;
		this.errorAction = errorAction == null ? Actions.<S, E> emptyAction()
				: errorAction;
	}

	/**
	 *
	 * @return the target {@link State}
	 */
	public State<S, E> getTarget() {
		return target;
	}

	/**
	 *
	 * @return all {@link Action}
	 */
	public Collection<Action<S, E>> getActions() {
		return actions;
	}

	/**
	 *
	 * @return the {@link Action} called of any error occurred while actions are executed.
	 */
	public Action<S, E> getErrorAction() {
		return errorAction;
	}

	protected final void executeAllActions(StateContext<S, E> context) {
		if (actions == null) {
			return;
		}

		for (Action<S, E> action : actions) {
			try {
				action.execute(context);
			}
			catch (Exception exception) {
				errorAction.execute(context); // notify something wrong is happening in
												// Actions execution.
				throw exception;
			}
		}
	}
}
