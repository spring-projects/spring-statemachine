/*
 * Copyright 2015-2016 the original author or authors.
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
package org.springframework.statemachine.config;

import java.util.Collection;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.ObjectState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Implementation of a {@link StateMachineFactory} which know the actual types of
 * {@link State} and {@link StateMachine}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ObjectStateMachineFactory<S, E> extends AbstractStateMachineFactory<S, E> {

	/**
	 * Instantiates a new object state machine factory.
	 *
	 * @param stateMachineModel the state machine model
	 */
	public ObjectStateMachineFactory(StateMachineModel<S, E> stateMachineModel) {
		super(stateMachineModel);
	}

	@Override
	protected StateMachine<S, E> buildStateMachineInternal(Collection<State<S, E>> states,
			Collection<Transition<S, E>> transitions, State<S, E> initialState, Transition<S, E> initialTransition,
			Message<E> initialEvent, ExtendedState extendedState, PseudoState<S, E> historyState,
			Boolean contextEventsEnabled, BeanFactory beanFactory, TaskExecutor taskExecutor,
			TaskScheduler taskScheduler, String beanName, String machineId) {
		ObjectStateMachine<S, E> machine = new ObjectStateMachine<S, E>(states, transitions, initialState, initialTransition, initialEvent,
				extendedState);
		machine.setId(machineId);
		machine.setHistoryState(historyState);
		if (contextEventsEnabled != null) {
			machine.setContextEventsEnabled(contextEventsEnabled);
		}
		if (beanFactory != null) {
			machine.setBeanFactory(beanFactory);
		}
		if (taskExecutor != null) {
			machine.setTaskExecutor(taskExecutor);
		}
		if (taskScheduler != null) {
			machine.setTaskScheduler(taskScheduler);
		}
		if (machine instanceof BeanNameAware) {
			((BeanNameAware)machine).setBeanName(beanName);
		}
		machine.afterPropertiesSet();
		return machine;
	}

	@Override
	protected State<S, E> buildStateInternal(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState) {
		return new ObjectState<S, E>(id, deferred, entryActions, exitActions, pseudoState);
	}

	@Override
	protected RegionState<S, E> buildRegionStateInternal(S id, Collection<Region<S, E>> regions, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState) {
		return new RegionState<S, E>(id, regions, deferred, entryActions, exitActions, pseudoState);
	}

}
