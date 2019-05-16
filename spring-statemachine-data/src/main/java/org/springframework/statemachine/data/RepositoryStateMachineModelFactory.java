/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.statemachine.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.config.model.AbstractStateMachineModelFactory;
import org.springframework.statemachine.config.model.ChoiceData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.EntryData;
import org.springframework.statemachine.config.model.ExitData;
import org.springframework.statemachine.config.model.HistoryData;
import org.springframework.statemachine.config.model.JunctionData;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.SpelExpressionGuard;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.StringUtils;

/**
 * A generic {@link StateMachineModelFactory} which is backed by a Spring Data
 * Repository abstraction.
 *
 * @author Janne Valkealahti
 *
 */
public class RepositoryStateMachineModelFactory extends AbstractStateMachineModelFactory<String, String>
		implements StateMachineModelFactory<String, String> {

	private final StateRepository<? extends RepositoryState> stateRepository;
	private final TransitionRepository<? extends RepositoryTransition> transitionRepository;

	/**
	 * Instantiates a new repository state machine model factory.
	 *
	 * @param stateRepository the state repository
	 * @param transitionRepository the transition repository
	 */
	public RepositoryStateMachineModelFactory(StateRepository<? extends RepositoryState> stateRepository,
			TransitionRepository<? extends RepositoryTransition> transitionRepository) {
		this.stateRepository = stateRepository;
		this.transitionRepository = transitionRepository;
	}

	@Override
	public StateMachineModel<String, String> build() {
		return build(null);
	}

	@Override
	public StateMachineModel<String, String> build(String machineId) {
		Collection<StateData<String, String>> stateDatas = new ArrayList<>();
		for (RepositoryState s : stateRepository.findByMachineId(machineId == null ? "" : machineId)) {

			// do recursive build to get states for a submachine
			StateMachineModel<String, String> subStateMachineModel = null;
			String submachineId = s.getSubmachineId();
			if (submachineId != null) {
				subStateMachineModel = build(submachineId);
			}

			Collection<Action<String, String>> stateActions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryStateActions = s.getStateActions();
			if (repositoryStateActions != null) {
				for (RepositoryAction repositoryAction : repositoryStateActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						stateActions.add(action);
					}
				}
			}

			Collection<Action<String, String>> entryActions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryEntryActions = s.getEntryActions();
			if (repositoryEntryActions != null) {
				for (RepositoryAction repositoryAction : repositoryEntryActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						stateActions.add(action);
					}
				}
			}

			Collection<Action<String, String>> exitActions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryExitActions = s.getExitActions();
			if (repositoryExitActions != null) {
				for (RepositoryAction repositoryAction : repositoryExitActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						stateActions.add(action);
					}
				}
			}

			RepositoryState parentState = s.getParentState();
			Object region = s.getRegion();
			StateData<String, String> stateData = new StateData<String, String>(parentState != null ? parentState.getState() : null, region,
					s.getState(), s.isInitial());
			Action<String, String> initialAction = null;
			if (s.getInitialAction() != null) {
				if (StringUtils.hasText(s.getInitialAction().getName())) {
					initialAction = resolveAction(s.getInitialAction().getName());
				} else if (StringUtils.hasText(s.getInitialAction().getSpel())) {
					SpelExpressionParser parser = new SpelExpressionParser(
							new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

					initialAction = new SpelExpressionAction<String, String>(parser.parseExpression(s.getInitialAction().getSpel()));
				}
			}
			stateData.setInitialAction(initialAction);
			stateData.setStateActions(stateActions);
			stateData.setEntryActions(entryActions);
			stateData.setExitActions(exitActions);
			if (s.getKind() != null) {
				stateData.setPseudoStateKind(s.getKind());
				if (s.getKind() == PseudoStateKind.END) {
					stateData.setEnd(true);
				}
			}
			stateData.setDeferred(s.getDeferredEvents());

			if (subStateMachineModel != null) {
				// copy are set parent as state we're currently on
				Collection<StateData<String, String>> submachineStateData = new ArrayList<>();
				Collection<StateData<String, String>> submachineStateDataOrig = subStateMachineModel.getStatesData().getStateData();
				for (StateData<String, String> sd : submachineStateDataOrig) {
					submachineStateData.add(new StateData<String, String>(s.getState(), sd.getRegion(), sd.getState(),
							sd.getDeferred(), sd.getEntryActions(), sd.getExitActions(), sd.isInitial(), sd.getInitialAction()));
				}
				stateData.setSubmachineStateData(submachineStateData);
			}

			stateDatas.add(stateData);
		}
		StatesData<String, String> statesData = new StatesData<>(stateDatas);
		Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
		Collection<EntryData<String, String>> entrys = new ArrayList<EntryData<String, String>>();
		Collection<ExitData<String, String>> exits = new ArrayList<ExitData<String, String>>();
		Collection<HistoryData<String, String>> historys = new ArrayList<HistoryData<String, String>>();
		Map<String, LinkedList<ChoiceData<String, String>>> choices = new HashMap<String, LinkedList<ChoiceData<String,String>>>();
		Map<String, LinkedList<JunctionData<String, String>>> junctions = new HashMap<String, LinkedList<JunctionData<String,String>>>();
		Map<String, List<String>> forks = new HashMap<String, List<String>>();
		Map<String, List<String>> joins = new HashMap<String, List<String>>();

		for (RepositoryTransition t : transitionRepository.findByMachineId(machineId == null ? "" : machineId)) {

			Collection<Action<String, String>> actions = new ArrayList<Action<String, String>>();
			Set<? extends RepositoryAction> repositoryActions = t.getActions();
			if (repositoryActions != null) {
				for (RepositoryAction repositoryAction : repositoryActions) {
					Action<String, String> action = null;
					if (StringUtils.hasText(repositoryAction.getName())) {
						action = resolveAction(repositoryAction.getName());
					} else if (StringUtils.hasText(repositoryAction.getSpel())) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));

						action = new SpelExpressionAction<String, String>(parser.parseExpression(repositoryAction.getSpel()));
					}
					if (action != null) {
						actions.add(action);
					}
				}
			}

			TransitionKind kind = t.getKind();

			Guard<String, String> guard = resolveGuard(t);
			transitionData.add(new TransitionData<>(t.getSource().getState(), t.getTarget().getState(), t.getEvent(), actions, guard, kind != null ? kind : TransitionKind.EXTERNAL));

			if (t.getSource().getKind() == PseudoStateKind.ENTRY) {
				entrys.add(new EntryData<String, String>(t.getSource().getState(), t.getTarget().getState()));
			} else if (t.getSource().getKind() == PseudoStateKind.EXIT) {
				exits.add(new ExitData<String, String>(t.getSource().getState(), t.getTarget().getState()));
			} else if (t.getSource().getKind() == PseudoStateKind.CHOICE) {
				LinkedList<ChoiceData<String, String>> list = choices.get(t.getSource().getState());
				if (list == null) {
					list = new LinkedList<ChoiceData<String, String>>();
					choices.put(t.getSource().getState(), list);
				}
				guard = resolveGuard(t);
				// we want null guards to be at the end
				if (guard == null) {
					list.addLast(new ChoiceData<String, String>(t.getSource().getState(), t.getTarget().getState(), guard));
				} else {
					list.addFirst(new ChoiceData<String, String>(t.getSource().getState(), t.getTarget().getState(), guard));
				}
			} else if (t.getSource().getKind() == PseudoStateKind.JUNCTION) {
				LinkedList<JunctionData<String, String>> list = junctions.get(t.getSource().getState());
				if (list == null) {
					list = new LinkedList<JunctionData<String, String>>();
					junctions.put(t.getSource().getState(), list);
				}
				guard = resolveGuard(t);
				// we want null guards to be at the end
				if (guard == null) {
					list.addLast(new JunctionData<String, String>(t.getSource().getState(), t.getTarget().getState(), guard));
				} else {
					list.addFirst(new JunctionData<String, String>(t.getSource().getState(), t.getTarget().getState(), guard));
				}
			} else if (t.getSource().getKind() == PseudoStateKind.FORK) {
				List<String> list = forks.get(t.getSource().getState());
				if (list == null) {
					list = new ArrayList<String>();
					forks.put(t.getSource().getState(), list);
				}
				list.add(t.getTarget().getState());
			} else if (t.getTarget().getKind() == PseudoStateKind.JOIN) {
				List<String> list = joins.get(t.getTarget().getState());
				if (list == null) {
					list = new ArrayList<String>();
					joins.put(t.getTarget().getState(), list);
				}
				list.add(t.getSource().getState());
			} else if (t.getSource().getKind() == PseudoStateKind.HISTORY_SHALLOW) {
				historys.add(new HistoryData<String, String>(t.getSource().getState(), t.getTarget().getState()));
			} else if (t.getSource().getKind() == PseudoStateKind.HISTORY_DEEP) {
				historys.add(new HistoryData<String, String>(t.getSource().getState(), t.getTarget().getState()));
			}
		}

		HashMap<String, List<ChoiceData<String, String>>> choicesCopy = new HashMap<String, List<ChoiceData<String, String>>>();
		choicesCopy.putAll(choices);
		HashMap<String, List<JunctionData<String, String>>> junctionsCopy = new HashMap<String, List<JunctionData<String, String>>>();
		junctionsCopy.putAll(junctions);

		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData, choicesCopy, junctionsCopy, forks, joins,
				entrys, exits, historys);

		return new DefaultStateMachineModel<>(null, statesData, transitionsData);
	}

	private Guard<String, String> resolveGuard(RepositoryTransition t) {
		Guard<String, String> guard = null;
		RepositoryGuard repositoryGuard = t.getGuard();
		if (repositoryGuard != null) {
			if (StringUtils.hasText(repositoryGuard.getName())) {
				guard = resolveGuard(repositoryGuard.getName());
			} else if (StringUtils.hasText(repositoryGuard.getSpel())) {
				SpelExpressionParser parser = new SpelExpressionParser(
						new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
				guard = new SpelExpressionGuard<>(parser.parseExpression(repositoryGuard.getSpel()));
			}
		}
		return guard;
	}
}
