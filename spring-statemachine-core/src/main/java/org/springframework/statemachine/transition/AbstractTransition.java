/*
 * Copyright 2015-2019 the original author or authors.
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
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.ActionListener;
import org.springframework.statemachine.action.CompositeActionListener;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Base implementation of a {@link Transition}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractTransition<S, E> implements Transition<S, E> {

	private final static Log log = LogFactory.getLog(AbstractTransition.class);
	protected final State<S, E> target;
	protected final Collection<Function<StateContext<S, E>, Mono<Void>>> actions;
	private final State<S, E> source;
	private final TransitionKind kind;
	private final Function<StateContext<S, E>, Mono<Boolean>> guard;
	private final Trigger<S, E> trigger;
	private final SecurityRule securityRule;
	private CompositeActionListener<S, E> actionListener;

	/**
	 * Instantiates a new abstract transition.
	 *
	 * @param source the source
	 * @param target the target
	 * @param actions the actions
	 * @param event the event
	 * @param kind the kind
	 * @param guard the guard
	 * @param trigger the trigger
	 */
	public AbstractTransition(State<S, E> source, State<S, E> target,
			Collection<Function<StateContext<S, E>, Mono<Void>>> actions, E event, TransitionKind kind,
			Function<StateContext<S, E>, Mono<Boolean>> guard, Trigger<S, E> trigger) {
		this(source, target, actions, event, kind, guard, trigger, null);
	}

	/**
	 * Instantiates a new abstract transition.
	 *
	 * @param source the source
	 * @param target the target
	 * @param actions the actions
	 * @param event the event
	 * @param kind the kind
	 * @param guard the guard
	 * @param trigger the trigger
	 * @param securityRule the security rule
	 */
	public AbstractTransition(State<S, E> source, State<S, E> target,
			Collection<Function<StateContext<S, E>, Mono<Void>>> actions, E event, TransitionKind kind,
			Function<StateContext<S, E>, Mono<Boolean>> guard, Trigger<S, E> trigger, SecurityRule securityRule) {
		Assert.notNull(kind, "Transition type must be set");
		this.source = source;
		this.target = target;
		this.actions = actions;
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
	public Mono<Boolean> transit(StateContext<S, E> context) {
		if (guard != null) {
			return guard.apply(context)
				.doOnError(e -> {
					log.warn("Deny guard due to throw as GUARD should not error", e);
				})
				.onErrorReturn(false);
		}
		return Mono.just(true);
	}

	@Override
	public Function<StateContext<S, E>, Mono<Boolean>> getGuard() {
		return guard;
	}

	@Override
	public TransitionKind getKind() {
		return kind;
	}

	@Override
	public SecurityRule getSecurityRule() {
		return securityRule;
	}

	@Override
	public State<S, E> getTarget() {
		return target;
	}

	@Override
	public Collection<Function<StateContext<S, E>, Mono<Void>>> getActions() {
		return actions;
	}

	@Override
	public void addActionListener(ActionListener<S, E> listener) {
		synchronized (this) {
			if (this.actionListener == null) {
				this.actionListener = new CompositeActionListener<>();
			}
			this.actionListener.register(listener);
		}
	}

	@Override
	public void removeActionListener(ActionListener<S, E> listener) {
		synchronized (this) {
			if (this.actionListener != null) {
				this.actionListener.unregister(listener);
			}
		}
	}

	@Override
	public Mono<Void> executeTransitionActions(StateContext<S, E> context) {
		if (getActions() == null) {
			return Mono.empty();
		}
		return Flux.fromIterable(getActions())
			.flatMap(a -> {
				long now = System.currentTimeMillis();
				return a.apply(context)
					.thenEmpty(Mono.fromRunnable(() -> {
						if (this.actionListener != null) {
							try {
								this.actionListener.onExecute(context.getStateMachine(), a, System.currentTimeMillis() - now);
							} catch (Exception e) {
								log.warn("Error with actionListener", e);
							}
						}
					}));
			})
			.then();
	}

	@Override
	public String toString() {
		return "AbstractTransition [source=" + source + ", target=" + target + ", kind=" + kind + ", guard=" + guard + "]";
	}
}
