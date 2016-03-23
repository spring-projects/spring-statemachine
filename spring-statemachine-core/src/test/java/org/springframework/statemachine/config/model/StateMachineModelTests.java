/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.config.model;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.ObjectStateMachineFactory;
import org.springframework.statemachine.config.model.verifier.DefaultStateMachineModelVerifier;
import org.springframework.statemachine.config.model.verifier.StateMachineModelVerifier;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.transition.TransitionKind;

public class StateMachineModelTests {

	@Test
	public void testMachineManuallyViaModel() {
		BeanFactory beanFactory = null;
		TaskExecutor taskExecutor = new SyncTaskExecutor();
		TaskScheduler taskScheduler = null;
		boolean autoStart = false;
		StateMachineEnsemble<String, String> ensemble = null;
		List<StateMachineListener<String, String>> listeners = new ArrayList<>();
		boolean securityEnabled = false;
		AccessDecisionManager transitionSecurityAccessDecisionManager = null;
		AccessDecisionManager eventSecurityAccessDecisionManager = null;
		SecurityRule eventSecurityRule = null;
		SecurityRule transitionSecurityRule = null;
		boolean verifierEnabled = true;
		StateMachineModelVerifier<String, String> verifier = new DefaultStateMachineModelVerifier<>();

		ConfigurationData<String, String> configurationData = new ConfigurationData<>(beanFactory, taskExecutor, taskScheduler, autoStart,
				ensemble, listeners, securityEnabled, transitionSecurityAccessDecisionManager, eventSecurityAccessDecisionManager,
				eventSecurityRule, transitionSecurityRule, verifierEnabled, verifier, null);

		Collection<StateData<String, String>> stateData = new ArrayList<>();
		StateData<String, String> stateData1 = new StateData<String, String>(null, null, "S1", null, null, null);
		stateData1.setInitial(true);
		stateData.add(stateData1);
		StateData<String, String> stateData2 = new StateData<String, String>(null, null, "S2", null, null, null);
		stateData.add(stateData2);
		StatesData<String, String> statesData = new StatesData<>(stateData);


		Collection<TransitionData<String, String>> transitions = new ArrayList<>();
		TransitionData<String, String> transitionData1 = new TransitionData<String, String>("S1", "S2", null, "E1", null, null, null, null, TransitionKind.EXTERNAL, null);
		transitions.add(transitionData1);
		Map<String, List<ChoiceData<String, String>>> choices = new HashMap<>();
		Map<String, List<String>> forks = new HashMap<>();
		Map<String, List<String>> joins = new HashMap<>();
		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitions, choices, forks, joins);

		StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);
		ObjectStateMachineFactory<String, String> factory = new ObjectStateMachineFactory<>(stateMachineModel);

		StateMachine<String,String> stateMachine = factory.getStateMachine();
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

	@Test
	public void testMinimalConfig() {
		ConfigurationData<String, String> configurationData = new ConfigurationData<>();

		Collection<StateData<String, String>> stateData = new ArrayList<>();
		stateData.add(new StateData<String, String>("S1", true));
		stateData.add(new StateData<String, String>("S2"));
		StatesData<String, String> statesData = new StatesData<>(stateData);

		Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
		transitionData.add(new TransitionData<String, String>("S1", "S2", "E1"));
		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

		StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);
		ObjectStateMachineFactory<String, String> factory = new ObjectStateMachineFactory<>(stateMachineModel);

		StateMachine<String,String> stateMachine = factory.getStateMachine();
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}
}
