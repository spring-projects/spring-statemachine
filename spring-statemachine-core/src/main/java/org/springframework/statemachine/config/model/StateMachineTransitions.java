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
package org.springframework.statemachine.config.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Data object for transitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineTransitions<S, E> {

	private final Collection<TransitionData<S, E>> transitions;
	private final Map<S, List<ChoiceData<S, E>>> choices;
	private final Map<S, List<S>> forks;
	private final Map<S, List<S>> joins;

	/**
	 * Instantiates a new state machine transitions.
	 *
	 * @param transitions the transitions
	 * @param choices the choices
	 * @param forks the forks
	 * @param joins the joins
	 */
	public StateMachineTransitions(Collection<TransitionData<S, E>> transitions,
			Map<S, List<ChoiceData<S, E>>> choices, Map<S, List<S>> forks, Map<S, List<S>> joins) {
		this.transitions = transitions;
		this.choices = choices;
		this.forks = forks;
		this.joins = joins;
	}

	/**
	 * Gets the transitions.
	 *
	 * @return the transitions
	 */
	public Collection<TransitionData<S, E>> getTransitions() {
		return transitions;
	}

	/**
	 * Gets the choices.
	 *
	 * @return the choices
	 */
	public Map<S, List<ChoiceData<S, E>>> getChoices() {
		return choices;
	}

	/**
	 * Gets the forks.
	 *
	 * @return the forks
	 */
	public Map<S, List<S>> getForks() {
		return forks;
	}

	/**
	 * Gets the joins.
	 *
	 * @return the joins
	 */
	public Map<S, List<S>> getJoins() {
		return joins;
	}

}
