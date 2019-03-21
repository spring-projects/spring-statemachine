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
package org.springframework.statemachine.transition;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.trigger.Trigger;

/**
 * {@link Transition} used during a state machine start.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class InitialTransition<S, E> implements Transition<S, E> {

	private final State<S, E> target;

	private final Collection<Action<S, E>> actions;

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 */
	public InitialTransition(State<S, E> target) {
		this.target = target;
		this.actions = null;
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 * @param action the initial action
	 */
	public InitialTransition(State<S, E> target, Action<S, E> action) {
		this.target = target;
		ArrayList<Action<S,E>> list = new ArrayList<Action<S, E>>();
		if (action != null) {
			list.add(action);
		}
		this.actions = list;
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 * @param actions the initial actions
	 */
	public InitialTransition(State<S, E> target, Collection<Action<S, E>> actions) {
		this.target = target;
		this.actions = actions;
	}

	@Override
	public boolean transit(StateContext<S, E> context) {
		if (actions != null) {
			for (Action<S, E> action : actions) {
				action.execute(context);
			}
		}
		return false;
	}

	@Override
	public State<S, E> getSource() {
		return null;
	}

	@Override
	public State<S, E> getTarget() {
		return target;
	}

	@Override
	public Collection<Action<S, E>> getActions() {
		return actions;
	}

	@Override
	public Trigger<S, E> getTrigger() {
		return null;
	}

	@Override
	public TransitionKind getKind() {
		return TransitionKind.INITIAL;
	}

}
