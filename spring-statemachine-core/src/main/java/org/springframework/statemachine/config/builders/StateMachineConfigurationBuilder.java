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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.statemachine.config.configurers.ConfigurationConfigurer;
import org.springframework.statemachine.config.configurers.DefaultConfigurationConfigurer;
import org.springframework.statemachine.config.configurers.DefaultDistributedStateMachineConfigurer;
import org.springframework.statemachine.config.configurers.DistributedStateMachineConfigurer;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.listener.StateMachineListener;

/**
 * {@link AnnotationBuilder} for {@link StateMachineStates}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineConfigurationBuilder<S, E>
		extends AbstractConfiguredAnnotationBuilder<StateMachineConfigurationConfig<S, E>, StateMachineConfigurationConfigurer<S, E>, StateMachineConfigurationBuilder<S, E>>
		implements StateMachineConfigurationConfigurer<S, E> {

	private BeanFactory beanFactory;
	private TaskExecutor taskExecutor;
	private TaskScheduler taskScheculer;
	private boolean autoStart = false;
	private StateMachineEnsemble<S, E> ensemble;
	private final List<StateMachineListener<S, E>> listeners = new ArrayList<StateMachineListener<S, E>>();

	/**
	 * Instantiates a new state machine configuration builder.
	 */
	public StateMachineConfigurationBuilder() {
		super();
	}

	/**
	 * Instantiates a new state machine configuration builder.
	 *
	 * @param objectPostProcessor the object post processor
	 * @param allowConfigurersOfSameType the allow configurers of same type
	 */
	public StateMachineConfigurationBuilder(ObjectPostProcessor<Object> objectPostProcessor,
			boolean allowConfigurersOfSameType) {
		super(objectPostProcessor, allowConfigurersOfSameType);
	}

	/**
	 * Instantiates a new state machine configuration builder.
	 *
	 * @param objectPostProcessor the object post processor
	 */
	public StateMachineConfigurationBuilder(ObjectPostProcessor<Object> objectPostProcessor) {
		super(objectPostProcessor);
	}

	@Override
	public ConfigurationConfigurer<S, E> withConfiguration() throws Exception {
		return apply(new DefaultConfigurationConfigurer<S, E>());
	}

	@Override
	public DistributedStateMachineConfigurer<S, E> withDistributed() throws Exception {
		return apply(new DefaultDistributedStateMachineConfigurer<S, E>());
	}

	@Override
	protected StateMachineConfigurationConfig<S, E> performBuild() throws Exception {
		return new StateMachineConfigurationConfig<>(beanFactory, taskExecutor, taskScheculer, autoStart, ensemble, listeners);
	}

	/**
	 * Sets the bean factory.
	 *
	 * @param beanFactory the new bean factory
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets the task executor.
	 *
	 * @param taskExecutor the new task executor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Sets the task scheculer.
	 *
	 * @param taskScheculer the new task scheculer
	 */
	public void setTaskScheculer(TaskScheduler taskScheculer) {
		this.taskScheculer = taskScheculer;
	}

	/**
	 * Sets the state machine ensemble.
	 *
	 * @param ensemble the ensemble
	 */
	public void setStateMachineEnsemble(StateMachineEnsemble<S, E> ensemble) {
		this.ensemble = ensemble;
	}

	/**
	 * Sets the auto start.
	 *
	 * @param autoStart the new autostart flag
	 */
	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	/**
	 * Sets the state machine listeners.
	 *
	 * @param listeners the listeners
	 */
	public void setStateMachineListeners(List<StateMachineListener<S, E>> listeners) {
		this.listeners.clear();
		this.listeners.addAll(listeners);
	}

}
