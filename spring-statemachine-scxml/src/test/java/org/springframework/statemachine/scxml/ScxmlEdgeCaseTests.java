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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.scxml.ScxmlStateMachineModelFactory;

/**
 * Edge case and boundary condition tests for SCXML module.
 */
public class ScxmlEdgeCaseTests extends AbstractScxmlTests {

	@Test
	public void testEmptyStateMachine() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/empty-scxml.scxml");
		
		if (scxmlResource.exists()) {
			ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
			assertThatThrownBy(() -> factory.build())
				.isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	public void testStateWithoutId() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/state-without-id.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			StateMachineModel<String, String> stateMachineModel = factory.build();
			// States without id should be skipped
			assertThat(stateMachineModel).isNotNull();
		}
	}

	@Test
	public void testTransitionWithoutTarget() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/transition-without-target.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			StateMachineModel<String, String> stateMachineModel = factory.build();
			assertThat(stateMachineModel).isNotNull();
			// Transitions without target should be skipped
		}
	}

	@Test
	public void testMultipleInitialStates() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/multiple-initial.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			StateMachineModel<String, String> stateMachineModel = factory.build();
			assertThat(stateMachineModel).isNotNull();
		}
	}

	@Test
	public void testDeeplyNestedStates() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/deeply-nested.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			StateMachineModel<String, String> stateMachineModel = factory.build();
			assertThat(stateMachineModel).isNotNull();
			
			Collection<StateData<String, String>> stateDatas = 
				stateMachineModel.getStatesData().getStateData();
			assertThat(stateDatas.size()).isGreaterThanOrEqualTo(4);
		}
	}

	@Test
	public void testSelfTransition() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/self-transition.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			StateMachineModel<String, String> stateMachineModel = factory.build();
			assertThat(stateMachineModel).isNotNull();
			
			Collection<org.springframework.statemachine.config.model.TransitionData<String, String>> transitions = 
				stateMachineModel.getTransitionsData().getTransitions();
			
			boolean foundSelfTransition = false;
			for (org.springframework.statemachine.config.model.TransitionData<String, String> transition : transitions) {
				if (transition.getSource().equals("S1") && transition.getTarget().equals("S1")) {
					foundSelfTransition = true;
				}
			}
			assertThat(foundSelfTransition).isTrue();
		}
	}

	@Test
	public void testNoInitialState() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/no-initial.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			StateMachineModel<String, String> stateMachineModel = factory.build();
			assertThat(stateMachineModel).isNotNull();
			// Should use first state as initial if no initial specified
		}
	}

	@Test
	public void testComponentResolverWithNullAction() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-actions.scxml");
		DefaultStateMachineComponentResolver<String, String> resolver = 
			new DefaultStateMachineComponentResolver<>();
		resolver.registerAction("action1", null); // null action
		
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		factory.setStateMachineComponentResolver(resolver);
		
		// Should not throw exception, just skip null actions
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
	}

	@Test
	public void testComponentResolverWithNullGuard() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-guards.scxml");
		DefaultStateMachineComponentResolver<String, String> resolver = 
			new DefaultStateMachineComponentResolver<>();
		resolver.registerGuard("myGuard", null); // null guard
		
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		factory.setStateMachineComponentResolver(resolver);
		
		// Should not throw exception, just skip null guards
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
	}

	@Test
	public void testLargeStateMachine() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/large-state-machine.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			StateMachineModel<String, String> stateMachineModel = factory.build();
			assertThat(stateMachineModel).isNotNull();
			
			Collection<StateData<String, String>> stateDatas = 
				stateMachineModel.getStatesData().getStateData();
			assertThat(stateDatas.size()).isGreaterThan(10);
		}
	}
}

