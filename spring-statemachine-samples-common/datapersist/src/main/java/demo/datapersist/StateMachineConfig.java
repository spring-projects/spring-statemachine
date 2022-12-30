/*
 * Copyright 2017-2018 the original author or authors.
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
package demo.datapersist;

import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.data.mongodb.MongoDbPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;
import org.springframework.statemachine.data.redis.RedisPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.redis.RedisStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
public class StateMachineConfig {

//tag::snippetA[]
	@Configuration
	@Profile("jpa")
	public static class JpaPersisterConfig {

		@Bean
		public StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister(
				JpaStateMachineRepository jpaStateMachineRepository) {
			return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
		}
	}
//end::snippetA[]

//tag::snippetB[]
	@Configuration
	@Profile("mongo")
	public static class MongoPersisterConfig {

		@Bean
		public StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister(
				MongoDbStateMachineRepository jpaStateMachineRepository) {
			return new MongoDbPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
		}
	}
//end::snippetB[]

//tag::snippetC[]
	@Configuration
	@Profile("redis")
	public static class RedisPersisterConfig {

		@Bean
		public StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister(
				RedisStateMachineRepository jpaStateMachineRepository) {
			return new RedisPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
		}
	}
//end::snippetC[]

	@Configuration
	@EnableStateMachineFactory
	public static class MachineConfig extends StateMachineConfigurerAdapter<States, Events> {

//tag::snippetD[]
		@Autowired
		private StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister;

		@Override
		public void configure(StateMachineConfigurationConfigurer<States, Events> config)
				throws Exception {
			config
				.withPersistence()
					.runtimePersister(stateMachineRuntimePersister);
		}
//end::snippetD[]

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.S1)
					.states(EnumSet.allOf(States.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.S1).target(States.S2)
					.event(Events.E1)
					.and()
				.withExternal()
					.source(States.S2).target(States.S3)
					.event(Events.E2)
					.and()
				.withExternal()
					.source(States.S3).target(States.S4)
					.event(Events.E3)
					.and()
				.withExternal()
					.source(States.S4).target(States.S5)
					.event(Events.E4)
					.and()
				.withExternal()
					.source(States.S5).target(States.S6)
					.event(Events.E5)
					.and()
				.withExternal()
					.source(States.S6).target(States.S1)
					.event(Events.E6);
		}
	}

	@Configuration
	public static class ServiceConfig {

//tag::snippetE[]
		@Bean
		public StateMachineService<States, Events> stateMachineService(
				StateMachineFactory<States, Events> stateMachineFactory,
				StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister) {
			return new DefaultStateMachineService<States, Events>(stateMachineFactory, stateMachineRuntimePersister);
		}
//end::snippetE[]
	}

	public enum States {
		S1, S2, S3, S4, S5, S6;
	}

	public enum Events {
		E1, E2, E3, E4, E5, E6;
	}
}
