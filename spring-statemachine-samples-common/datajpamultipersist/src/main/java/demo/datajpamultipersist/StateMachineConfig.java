/*
 * Copyright 2018-2019 the original author or authors.
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
package demo.datajpamultipersist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryStateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.data.support.StateMachineJackson2RepositoryPopulatorFactoryBean;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
public class StateMachineConfig {

//tag::snippetA[]
	@Bean
	public StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister(
			JpaStateMachineRepository jpaStateMachineRepository) {
		return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
	}
//end::snippetA[]

//tag::snippetB[]
	@Bean
	public StateMachineService<String, String> stateMachineService(
			StateMachineFactory<String, String> stateMachineFactory,
			StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister) {
		return new DefaultStateMachineService<String, String>(stateMachineFactory, stateMachineRuntimePersister);
	}
//end::snippetB[]

//tag::snippetC[]
	@Bean
	public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
		StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
		factoryBean.setResources(new Resource[] { new ClassPathResource("datajpamultipersist.json") });
		return factoryBean;
	}
//end::snippetC[]

//tag::snippetD[]
	@Configuration
	@EnableStateMachineFactory
	public static class Config extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private StateRepository<? extends RepositoryState> stateRepository;

		@Autowired
		private TransitionRepository<? extends RepositoryTransition> transitionRepository;

		@Autowired
		private StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister;

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withPersistence()
					.runtimePersister(stateMachineRuntimePersister);
		}

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model)
				throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new RepositoryStateMachineModelFactory(stateRepository, transitionRepository);
		}
	}
//end::snippetD[]
}
