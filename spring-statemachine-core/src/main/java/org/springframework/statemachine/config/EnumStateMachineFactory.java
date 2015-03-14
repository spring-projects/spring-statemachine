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
import java.util.Map;
import java.util.Stack;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.builders.StateMachineTransitions.TransitionData;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.tree.Tree;
import org.springframework.statemachine.support.tree.Tree.Node;
import org.springframework.statemachine.support.tree.TreeTraverser;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultInternalTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.EventTrigger;
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
		StateMachine<S, E> machine = null;

		// we store mappings from state id's to states which gets
		// created during the process. This is needed for transitions to
		// find a correct mappings because they use state id's, not actual
		// states.
		final Map<S, State<S, E>> stateMap = new HashMap<S, State<S, E>>();

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

		Stack<MachineStackItem<S, E>> regionStack = new Stack<MachineStackItem<S, E>>();
		Stack<StateData<S, E>> stateStack = new Stack<StateData<S, E>>();

		Iterable<Node<StateData<S, E>>> postOrderTraversal = traverser.postOrderTraversal(tree.getRoot());
		Iterator<Node<StateData<S, E>>> iterator = postOrderTraversal.iterator();

		Map<Object, StateMachine<S, E>> machineMap = new HashMap<Object, StateMachine<S,E>>();

		while (iterator.hasNext()) {
			Node<StateData<S, E>> node = iterator.next();
			StateData<S, E> stateData = node.getData();
			StateData<S, E> peek = stateStack.isEmpty() ? null : stateStack.peek();

			// simply push and continue
			if (stateStack.isEmpty()) {
				stateStack.push(stateData);
				continue;
			}

			if (stateData != null && ObjectUtils.nullSafeEquals(peek.getParent(), stateData.getParent())) {
				stateStack.push(stateData);
				continue;
			}

			Collection<StateData<S, E>> stateDatas = popSameParents(stateStack);
			Collection<TransitionData<S, E>> transitionsData = getTransitionData(iterator.hasNext(), stateDatas);

			machine = buildMachine(machineMap, stateMap, stateDatas, transitionsData, getBeanFactory());
			// TODO: last part in if feels a bit hack
			//       (!peek.isInitial() && !machineMap.containsKey(peek.getParent()))
			if (peek.isInitial() || (!peek.isInitial() && !machineMap.containsKey(peek.getParent()))) {
				machineMap.put(peek.getParent(), machine);
			}
			if (peek.getParent() == null) {
				regionStack.push(new MachineStackItem<S, E>(machine, peek.getParent(), peek));
			}
			stateStack.push(stateData);
		}

		// TODO: usage of initials is a temporary fix to workaround for missing
		//       full support of regions
		int initials = 0;
		if (regionStack.size() > 1) {
			Collection<TransitionData<S, E>> transitionsData = resolveTransitionData2(stateMachineTransitions.getTransitions());
			Collection<StateData<S, E>> stateDatas = new ArrayList<StateData<S, E>>();
			Iterator<MachineStackItem<S, E>> i = regionStack.iterator();
			while (i.hasNext()) {
				MachineStackItem<S, E> next = i.next();
				stateDatas.add(next.stateData);
				if (next.stateData.isInitial()) {
					initials++;
				}
			}

			if (initials == 1) {
				machine = buildMachine(machineMap, stateMap, stateDatas, transitionsData, getBeanFactory());
			}
		}

		if (initials > 1) {
			Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
			for (MachineStackItem<S, E> si : regionStack) {
				if (si.parent == null) {
					regions.add(si.machine);
				}
			}
			if (regions.size() > 1) {
				RegionState<S, E> rstate = new RegionState<S, E>(null, regions);
				Collection<State<S, E>> states = new ArrayList<State<S, E>>();
				states.add(rstate);
				machine = new EnumStateMachine<S, E>(states, null, rstate, null);
			}
		}

		return machine;
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
			Map<Object, StateMachine<S, E>> machineMap, Map<S, State<S, E>> stateMap, Collection<StateData<S, E>> stateDatas,
			Collection<TransitionData<S, E>> transitionsData, BeanFactory beanFactory) {
		State<S, E> state = null;
		State<S, E> initialState = null;
		State<S, E> endState = null;
		Collection<State<S, E>> states = new ArrayList<State<S,E>>();
		for (StateData<S, E> stateData : stateDatas) {
			StateMachine<S, E> stateMachine = machineMap.get(stateData.getState());
			if (stateMachine != null) {
				state = new StateMachineState<S, E>(stateData.getState(), stateMachine, stateData.getDeferred(),
				stateData.getEntryActions(), stateData.getExitActions(), new DefaultPseudoState(
				PseudoStateKind.INITIAL));
				// TODO: below if/else doesn't feel right
				if (stateDatas.size() > 1 && stateData.isInitial()) {
					initialState = state;
				} else if (stateDatas.size() == 1) {
					initialState = state;
				}
//				initialState = state;
				states.add(state);
			} else {
				PseudoState pseudoState = null;
				if (stateData.isInitial()) {
					pseudoState = new DefaultPseudoState(PseudoStateKind.INITIAL);
				}
				state = new EnumState<S, E>(stateData.getState(), stateData.getDeferred(),
						stateData.getEntryActions(), stateData.getExitActions(), pseudoState);
				if (stateData.isInitial()) {
					initialState = state;
				}
				if (stateData.isEnd()) {
					endState = state;
				}
				states.add(state);

			}


			// TODO: doesn't feel right to tweak initial kind like this
			stateMap.put(stateData.getState(), state);
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData) {
			S source = transitionData.getSource();
			S target = transitionData.getTarget();
			E event = transitionData.getEvent();
			if (transitionData.getKind() == TransitionKind.EXTERNAL) {
				DefaultExternalTransition<S, E> transition = new DefaultExternalTransition<S, E>(stateMap.get(source),
						stateMap.get(target), transitionData.getActions(), event, transitionData.getGuard(), event != null ? new EventTrigger<S, E>(event) : null);
				transitions.add(transition);

			} else if (transitionData.getKind() == TransitionKind.INTERNAL) {
				DefaultInternalTransition<S, E> transition = new DefaultInternalTransition<S, E>(stateMap.get(source),
						transitionData.getActions(), event, transitionData.getGuard(), event != null ? new EventTrigger<S, E>(event) : null);
				transitions.add(transition);
			}
		}

		EnumStateMachine<S, E> machine = new EnumStateMachine<S, E>(/*stateMap.values()*/ states, transitions,
				initialState, endState);
		machine.afterPropertiesSet();
		if (beanFactory != null) {
			machine.setBeanFactory(beanFactory);
		}
		return machine;
	}

}
