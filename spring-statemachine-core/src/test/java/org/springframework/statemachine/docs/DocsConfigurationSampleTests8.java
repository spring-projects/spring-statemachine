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

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;

public class DocsConfigurationSampleTests8 {

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new CustomStateMachineModelFactory();
		}
	}
// end::snippetA[]

// tag::snippetB[]
	public static class CustomStateMachineModelFactory implements StateMachineModelFactory<String, String> {

		@Override
		public StateMachineModel<String, String> build() {
			ConfigurationData<String, String> configurationData = new ConfigurationData<>();
			Collection<StateData<String, String>> stateData = new ArrayList<>();
			stateData.add(new StateData<String, String>("S1", true));
			stateData.add(new StateData<String, String>("S2"));
			StatesData<String, String> statesData = new StatesData<>(stateData);
			Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
			transitionData.add(new TransitionData<String, String>("S1", "S2", "E1"));
			TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);
			StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<String, String>(configurationData,
					statesData, transitionsData);
			return stateMachineModel;
		}

		@Override
		public StateMachineModel<String, String> build(String machineId) {
			return build();
		}
	}
// end::snippetB[]
}
