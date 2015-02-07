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

import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.builders.StateMachineStates.StateData;
import org.springframework.statemachine.config.builders.StateMachineTransitions.TransitionData;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultInternalTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;

/**
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
		return stateMachine();
	}

	public StateMachine<S, E> stateMachine() {
		Map<S, State<S, E>> stateMap = new HashMap<S, State<S, E>>();
		for (StateData<S, E> stateData : stateMachineStates.getStates()) {
			
			// TODO: doesn't feel right to tweak initial kind like this
			PseudoState pseudoState = null;
			if (stateData.getState() == stateMachineStates.getInitialState()) {
				pseudoState = new DefaultPseudoState(PseudoStateKind.INITIAL);
			}
			stateMap.put(stateData.getState(), new EnumState<S, E>(stateData.getState(), stateData.getDeferred(),
					stateData.getEntryActions(), stateData.getExitActions(), pseudoState));
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : stateMachineTransitions.getTransitions()) {
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
				stateMap.get(stateMachineStates.getInitialState()));
		machine.afterPropertiesSet();
		if (getBeanFactory() != null) {
			machine.setBeanFactory(getBeanFactory());
		}
		machine.setAutoStartup(isAutoStartup());
		machine.start();
		return machine;
	}

}
