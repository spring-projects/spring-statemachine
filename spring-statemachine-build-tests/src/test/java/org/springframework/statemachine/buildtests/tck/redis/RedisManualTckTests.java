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
package org.springframework.statemachine.buildtests.tck.redis;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
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
import org.springframework.statemachine.data.redis.RedisActionRepository;
import org.springframework.statemachine.data.redis.RedisGuardRepository;
import org.springframework.statemachine.data.redis.RedisRepositoryAction;
import org.springframework.statemachine.data.redis.RedisRepositoryGuard;
import org.springframework.statemachine.data.redis.RedisRepositoryState;
import org.springframework.statemachine.data.redis.RedisRepositoryTransition;
import org.springframework.statemachine.data.redis.RedisStateRepository;
import org.springframework.statemachine.data.redis.RedisTransitionRepository;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * Tck tests for machine configs build manually agains repository interfaces.
 *
 * @author Janne Valkealahti
 *
 */
public class RedisManualTckTests extends AbstractTckTests {

	@Rule
	public RedisRule redisAvailableRule = new RedisRule();

	@Override
	protected void cleanInternal() {
		AnnotationConfigApplicationContext c = new AnnotationConfigApplicationContext();
		c.register(TestConfig.class);
		c.refresh();
		KeyValueTemplate kvTemplate = c.getBean(KeyValueTemplate.class);
		kvTemplate.delete(RedisRepositoryAction.class);
		kvTemplate.delete(RedisRepositoryGuard.class);
		kvTemplate.delete(RedisRepositoryState.class);
		kvTemplate.delete(RedisRepositoryTransition.class);
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

		RedisStateRepository stateRepository = context.getBean(RedisStateRepository.class);
		RedisTransitionRepository transitionRepository = context.getBean(RedisTransitionRepository.class);

		RedisRepositoryState stateS1 = new RedisRepositoryState("S1", true);
		RedisRepositoryState stateS2 = new RedisRepositoryState("S2");
		RedisRepositoryState stateS3 = new RedisRepositoryState("S3");

		stateRepository.save(stateS1);
		stateRepository.save(stateS2);
		stateRepository.save(stateS3);


		RedisRepositoryTransition transitionS1ToS2 = new RedisRepositoryTransition(stateS1, stateS2, "E1");
		RedisRepositoryTransition transitionS2ToS3 = new RedisRepositoryTransition(stateS2, stateS3, "E2");

		transitionRepository.save(transitionS1ToS2);
		transitionRepository.save(transitionS2ToS3);

		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		context.register(TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		RedisStateRepository stateRepository = context.getBean(RedisStateRepository.class);
		RedisTransitionRepository transitionRepository = context.getBean(RedisTransitionRepository.class);

		RedisRepositoryState stateS1 = new RedisRepositoryState("S1", true);
		RedisRepositoryState stateS2 = new RedisRepositoryState("S2");
		RedisRepositoryState stateS3 = new RedisRepositoryState("S3");

		RedisRepositoryState stateS21 = new RedisRepositoryState("S21", true);
		stateS21.setParentState(stateS2);
		RedisRepositoryState stateS22 = new RedisRepositoryState("S22");
		stateS22.setParentState(stateS2);

		stateRepository.save(stateS1);
		stateRepository.save(stateS2);
		stateRepository.save(stateS3);
		stateRepository.save(stateS21);
		stateRepository.save(stateS22);

		RedisRepositoryTransition transitionS1ToS2 = new RedisRepositoryTransition(stateS1, stateS2, "E1");
		RedisRepositoryTransition transitionS2ToS3 = new RedisRepositoryTransition(stateS21, stateS22, "E2");
		RedisRepositoryTransition transitionS21ToS22 = new RedisRepositoryTransition(stateS2, stateS3, "E3");

		transitionRepository.save(transitionS1ToS2);
		transitionRepository.save(transitionS2ToS3);
		transitionRepository.save(transitionS21ToS22);

		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getShowcaseMachine() throws Exception {
		context.register(ShowcaseMachineBeansConfig.class, TestConfig.class, StateMachineFactoryConfig.class);
		context.refresh();

		RedisStateRepository stateRepository = context.getBean(RedisStateRepository.class);
		RedisTransitionRepository transitionRepository = context.getBean(RedisTransitionRepository.class);
		RedisActionRepository actionRepository = context.getBean(RedisActionRepository.class);
		RedisGuardRepository guardRepository = context.getBean(RedisGuardRepository.class);

		RedisRepositoryGuard foo0Guard = new RedisRepositoryGuard();
		foo0Guard.setName("foo0Guard");

		RedisRepositoryGuard foo1Guard = new RedisRepositoryGuard();
		foo1Guard.setName("foo1Guard");

		RedisRepositoryAction fooAction = new RedisRepositoryAction();
		fooAction.setName("fooAction");

		guardRepository.save(foo0Guard);
		guardRepository.save(foo1Guard);
		actionRepository.save(fooAction);

		RedisRepositoryState stateS0 = new RedisRepositoryState("S0", true);
		stateS0.setInitialAction(fooAction);
		RedisRepositoryState stateS1 = new RedisRepositoryState("S1", true);
		stateS1.setParentState(stateS0);
		RedisRepositoryState stateS11 = new RedisRepositoryState("S11", true);
		stateS11.setParentState(stateS1);
		RedisRepositoryState stateS12 = new RedisRepositoryState("S12");
		stateS12.setParentState(stateS1);
		RedisRepositoryState stateS2 = new RedisRepositoryState("S2");
		stateS2.setParentState(stateS0);
		RedisRepositoryState stateS21 = new RedisRepositoryState("S21", true);
		stateS21.setParentState(stateS2);
		RedisRepositoryState stateS211 = new RedisRepositoryState("S211", true);
		stateS211.setParentState(stateS21);
		RedisRepositoryState stateS212 = new RedisRepositoryState("S212");
		stateS212.setParentState(stateS21);

		stateRepository.save(stateS0);
		stateRepository.save(stateS1);
		stateRepository.save(stateS11);
		stateRepository.save(stateS12);
		stateRepository.save(stateS2);
		stateRepository.save(stateS21);
		stateRepository.save(stateS211);
		stateRepository.save(stateS212);

		RedisRepositoryTransition transitionS1ToS1 = new RedisRepositoryTransition(stateS1, stateS1, "A");
		transitionS1ToS1.setGuard(foo1Guard);

		RedisRepositoryTransition transitionS1ToS11 = new RedisRepositoryTransition(stateS1, stateS11, "B");
		RedisRepositoryTransition transitionS21ToS211 = new RedisRepositoryTransition(stateS21, stateS211, "B");
		RedisRepositoryTransition transitionS1ToS2 = new RedisRepositoryTransition(stateS1, stateS2, "C");
		RedisRepositoryTransition transitionS1ToS0 = new RedisRepositoryTransition(stateS1, stateS0, "D");
		RedisRepositoryTransition transitionS211ToS21 = new RedisRepositoryTransition(stateS211, stateS21, "D");
		RedisRepositoryTransition transitionS0ToS211 = new RedisRepositoryTransition(stateS0, stateS211, "E");
		RedisRepositoryTransition transitionS1ToS211 = new RedisRepositoryTransition(stateS1, stateS211, "F");
		RedisRepositoryTransition transitionS2ToS21 = new RedisRepositoryTransition(stateS2, stateS21, "F");
		RedisRepositoryTransition transitionS11ToS211 = new RedisRepositoryTransition(stateS11, stateS211, "G");

		RedisRepositoryTransition transitionS0 = new RedisRepositoryTransition(stateS0, stateS0, "H");
		transitionS0.setKind(TransitionKind.INTERNAL);
		transitionS0.setGuard(foo0Guard);
		transitionS0.setActions(new HashSet<>(Arrays.asList(fooAction)));

		RedisRepositoryTransition transitionS1 = new RedisRepositoryTransition(stateS1, stateS1, "H");
		transitionS1.setKind(TransitionKind.INTERNAL);

		RedisRepositoryTransition transitionS2 = new RedisRepositoryTransition(stateS2, stateS2, "H");
		transitionS2.setKind(TransitionKind.INTERNAL);
		transitionS2.setGuard(foo1Guard);
		transitionS2.setActions(new HashSet<>(Arrays.asList(fooAction)));

		RedisRepositoryTransition transitionS11ToS12 = new RedisRepositoryTransition(stateS11, stateS12, "I");
		RedisRepositoryTransition transitionS12ToS212 = new RedisRepositoryTransition(stateS12, stateS212, "I");
		RedisRepositoryTransition transitionS211ToS12 = new RedisRepositoryTransition(stateS211, stateS12, "I");

		RedisRepositoryTransition transitionS11 = new RedisRepositoryTransition(stateS11, stateS11, "J");
		RedisRepositoryTransition transitionS2ToS1 = new RedisRepositoryTransition(stateS2, stateS1, "K");

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
	@EntityScan(basePackages = {"org.springframework.statemachine.data.redis"})
	@EnableRedisRepositories(basePackages = {"org.springframework.statemachine.data.redis"})
	static class TestConfig {
	}
}
