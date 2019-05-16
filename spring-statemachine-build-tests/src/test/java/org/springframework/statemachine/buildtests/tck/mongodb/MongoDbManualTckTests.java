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
package org.springframework.statemachine.buildtests.tck.mongodb;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
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
import org.springframework.statemachine.data.mongodb.MongoDbActionRepository;
import org.springframework.statemachine.data.mongodb.MongoDbGuardRepository;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryAction;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryGuard;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryState;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryTransition;
import org.springframework.statemachine.data.mongodb.MongoDbStateRepository;
import org.springframework.statemachine.data.mongodb.MongoDbTransitionRepository;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * Tck tests for machine configs build manually agains repository interfaces.
 *
 * @author Janne Valkealahti
 *
 */
public class MongoDbManualTckTests extends AbstractTckTests {

	@Rule
	public MongoDbRule MongoDbAvailableRule = new MongoDbRule();

	@Override
	protected void cleanInternal() {
		AnnotationConfigApplicationContext c = new AnnotationConfigApplicationContext();
		c.register(TestConfig.class);
		c.refresh();
		MongoTemplate template = c.getBean(MongoTemplate.class);
		template.dropCollection(MongoDbRepositoryAction.class);
		template.dropCollection(MongoDbRepositoryGuard.class);
		template.dropCollection(MongoDbRepositoryState.class);
		template.dropCollection(MongoDbRepositoryTransition.class);
		c.close();
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Override
	protected StateMachine<String, String> getSimpleMachine() {
		context.register(TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		MongoDbStateRepository stateRepository = context.getBean(MongoDbStateRepository.class);
		MongoDbTransitionRepository transitionRepository = context.getBean(MongoDbTransitionRepository.class);

		MongoDbRepositoryState stateS1 = new MongoDbRepositoryState("S1", true);
		MongoDbRepositoryState stateS2 = new MongoDbRepositoryState("S2");
		MongoDbRepositoryState stateS3 = new MongoDbRepositoryState("S3");

		stateRepository.save(stateS1);
		stateRepository.save(stateS2);
		stateRepository.save(stateS3);

		MongoDbRepositoryTransition transitionS1ToS2 = new MongoDbRepositoryTransition(stateS1, stateS2, "E1");
		MongoDbRepositoryTransition transitionS2ToS3 = new MongoDbRepositoryTransition(stateS2, stateS3, "E2");

		transitionRepository.save(transitionS1ToS2);
		transitionRepository.save(transitionS2ToS3);

		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		context.register(TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		MongoDbStateRepository stateRepository = context.getBean(MongoDbStateRepository.class);
		MongoDbTransitionRepository transitionRepository = context.getBean(MongoDbTransitionRepository.class);

		MongoDbRepositoryState stateS1 = new MongoDbRepositoryState("S1", true);
		MongoDbRepositoryState stateS2 = new MongoDbRepositoryState("S2");
		MongoDbRepositoryState stateS3 = new MongoDbRepositoryState("S3");

		MongoDbRepositoryState stateS21 = new MongoDbRepositoryState("S21", true);
		stateS21.setParentState(stateS2);
		MongoDbRepositoryState stateS22 = new MongoDbRepositoryState("S22");
		stateS22.setParentState(stateS2);

		stateRepository.save(stateS1);
		stateRepository.save(stateS2);
		stateRepository.save(stateS3);
		stateRepository.save(stateS21);
		stateRepository.save(stateS22);

		MongoDbRepositoryTransition transitionS1ToS2 = new MongoDbRepositoryTransition(stateS1, stateS2, "E1");
		MongoDbRepositoryTransition transitionS2ToS3 = new MongoDbRepositoryTransition(stateS21, stateS22, "E2");
		MongoDbRepositoryTransition transitionS21ToS22 = new MongoDbRepositoryTransition(stateS2, stateS3, "E3");

		transitionRepository.save(transitionS1ToS2);
		transitionRepository.save(transitionS2ToS3);
		transitionRepository.save(transitionS21ToS22);

		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getShowcaseMachine() throws Exception {
		context.register(ShowcaseMachineBeansConfig.class, TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		MongoDbStateRepository stateRepository = context.getBean(MongoDbStateRepository.class);
		MongoDbTransitionRepository transitionRepository = context.getBean(MongoDbTransitionRepository.class);
		MongoDbActionRepository actionRepository = context.getBean(MongoDbActionRepository.class);
		MongoDbGuardRepository guardRepository = context.getBean(MongoDbGuardRepository.class);

		MongoDbRepositoryGuard foo0Guard = new MongoDbRepositoryGuard();
		foo0Guard.setName("foo0Guard");

		MongoDbRepositoryGuard foo1Guard = new MongoDbRepositoryGuard();
		foo1Guard.setName("foo1Guard");

		MongoDbRepositoryAction fooAction = new MongoDbRepositoryAction();
		fooAction.setName("fooAction");

		guardRepository.save(foo0Guard);
		guardRepository.save(foo1Guard);
		actionRepository.save(fooAction);

		MongoDbRepositoryState stateS0 = new MongoDbRepositoryState("S0", true);
		stateS0.setInitialAction(fooAction);
		MongoDbRepositoryState stateS1 = new MongoDbRepositoryState("S1", true);
		stateS1.setParentState(stateS0);
		MongoDbRepositoryState stateS11 = new MongoDbRepositoryState("S11", true);
		stateS11.setParentState(stateS1);
		MongoDbRepositoryState stateS12 = new MongoDbRepositoryState("S12");
		stateS12.setParentState(stateS1);
		MongoDbRepositoryState stateS2 = new MongoDbRepositoryState("S2");
		stateS2.setParentState(stateS0);
		MongoDbRepositoryState stateS21 = new MongoDbRepositoryState("S21", true);
		stateS21.setParentState(stateS2);
		MongoDbRepositoryState stateS211 = new MongoDbRepositoryState("S211", true);
		stateS211.setParentState(stateS21);
		MongoDbRepositoryState stateS212 = new MongoDbRepositoryState("S212");
		stateS212.setParentState(stateS21);

		stateRepository.save(stateS0);
		stateRepository.save(stateS1);
		stateRepository.save(stateS11);
		stateRepository.save(stateS12);
		stateRepository.save(stateS2);
		stateRepository.save(stateS21);
		stateRepository.save(stateS211);
		stateRepository.save(stateS212);

		MongoDbRepositoryTransition transitionS1ToS1 = new MongoDbRepositoryTransition(stateS1, stateS1, "A");
		transitionS1ToS1.setGuard(foo1Guard);

		MongoDbRepositoryTransition transitionS1ToS11 = new MongoDbRepositoryTransition(stateS1, stateS11, "B");
		MongoDbRepositoryTransition transitionS21ToS211 = new MongoDbRepositoryTransition(stateS21, stateS211, "B");
		MongoDbRepositoryTransition transitionS1ToS2 = new MongoDbRepositoryTransition(stateS1, stateS2, "C");
		MongoDbRepositoryTransition transitionS1ToS0 = new MongoDbRepositoryTransition(stateS1, stateS0, "D");
		MongoDbRepositoryTransition transitionS211ToS21 = new MongoDbRepositoryTransition(stateS211, stateS21, "D");
		MongoDbRepositoryTransition transitionS0ToS211 = new MongoDbRepositoryTransition(stateS0, stateS211, "E");
		MongoDbRepositoryTransition transitionS1ToS211 = new MongoDbRepositoryTransition(stateS1, stateS211, "F");
		MongoDbRepositoryTransition transitionS2ToS21 = new MongoDbRepositoryTransition(stateS2, stateS21, "F");
		MongoDbRepositoryTransition transitionS11ToS211 = new MongoDbRepositoryTransition(stateS11, stateS211, "G");

		MongoDbRepositoryTransition transitionS0 = new MongoDbRepositoryTransition(stateS0, stateS0, "H");
		transitionS0.setKind(TransitionKind.INTERNAL);
		transitionS0.setGuard(foo0Guard);
		transitionS0.setActions(new HashSet<>(Arrays.asList(fooAction)));

		MongoDbRepositoryTransition transitionS1 = new MongoDbRepositoryTransition(stateS1, stateS1, "H");
		transitionS1.setKind(TransitionKind.INTERNAL);

		MongoDbRepositoryTransition transitionS2 = new MongoDbRepositoryTransition(stateS2, stateS2, "H");
		transitionS2.setKind(TransitionKind.INTERNAL);
		transitionS2.setGuard(foo1Guard);
		transitionS2.setActions(new HashSet<>(Arrays.asList(fooAction)));

		MongoDbRepositoryTransition transitionS11ToS12 = new MongoDbRepositoryTransition(stateS11, stateS12, "I");
		MongoDbRepositoryTransition transitionS12ToS212 = new MongoDbRepositoryTransition(stateS12, stateS212, "I");
		MongoDbRepositoryTransition transitionS211ToS12 = new MongoDbRepositoryTransition(stateS211, stateS12, "I");

		MongoDbRepositoryTransition transitionS11 = new MongoDbRepositoryTransition(stateS11, stateS11, "J");
		MongoDbRepositoryTransition transitionS2ToS1 = new MongoDbRepositoryTransition(stateS2, stateS1, "K");

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
	@EntityScan(basePackages = {"org.springframework.statemachine.data.mongodb"})
	@EnableMongoRepositories(basePackages = {"org.springframework.statemachine.data.mongodb"})
	static class TestConfig {
	}
}
