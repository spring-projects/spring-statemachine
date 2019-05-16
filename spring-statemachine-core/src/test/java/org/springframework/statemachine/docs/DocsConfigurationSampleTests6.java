/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.docs;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.ObjectStateMachineFactory;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;

public class DocsConfigurationSampleTests6 {

	@Test
	public void testMinimalConfig() {
// tag::snippetA[]
		// setup configuration data
		ConfigurationData<String, String> configurationData = new ConfigurationData<>();

		// setup states data
		Collection<StateData<String, String>> stateData = new ArrayList<>();
		stateData.add(new StateData<String, String>("S1", true));
		stateData.add(new StateData<String, String>("S2"));
		StatesData<String, String> statesData = new StatesData<>(stateData);

		// setup transitions data
		Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
		transitionData.add(new TransitionData<String, String>("S1", "S2", "E1"));
		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

		// setup model
		StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData,
				transitionsData);

		// instantiate machine via factory
		ObjectStateMachineFactory<String, String> factory = new ObjectStateMachineFactory<>(stateMachineModel);
		StateMachine<String, String> stateMachine = factory.getStateMachine();
// end::snippetA[]
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

}
