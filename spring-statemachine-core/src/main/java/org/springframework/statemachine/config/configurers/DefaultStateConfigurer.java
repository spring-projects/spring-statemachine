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
package org.springframework.statemachine.config.configurers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateData;
import org.springframework.statemachine.config.builders.StateMachineStateBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.state.PseudoStateKind;

/**
 * Default implementation of a {@link StateConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateConfigurer<S, E>
		extends AnnotationConfigurerAdapter<StateMachineStates<S, E>, StateMachineStateConfigurer<S, E>, StateMachineStateBuilder<S, E>>
		implements StateConfigurer<S, E> {

	private Object parent;

	private final Object region = UUID.randomUUID().toString();

	private final Collection<StateData<S, E>> incomplete = new ArrayList<StateData<S, E>>();

	private S initialState;

	private Action<S, E> initialAction;

	private S end;

	private final Collection<S> choices = new ArrayList<S>();

	@Override
	public void configure(StateMachineStateBuilder<S, E> builder) throws Exception {
		// before passing state datas to builder, update structure
		// for missing parent, initial and end state infos.
		Collection<StateData<S, E>> stateDatas = new ArrayList<StateData<S, E>>();
		for (StateData<S, E> s : incomplete) {
			s.setParent(parent);
			stateDatas.add(s);
			if (s.getState() == initialState) {
				s.setInitial(true);
				s.setInitialAction(initialAction);
			}
			if (s.getState() == end) {
				s.setEnd(true);
			}
			if (choices.contains(s.getState())) {
				s.setPseudoStateKind(PseudoStateKind.CHOICE);
			}
		}
		builder.addStateData(stateDatas);
	}

	@Override
	public StateConfigurer<S, E> initial(S initial) {
		this.initialState = initial;
		return this;
	}

	@Override
	public StateConfigurer<S, E> initial(S initial, Action<S, E> action) {
		this.initialAction = action;
		return initial(initial);
	}

	@Override
	public StateConfigurer<S, E> parent(S state) {
		this.parent = state;
		return this;
	}

	@Override
	public StateConfigurer<S, E> end(S end) {
		this.end = end;
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state) {
		return state(state, (E[])null);
	}

	@Override
	public StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions) {
		addIncomplete(null, state, null, entryActions, exitActions);
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state, Action<S, E> entryAction, Action<S, E> exitAction) {
		Collection<Action<S, E>> entryActions = null;
		if (entryAction != null) {
			entryActions = new ArrayList<Action<S, E>>(1);
			entryActions.add(entryAction);
		}
		Collection<Action<S, E>> exitActions = null;
		if (exitAction != null) {
			exitActions = new ArrayList<Action<S, E>>(1);
			exitActions.add(exitAction);
		}
		return state(state, entryActions, exitActions);
	}

	@Override
	public StateConfigurer<S, E> state(S state, E... deferred) {
		Collection<E> d = null;
		if (deferred != null) {
			d = Arrays.asList(deferred);
		}
		addIncomplete(null, state, d, null, null);
		return this;
	}

	@Override
	public StateConfigurer<S, E> states(Set<S> states) {
		for (S s : states) {
			state(s);
		}
		return this;
	}

	@Override
	public StateConfigurer<S, E> choice(S choice) {
		choices.add(choice);
		return this;
	}

	private void addIncomplete(Object parent, S state, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions) {
		incomplete.add(new StateData<S, E>(parent, region, state, deferred, entryActions, exitActions));
	}

}
