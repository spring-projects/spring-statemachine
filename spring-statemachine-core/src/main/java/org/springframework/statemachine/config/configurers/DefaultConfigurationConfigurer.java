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
package org.springframework.statemachine.config.configurers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.config.builders.StateMachineConfigurationBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfig;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.listener.StateMachineListener;

/**
 * Default implementation of a {@link ConfigurationConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultConfigurationConfigurer<S, E>
		extends AnnotationConfigurerAdapter<StateMachineConfigurationConfig<S, E>, StateMachineConfigurationConfigurer<S, E>, StateMachineConfigurationBuilder<S, E>>
		implements ConfigurationConfigurer<S, E> {

	private BeanFactory beanFactory;
	private TaskExecutor taskExecutor;
	private TaskScheduler taskScheculer;
	private boolean autoStart = false;
	private final List<StateMachineListener<S, E>> listeners = new ArrayList<StateMachineListener<S, E>>();

	@Override
	public void configure(StateMachineConfigurationBuilder<S, E> builder) throws Exception {
		builder.setBeanFactory(beanFactory);
		builder.setTaskExecutor(taskExecutor);
		builder.setTaskScheculer(taskScheculer);
		builder.setAutoStart(autoStart);
		builder.setStateMachineListeners(listeners);
	}

	@Override
	public ConfigurationConfigurer<S, E> beanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> taskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> taskScheduler(TaskScheduler taskScheduler) {
		this.taskScheculer = taskScheduler;
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> autoStartup(boolean autoStart) {
		this.autoStart = autoStart;
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> listener(StateMachineListener<S, E> listener) {
		this.listeners.add(listener);
		return this;
	}

}
