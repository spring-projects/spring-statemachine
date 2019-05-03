/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.config.model;

import java.util.Collection;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

/**
 * A simple data object keeping choice related configs in a same place.
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ChoiceData<S, E> {
	private final S source;
	private final S target;
	private final Guard<S, E> guard;
	private final Collection<Action<S, E>> actions;

	/**
	 * Instantiates a new choice data.
	 *
	 * @param source the source
	 * @param target the target
	 * @param guard the guard
	 */
	public ChoiceData(S source, S target, Guard<S, E> guard) {
		this(source, target, guard, null);
	}

	/**
	 * Instantiates a new choice data.
	 *
	 * @param source the source
	 * @param target the target
	 * @param guard the guard
	 * @param actions the actions
	 */
	public ChoiceData(S source, S target, Guard<S, E> guard, Collection<Action<S, E>> actions) {
		this.source = source;
		this.target = target;
		this.guard = guard;
		this.actions = actions;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public S getSource() {
		return source;
	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public S getTarget() {
		return target;
	}

	/**
	 * Gets the guard.
	 *
	 * @return the guard
	 */
	public Guard<S, E> getGuard() {
		return guard;
	}

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	public Collection<Action<S, E>> getActions() {
		return actions;
	}
}