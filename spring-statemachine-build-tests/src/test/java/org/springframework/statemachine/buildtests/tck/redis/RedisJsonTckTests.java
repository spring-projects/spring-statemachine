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

import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
import org.springframework.statemachine.data.redis.RedisRepositoryAction;
import org.springframework.statemachine.data.redis.RedisRepositoryGuard;
import org.springframework.statemachine.data.redis.RedisRepositoryState;
import org.springframework.statemachine.data.redis.RedisRepositoryTransition;
import org.springframework.statemachine.data.support.StateMachineJackson2RepositoryPopulatorFactoryBean;

/**
 * Tck tests for machine configs imported from json entity definitions.
 *
 * @author Janne Valkealahti
 *
 */
public class RedisJsonTckTests extends AbstractTckTests {

	@Rule
	public RedisRule redisAvailableRule = new RedisRule();

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

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
	protected StateMachine<String, String> getSimpleMachine() {
		context.register(TestConfig.class, SimpleMachineConfig.class, StateMachineFactoryConfig.class);
		context.refresh();
		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		context.register(TestConfig.class, SimpleSubMachineConfig.class, StateMachineFactoryConfig.class);
		context.refresh();
		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Override
	protected StateMachine<String, String> getShowcaseMachine() throws Exception {
		context.register(TestConfig.class, ShowcaseMachineBeansConfig.class, ShowcaseMachineConfig.class, StateMachineFactoryConfig.class);
		context.refresh();
		return getStateMachineFactoryFromContext().getStateMachine();
	}

	@Configuration
	public static class SimpleMachineConfig {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("org/springframework/statemachine/buildtests/tck/redis/SimpleMachine.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class SimpleSubMachineConfig {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("org/springframework/statemachine/buildtests/tck/redis/SimpleSubMachine.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class ShowcaseMachineConfig {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("org/springframework/statemachine/buildtests/tck/redis/ShowcaseMachine.json")});
			return factoryBean;
		}
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
