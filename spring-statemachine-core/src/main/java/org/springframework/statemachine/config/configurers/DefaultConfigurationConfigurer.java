/*
 * Copyright 2015-2019 the original author or authors.
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
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.config.builders.StateMachineConfigurationBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.region.RegionExecutionPolicy;
import org.springframework.statemachine.transition.TransitionConflictPolicy;

/**
 * Default implementation of a {@link ConfigurationConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultConfigurationConfigurer<S, E>
		extends AnnotationConfigurerAdapter<ConfigurationData<S, E>, StateMachineConfigurationConfigurer<S, E>, StateMachineConfigurationBuilder<S, E>>
		implements ConfigurationConfigurer<S, E> {

	private String machineId;
	private BeanFactory beanFactory;
	private boolean autoStart = false;
	private TransitionConflictPolicy transitionConflightPolicy;
	private StateDoActionPolicy stateDoActionPolicy;
	private Long stateDoActionPolicyTimeout;
	private RegionExecutionPolicy regionExecutionPolicy;
	private final List<StateMachineListener<S, E>> listeners = new ArrayList<StateMachineListener<S, E>>();
	private boolean executeActionsInSyncEnabled = false;

	@Override
	public void configure(StateMachineConfigurationBuilder<S, E> builder) throws Exception {
		builder.setMachineId(machineId);
		builder.setBeanFactory(beanFactory);
		builder.setAutoStart(autoStart);
		builder.setStateMachineListeners(listeners);
		builder.setTransitionConflictPolicy(transitionConflightPolicy);
		builder.setStateDoActionPolicy(stateDoActionPolicy, stateDoActionPolicyTimeout);
		builder.setRegionExecutionPolicy(regionExecutionPolicy);
		builder.setExecuteActionsInSyncEnabled(executeActionsInSyncEnabled);
	}

	@Override
	public ConfigurationConfigurer<S, E> machineId(String id) {
		this.machineId = id;
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> beanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
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

	@Override
	public ConfigurationConfigurer<S, E> transitionConflictPolicy(TransitionConflictPolicy transitionConflightPolicy) {
		this.transitionConflightPolicy = transitionConflightPolicy;
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> stateDoActionPolicy(StateDoActionPolicy stateDoActionPolicy) {
		this.stateDoActionPolicy = stateDoActionPolicy;
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> stateDoActionPolicyTimeout(long timeout, TimeUnit unit) {
		this.stateDoActionPolicyTimeout = unit.toMillis(timeout);
		return this;
	}

	@Override
	public ConfigurationConfigurer<S, E> regionExecutionPolicy(RegionExecutionPolicy regionExecutionPolicy) {
		this.regionExecutionPolicy = regionExecutionPolicy;
		return this;
	}


	@Override
	public ConfigurationConfigurer<S, E> executeActionsInSyncEnabled(boolean executeActionsInSync) {
		this.executeActionsInSyncEnabled = executeActionsInSync;
		return this;
	}
}
