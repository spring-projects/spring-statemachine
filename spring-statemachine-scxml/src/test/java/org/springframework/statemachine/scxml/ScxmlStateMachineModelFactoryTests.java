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
import static org.springframework.statemachine.scxml.TestUtils.assertLatch;
import static org.springframework.statemachine.scxml.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.scxml.TestUtils.doStartAndAssert;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.scxml.ScxmlStateMachineModelFactory;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.transition.TransitionKind;

public class ScxmlStateMachineModelFactoryTests extends AbstractScxmlTests {

	// ========== Basic Model Tests ==========

	@Test
	public void testSimpleFlat() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-flat.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		assertThat(scxmlResource.exists()).isTrue();
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(2);
		
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			}
		}
	}

	@Test
	public void testSimpleFlatWithLocation() {
		context.refresh();
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(
			"classpath:org/springframework/statemachine/scxml/simple-flat.scxml");
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		assertThat(stateMachineModel.getStatesData().getStateData().size()).isEqualTo(2);
	}

	@Test
	public void testSimpleFinal() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-final.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(3);
		
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("Final")) {
				assertThat(stateData.isEnd()).isTrue();
			}
		}
	}

	@Test
	public void testNestedStates() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-nested.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isGreaterThanOrEqualTo(4);
		
		boolean foundS1 = false, foundS11 = false, foundS12 = false;
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				foundS1 = true;
				assertThat(stateData.getParent()).isNull();
			} else if (stateData.getState().equals("S11")) {
				foundS11 = true;
				assertThat(stateData.getParent()).isEqualTo("S1");
			} else if (stateData.getState().equals("S12")) {
				foundS12 = true;
				assertThat(stateData.getParent()).isEqualTo("S1");
			}
		}
		assertThat(foundS1 && foundS11 && foundS12).isTrue();
	}

	// ========== Transition Type Tests ==========

	@Test
	public void testExternalTransition() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-transition-types.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<org.springframework.statemachine.config.model.TransitionData<String, String>> transitions = 
			stateMachineModel.getTransitionsData().getTransitions();
		
		boolean foundExternal = false;
		for (org.springframework.statemachine.config.model.TransitionData<String, String> transition : transitions) {
			if (transition.getSource().equals("S1") && transition.getTarget().equals("S2")) {
				foundExternal = true;
				assertThat(transition.getKind()).isEqualTo(TransitionKind.EXTERNAL);
			}
		}
		assertThat(foundExternal).isTrue();
	}

	@Test
	public void testInternalTransition() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-transition-types.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		
		Collection<org.springframework.statemachine.config.model.TransitionData<String, String>> transitions = 
			stateMachineModel.getTransitionsData().getTransitions();
		
		boolean foundInternal = false;
		for (org.springframework.statemachine.config.model.TransitionData<String, String> transition : transitions) {
			if (transition.getSource().equals("S2") && transition.getTarget().equals("S2") 
					&& transition.getEvent().equals("E2")) {
				foundInternal = true;
				assertThat(transition.getKind()).isEqualTo(TransitionKind.INTERNAL);
			}
		}
		assertThat(foundInternal).isTrue();
	}

	@Test
	public void testLocalTransition() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-transition-types.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		
		Collection<org.springframework.statemachine.config.model.TransitionData<String, String>> transitions = 
			stateMachineModel.getTransitionsData().getTransitions();
		
		boolean foundLocal = false;
		for (org.springframework.statemachine.config.model.TransitionData<String, String> transition : transitions) {
			// Local transition is from S31 to S31 (self-transition within nested state)
			if (transition.getSource().equals("S31") && transition.getTarget().equals("S31") 
					&& transition.getEvent().equals("E3")) {
				foundLocal = true;
				assertThat(transition.getKind()).isEqualTo(TransitionKind.LOCAL);
			}
		}
		assertThat(foundLocal).isTrue();
	}

	// ========== Action Tests ==========

	@Test
	public void testEntryActions() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-actions.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.getEntryActions()).isNotNull();
				assertThat(stateData.getEntryActions().size()).isGreaterThan(0);
			}
		}
	}

	@Test
	public void testExitActions() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-actions.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S2")) {
				assertThat(stateData.getExitActions()).isNotNull();
				assertThat(stateData.getExitActions().size()).isGreaterThan(0);
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionsExecution() throws Exception {
		context.register(ConfigActions.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		TestUtils.LatchAction entryAction = context.getBean("entryAction", TestUtils.LatchAction.class);
		TestUtils.LatchAction exitAction = context.getBean("exitAction", TestUtils.LatchAction.class);
		TestUtils.LatchAction transitionAction = context.getBean("transitionAction", TestUtils.LatchAction.class);
		
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		
		// Wait for actions to execute
		assertLatch(exitAction.latch, 5);
		assertLatch(entryAction.latch, 5);
		assertLatch(transitionAction.latch, 5);
	}

	// ========== Guard Tests ==========

	@Test
	public void testGuardsWithBeanReference() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-guards.scxml");
		DefaultStateMachineComponentResolver<String, String> resolver = 
			new DefaultStateMachineComponentResolver<>();
		resolver.registerGuard("myGuard", new TestUtils.TestGuard(true));
		
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		factory.setStateMachineComponentResolver(resolver);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
	}

	@Test
	public void testGuardsWithSpelExpression() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-guards-spel.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGuardsExecution() throws Exception {
		context.register(ConfigGuards.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		
		// Guard returns true, should go to S2
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		
		stateMachine.stopReactively().block();
		stateMachine.startReactively().block();
		
		// Guard returns false, should go to S3
		stateMachine.getExtendedState().getVariables().put("allow", false);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	// ========== Parallel States Tests ==========

	@Test
	public void testParallelStates() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-parallel.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		
		boolean foundParallel = false;
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("ParallelState")) {
				foundParallel = true;
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.FORK);
			}
		}
		assertThat(foundParallel).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testParallelStatesExecution() throws Exception {
		context.register(ConfigParallel.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S3");
	}

	// ========== History States Tests ==========

	@Test
	public void testHistoryShallow() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-history-shallow.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		
		boolean foundHistory = false;
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("History")) {
				foundHistory = true;
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.HISTORY_SHALLOW);
			}
		}
		assertThat(foundHistory).isTrue();
	}

	@Test
	public void testHistoryDeep() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-history-deep.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<StateData<String, String>> stateDatas = 
			stateMachineModel.getStatesData().getStateData();
		
		boolean foundHistory = false;
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("History")) {
				foundHistory = true;
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.HISTORY_DEEP);
			}
		}
		assertThat(foundHistory).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testHistoryShallowExecution() throws Exception {
		context.register(ConfigHistoryShallow.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S1");
		
		// Should return to last state in S2 (S22)
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
	}

	// ========== Choice/Junction Tests ==========

	@Test
	public void testChoice() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-choice.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
		
		Collection<org.springframework.statemachine.config.model.ChoiceData<String, String>> choices = 
			stateMachineModel.getTransitionsData().getChoices().values().iterator().next();
		assertThat(choices).isNotNull();
		assertThat(choices.size()).isGreaterThan(0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testChoiceExecution() throws Exception {
		context.register(ConfigChoice.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		
		// Guard returns true, should go to S2
		stateMachine.getExtendedState().getVariables().put("choice", "s2");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1")
			.setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
		
		stateMachine.stopReactively().block();
		stateMachine.startReactively().block();
		
		// Guard returns false, should go to S3
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1")
			.setHeader("choice", "s3").build());
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	// ========== Component Resolver Tests ==========

	@Test
	public void testWithComponentResolver() {
		context.refresh();
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-actions.scxml");
		DefaultStateMachineComponentResolver<String, String> resolver = 
			new DefaultStateMachineComponentResolver<>();
		resolver.registerAction("action1", context -> {});
		
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		factory.setStateMachineComponentResolver(resolver);
		
		StateMachineModel<String, String> stateMachineModel = factory.build();
		assertThat(stateMachineModel).isNotNull();
	}

	// ========== Error Handling Tests ==========

	@Test
	public void testInvalidResource() {
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(
			"classpath:non-existent.scxml");
		
		assertThatThrownBy(() -> factory.build())
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testInvalidScxmlFormat() {
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/invalid-scxml.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		if (scxmlResource.exists()) {
			assertThatThrownBy(() -> factory.build())
				.isInstanceOf(IllegalArgumentException.class);
		}
	}

	// ========== Configuration Classes ==========

	@Configuration
	@EnableStateMachine
	static class ConfigActions extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/simple-actions-execution.scxml");
			factory.setStateMachineComponentResolver(componentResolver());
			return factory;
		}

		@Bean
		public StateMachineComponentResolver<String, String> componentResolver() {
			DefaultStateMachineComponentResolver<String, String> resolver = 
				new DefaultStateMachineComponentResolver<>();
			resolver.registerAction("entryAction", entryAction());
			resolver.registerAction("exitAction", exitAction());
			resolver.registerAction("transitionAction", transitionAction());
			return resolver;
		}

		@Bean
		public TestUtils.LatchAction entryAction() {
			return new TestUtils.LatchAction();
		}

		@Bean
		public TestUtils.LatchAction exitAction() {
			return new TestUtils.LatchAction();
		}

		@Bean
		public TestUtils.LatchAction transitionAction() {
			return new TestUtils.LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	static class ConfigGuards extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/simple-guards-execution.scxml");
			factory.setStateMachineComponentResolver(componentResolver());
			return factory;
		}

		@Bean
		public StateMachineComponentResolver<String, String> componentResolver() {
			DefaultStateMachineComponentResolver<String, String> resolver = 
				new DefaultStateMachineComponentResolver<>();
			resolver.registerGuard("myGuard", context -> 
				context.getExtendedState().getVariables().getOrDefault("allow", true).equals(true));
			return resolver;
		}
	}

	@Configuration
	@EnableStateMachine
	static class ConfigParallel extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/simple-parallel-execution.scxml");
		}
	}

	@Configuration
	@EnableStateMachine
	static class ConfigHistoryShallow extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/simple-history-shallow-execution.scxml");
		}
	}

	@Configuration
	@EnableStateMachine
	static class ConfigChoice extends StateMachineConfigurerAdapter<String, String> {
		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model.withModel().factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(
				"classpath:org/springframework/statemachine/scxml/simple-choice-execution.scxml");
			factory.setStateMachineComponentResolver(componentResolver());
			return factory;
		}

		@Bean
		public StateMachineComponentResolver<String, String> componentResolver() {
			DefaultStateMachineComponentResolver<String, String> resolver = 
				new DefaultStateMachineComponentResolver<>();
			resolver.registerGuard("s2Guard", context -> 
				"s2".equals(context.getMessageHeaders().get("choice")));
			resolver.registerGuard("s3Guard", context -> 
				"s3".equals(context.getMessageHeaders().get("choice")));
			return resolver;
		}
	}
}
