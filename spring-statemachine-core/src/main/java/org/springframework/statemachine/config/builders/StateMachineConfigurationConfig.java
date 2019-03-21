/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.config.builders;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.listener.StateMachineListener;

/**
 * Configuration object used to keep things together in {@link StateMachineConfigurationBuilder}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineConfigurationConfig<S, E> {

	private final BeanFactory beanFactory;
	private final TaskExecutor taskExecutor;
	private final TaskScheduler taskScheduler;
	private final boolean autoStart;
	private final StateMachineEnsemble<S, E> ensemble;
	private final List<StateMachineListener<S, E>> listeners;

	/**
	 * Instantiates a new state machine configuration config.
	 *
	 * @param beanFactory the bean factory
	 * @param taskExecutor the task executor
	 * @param taskScheduler the task scheduler
	 * @param autoStart the autostart flag
	 * @param ensemble the state machine ensemble
	 * @param listeners the state machine listeners
	 */
	public StateMachineConfigurationConfig(BeanFactory beanFactory, TaskExecutor taskExecutor,
			TaskScheduler taskScheduler, boolean autoStart, StateMachineEnsemble<S, E> ensemble,
			List<StateMachineListener<S, E>> listeners) {
		this.beanFactory = beanFactory;
		this.taskExecutor = taskExecutor;
		this.taskScheduler = taskScheduler;
		this.autoStart = autoStart;
		this.ensemble = ensemble;
		this.listeners = listeners;
	}

	/**
	 * Gets the bean factory.
	 *
	 * @return the bean factory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Gets the task executor.
	 *
	 * @return the task executor
	 */
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	/**
	 * Gets the task scheduler.
	 *
	 * @return the task scheduler
	 */
	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}

	/**
	 * Gets the state machine ensemble.
	 *
	 * @return the state machine ensemble
	 */
	public StateMachineEnsemble<S, E> getStateMachineEnsemble() {
		return ensemble;
	}

	/**
	 * Returns autostart flag.
	 *
	 * @return true, if is autostart is enabled.
	 */
	public boolean isAutoStart() {
		return autoStart;
	}

	/**
	 * Gets the state machine listeners.
	 *
	 * @return the state machine listeners
	 */
	public List<StateMachineListener<S, E>> getStateMachineListeners() {
		return listeners;
	}

}
