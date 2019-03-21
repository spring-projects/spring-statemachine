/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.statemachine.config.configurers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StatesData;
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
		extends AnnotationConfigurerAdapter<StatesData<S, E>, StateMachineStateConfigurer<S, E>, StateMachineStateBuilder<S, E>>
		implements StateConfigurer<S, E> {

	private Object parent;
	private final Object region = UUID.randomUUID().toString();
	private final Map<S, StateData<S, E>> incomplete = new HashMap<S, StateData<S, E>>();
	private S initialState;
	private Action<S, E> initialAction;
	private final Collection<S> ends = new ArrayList<>();
	private S history;
	private History historyType;
	private final Collection<S> choices = new ArrayList<S>();
	private final Collection<S> junctions = new ArrayList<S>();
	private final Collection<S> forks = new ArrayList<S>();
	private final Collection<S> joins = new ArrayList<S>();
	private final Collection<S> exits = new ArrayList<S>();
	private final Collection<S> entrys = new ArrayList<S>();
	private final Map<S, StateMachine<S, E>> submachines = new HashMap<>();
	private final Map<S, StateMachineFactory<S, E>> submachinefactories = new HashMap<>();

	@Override
	public void configure(StateMachineStateBuilder<S, E> builder) throws Exception {
		// before passing state datas to builder, update structure
		// for missing parent, initial and end state infos.
		Collection<StateData<S, E>> stateDatas = new ArrayList<StateData<S, E>>();
		for (StateData<S, E> s : incomplete.values()) {
			s.setParent(parent);
			stateDatas.add(s);
			if (s.getState() == initialState) {
				s.setInitial(true);
				s.setInitialAction(initialAction);
			}
			if (ends.contains(s.getState())) {
				s.setEnd(true);
			}
			if (choices.contains(s.getState())) {
				s.setPseudoStateKind(PseudoStateKind.CHOICE);
			} else if (junctions.contains(s.getState())) {
				s.setPseudoStateKind(PseudoStateKind.JUNCTION);
			} else if (forks.contains(s.getState())) {
				s.setPseudoStateKind(PseudoStateKind.FORK);
			} else if (joins.contains(s.getState())) {
				s.setPseudoStateKind(PseudoStateKind.JOIN);
			} else if (entrys.contains(s.getState())) {
				s.setPseudoStateKind(PseudoStateKind.ENTRY);
			} else if (exits.contains(s.getState())) {
				s.setPseudoStateKind(PseudoStateKind.EXIT);
			}
			if (s.getState() == history) {
				if (History.SHALLOW == historyType) {
					s.setPseudoStateKind(PseudoStateKind.HISTORY_SHALLOW);
				} else if (History.DEEP == historyType) {
					s.setPseudoStateKind(PseudoStateKind.HISTORY_DEEP);
				}
			}
			s.setSubmachine(submachines.get(s.getState()));
			s.setSubmachineFactory(submachinefactories.get(s.getState()));
		}
		builder.addStateData(stateDatas);
	}

	@Override
	public StateConfigurer<S, E> initial(S initial) {
		this.initialState = initial;
		state(initial);
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
		this.ends.add(end);
		state(end);
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state) {
		return state(state, (E[])null);
	}

	@Override
	public StateConfigurer<S, E> state(S state, StateMachine<S, E> stateMachine) {
		state(state);
		submachines.put(state, stateMachine);
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state, StateMachineFactory<S, E> stateMachineFactory) {
		state(state);
		submachinefactories.put(state, stateMachineFactory);
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> stateActions) {
		addIncomplete(null, state, null, null, null, stateActions);
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state, Action<S, E> stateAction) {
		Collection<Action<S, E>> stateActions = null;
		if (stateAction != null) {
			stateActions = new ArrayList<Action<S, E>>(1);
			stateActions.add(stateAction);
		}
		return state(state, stateActions);
	}

	@Override
	public StateConfigurer<S, E> stateDo(S state, Action<S, E> action) {
		return stateDo(state, action, null);
	}

	@Override
	public StateConfigurer<S, E> stateDo(S state, Action<S, E> action, Action<S, E> error) {
		Collection<Action<S, E>> stateActions = null;
		if (action != null) {
			stateActions = new ArrayList<Action<S, E>>(1);
			stateActions.add(error != null ? Actions.errorCallingAction(action, error) : action);
		}
		return state(state, stateActions);
	}

	@Override
	public StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions) {
		addIncomplete(null, state, null, entryActions, exitActions, null);
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
	public StateConfigurer<S, E> stateEntry(S state, Action<S, E> action) {
		return state(state, action, null);
	}

	@Override
	public StateConfigurer<S, E> stateEntry(S state, Action<S, E> action, Action<S, E> error) {
		Collection<Action<S, E>> entryActions = null;
		if (action != null) {
			entryActions = new ArrayList<Action<S, E>>(1);
			entryActions.add(error != null ? Actions.errorCallingAction(action, error) : action);
		}
		return state(state, entryActions, null);
	}

	@Override
	public StateConfigurer<S, E> stateExit(S state, Action<S, E> action) {
		return state(state, null, action);
	}

	@Override
	public StateConfigurer<S, E> stateExit(S state, Action<S, E> action, Action<S, E> error) {
		Collection<Action<S, E>> exitActions = null;
		if (action != null) {
			exitActions = new ArrayList<Action<S, E>>(1);
			exitActions.add(error != null ? Actions.errorCallingAction(action, error) : action);
		}
		return state(state, null, exitActions);
	}

	@SuppressWarnings("unchecked")
	@Override
	public StateConfigurer<S, E> state(S state, E... deferred) {
		Collection<E> d = null;
		if (deferred != null) {
			d = Arrays.asList(deferred);
		}
		addIncomplete(null, state, d, null, null, null);
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
		state(choice);
		choices.add(choice);
		return this;
	}

	@Override
	public StateConfigurer<S, E> junction(S junction) {
		state(junction);
		junctions.add(junction);
		return this;
	}

	@Override
	public StateConfigurer<S, E> fork(S fork) {
		state(fork);
		forks.add(fork);
		return this;
	}

	@Override
	public StateConfigurer<S, E> join(S join) {
		state(join);
		joins.add(join);
		return this;
	}

	@Override
	public StateConfigurer<S, E> history(S history, History type) {
		this.history = history;
		this.historyType = type;
		state(history);
		return this;
	}

	@Override
	public StateConfigurer<S, E> entry(S entry) {
		state(entry);
		entrys.add(entry);
		return this;
	}

	@Override
	public StateConfigurer<S, E> exit(S exit) {
		state(exit);
		exits.add(exit);
		return this;
	}

	private void addIncomplete(Object parent, S state, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			Collection<? extends Action<S, E>> stateActions) {
		StateData<S, E> stateData = incomplete.get(state);
		if (stateData == null) {
			stateData = new StateData<S, E>(parent, region, state, deferred, entryActions, exitActions);
			incomplete.put(state, stateData);
		}
		if (stateData.getParent() == null) {
			stateData.setParent(parent);
		}
		if (stateData.getRegion() == null) {
			stateData.setRegion(region);
		}
		if (stateData.getDeferred() == null) {
			stateData.setDeferred(deferred);
		}
		if (stateData.getEntryActions() == null) {
			stateData.setEntryActions(entryActions);
		}
		if (stateData.getExitActions() == null) {
			stateData.setExitActions(exitActions);
		}
		if (stateData.getStateActions() == null) {
			stateData.setStateActions(stateActions);
		}
	}

}
