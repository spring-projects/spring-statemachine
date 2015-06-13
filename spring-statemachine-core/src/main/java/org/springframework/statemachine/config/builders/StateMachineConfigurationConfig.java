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

public class StateMachineConfigurationConfig<S, E> {

	private final BeanFactory beanFactory;
	private final TaskExecutor taskExecutor;
	private final TaskScheduler taskScheculer;

	public StateMachineConfigurationConfig(BeanFactory beanFactory, TaskExecutor taskExecutor,
			TaskScheduler taskScheculer) {
		this.beanFactory = beanFactory;
		this.taskExecutor = taskExecutor;
		this.taskScheculer = taskScheculer;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public TaskScheduler getTaskScheculer() {
		return taskScheculer;
	}

}
