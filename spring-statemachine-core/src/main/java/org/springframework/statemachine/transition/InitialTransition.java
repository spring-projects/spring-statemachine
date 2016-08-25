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
package org.springframework.statemachine.transition;

import java.util.Collection;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.security.SecurityRule;
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
public class InitialTransition<S, E> extends AbstractBasicTransition<S, E>
		implements Transition<S, E> {

	public InitialTransition(State<S, E> target) {
		super(target);
	}

	public InitialTransition(State<S, E> target, Action<S, E> action) {
		super(target, action);
	}

	public InitialTransition(State<S, E> target, Collection<Action<S, E>> actions, Action<S, E> errorAction) {
		super(target, actions, errorAction);
	}

	@Override
	public boolean transit(StateContext<S, E> context) {
		executeAllActions(context);
		return false;
	}

	@Override
	public State<S, E> getSource() {
		return null;
	}

	@Override
	public Trigger<S, E> getTrigger() {
		return null;
	}

	@Override
	public TransitionKind getKind() {
		return TransitionKind.INITIAL;
	}

	@Override
	public SecurityRule getSecurityRule() {
		// initial cannot have security
		return null;
	}

}
