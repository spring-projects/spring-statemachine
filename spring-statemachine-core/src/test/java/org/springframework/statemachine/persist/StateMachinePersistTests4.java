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
package org.springframework.statemachine.persist;

import static org.hamcrest.Matchers.containsInAnyOrder;
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
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class StateMachinePersistTests4 extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testPersistRegions() throws Exception {
		context.register(Config1.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = context.getBean(StateMachineFactory.class);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		stateMachine.start();
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getId(), is("testid"));

		stateMachine.sendEvent(TestEvents.E1);
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S30));
		persister.persist(stateMachine, "xxx1");

		stateMachine.sendEvent(TestEvents.E2);
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S30));
		persister.persist(stateMachine, "xxx2");

		stateMachine.sendEvent(TestEvents.E3);
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(TestStates.S4));
		persister.persist(stateMachine, "xxx3");

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getId(), nullValue());
		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId(), is("testid"));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S30));

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getId(), nullValue());
		stateMachine = persister.restore(stateMachine, "xxx2");
		assertThat(stateMachine.getId(), is("testid"));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S30));

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getId(), nullValue());
		stateMachine = persister.restore(stateMachine, "xxx3");
		assertThat(stateMachine.getId(), is("testid"));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder(TestStates.S4));
	}

	@Configuration
	@EnableStateMachineFactory
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.SI)
					.fork(TestStates.S1)
					.state(TestStates.S2)
					.end(TestStates.SF)
					.join(TestStates.S3)
					.state(TestStates.S4)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30)
						.state(TestStates.S31);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S30)
					.target(TestStates.S31)
					.event(TestEvents.E3)
					.and()
				.withFork()
					.source(TestStates.S1)
					.target(TestStates.S20)
					.target(TestStates.S30)
					.and()
				.withJoin()
					.source(TestStates.S21)
					.source(TestStates.S31)
					.target(TestStates.S3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4);
		}

	}

	static class InMemoryStateMachinePersist implements StateMachinePersist<TestStates, TestEvents, String> {

		private final HashMap<String, StateMachineContext<TestStates, TestEvents>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<TestStates, TestEvents> context, String contextOjb) throws Exception {
			contexts.put(contextOjb, context);
		}

		@Override
		public StateMachineContext<TestStates, TestEvents> read(String contextOjb) throws Exception {
			return contexts.get(contextOjb);
		}
	}
}
