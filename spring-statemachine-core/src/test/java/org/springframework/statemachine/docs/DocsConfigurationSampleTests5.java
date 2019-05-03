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
package org.springframework.statemachine.docs;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

public class DocsConfigurationSampleTests5 extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testPersist() throws Exception {
		context.register(Config1.class, Config2.class);
		context.refresh();

// tag::snippetC[]
		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<String, String> stateMachine1 = context.getBean("machine1", StateMachine.class);
		StateMachine<String, String> stateMachine2 = context.getBean("machine2", StateMachine.class);
		stateMachine1.start();

		stateMachine1.sendEvent("E1");
		assertThat(stateMachine1.getState().getIds(), contains("S2"));

		persister.persist(stateMachine1, "myid");
		persister.restore(stateMachine2, "myid");
		assertThat(stateMachine2.getState().getIds(), contains("S2"));
// end::snippetC[]
	}

// tag::snippetA[]
	@Configuration
	@EnableStateMachine(name = "machine1")
	static class Config1 extends Config {
	}

	@Configuration
	@EnableStateMachine(name = "machine2")
	static class Config2 extends Config {
	}

	static class Config extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1")
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
// end::snippetA[]

// tag::snippetB[]
	static class InMemoryStateMachinePersist implements StateMachinePersist<String, String, String> {

		private final HashMap<String, StateMachineContext<String, String>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<String, String> context, String contextObj) throws Exception {
			contexts.put(contextObj, context);
		}

		@Override
		public StateMachineContext<String, String> read(String contextObj) throws Exception {
			return contexts.get(contextObj);
		}
	}
// end::snippetB[]
}
