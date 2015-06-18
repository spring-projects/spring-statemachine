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
package org.springframework.statemachine.config.builders;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;

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
	private final TaskScheduler taskScheculer;
	private final StateMachineEnsemble<S, E> ensemble;

	/**
	 * Instantiates a new state machine configuration config.
	 *
	 * @param beanFactory the bean factory
	 * @param taskExecutor the task executor
	 * @param taskScheculer the task scheculer
	 * @param ensemble the state machine ensemble
	 */
	public StateMachineConfigurationConfig(BeanFactory beanFactory, TaskExecutor taskExecutor,
			TaskScheduler taskScheculer, StateMachineEnsemble<S, E> ensemble) {
		this.beanFactory = beanFactory;
		this.taskExecutor = taskExecutor;
		this.taskScheculer = taskScheculer;
		this.ensemble = ensemble;
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
	 * Gets the task scheculer.
	 *
	 * @return the task scheculer
	 */
	public TaskScheduler getTaskScheculer() {
		return taskScheculer;
	}

	/**
	 * Gets the state machine ensemble.
	 *
	 * @return the state machine ensemble
	 */
	public StateMachineEnsemble<S, E> getStateMachineEnsemble() {
		return ensemble;
	}

}
