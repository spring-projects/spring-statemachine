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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineStates.StateData;
import org.springframework.statemachine.config.builders.StateMachineStates.StateMachineStatesData;
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
		// we store mappings from state id's to states which gets
		// created during the process. This is needed for transitions to
		// find a correct mappings because they use state id's, not actual
		// states.
		final Map<S, State<S, E>> stateMap = new HashMap<S, State<S, E>>();

		// we eventually return this machine which is a last one build
		StateMachine<S, E> machine = null;

		Map<Object, StateMachineStatesData<S, E>> states = stateMachineStates.getStates();
		Tree<StateMachineStatesData<S, E>> tree = new Tree<StateMachineStatesData<S, E>>();

		for (Entry<Object, StateMachineStatesData<S, E>> e : states.entrySet()) {
			Object id = e.getKey();
			StateMachineStatesData<S, E> value = e.getValue();
			Object parent = value.getParent();
			tree.add(value, id, parent);
		}

		TreeTraverser<Node<StateMachineStatesData<S, E>>> traverser = new TreeTraverser<Node<StateMachineStatesData<S, E>>>() {
		    @Override
		    public Iterable<Node<StateMachineStatesData<S, E>>> children(Node<StateMachineStatesData<S, E>> root) {
		        return root.getChildren();
		    }
		};

		Stack<StackItem<S, E>> stack = new Stack<StackItem<S, E>>();
		for (Node<StateMachineStatesData<S, E>> node : traverser.postOrderTraversal(tree.getRoot())) {
			System.out.println(node.getData());

			Object parent = node.getData().getParent();

			if (!stack.empty()) {
				StackItem<S, E> pop = stack.pop();
				machine = buildSubMachine(stateMap, pop.machine, node.getData(), stateMachineTransitions, getBeanFactory());
				stack.push(new StackItem<S, E>(machine, parent));
			} else {
				machine = buildSimpleMachine(stateMap, node.getData(), stateMachineTransitions, getBeanFactory());
				stack.push(new StackItem<S, E>(machine, parent));
			}


		}

		return machine;
	}

	private static class StackItem<S, E> {
		StateMachine<S, E> machine;
		Object id;
		public StackItem(StateMachine<S, E> machine, Object id) {
			this.machine = machine;
			this.id = id;
		}
	}

	private static <S extends Enum<S>, E extends Enum<E>> StateMachine<S, E> buildSimpleMachine(Map<S, State<S, E>> stateMap, StateMachineStatesData<S, E> statesData, StateMachineTransitions<S, E> transitionsData, BeanFactory beanFactory) {
		for (StateData<S, E> stateData : statesData.getStates()) {

			// TODO: doesn't feel right to tweak initial kind like this
			PseudoState pseudoState = null;
			if (stateData.getState() == statesData.getInitialState()) {
				pseudoState = new DefaultPseudoState(PseudoStateKind.INITIAL);
			}
			stateMap.put(stateData.getState(), new EnumState<S, E>(stateData.getState(), stateData.getDeferred(),
					stateData.getEntryActions(), stateData.getExitActions(), pseudoState));
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData.getTransitions()) {
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
				stateMap.get(statesData.getInitialState()), stateMap.get(statesData.getEndState()));
		machine.afterPropertiesSet();
		if (beanFactory != null) {
			machine.setBeanFactory(beanFactory);
		}
		return machine;
	}

	private static <S extends Enum<S>, E extends Enum<E>> StateMachine<S, E> buildSubMachine(Map<S, State<S, E>> stateMap, StateMachine<S, E> submachine, StateMachineStatesData<S, E> statesData, StateMachineTransitions<S, E> transitionsData, BeanFactory beanFactory) {
		Collection<State<S, E>> states = new ArrayList<State<S,E>>();
		State<S, E> state = null;
		for (StateData<S, E> stateData : statesData.getStates()) {
			state = new StateMachineState<S, E>(stateData.getState(), submachine, stateData.getDeferred(),
					stateData.getEntryActions(), stateData.getExitActions(), new DefaultPseudoState(
					PseudoStateKind.INITIAL));
			states.add(state);
			stateMap.put(stateData.getState(), state);
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData.getTransitions()) {
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

		EnumStateMachine<S, E> machine = new EnumStateMachine<S, E>(states, transitions, state, null);

		machine.afterPropertiesSet();
		if (beanFactory != null) {
			machine.setBeanFactory(beanFactory);
		}
		return machine;
	}

}
