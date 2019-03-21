/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.uml.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.TimeEvent;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Vertex;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.config.model.ChoiceData;
import org.springframework.statemachine.config.model.EntryData;
import org.springframework.statemachine.config.model.ExitData;
import org.springframework.statemachine.config.model.HistoryData;
import org.springframework.statemachine.config.model.JunctionData;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.SpelExpressionGuard;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Model parser which constructs states and transitions data out from
 * an uml model.
 *
 * @author Janne Valkealahti
 */
public class UmlModelParser {

	public final static String LANGUAGE_BEAN = "bean";
	public final static String LANGUAGE_SPEL = "spel";
	private final Model model;
	private final StateMachineComponentResolver<String, String> resolver;
	private final Collection<StateData<String, String>> stateDatas = new ArrayList<StateData<String, String>>();
	private final Collection<TransitionData<String, String>> transitionDatas = new ArrayList<TransitionData<String, String>>();
	private final Collection<EntryData<String, String>> entrys = new ArrayList<EntryData<String, String>>();
	private final Collection<ExitData<String, String>> exits = new ArrayList<ExitData<String, String>>();
	private final Collection<HistoryData<String, String>> historys = new ArrayList<HistoryData<String, String>>();
	private final Map<String, LinkedList<ChoiceData<String, String>>> choices = new HashMap<String, LinkedList<ChoiceData<String,String>>>();
	private final Map<String, LinkedList<JunctionData<String, String>>> junctions = new HashMap<String, LinkedList<JunctionData<String,String>>>();
	private final Map<String, List<String>> forks = new HashMap<String, List<String>>();
	private final Map<String, List<String>> joins = new HashMap<String, List<String>>();

	/**
	 * Instantiates a new uml model parser.
	 *
	 * @param model the model
	 * @param resolver the resolver
	 */
	public UmlModelParser(Model model, StateMachineComponentResolver<String, String> resolver) {
		Assert.notNull(model, "Model must be set");
		Assert.notNull(resolver, "Resolver must be set");
		this.model = model;
		this.resolver = resolver;
	}

	/**
	 * Parses the model.
	 *
	 * @return the data holder for states and transitions
	 */
	public DataHolder parseModel() {
		EList<PackageableElement> packagedElements = model.getPackagedElements();
		// expect model having exactly one machine
		StateMachine stateMachine = (StateMachine) EcoreUtil.getObjectByType(packagedElements, UMLPackage.Literals.STATE_MACHINE);
		if (stateMachine == null) {
			throw new IllegalArgumentException("Can't find statemachine from model");
		}

		for (Region region : stateMachine.getRegions()) {
			handleRegion(region);
		}
		// LinkedList can be passed due to generics, need to copy
		HashMap<String, List<ChoiceData<String, String>>> choicesCopy = new HashMap<String, List<ChoiceData<String, String>>>();
		choicesCopy.putAll(choices);
		HashMap<String, List<JunctionData<String, String>>> junctionsCopy = new HashMap<String, List<JunctionData<String, String>>>();
		junctionsCopy.putAll(junctions);
		return new DataHolder(new StatesData<>(stateDatas),
				new TransitionsData<String, String>(transitionDatas, choicesCopy, junctionsCopy, forks, joins, entrys, exits, historys));
	}

	private void handleRegion(Region region) {
		// build states
		for (Vertex vertex : region.getSubvertices()) {
			// normal states
			if (vertex instanceof State) {
				State state = (State)vertex;
				// find parent state if submachine state, root states have null parent
				String parent = null;
				String regionId = null;
				if (state.getContainer().getOwner() instanceof State) {
					parent = ((State)state.getContainer().getOwner()).getName();
				}
				if (state.getOwner() instanceof Region) {
					regionId = ((Region)state.getOwner()).getName();
				}
				boolean isInitialState = UmlUtils.isInitialState(state);
				StateData<String, String> stateData = handleActions(
						new StateData<String, String>(parent, regionId, state.getName(), isInitialState), state);
				if (isInitialState) {
					// set possible initial transition
					stateData.setInitialAction(resolveInitialTransitionAction(state));
				}
				stateData.setDeferred(UmlUtils.resolveDererredEvents(state));
				if (UmlUtils.isFinalState(state)) {
					stateData.setEnd(true);
				}
				stateDatas.add(stateData);

				// add states via entry/exit points
				for (Pseudostate cp : state.getConnectionPoints()) {
					PseudoStateKind kind = null;
					if (cp.getKind() == PseudostateKind.ENTRY_POINT_LITERAL) {
						kind = PseudoStateKind.ENTRY;
					} else if (cp.getKind() == PseudostateKind.EXIT_POINT_LITERAL) {
						kind = PseudoStateKind.EXIT;
					}
					if (kind != null) {
						StateData<String, String> cpStateData = new StateData<>(parent, regionId, cp.getName(), false);
						cpStateData.setPseudoStateKind(kind);
						stateDatas.add(cpStateData);
					}
				}

				// do recursive handling of regions
				for (Region sub : state.getRegions()) {
					handleRegion(sub);
				}
			}
			// pseudostates like choice, etc
			if (vertex instanceof Pseudostate) {
				Pseudostate state = (Pseudostate)vertex;
				String parent = null;
				String regionId = null;
				if (state.getContainer().getOwner() instanceof State) {
					parent = ((State)state.getContainer().getOwner()).getName();
				}
				if (state.getOwner() instanceof Region) {
					regionId = ((Region)state.getOwner()).getName();
				}
				if (state.getKind() == PseudostateKind.CHOICE_LITERAL) {
					StateData<String, String> cpStateData = new StateData<>(parent, regionId, state.getName(), false);
					cpStateData.setPseudoStateKind(PseudoStateKind.CHOICE);
					stateDatas.add(cpStateData);
				} else if (state.getKind() == PseudostateKind.JUNCTION_LITERAL) {
					StateData<String, String> cpStateData = new StateData<>(parent, regionId, state.getName(), false);
					cpStateData.setPseudoStateKind(PseudoStateKind.JUNCTION);
					stateDatas.add(cpStateData);
				} else if (state.getKind() == PseudostateKind.FORK_LITERAL) {
					StateData<String, String> cpStateData = new StateData<>(parent, regionId, state.getName(), false);
					cpStateData.setPseudoStateKind(PseudoStateKind.FORK);
					stateDatas.add(cpStateData);
				} else if (state.getKind() == PseudostateKind.JOIN_LITERAL) {
					StateData<String, String> cpStateData = new StateData<>(parent, regionId, state.getName(), false);
					cpStateData.setPseudoStateKind(PseudoStateKind.JOIN);
					stateDatas.add(cpStateData);
				} else if (state.getKind() == PseudostateKind.SHALLOW_HISTORY_LITERAL) {
					StateData<String, String> cpStateData = new StateData<>(parent, regionId, state.getName(), false);
					cpStateData.setPseudoStateKind(PseudoStateKind.HISTORY_SHALLOW);
					stateDatas.add(cpStateData);
				} else if (state.getKind() == PseudostateKind.DEEP_HISTORY_LITERAL) {
					StateData<String, String> cpStateData = new StateData<>(parent, regionId, state.getName(), false);
					cpStateData.setPseudoStateKind(PseudoStateKind.HISTORY_DEEP);
					stateDatas.add(cpStateData);
				}
			}
		}

		// build transitions
		for (Transition transition : region.getTransitions()) {
			// for entry/exit points we need to create these outside
			// of triggers as link from point to a state is most likely
			// just a link and don't have any triggers.
			// little unclear for now if link from points to a state should
			// have trigger?
			// anyway, we need to add entrys and exits to a model
			if (transition.getSource() instanceof Pseudostate) {
				if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.ENTRY_POINT_LITERAL) {
					entrys.add(new EntryData<String, String>(transition.getSource().getName(), transition.getTarget().getName()));
				} else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.EXIT_POINT_LITERAL) {
					exits.add(new ExitData<String, String>(transition.getSource().getName(), transition.getTarget().getName()));
				} else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.CHOICE_LITERAL) {
					LinkedList<ChoiceData<String, String>> list = choices.get(transition.getSource().getName());
					if (list == null) {
						list = new LinkedList<ChoiceData<String, String>>();
						choices.put(transition.getSource().getName(), list);
					}
					Guard<String, String> guard = resolveGuard(transition);
					// we want null guards to be at the end
					if (guard == null) {
						list.addLast(new ChoiceData<String, String>(transition.getSource().getName(), transition.getTarget().getName(), guard));
					} else {
						list.addFirst(new ChoiceData<String, String>(transition.getSource().getName(), transition.getTarget().getName(), guard));
					}
				} else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.JUNCTION_LITERAL) {
					LinkedList<JunctionData<String, String>> list = junctions.get(transition.getSource().getName());
					if (list == null) {
						list = new LinkedList<JunctionData<String, String>>();
						junctions.put(transition.getSource().getName(), list);
					}
					Guard<String, String> guard = resolveGuard(transition);
					// we want null guards to be at the end
					if (guard == null) {
						list.addLast(new JunctionData<String, String>(transition.getSource().getName(), transition.getTarget().getName(), guard));
					} else {
						list.addFirst(new JunctionData<String, String>(transition.getSource().getName(), transition.getTarget().getName(), guard));
					}
				} else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.FORK_LITERAL) {
					List<String> list = forks.get(transition.getSource().getName());
					if (list == null) {
						list = new ArrayList<String>();
						forks.put(transition.getSource().getName(), list);
					}
					list.add(transition.getTarget().getName());
				} else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.SHALLOW_HISTORY_LITERAL) {
					historys.add(new HistoryData<String, String>(transition.getSource().getName(), transition.getTarget().getName()));
				} else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.DEEP_HISTORY_LITERAL) {
					historys.add(new HistoryData<String, String>(transition.getSource().getName(), transition.getTarget().getName()));
				}
			}
			if (transition.getTarget() instanceof Pseudostate) {
				if (((Pseudostate)transition.getTarget()).getKind() == PseudostateKind.JOIN_LITERAL) {
					List<String> list = joins.get(transition.getTarget().getName());
					if (list == null) {
						list = new ArrayList<String>();
						joins.put(transition.getTarget().getName(), list);
					}
					list.add(transition.getSource().getName());
				}
			}

			// go through all triggers and create transition
			// from signals, or transitions from timers
			for (Trigger trigger : transition.getTriggers()) {
				Guard<String, String> guard = resolveGuard(transition);
				Event event = trigger.getEvent();
				if (event instanceof SignalEvent) {
					Signal signal = ((SignalEvent)event).getSignal();
					if (signal != null) {
						transitionDatas.add(new TransitionData<String, String>(transition.getSource().getName(),
								transition.getTarget().getName(), signal.getName(), UmlUtils.resolveTransitionActions(transition, resolver),
								guard, UmlUtils.mapUmlTransitionType(transition)));
					}
				} else if (event instanceof TimeEvent) {
					TimeEvent timeEvent = (TimeEvent)event;
					Long period = getTimePeriod(timeEvent);
					if (period != null) {
						Integer count = null;
						if (timeEvent.isRelative()) {
							count = 1;
						}
						transitionDatas.add(new TransitionData<String, String>(transition.getSource().getName(),
								transition.getTarget().getName(), period, count, UmlUtils.resolveTransitionActions(transition, resolver),
								guard, UmlUtils.mapUmlTransitionType(transition)));
					}
				}
			}

			// create anonymous transition if needed
			if (shouldCreateAnonymousTransition(transition)) {
				transitionDatas.add(new TransitionData<String, String>(transition.getSource().getName(), transition.getTarget().getName(),
						null, UmlUtils.resolveTransitionActions(transition, resolver), resolveGuard(transition),
						UmlUtils.mapUmlTransitionType(transition)));
			}
		}
	}

	private Guard<String, String> resolveGuard(Transition transition) {
		Guard<String, String> guard = null;
		for (Constraint c : transition.getOwnedRules()) {
			if (c.getSpecification() instanceof OpaqueExpression) {
				String beanId = UmlUtils.resolveBodyByLanguage(LANGUAGE_BEAN, (OpaqueExpression)c.getSpecification());
				if (StringUtils.hasText(beanId)) {
					guard = resolver.resolveGuard(beanId);
				} else {
					String expression = UmlUtils.resolveBodyByLanguage(LANGUAGE_SPEL, (OpaqueExpression)c.getSpecification());
					if (StringUtils.hasText(expression)) {
						SpelExpressionParser parser = new SpelExpressionParser(
								new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
						guard = new SpelExpressionGuard<String, String>(parser.parseExpression(expression));
					}
				}
			}
		}
		return guard;
	}

	private Long getTimePeriod(TimeEvent event) {
		try {
			return Long.valueOf(event.getWhen().getExpr().integerValue());
		} catch (Exception e) {
			return null;
		}
	}

	private Action<String, String> resolveInitialTransitionAction(State state) {
		Transition transition = UmlUtils.resolveInitialTransition(state);
		if (transition != null) {
			return UmlUtils.resolveTransitionAction(transition, resolver);
		} else {
			return null;
		}
	}

	private boolean shouldCreateAnonymousTransition(Transition transition) {
		if (transition.getSource() == null || transition.getTarget() == null) {
			// nothing to do as would cause NPE later
			return false;
		}
		if (!transition.getTriggers().isEmpty()) {
			return false;
		}
		if (!StringUtils.hasText(transition.getSource().getName())) {
			return false;
		}
		if (!StringUtils.hasText(transition.getTarget().getName())) {
			return false;
		}
		if (transition.getSource() instanceof Pseudostate) {
			if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.FORK_LITERAL) {
				return false;
			}
		}
		if (transition.getTarget() instanceof Pseudostate) {
			if (((Pseudostate)transition.getTarget()).getKind() == PseudostateKind.JOIN_LITERAL) {
				return false;
			}
		}
		return true;
	}

	private StateData<String, String> handleActions(StateData<String, String> stateData, State state) {
		if (state.getEntry() instanceof OpaqueBehavior) {
			String beanId = UmlUtils.resolveBodyByLanguage(LANGUAGE_BEAN, (OpaqueBehavior)state.getEntry());
			if (StringUtils.hasText(beanId)) {
				Action<String, String> bean = resolver.resolveAction(beanId);
				if (bean != null) {
					ArrayList<Action<String, String>> entrys = new ArrayList<Action<String, String>>();
					entrys.add(bean);
					stateData.setEntryActions(entrys);
				}
			} else {
				String expression = UmlUtils.resolveBodyByLanguage(LANGUAGE_SPEL, (OpaqueBehavior)state.getEntry());
				if (StringUtils.hasText(expression)) {
					SpelExpressionParser parser = new SpelExpressionParser(
							new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
					ArrayList<Action<String, String>> entrys = new ArrayList<Action<String, String>>();
					entrys.add(new SpelExpressionAction<String, String>(parser.parseExpression(expression)));
					stateData.setEntryActions(entrys);
				}
			}
		}
		if (state.getExit() instanceof OpaqueBehavior) {
			String beanId = UmlUtils.resolveBodyByLanguage(LANGUAGE_BEAN, (OpaqueBehavior)state.getExit());
			if (StringUtils.hasText(beanId)) {
				Action<String, String> bean = resolver.resolveAction(beanId);
				if (bean != null) {
					ArrayList<Action<String, String>> exits = new ArrayList<Action<String, String>>();
					exits.add(bean);
					stateData.setExitActions(exits);
				}
			} else {
				String expression = UmlUtils.resolveBodyByLanguage(LANGUAGE_SPEL, (OpaqueBehavior)state.getExit());
				if (StringUtils.hasText(expression)) {
					SpelExpressionParser parser = new SpelExpressionParser(
							new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
					ArrayList<Action<String, String>> exits = new ArrayList<Action<String, String>>();
					exits.add(new SpelExpressionAction<String, String>(parser.parseExpression(expression)));
					stateData.setExitActions(exits);
				}
			}
		}
		if (state.getEntry() instanceof Activity) {
			String beanId = ((Activity)state.getEntry()).getName();
			Action<String, String> bean = resolver.resolveAction(beanId);
			if (bean != null) {
				ArrayList<Action<String, String>> entrys = new ArrayList<Action<String, String>>();
				entrys.add(bean);
				stateData.setEntryActions(entrys);
			}
		}
		if (state.getExit() instanceof Activity) {
			String beanId = ((Activity)state.getExit()).getName();
			Action<String, String> bean = resolver.resolveAction(beanId);
			if (bean != null) {
				ArrayList<Action<String, String>> exits = new ArrayList<Action<String, String>>();
				exits.add(bean);
				stateData.setExitActions(exits);
			}
		}
		return stateData;
	}

	/**
	 * Holder object for results returned from uml parser.
	 */
	public class DataHolder {
		private final StatesData<String, String> statesData;
		private final TransitionsData<String, String> transitionsData;

		/**
		 * Instantiates a new data holder.
		 *
		 * @param statesData the states data
		 * @param transitionsData the transitions data
		 */
		public DataHolder(StatesData<String, String> statesData, TransitionsData<String, String> transitionsData) {
			this.statesData = statesData;
			this.transitionsData = transitionsData;
		}

		/**
		 * Gets the states data.
		 *
		 * @return the states data
		 */
		public StatesData<String, String> getStatesData() {
			return statesData;
		}

		/**
		 * Gets the transitions data.
		 *
		 * @return the transitions data
		 */
		public TransitionsData<String, String> getTransitionsData() {
			return transitionsData;
		}
	}
}
