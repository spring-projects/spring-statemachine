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
package org.springframework.statemachine.buildtests.tck.jpa;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.buildtests.tck.AbstractTckTests;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryStateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.jpa.JpaActionRepository;
import org.springframework.statemachine.data.jpa.JpaGuardRepository;
import org.springframework.statemachine.data.jpa.JpaRepositoryAction;
import org.springframework.statemachine.data.jpa.JpaRepositoryGuard;
import org.springframework.statemachine.data.jpa.JpaRepositoryState;
import org.springframework.statemachine.data.jpa.JpaRepositoryTransition;
import org.springframework.statemachine.data.jpa.JpaStateRepository;
import org.springframework.statemachine.data.jpa.JpaTransitionRepository;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * Tck tests for machine configs build manually agains repository interfaces.
 *
 * @author Janne Valkealahti
 *
 */
public class JpaManualTckTests extends AbstractTckTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Override
	protected StateMachine<String, String> getSimpleMachine() {
		context.register(TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		JpaStateRepository stateRepository = context.getBean(JpaStateRepository.class);
		JpaTransitionRepository transitionRepository = context.getBean(JpaTransitionRepository.class);

		JpaRepositoryState stateS1 = new JpaRepositoryState("S1", true);
		JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
		JpaRepositoryState stateS3 = new JpaRepositoryState("S3");

		stateRepository.save(stateS1);
		stateRepository.save(stateS2);
		stateRepository.save(stateS3);

		JpaRepositoryTransition transitionS1ToS2 = new JpaRepositoryTransition(stateS1, stateS2, "E1");
		JpaRepositoryTransition transitionS2ToS3 = new JpaRepositoryTransition(stateS2, stateS3, "E2");

		transitionRepository.save(transitionS1ToS2);
		transitionRepository.save(transitionS2ToS3);

		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		context.register(TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		JpaStateRepository stateRepository = context.getBean(JpaStateRepository.class);
		JpaTransitionRepository transitionRepository = context.getBean(JpaTransitionRepository.class);

		JpaRepositoryState stateS1 = new JpaRepositoryState("S1", true);
		JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
		JpaRepositoryState stateS3 = new JpaRepositoryState("S3");

		JpaRepositoryState stateS21 = new JpaRepositoryState("S21", true);
		stateS21.setParentState(stateS2);
		JpaRepositoryState stateS22 = new JpaRepositoryState("S22");
		stateS22.setParentState(stateS2);

		stateRepository.save(stateS1);
		stateRepository.save(stateS2);
		stateRepository.save(stateS3);
		stateRepository.save(stateS21);
		stateRepository.save(stateS22);

		JpaRepositoryTransition transitionS1ToS2 = new JpaRepositoryTransition(stateS1, stateS2, "E1");
		JpaRepositoryTransition transitionS2ToS3 = new JpaRepositoryTransition(stateS21, stateS22, "E2");
		JpaRepositoryTransition transitionS21ToS22 = new JpaRepositoryTransition(stateS2, stateS3, "E3");

		transitionRepository.save(transitionS1ToS2);
		transitionRepository.save(transitionS2ToS3);
		transitionRepository.save(transitionS21ToS22);

		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getShowcaseMachine() throws Exception {
		context.register(ShowcaseMachineBeansConfig.class, TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		JpaStateRepository stateRepository = context.getBean(JpaStateRepository.class);
		JpaTransitionRepository transitionRepository = context.getBean(JpaTransitionRepository.class);
		JpaActionRepository actionRepository = context.getBean(JpaActionRepository.class);
		JpaGuardRepository guardRepository = context.getBean(JpaGuardRepository.class);

		JpaRepositoryGuard foo0Guard = new JpaRepositoryGuard();
		foo0Guard.setName("foo0Guard");

		JpaRepositoryGuard foo1Guard = new JpaRepositoryGuard();
		foo1Guard.setName("foo1Guard");

		JpaRepositoryAction fooAction = new JpaRepositoryAction();
		fooAction.setName("fooAction");

		guardRepository.save(foo0Guard);
		guardRepository.save(foo1Guard);
		actionRepository.save(fooAction);

		JpaRepositoryState stateS0 = new JpaRepositoryState("S0", true);
		stateS0.setInitialAction(fooAction);
		JpaRepositoryState stateS1 = new JpaRepositoryState("S1", true);
		stateS1.setParentState(stateS0);
		JpaRepositoryState stateS11 = new JpaRepositoryState("S11", true);
		stateS11.setParentState(stateS1);
		JpaRepositoryState stateS12 = new JpaRepositoryState("S12");
		stateS12.setParentState(stateS1);
		JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
		stateS2.setParentState(stateS0);
		JpaRepositoryState stateS21 = new JpaRepositoryState("S21", true);
		stateS21.setParentState(stateS2);
		JpaRepositoryState stateS211 = new JpaRepositoryState("S211", true);
		stateS211.setParentState(stateS21);
		JpaRepositoryState stateS212 = new JpaRepositoryState("S212");
		stateS212.setParentState(stateS21);

		stateRepository.save(stateS0);
		stateRepository.save(stateS1);
		stateRepository.save(stateS11);
		stateRepository.save(stateS12);
		stateRepository.save(stateS2);
		stateRepository.save(stateS21);
		stateRepository.save(stateS211);
		stateRepository.save(stateS212);

		JpaRepositoryTransition transitionS1ToS1 = new JpaRepositoryTransition(stateS1, stateS1, "A");
		transitionS1ToS1.setGuard(foo1Guard);

		JpaRepositoryTransition transitionS1ToS11 = new JpaRepositoryTransition(stateS1, stateS11, "B");
		JpaRepositoryTransition transitionS21ToS211 = new JpaRepositoryTransition(stateS21, stateS211, "B");
		JpaRepositoryTransition transitionS1ToS2 = new JpaRepositoryTransition(stateS1, stateS2, "C");
		JpaRepositoryTransition transitionS1ToS0 = new JpaRepositoryTransition(stateS1, stateS0, "D");
		JpaRepositoryTransition transitionS211ToS21 = new JpaRepositoryTransition(stateS211, stateS21, "D");
		JpaRepositoryTransition transitionS0ToS211 = new JpaRepositoryTransition(stateS0, stateS211, "E");
		JpaRepositoryTransition transitionS1ToS211 = new JpaRepositoryTransition(stateS1, stateS211, "F");
		JpaRepositoryTransition transitionS2ToS21 = new JpaRepositoryTransition(stateS2, stateS21, "F");
		JpaRepositoryTransition transitionS11ToS211 = new JpaRepositoryTransition(stateS11, stateS211, "G");

		JpaRepositoryTransition transitionS0 = new JpaRepositoryTransition(stateS0, stateS0, "H");
		transitionS0.setKind(TransitionKind.INTERNAL);
		transitionS0.setGuard(foo0Guard);
		transitionS0.setActions(new HashSet<>(Arrays.asList(fooAction)));

		JpaRepositoryTransition transitionS1 = new JpaRepositoryTransition(stateS1, stateS1, "H");
		transitionS1.setKind(TransitionKind.INTERNAL);

		JpaRepositoryTransition transitionS2 = new JpaRepositoryTransition(stateS2, stateS2, "H");
		transitionS2.setKind(TransitionKind.INTERNAL);
		transitionS2.setGuard(foo1Guard);
		transitionS2.setActions(new HashSet<>(Arrays.asList(fooAction)));

		JpaRepositoryTransition transitionS11ToS12 = new JpaRepositoryTransition(stateS11, stateS12, "I");
		JpaRepositoryTransition transitionS12ToS212 = new JpaRepositoryTransition(stateS12, stateS212, "I");
		JpaRepositoryTransition transitionS211ToS12 = new JpaRepositoryTransition(stateS211, stateS12, "I");

		JpaRepositoryTransition transitionS11 = new JpaRepositoryTransition(stateS11, stateS11, "J");
		JpaRepositoryTransition transitionS2ToS1 = new JpaRepositoryTransition(stateS2, stateS1, "K");

		transitionRepository.save(transitionS1ToS1);
		transitionRepository.save(transitionS1ToS11);
		transitionRepository.save(transitionS21ToS211);
		transitionRepository.save(transitionS1ToS2);
		transitionRepository.save(transitionS1ToS0);
		transitionRepository.save(transitionS211ToS21);
		transitionRepository.save(transitionS0ToS211);
		transitionRepository.save(transitionS1ToS211);
		transitionRepository.save(transitionS2ToS21);
		transitionRepository.save(transitionS11ToS211);
		transitionRepository.save(transitionS0);
		transitionRepository.save(transitionS1);
		transitionRepository.save(transitionS2);
		transitionRepository.save(transitionS11ToS12);
		transitionRepository.save(transitionS12ToS212);
		transitionRepository.save(transitionS211ToS12);
		transitionRepository.save(transitionS11);
		transitionRepository.save(transitionS2ToS1);

		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Configuration
	@EnableStateMachineFactory
	public static class StateMachineFactoryConfig extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private StateRepository<? extends RepositoryState> stateRepository;

		@Autowired
		private TransitionRepository<? extends RepositoryTransition> transitionRepository;

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new RepositoryStateMachineModelFactory(stateRepository, transitionRepository);
		}
	}

	@EnableAutoConfiguration
	@EntityScan(basePackages = {"org.springframework.statemachine.data.jpa"})
	@EnableJpaRepositories(basePackages = {"org.springframework.statemachine.data.jpa"})
	static class TestConfig {
	}
}
