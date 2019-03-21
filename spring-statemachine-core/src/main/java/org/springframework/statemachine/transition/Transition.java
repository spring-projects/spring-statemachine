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

import java.util.Collection;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.trigger.Trigger;

/**
 * {@code Transition} is something what a state machine associates with a state
 * changes.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface Transition<S, E> {

	/**
	 * Transit this transition with a give state context.
	 *
	 * @param context the state context
	 * @return true, if transition happened, false otherwise
	 */
	boolean transit(StateContext<S, E> context);

	/**
	 * Gets the source state of this transition.
	 *
	 * @return the source state
	 */
	State<S,E> getSource();

	/**
	 * Gets the target state of this transition.
	 *
	 * @return the target state
	 */
	State<S,E> getTarget();

	/**
	 * Gets the transition actions.
	 *
	 * @return the transition actions
	 */
	Collection<Action<S, E>> getActions();

	/**
	 * Gets the transition trigger.
	 *
	 * @return the transition trigger
	 */
	Trigger<S, E> getTrigger();

	/**
	 * Gets the transition kind.
	 *
	 * @return the transition kind
	 */
	TransitionKind getKind();

	/**
	 * Gets the security rule.
	 *
	 * @return the security rule
	 */
	SecurityRule getSecurityRule();

}
