/*
 * Copyright 2015-2018 the original author or authors.
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
import java.util.UUID;

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
import org.springframework.statemachine.config.model.StateMachineModelFactory;
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
	 * @param defaultStateMachineModel the default state machine model
	 */
	public ObjectStateMachineFactory(StateMachineModel<S, E> defaultStateMachineModel) {
		this(defaultStateMachineModel, null);
	}

	/**
	 * Instantiates a new object state machine factory.
	 *
	 * @param defaultStateMachineModel the default state machine model
	 * @param stateMachineModelFactory the state machine model factory
	 */
	public ObjectStateMachineFactory(StateMachineModel<S, E> defaultStateMachineModel,
			StateMachineModelFactory<S, E> stateMachineModelFactory) {
		super(defaultStateMachineModel, stateMachineModelFactory);
	}

	@Override
	protected StateMachine<S, E> buildStateMachineInternal(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, Transition<S, E> initialTransition, Message<E> initialEvent, ExtendedState extendedState,
			PseudoState<S, E> historyState, Boolean contextEventsEnabled, BeanFactory beanFactory, TaskExecutor taskExecutor,
			TaskScheduler taskScheduler, String beanName, String machineId, UUID uuid, StateMachineModel<S, E> stateMachineModel) {
		ObjectStateMachine<S, E> machine = new ObjectStateMachine<S, E>(states, transitions, initialState, initialTransition, initialEvent,
				extendedState, uuid);
		machine.setId(machineId);
		machine.setHistoryState(historyState);
		machine.setTransitionConflightPolicy(stateMachineModel.getConfigurationData().getTransitionConflictPolicy());
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
		return machine;
	}

	@Override
	protected State<S, E> buildStateInternal(S id, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			Collection<? extends Action<S, E>> stateActions, PseudoState<S, E> pseudoState, StateMachineModel<S, E> stateMachineModel) {
		ObjectState<S,E> objectState = new ObjectState<S, E>(id, deferred, entryActions, exitActions, stateActions, pseudoState, null, null);
		BeanFactory beanFactory = resolveBeanFactory(stateMachineModel);
		if (beanFactory != null) {
			objectState.setBeanFactory(beanFactory);
		}
		TaskExecutor taskExecutor = resolveTaskExecutor(stateMachineModel);
		if (taskExecutor != null) {
			objectState.setTaskExecutor(taskExecutor);
		}
		TaskScheduler taskScheduler = resolveTaskScheduler(stateMachineModel);
		if (taskScheduler != null) {
			objectState.setTaskScheduler(taskScheduler);
		}
		objectState.setStateDoActionPolicy(stateMachineModel.getConfigurationData().getStateDoActionPolicy());
		objectState.setStateDoActionPolicyTimeout(stateMachineModel.getConfigurationData().getStateDoActionPolicyTimeout());
		return objectState;
	}

	@Override
	protected RegionState<S, E> buildRegionStateInternal(S id, Collection<Region<S, E>> regions, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState<S, E> pseudoState, StateMachineModel<S, E> stateMachineModel) {
		RegionState<S,E> regionState = new RegionState<S, E>(id, regions, deferred, entryActions, exitActions, pseudoState);
		regionState.setStateDoActionPolicy(stateMachineModel.getConfigurationData().getStateDoActionPolicy());
		regionState.setStateDoActionPolicyTimeout(stateMachineModel.getConfigurationData().getStateDoActionPolicyTimeout());
		return regionState;
	}

}
