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
package demo.datajpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryStateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.support.StateMachineJackson2RepositoryPopulatorFactoryBean;

@Configuration
public class StateMachineConfig {

//tag::snippetA[]
	@Bean
	public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
		StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
		factoryBean.setResources(new Resource[]{new ClassPathResource("data.json")});
		return factoryBean;
	}
//end::snippetA[]

//tag::snippetB[]
	@Configuration
	@EnableStateMachineFactory
	public static class Config extends StateMachineConfigurerAdapter<String, String> {

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
//end::snippetB[]
}
