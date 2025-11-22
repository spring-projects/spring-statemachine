/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.statemachine.scxml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.scxml.ScxmlStateMachineModelFactory;

/**
 * Basic validation test to verify SCXML parsing works correctly.
 */
public class ScxmlBasicValidationTest {

	@Test
	public void testBasicParsing() {
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-flat.scxml");
		assertThat(scxmlResource.exists()).isTrue();
		
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		StateMachineModel<String, String> model = factory.build();
		
		assertThat(model).isNotNull();
		assertThat(model.getStatesData()).isNotNull();
		assertThat(model.getTransitionsData()).isNotNull();
		
		Collection<StateData<String, String>> states = model.getStatesData().getStateData();
		assertThat(states.size()).isGreaterThan(0);
		
		// Verify S1 is initial
		boolean foundS1 = false;
		for (StateData<String, String> state : states) {
			if ("S1".equals(state.getState())) {
				foundS1 = true;
				assertThat(state.isInitial()).isTrue();
			}
		}
		assertThat(foundS1).isTrue();
	}
}

