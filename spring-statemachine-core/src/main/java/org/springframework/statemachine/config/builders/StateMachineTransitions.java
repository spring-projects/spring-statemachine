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
package org.springframework.statemachine.config.builders;

import java.util.Collection;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.transition.TransitionKind;

public class StateMachineTransitions<S, E> {

	private Collection<TransitionData<S, E>> transitions;

	public StateMachineTransitions(Collection<TransitionData<S, E>> transitions) {
		this.transitions = transitions;
	}

	public Collection<TransitionData<S, E>> getTransitions() {
		return transitions;
	}

	public static class TransitionData<S, E> {
		S source;
		S target;
		S state;
		E event;
		Long period;
		Collection<Action<S, E>> actions;
		Guard<S, E> guard;
		TransitionKind kind;
		public TransitionData(S source, S target, S state, E event, Long period, Collection<Action<S, E>> actions, Guard<S, E> guard, TransitionKind kind) {
			this.source = source;
			this.target = target;
			this.state = state;
			this.event = event;
			this.period = period;
			this.actions = actions;
			this.guard = guard;
			this.kind = kind;
		}
		public S getSource() {
			return source;
		}
		public S getTarget() {
			return target;
		}
		public S getState() {
			return state;
		}
		public E getEvent() {
			return event;
		}
		public Long getPeriod() {
			return period;
		}
		public Collection<Action<S, E>> getActions() {
			return actions;
		}
		public Guard<S, E> getGuard() {
			return guard;
		}
		public TransitionKind getKind() {
			return kind;
		}
	}

}
