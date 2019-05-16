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
package org.springframework.statemachine.config.builders;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.transition.TransitionKind;

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

	/**
	 * A simple data object keeping transition related configs in a same place.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 */
	public static class TransitionData<S, E> {
		private final S source;
		private final S target;
		private final S state;
		private final E event;
		private final Long period;
		private final Collection<Action<S, E>> actions;
		private final Guard<S, E> guard;
		private final TransitionKind kind;

		/**
		 * Instantiates a new transition data.
		 *
		 * @param source the source
		 * @param target the target
		 * @param state the state
		 * @param event the event
		 * @param period the period
		 * @param actions the actions
		 * @param guard the guard
		 * @param kind the kind
		 */
		public TransitionData(S source, S target, S state, E event, Long period, Collection<Action<S, E>> actions,
				Guard<S, E> guard, TransitionKind kind) {
			this.source = source;
			this.target = target;
			this.state = state;
			this.event = event;
			this.period = period;
			this.actions = actions;
			this.guard = guard;
			this.kind = kind;
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
	}

	/**
	 * A simple data object keeping choice related configs in a same place.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 */
	public static class ChoiceData<S, E> {
		private final S source;
		private final S target;
		private final Guard<S, E> guard;

		/**
		 * Instantiates a new choice data.
		 *
		 * @param source the source
		 * @param target the target
		 * @param guard the guard
		 */
		public ChoiceData(S source, S target, Guard<S, E> guard) {
			this.source = source;
			this.target = target;
			this.guard = guard;
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
	}

}
