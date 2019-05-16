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
package org.springframework.statemachine.config.builders;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.statemachine.config.configurers.ConfigurationConfigurer;
import org.springframework.statemachine.config.configurers.DefaultConfigurationConfigurer;
import org.springframework.statemachine.config.configurers.DefaultDistributedStateMachineConfigurer;
import org.springframework.statemachine.config.configurers.DefaultMonitoringConfigurer;
import org.springframework.statemachine.config.configurers.DefaultPersistenceConfigurer;
import org.springframework.statemachine.config.configurers.DefaultSecurityConfigurer;
import org.springframework.statemachine.config.configurers.DefaultVerifierConfigurer;
import org.springframework.statemachine.config.configurers.DistributedStateMachineConfigurer;
import org.springframework.statemachine.config.configurers.MonitoringConfigurer;
import org.springframework.statemachine.config.configurers.PersistenceConfigurer;
import org.springframework.statemachine.config.configurers.SecurityConfigurer;
import org.springframework.statemachine.config.configurers.VerifierConfigurer;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.verifier.StateMachineModelVerifier;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.TransitionConflictPolicy;

/**
 * {@link AnnotationBuilder} for {@link StatesData}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineConfigurationBuilder<S, E>
		extends AbstractConfiguredAnnotationBuilder<ConfigurationData<S, E>, StateMachineConfigurationConfigurer<S, E>, StateMachineConfigurationBuilder<S, E>>
		implements StateMachineConfigurationConfigurer<S, E> {

	private String machineId;
	private BeanFactory beanFactory;
	private TaskExecutor taskExecutor;
	private TaskScheduler taskScheculer;
	private boolean autoStart = false;
	private TransitionConflictPolicy transitionConflictPolicy;
	private StateDoActionPolicy stateDoActionPolicy;
	private Long stateDoActionPolicyTimeout;
	private StateMachineEnsemble<S, E> ensemble;
	private final List<StateMachineListener<S, E>> listeners = new ArrayList<StateMachineListener<S, E>>();
	private boolean securityEnabled = false;
	private boolean verifierEnabled = true;
	private StateMachineModelVerifier<S, E> verifier;
	private AccessDecisionManager transitionSecurityAccessDecisionManager;
	private AccessDecisionManager eventSecurityAccessDecisionManager;
	private SecurityRule eventSecurityRule;
	private SecurityRule transitionSecurityRule;
	private StateMachineMonitor<S, E> stateMachineMonitor;
	private final List<StateMachineInterceptor<S, E>> interceptors = new ArrayList<StateMachineInterceptor<S, E>>();
	private StateMachineRuntimePersister<S, E, ?> persister;

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
	public SecurityConfigurer<S, E> withSecurity() throws Exception {
		return apply(new DefaultSecurityConfigurer<S, E>());
	}

	@Override
	public VerifierConfigurer<S, E> withVerifier() throws Exception {
		return apply(new DefaultVerifierConfigurer<S, E>());
	}

	@Override
	public MonitoringConfigurer<S, E> withMonitoring() throws Exception {
		return apply(new DefaultMonitoringConfigurer<S, E>());
	}

	@Override
	public PersistenceConfigurer<S, E> withPersistence() throws Exception {
		return apply(new DefaultPersistenceConfigurer<S, E>());
	}

	@Override
	protected ConfigurationData<S, E> performBuild() throws Exception {
		ArrayList<StateMachineInterceptor<S, E>> interceptorsCopy = new ArrayList<StateMachineInterceptor<S, E>>(interceptors);
		if (persister != null) {
			StateMachineInterceptor<S, E> interceptor = persister.getInterceptor();
			if (interceptor != null) {
				interceptorsCopy.add((StateMachineInterceptor<S, E>) interceptor);
			}
		}
		return new ConfigurationData<S, E>(beanFactory, taskExecutor, taskScheculer, autoStart, ensemble, listeners,
				securityEnabled, transitionSecurityAccessDecisionManager, eventSecurityAccessDecisionManager,
				eventSecurityRule, transitionSecurityRule, verifierEnabled, verifier, machineId, stateMachineMonitor,
				interceptorsCopy, transitionConflictPolicy, stateDoActionPolicy, stateDoActionPolicyTimeout);
	}

	/**
	 * Sets the machine id.
	 *
	 * @param machineId the new machine id
	 */
	public void setMachineId(String machineId) {
		this.machineId = machineId;
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

	/**
	 * Sets the security enabled.
	 *
	 * @param securityEnabled the new security enabled
	 */
	public void setSecurityEnabled(boolean securityEnabled) {
		this.securityEnabled = securityEnabled;
	}

	/**
	 * Sets the verifier enabled.
	 *
	 * @param verifierEnabled the new verifier enabled
	 */
	public void setVerifierEnabled(boolean verifierEnabled) {
		this.verifierEnabled = verifierEnabled;
	}

	/**
	 * Sets the state machine monitor.
	 *
	 * @param stateMachineMonitor the state machine monitor
	 */
	public void setStateMachineMonitor(StateMachineMonitor<S, E> stateMachineMonitor) {
		this.stateMachineMonitor = stateMachineMonitor;
	}

	/**
	 * Sets the security transition access decision manager.
	 *
	 * @param transitionSecurityAccessDecisionManager the new security transition access decision manager
	 */
	public void setTransitionSecurityAccessDecisionManager(AccessDecisionManager transitionSecurityAccessDecisionManager) {
		this.transitionSecurityAccessDecisionManager = transitionSecurityAccessDecisionManager;
	}

	/**
	 * Sets the security event access decision manager.
	 *
	 * @param eventSecurityAccessDecisionManager the new security event access decision manager
	 */
	public void setEventSecurityAccessDecisionManager(AccessDecisionManager eventSecurityAccessDecisionManager) {
		this.eventSecurityAccessDecisionManager = eventSecurityAccessDecisionManager;
	}

	/**
	 * Sets the event security rule.
	 *
	 * @param eventSecurityRule the new event security rule
	 */
	public void setEventSecurityRule(SecurityRule eventSecurityRule) {
		this.eventSecurityRule = eventSecurityRule;
	}

	/**
	 * Sets the transition security rule.
	 *
	 * @param transitionSecurityRule the new event security rule
	 */
	public void setTransitionSecurityRule(SecurityRule transitionSecurityRule) {
		this.transitionSecurityRule = transitionSecurityRule;
	}

	/**
	 * Sets the state machine model verifier.
	 *
	 * @param verifier the state machine model verifier
	 */
	public void setVerifier(StateMachineModelVerifier<S, E> verifier) {
		this.verifier = verifier;
	}

	/**
	 * Sets the state machine runtime persister.
	 *
	 * @param persister the state machine runtime persister
	 */
	public void setStateMachineRuntimePersister(StateMachineRuntimePersister<S, E, ?> persister) {
		this.persister = persister;
	}

	/**
	 * Sets the transition conflict policy.
	 *
	 * @param transitionConflictPolicy the new transition conflict policy
	 */
	public void setTransitionConflictPolicy(TransitionConflictPolicy transitionConflictPolicy) {
		this.transitionConflictPolicy = transitionConflictPolicy;
	}

	/**
	 * Sets the state do action policy and timeout.
	 *
	 * @param stateDoActionPolicy the new state do action policy
	 * @param stateDoActionPolicyTimeout the new state do action policy timeout
	 */
	public void setStateDoActionPolicy(StateDoActionPolicy stateDoActionPolicy, Long stateDoActionPolicyTimeout) {
		this.stateDoActionPolicy = stateDoActionPolicy;
		this.stateDoActionPolicyTimeout = stateDoActionPolicyTimeout;
	}
}
