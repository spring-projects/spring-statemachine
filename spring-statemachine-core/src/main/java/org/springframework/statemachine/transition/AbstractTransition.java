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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Base implementation of a {@link Transition}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractTransition<S, E> extends AbstractBasicTransition<S, E> implements Transition<S, E> {

	private final static Log log = LogFactory.getLog(AbstractTransition.class);

	private final State<S, E> source;

	private final TransitionKind kind;

	private final Guard<S, E> guard;

	private final Trigger<S, E> trigger;

	private final SecurityRule securityRule;

	public AbstractTransition(State<S, E> source, State<S, E> target,
			Collection<Action<S, E>> actions, E event, TransitionKind kind,
			Guard<S, E> guard, Trigger<S, E> trigger) {
		this(source, target, actions, event, kind, guard, trigger, null, Actions.<S, E>emptyAction());
	}

	public AbstractTransition(State<S, E> source, State<S, E> target,
			Collection<Action<S, E>> actions, E event, TransitionKind kind,
			Guard<S, E> guard, Trigger<S, E> trigger, SecurityRule securityRule, Action<S, E> errorAction) {
		super(target, actions, errorAction);
		Assert.notNull(source, "Source must be set");
		Assert.notNull(kind, "Transition type must be set");
		this.source = source;
		this.kind = kind;
		this.guard = guard;
		this.trigger = trigger;
		this.securityRule = securityRule;
	}

	@Override
	public State<S, E> getSource() {
		return source;
	}

	@Override
	public Trigger<S, E> getTrigger() {
		return trigger;
	}

	@Override
	public boolean transit(StateContext<S, E> context) {
		if (guard != null) {
			try {
				if (!guard.evaluate(context)) {
					return false;
				}
			}
			catch (Throwable t) {
				log.warn("Deny guard due to throw as GUARD should not error", t);
				return false;
			}
		}
		executeAllActions(context);

		return true;
	}

	@Override
	public TransitionKind getKind() {
		return kind;
	}

	@Override
	public SecurityRule getSecurityRule() {
		return securityRule;
	}

}
