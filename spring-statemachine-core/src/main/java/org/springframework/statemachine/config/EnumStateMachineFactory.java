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
import java.util.Enumeration;
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
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
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

		// use two stack, first for states and second for machines
		Stack<MachineStackItem<S, E>> machineStack = new Stack<MachineStackItem<S, E>>();
		Stack<StateData<S, E>> stateStack = new Stack<StateData<S, E>>();

		Iterable<Node<StateData<S, E>>> postOrderTraversal = traverser.postOrderTraversal(tree.getRoot());
		Iterator<Node<StateData<S, E>>> iterator = postOrderTraversal.iterator();


		while (iterator.hasNext()) {
			Node<StateData<S, E>> node = iterator.next();
			StateData<S, E> stateData = node.getData();
			if (stateStack.isEmpty()) {
				stateStack.push(stateData);
			} else {
				StateData<S, E> peek = stateStack.peek();
				if ((stateData != null) && ((peek.getParent() == null && stateData.getParent() == null) || peek.getParent().equals(stateData.getParent()))) {
					stateStack.push(stateData);
				} else {
					Collection<StateData<S, E>> stateDatas = new ArrayList<StateData<S,E>>();
					Enumeration<StateData<S, E>> elements = stateStack.elements();
					while (elements.hasMoreElements()) {
						StateData<S, E> next = elements.nextElement();
						stateDatas.add(next);
					}
					stateStack.clear();

					Collection<TransitionData<S, E>> transitionsData = null;
					if (iterator.hasNext()) {
						transitionsData = resolveTransitionData(stateMachineTransitions.getTransitions(), stateDatas);
					} else {
						transitionsData = resolveTransitionData2(stateMachineTransitions.getTransitions());
					}

					if (machineStack.isEmpty()) {
						machine = buildSimpleMachine(stateMap, stateDatas, transitionsData, getBeanFactory());
						machineStack.push(new MachineStackItem<S, E>(machine, peek.getParent()));
					} else {
						MachineStackItem<S, E> pop = machineStack.pop();
						machine = buildSubMachine(stateMap, pop, stateDatas, transitionsData, getBeanFactory());
						machineStack.push(new MachineStackItem<S, E>(machine, null));
					}
					stateStack.push(stateData);
				}
			}
		}
		return machine;
	}

	private static class MachineStackItem<S, E> {

		StateMachine<S, E> machine;
		Object parent;

		public MachineStackItem(StateMachine<S, E> machine, Object parent) {
			super();
			this.machine = machine;
			this.parent = parent;
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

	private static <S extends Enum<S>, E extends Enum<E>> StateMachine<S, E> buildSimpleMachine(
			Map<S, State<S, E>> stateMap, Collection<StateData<S, E>> stateDatas,
			Collection<TransitionData<S, E>> transitionsData, BeanFactory beanFactory) {
		State<S, E> initialState = null;
		State<S, E> endState = null;
		for (StateData<S, E> stateData : stateDatas) {

			// TODO: doesn't feel right to tweak initial kind like this
			PseudoState pseudoState = null;
			if (stateData.isInitial()) {
				pseudoState = new DefaultPseudoState(PseudoStateKind.INITIAL);
			}
			EnumState<S,E> state = new EnumState<S, E>(stateData.getState(), stateData.getDeferred(),
					stateData.getEntryActions(), stateData.getExitActions(), pseudoState);
			if (stateData.isInitial()) {
				initialState = state;
			}
			if (stateData.isEnd()) {
				endState = state;
			}
			stateMap.put(stateData.getState(), state);
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData) {
			S source = transitionData.getSource();
			S target = transitionData.getTarget();
			E event = transitionData.getEvent();
			if (transitionData.getKind() == TransitionKind.EXTERNAL) {
				DefaultExternalTransition<S, E> transition = new DefaultExternalTransition<S, E>(stateMap.get(source),
						stateMap.get(target), transitionData.getActions(), event, transitionData.getGuard());
				transitions.add(transition);

			} else if (transitionData.getKind() == TransitionKind.INTERNAL) {
				DefaultInternalTransition<S, E> transition = new DefaultInternalTransition<S, E>(stateMap.get(source),
						transitionData.getActions(), event, transitionData.getGuard());
				transitions.add(transition);
			}
		}

		EnumStateMachine<S, E> machine = new EnumStateMachine<S, E>(stateMap.values(), transitions,
				initialState, endState);
		machine.afterPropertiesSet();
		if (beanFactory != null) {
			machine.setBeanFactory(beanFactory);
		}
		return machine;
	}

	private static <S extends Enum<S>, E extends Enum<E>> StateMachine<S, E> buildSubMachine(
			Map<S, State<S, E>> stateMap, MachineStackItem<S, E> stackItem, Collection<StateData<S, E>> stateDatas,
			Collection<TransitionData<S, E>> transitionsData, BeanFactory beanFactory) {
		Collection<State<S, E>> states = new ArrayList<State<S,E>>();
		State<S, E> state = null;
		State<S, E> initialState = null;
		for (StateData<S, E> stateData : stateDatas) {
			if (stackItem.parent == null || stateData.getState().equals(stackItem.parent)) {
				state = new StateMachineState<S, E>(stateData.getState(), stackItem.machine, stateData.getDeferred(),
						stateData.getEntryActions(), stateData.getExitActions(), new DefaultPseudoState(
						PseudoStateKind.INITIAL));
				initialState = state;
			} else {
				state = new EnumState<S, E>(stateData.getState(), stateData.getDeferred(),
						stateData.getEntryActions(), stateData.getExitActions(), null);
			}
			states.add(state);
			stateMap.put(stateData.getState(), state);
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData) {
			S source = transitionData.getSource();
			S target = transitionData.getTarget();
			E event = transitionData.getEvent();
			if (transitionData.getKind() == TransitionKind.EXTERNAL) {
				DefaultExternalTransition<S, E> transition = new DefaultExternalTransition<S, E>(stateMap.get(source),
						stateMap.get(target), transitionData.getActions(), event, transitionData.getGuard());
				transitions.add(transition);

			} else if (transitionData.getKind() == TransitionKind.INTERNAL) {
				DefaultInternalTransition<S, E> transition = new DefaultInternalTransition<S, E>(stateMap.get(source),
						transitionData.getActions(), event, transitionData.getGuard());
				transitions.add(transition);
			}
		}

		EnumStateMachine<S, E> machine = new EnumStateMachine<S, E>(states, transitions, initialState, null);

		machine.afterPropertiesSet();
		if (beanFactory != null) {
			machine.setBeanFactory(beanFactory);
		}
		return machine;
	}

}
