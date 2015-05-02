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
package org.springframework.statemachine.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.builders.StateMachineTransitions.ChoiceData;
import org.springframework.statemachine.config.builders.StateMachineTransitions.TransitionData;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.ChoicePseudoState;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.state.ChoicePseudoState.ChoiceStateData;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.tree.Tree;
import org.springframework.statemachine.support.tree.Tree.Node;
import org.springframework.statemachine.support.tree.TreeTraverser;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultInternalTransition;
import org.springframework.statemachine.transition.InitialTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.EventTrigger;
import org.springframework.statemachine.trigger.TimerTrigger;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.util.ObjectUtils;

/**
 * {@link StateMachineFactory} implementation using enums to build {@link StateMachine}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class EnumStateMachineFactory<S extends Enum<S>, E extends Enum<E>> extends LifecycleObjectSupport implements
		StateMachineFactory<S, E> {

	private final StateMachineTransitions<S, E> stateMachineTransitions;

	private final StateMachineStates<S, E> stateMachineStates;

	private Boolean contextEvents;

	/**
	 * Instantiates a new enum state machine factory.
	 *
	 * @param stateMachineTransitions the state machine transitions
	 * @param stateMachineStates the state machine states
	 */
	public EnumStateMachineFactory(StateMachineTransitions<S, E> stateMachineTransitions,
			StateMachineStates<S, E> stateMachineStates) {
		this.stateMachineTransitions = stateMachineTransitions;
		this.stateMachineStates = stateMachineStates;
	}

	@Override
	public StateMachine<S, E> getStateMachine() {

		// shared
		DefaultExtendedState defaultExtendedState = new DefaultExtendedState();

		StateMachine<S, E> machine = null;

		// we store mappings from state id's to states which gets
		// created during the process. This is needed for transitions to
		// find a correct mappings because they use state id's, not actual
		// states.
		final Map<S, State<S, E>> stateMap = new HashMap<S, State<S, E>>();
		Stack<MachineStackItem<S, E>> regionStack = new Stack<MachineStackItem<S, E>>();
		Stack<StateData<S, E>> stateStack = new Stack<StateData<S, E>>();
		Map<Object, StateMachine<S, E>> machineMap = new HashMap<Object, StateMachine<S,E>>();

		Iterator<Node<StateData<S, E>>> iterator = buildStateDataIterator();
		while (iterator.hasNext()) {
			Node<StateData<S, E>> node = iterator.next();
			StateData<S, E> stateData = node.getData();
			StateData<S, E> peek = stateStack.isEmpty() ? null : stateStack.peek();

			// simply push and continue
			if (stateStack.isEmpty()) {
				stateStack.push(stateData);
				continue;
			}

			boolean stackContainsSameParent = false;
			Iterator<StateData<S, E>> ii = stateStack.iterator();
			while (ii.hasNext()) {
				StateData<S, E> sd = ii.next();
				if (stateData != null && ObjectUtils.nullSafeEquals(stateData.getState(), sd.getParent())) {
					stackContainsSameParent = true;
					break;
				}
			}

			if (stateData != null && !stackContainsSameParent) {
				stateStack.push(stateData);
				continue;
			}

			Collection<StateData<S, E>> stateDatas = popSameParents(stateStack);
			int initialCount = getInitialCount(stateDatas);
			Collection<Collection<StateData<S, E>>> regionsStateDatas = splitIntoRegions(stateDatas);
			Collection<TransitionData<S, E>> transitionsData = getTransitionData(iterator.hasNext(), stateDatas);

			if (initialCount > 1) {
				for (Collection<StateData<S, E>> regionStateDatas : regionsStateDatas) {
					machine = buildMachine(machineMap, stateMap, regionStateDatas, transitionsData, getBeanFactory(),
							contextEvents, defaultExtendedState, stateMachineTransitions);
					regionStack.push(new MachineStackItem<S, E>(machine, peek.getParent(), peek));
				}

				Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
				for (MachineStackItem<S, E> si : regionStack) {
					regions.add(si.machine);
				}
				@SuppressWarnings("unchecked")
				S parent = (S)peek.getParent();
				RegionState<S, E> rstate = new RegionState<S, E>(parent, regions, null, null, null,
						new DefaultPseudoState<S, E>(PseudoStateKind.INITIAL));
				if (stateData != null) {
					stateMap.put(stateData.getState(), rstate);
				}
			} else {
				machine = buildMachine(machineMap, stateMap, stateDatas, transitionsData, getBeanFactory(),
						contextEvents, defaultExtendedState, stateMachineTransitions);
				if (peek.isInitial() || (!peek.isInitial() && !machineMap.containsKey(peek.getParent()))) {
					machineMap.put(peek.getParent(), machine);
				}
			}

			stateStack.push(stateData);
		}

		return machine;
	}

	public void setContextEventsEnabled(Boolean contextEvents) {
		this.contextEvents = contextEvents;
	}

	private int getInitialCount(Collection<StateData<S, E>> stateDatas) {
		int count = 0;
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.isInitial()) {
				count++;
			}
		}
		return count;
	}

	private Collection<Collection<StateData<S, E>>> splitIntoRegions(Collection<StateData<S, E>> stateDatas) {
		Map<Object, Collection<StateData<S, E>>> map = new HashMap<Object, Collection<StateData<S, E>>>();
		for (StateData<S, E> stateData : stateDatas) {
			Collection<StateData<S, E>> c = map.get(stateData.getRegion());
			if (c == null) {
				c = new ArrayList<StateData<S,E>>();
			}
			c.add(stateData);
			map.put(stateData.getRegion(), c);
		}
		return map.values();
	}

	private Collection<TransitionData<S, E>> getTransitionData(boolean roots, Collection<StateData<S, E>> stateDatas) {
		if (roots) {
			return resolveTransitionData(stateMachineTransitions.getTransitions(), stateDatas);
		} else {
			return resolveTransitionData2(stateMachineTransitions.getTransitions());
		}
	}

	private static <S, E> Collection<StateData<S, E>> popSameParents(Stack<StateData<S, E>> stack) {
		Collection<StateData<S, E>> data = new ArrayList<StateData<S, E>>();
		Object parent = null;
		if (!stack.isEmpty()) {
			parent = stack.peek().getParent();
		}
		while (!stack.isEmpty() && ObjectUtils.nullSafeEquals(parent, stack.peek().getParent())) {
			data.add(stack.pop());
		}
		return data;
	}


	private static class MachineStackItem<S, E> {

		StateMachine<S, E> machine;
		Object parent;
		StateData<S, E> stateData;

		public MachineStackItem(StateMachine<S, E> machine, Object parent, StateData<S, E> stateData) {
			super();
			this.machine = machine;
			this.parent = parent;
			this.stateData = stateData;
		}

	}

	private Collection<TransitionData<S, E>> resolveTransitionData(Collection<TransitionData<S, E>> in, Collection<StateData<S, E>> stateDatas) {
		ArrayList<TransitionData<S, E>> out = new ArrayList<TransitionData<S,E>>();

		Collection<Object> states = new ArrayList<Object>();
		for (StateData<S, E> stateData : stateDatas) {
			states.add(stateData.getParent());
		}

		for (TransitionData<S, E> transitionData : in) {
			S state = transitionData.getState();
			if (state != null && states.contains(state)) {
				out.add(transitionData);
			}
		}

		return out;
	}

	private Collection<TransitionData<S, E>> resolveTransitionData2(Collection<TransitionData<S, E>> in) {
		ArrayList<TransitionData<S, E>> out = new ArrayList<TransitionData<S,E>>();
		for (TransitionData<S, E> transitionData : in) {
			if (transitionData.getState() == null) {
				out.add(transitionData);
			}
		}
		return out;
	}


	private static <S extends Enum<S>, E extends Enum<E>> StateMachine<S, E> buildMachine(
			Map<Object, StateMachine<S, E>> machineMap, Map<S, State<S, E>> stateMap,
			Collection<StateData<S, E>> stateDatas, Collection<TransitionData<S, E>> transitionsData,
			BeanFactory beanFactory, Boolean contextEvents, DefaultExtendedState defaultExtendedState,
			StateMachineTransitions<S, E> stateMachineTransitions) {
		State<S, E> state = null;
		State<S, E> initialState = null;
		PseudoState<S, E> historyState = null;
		Action<S, E> initialAction = null;
		Collection<State<S, E>> states = new ArrayList<State<S,E>>();

		// for now loop twice and build states for
		// non initial/end pseudostates last

		for (StateData<S, E> stateData : stateDatas) {
			StateMachine<S, E> stateMachine = machineMap.get(stateData.getState());
			state = stateMap.get(stateData.getState());
			if (state != null) {
				states.add(state);
				initialState = state;
				continue;
			}
			if (stateMachine != null) {
				state = new StateMachineState<S, E>(stateData.getState(), stateMachine, stateData.getDeferred(),
				stateData.getEntryActions(), stateData.getExitActions(), new DefaultPseudoState<S, E>(
				PseudoStateKind.INITIAL));
				// TODO: below if/else doesn't feel right
				if (stateDatas.size() > 1 && stateData.isInitial()) {
					initialState = state;
					initialAction = stateData.getInitialAction();
				} else if (stateDatas.size() == 1) {
					initialState = state;
					initialAction = stateData.getInitialAction();
				}
				states.add(state);
			} else {
				PseudoState<S, E> pseudoState = null;
				if (stateData.isInitial()) {
					pseudoState = new DefaultPseudoState<S, E>(PseudoStateKind.INITIAL);
				} else if (stateData.isEnd()) {
					pseudoState = new DefaultPseudoState<S, E>(PseudoStateKind.END);
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.HISTORY_SHALLOW) {
					pseudoState = new HistoryPseudoState<S, E>(PseudoStateKind.HISTORY_SHALLOW);
					historyState = pseudoState;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.HISTORY_DEEP) {
					pseudoState = new HistoryPseudoState<S, E>(PseudoStateKind.HISTORY_DEEP);
					historyState = pseudoState;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.CHOICE) {
					continue;
				}
				state = new EnumState<S, E>(stateData.getState(), stateData.getDeferred(),
						stateData.getEntryActions(), stateData.getExitActions(), pseudoState);
				if (stateData.isInitial()) {
					initialState = state;
					initialAction = stateData.getInitialAction();
				}
				states.add(state);

			}
			stateMap.put(stateData.getState(), state);
		}

		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getPseudoStateKind() == PseudoStateKind.CHOICE) {
				S s = stateData.getState();
				List<ChoiceData<S, E>> list = stateMachineTransitions.getChoices().get(s);
				List<ChoiceStateData<S, E>> choices = new ArrayList<ChoiceStateData<S, E>>();
				for (ChoiceData<S, E> c : list) {
					choices.add(new ChoiceStateData<S, E>(stateMap.get(c.getTarget()), c.getGuard()));
				}
				PseudoState<S, E> pseudoState = new ChoicePseudoState<S, E>(choices);
				state = new EnumState<S, E>(stateData.getState(), stateData.getDeferred(),
						stateData.getEntryActions(), stateData.getExitActions(), pseudoState);
				states.add(state);
				stateMap.put(stateData.getState(), state);
			}
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData) {
			S source = transitionData.getSource();
			S target = transitionData.getTarget();
			E event = transitionData.getEvent();
			Long period = transitionData.getPeriod();

			Trigger<S, E> trigger = null;
			if (event != null) {
				trigger = new EventTrigger<S, E>(event);
			} else if (period != null) {
				TimerTrigger<S, E> t = new TimerTrigger<S, E>(period);
				if (beanFactory != null) {
					t.setBeanFactory(beanFactory);
				}
				trigger = t;
			}

			if (transitionData.getKind() == TransitionKind.EXTERNAL) {
				// TODO can we do this?
				if (stateMap.get(source) == null && stateMap.get(target) == null) {
					continue;
				}
				DefaultExternalTransition<S, E> transition = new DefaultExternalTransition<S, E>(stateMap.get(source),
						stateMap.get(target), transitionData.getActions(), event, transitionData.getGuard(), trigger);
				transitions.add(transition);

			} else if (transitionData.getKind() == TransitionKind.INTERNAL) {
				DefaultInternalTransition<S, E> transition = new DefaultInternalTransition<S, E>(stateMap.get(source),
						transitionData.getActions(), event, transitionData.getGuard(), trigger);
				transitions.add(transition);
			}
		}

		// TODO: should make a proper transition
		Transition<S, E> initialTransition = null;
		if (initialAction != null) {
			initialTransition = new InitialTransition<S, E>(initialState, initialAction);
		}

		EnumStateMachine<S, E> machine = new EnumStateMachine<S, E>(states, transitions, initialState,
				initialTransition, null, defaultExtendedState);
		machine.setHistoryState(historyState);
		if (contextEvents != null) {
			machine.setContextEventsEnabled(contextEvents);
		}
		machine.afterPropertiesSet();
		if (beanFactory != null) {
			machine.setBeanFactory(beanFactory);
		}
		return machine;
	}

	private Iterator<Node<StateData<S, E>>> buildStateDataIterator() {
		Tree<StateData<S, E>> tree = new Tree<StateData<S, E>>();

		for (StateData<S, E> stateData : stateMachineStates.getStateDatas()) {
			Object id = stateData.getState();
			Object parent = stateData.getParent();
			tree.add(stateData, id, parent);
		}

		TreeTraverser<Node<StateData<S, E>>> traverser = new TreeTraverser<Node<StateData<S, E>>>() {
		    @Override
		    public Iterable<Node<StateData<S, E>>> children(Node<StateData<S, E>> root) {
		        return root.getChildren();
		    }
		};

		Iterable<Node<StateData<S, E>>> postOrderTraversal = traverser.postOrderTraversal(tree.getRoot());
		Iterator<Node<StateData<S, E>>> iterator = postOrderTraversal.iterator();
		return iterator;
	}

}
