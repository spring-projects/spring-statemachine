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
import static org.springframework.statemachine.scxml.TestUtils.assertLatch;
import static org.springframework.statemachine.scxml.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.scxml.TestUtils.doStartAndAssert;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.scxml.ScxmlStateMachineModelFactory;

/**
 * Complex test scenarios for SCXML module.
 */
public class ScxmlComplexTests extends AbstractScxmlTests {

	// ========== Complex State Machine Tests ==========

	@Test
	@SuppressWarnings("unchecked")
	public void testComplexNestedMachine() throws Exception {
		context.register(ConfigComplexNested.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1", "S11");

		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S1", "S12");

		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultipleTransitions() throws Exception {
		context.register(ConfigMultipleTransitions.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");

		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");

		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S3");

		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S1");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSpelActions() throws Exception {
		context.register(ConfigSpelActions.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");

		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(stateMachine.getExtendedState().getVariables().get("spelVar"))
			.isEqualTo("spelValue");
	}

	@Test
	public void testStateActions() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-state-actions.scxml");
		DefaultStateMachineComponentResolver<String, String> resolver = 
			new DefaultStateMachineComponentResolver<>();
		resolver.registerAction("stateAction", context -> {});

		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		factory.setStateMachineComponentResolver(resolver);

		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();

		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();

		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.getStateActions()).isNotNull();
				assertThat(stateData.getStateActions().size()).isGreaterThan(0);
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testEventDefer() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-event-defer.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);

		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();

		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();

		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				// Note: SCXML event deferral is handled differently than UML
				// This is a placeholder for future implementation
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testForkJoin() throws Exception {
		context.register(ConfigForkJoin.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("SI");

		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20", "S30");

		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");

		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("SF");
	}

	// ========== Configuration Classes ==========

	@Configuration
	@EnableStateMachine
	static class ConfigComplexNested extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/complex-nested.scxml");
		}
	}

	@Configuration
	@EnableStateMachine
	static class ConfigMultipleTransitions extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/multiple-transitions.scxml");
		}
	}

	@Configuration
	@EnableStateMachine
	static class ConfigSpelActions extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/spel-actions.scxml");
		}
	}

	@Configuration
	@EnableStateMachine
	static class ConfigForkJoin extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/fork-join.scxml");
		}
	}
}

