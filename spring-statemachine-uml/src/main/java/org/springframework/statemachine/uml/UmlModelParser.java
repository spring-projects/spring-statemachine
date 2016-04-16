/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.uml;

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
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.SignalEvent;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Trigger;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Vertex;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.ChoiceData;
import org.springframework.statemachine.config.model.EntryData;
import org.springframework.statemachine.config.model.ExitData;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.util.Assert;

/**
 * Model parser which constructs states and transitions data out from
 * an uml model.
 *
 * @author Janne Valkealahti
 */
public class UmlModelParser {

	private final Model model;
	private final StateMachineComponentResolver<String, String> resolver;
	private final Collection<StateData<String, String>> stateDatas = new ArrayList<StateData<String, String>>();
	private final Collection<TransitionData<String, String>> transitionDatas = new ArrayList<TransitionData<String, String>>();
	private final Collection<EntryData<String, String>> entrys = new ArrayList<EntryData<String, String>>();
	private final Collection<ExitData<String, String>> exits = new ArrayList<ExitData<String, String>>();
	private final Map<String, LinkedList<ChoiceData<String, String>>> choices = new HashMap<String, LinkedList<ChoiceData<String,String>>>();

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
		HashMap<String, List<ChoiceData<String, String>>> choicesCopy = new HashMap<String, List<ChoiceData<String,String>>>();
		choicesCopy.putAll(choices);
		return new DataHolder(new StatesData<>(stateDatas), new TransitionsData<String, String>(transitionDatas, choicesCopy, null, null, entrys, exits));
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
				StateData<String, String> stateData = handleActions(
						new StateData<String, String>(parent, regionId, state.getName(), UmlUtils.isInitialState(state)), state);
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
					Guard<String, String> guard = null;
					for (Constraint c : transition.getOwnedRules()) {
						if (c.getSpecification() instanceof OpaqueExpression) {
							OpaqueExpression oe = (OpaqueExpression)c.getSpecification();
							if (oe.getBodies().size() == 1) {
								guard = resolver.resolveGuard(oe.getBodies().get(0).trim());
							}
						}
					}
					// we want null guards to be at the end
					if (guard == null) {
						list.addLast(new ChoiceData<String, String>(transition.getSource().getName(), transition.getTarget().getName(), guard));
					} else {
						list.addFirst(new ChoiceData<String, String>(transition.getSource().getName(), transition.getTarget().getName(), guard));
					}
				}
			}

			// go through all triggers and create transition
			// from signals
			for (Trigger trigger : transition.getTriggers()) {
				Event event = trigger.getEvent();
				if (event instanceof SignalEvent) {
					Signal signal = ((SignalEvent)event).getSignal();
					if (signal != null) {
						transitionDatas.add(new TransitionData<String, String>(transition.getSource().getName(),
								transition.getTarget().getName(), signal.getName()));
					}
				}
			}
		}
	}

	private StateData<String, String> handleActions(StateData<String, String> stateData, State state) {
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
	protected class DataHolder {
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
