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
package org.springframework.statemachine.config.model;

import java.util.Collection;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * A simple data object keeping transition related configs in a same place.
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class TransitionData<S, E> {
	private final S source;
	private final S target;
	private final S state;
	private final E event;
	private final Long period;
	private final Integer count;
	private final Collection<Action<S, E>> actions;
	private final Guard<S, E> guard;
	private final TransitionKind kind;
	private final SecurityRule securityRule;

	/**
	 * Instantiates a new transition data.
	 *
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 */
	public TransitionData(S source, S target, E event) {
		this(source, target, null, event, null, null, null, null, TransitionKind.EXTERNAL, null);
	}

	/**
	 * Instantiates a new transition data.
	 *
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 * @param actions the actions
	 * @param guard the guard
	 * @param kind the kind
	 */
	public TransitionData(S source, S target, E event, Collection<Action<S, E>> actions,
			Guard<S, E> guard, TransitionKind kind) {
		this(source, target, null, event, null, null, actions, guard, kind, null);
	}

	/**
	 * Instantiates a new transition data.
	 *
	 * @param source the source
	 * @param target the target
	 * @param period the period
	 * @param count the count
	 * @param actions the actions
	 * @param guard the guard
	 * @param kind the kind
	 */
	public TransitionData(S source, S target, Long period, Integer count, Collection<Action<S, E>> actions,
			Guard<S, E> guard, TransitionKind kind) {
		this(source, target, null, null, period, count, actions, guard, kind, null);
	}

	/**
	 * Instantiates a new transition data.
	 *
	 * @param source the source
	 * @param target the target
	 * @param state the state
	 * @param event the event
	 * @param period the period
	 * @param count the count
	 * @param actions the actions
	 * @param guard the guard
	 * @param kind the kind
	 * @param securityRule the security rule
	 */
	public TransitionData(S source, S target, S state, E event, Long period, Integer count, Collection<Action<S, E>> actions,
			Guard<S, E> guard, TransitionKind kind, SecurityRule securityRule) {
		this.source = source;
		this.target = target;
		this.state = state;
		this.event = event;
		this.period = period;
		this.count = count;
		this.actions = actions;
		this.guard = guard;
		this.kind = kind;
		this.securityRule = securityRule;
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
	 * Gets the state.
	 *
	 * @return the state
	 */
	public S getState() {
		return state;
	}

	/**
	 * Gets the event.
	 *
	 * @return the event
	 */
	public E getEvent() {
		return event;
	}

	/**
	 * Gets the period.
	 *
	 * @return the period
	 */
	public Long getPeriod() {
		return period;
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	public Collection<Action<S, E>> getActions() {
		return actions;
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
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	public TransitionKind getKind() {
		return kind;
	}

	/**
	 * Gets the security rule.
	 *
	 * @return the security rule
	 */
	public SecurityRule getSecurityRule() {
		return securityRule;
	}
}