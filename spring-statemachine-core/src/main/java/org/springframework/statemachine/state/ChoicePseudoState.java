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
package org.springframework.statemachine.state;

import java.util.List;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;

/**
 * Choice implementation of a {@link PseudoState}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ChoicePseudoState<S, E> implements PseudoState<S, E> {

	private List<ChoiceStateData<S, E>> choices;

	public ChoicePseudoState(List<ChoiceStateData<S, E>> choices) {
		this.choices = choices;
	}

	@Override
	public PseudoStateKind getKind() {
		return PseudoStateKind.CHOICE;
	}

	@Override
	public State<S, E> entry(StateContext<S, E> context) {
		State<S, E> s = null;
		for (ChoiceStateData<S, E> c : choices) {
			s = c.getState();
			if (c.guard != null && c.guard.evaluate(context)) {
				break;
			}
		}
		return s;
	}
	
	@Override
	public void exit(StateContext<S, E> context) {
	}

	@Override
	public void addPseudoStateListener(PseudoStateListener<S, E> listener) {
	}

	public static class ChoiceStateData<S, E> {
		private final State<S, E> state;
		private final Guard<S, E> guard;
		public ChoiceStateData(State<S, E> state, Guard<S, E> guard) {
			this.state = state;
			this.guard = guard;
		}
		public State<S, E> getState() {
			return state;
		}
		public Guard<S, E> getGuard() {
			return guard;
		}
	}

}
