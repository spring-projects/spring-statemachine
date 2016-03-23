/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.persist;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class StateMachinePersistTests3 extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testPersistMachineId() throws Exception {
		context.register(Config1.class);
		context.refresh();
		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);
		@SuppressWarnings("unchecked")
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);

		StateMachine<String,String> stateMachine = stateMachineFactory.getStateMachine("testid2");
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getId(), is("testid2"));

		persister.persist(stateMachine, "xxx");

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getId(), nullValue());

		stateMachine = persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getId(), is("testid2"));
	}

	@Configuration
	@EnableStateMachineFactory
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
		}
	}

	static class InMemoryStateMachinePersist1 implements StateMachinePersist<String, String, String> {

		private final HashMap<String, StateMachineContext<String, String>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<String, String> context, String contextOjb) throws Exception {
			contexts.put(contextOjb, context);
		}

		@Override
		public StateMachineContext<String, String> read(String contextOjb) throws Exception {
			return contexts.get(contextOjb);
		}
	}
}
