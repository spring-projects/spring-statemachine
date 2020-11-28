/*
 * Copyright 2016-2020 the original author or authors.
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
package org.springframework.statemachine.config.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
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

		ConfigurationData<String, String> configurationData = new ConfigurationData<>(beanFactory, autoStart, ensemble,
				listeners, securityEnabled, transitionSecurityAccessDecisionManager, eventSecurityAccessDecisionManager,
				eventSecurityRule, transitionSecurityRule, verifierEnabled, verifier, null, null, null);

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
		Map<String, List<JunctionData<String, String>>> junctions = new HashMap<>();
		Map<String, List<String>> forks = new HashMap<>();
		Map<String, List<String>> joins = new HashMap<>();
		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitions, choices, junctions, forks, joins, null, null, null);

		StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);
		ObjectStateMachineFactory<String, String> factory = new ObjectStateMachineFactory<>(stateMachineModel);

		StateMachine<String,String> stateMachine = factory.getStateMachine();
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).containsExactly("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).containsExactly("S2");
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
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).containsExactly("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).containsExactly("S2");
	}

	@Test
	public void testSubmachineRefConfig() {
		// *S1     S2
		//        /  \
		//     *S20   S21
		//           /   \
		//        *S30   S31
		ConfigurationData<String, String> configurationData = new ConfigurationData<>();

		Collection<StateData<String, String>> stateData2 = new ArrayList<>();
		stateData2.add(new StateData<String, String>("S2", null, "S20", true));
		stateData2.add(new StateData<String, String>("S2", null, "S21", false));
		stateData2.add(new StateData<String, String>("S21", null, "S30", true));
		stateData2.add(new StateData<String, String>("S21", null, "S31", false));

		Collection<StateData<String, String>> stateData1 = new ArrayList<>();
		stateData1.add(new StateData<String, String>("S1", true));
		StateData<String, String> stateDataS2 = new StateData<String, String>("S2");
		stateDataS2.setSubmachineStateData(stateData2);
		stateData1.add(stateDataS2);
		StatesData<String, String> statesData = new StatesData<>(stateData1);

		Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
		transitionData.add(new TransitionData<String, String>("S1", "S2", "E1"));
		transitionData.add(new TransitionData<String, String>("S20", "S21", "E2"));
		transitionData.add(new TransitionData<String, String>("S30", "S31", "E3"));
		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

		StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);

		ObjectStateMachineFactory<String, String> factory = new ObjectStateMachineFactory<>(stateMachineModel);

		StateMachine<String,String> stateMachine = factory.getStateMachine();
		doStartAndAssert(stateMachine);

		assertThat(stateMachine.getState().getIds()).containsOnly("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).containsOnly("S2", "S20");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).containsOnly("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).containsOnly("S2", "S21", "S31");
	}
}
