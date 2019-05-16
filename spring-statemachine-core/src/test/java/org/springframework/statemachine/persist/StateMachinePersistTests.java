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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer.History;
import org.springframework.statemachine.state.HistoryPseudoState;

public class StateMachinePersistTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSimplePersist1() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		persister.persist(stateMachine, "xxx");
		persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getState().getIds(), contains("S2"));

		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), contains("S3"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSimplePersist2() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<TestStates, TestEvents> stateMachine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		stateMachine.start();
		stateMachine.sendEvent(TestEvents.E1);
		assertThat(stateMachine.getState().getIds(), contains(TestStates.S2));

		InMemoryStateMachinePersist2 stateMachinePersist = new InMemoryStateMachinePersist2();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		persister.persist(stateMachine, "xxx");
		persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getState().getIds(), contains(TestStates.S2));

		stateMachine.sendEvent(TestEvents.E2);
		assertThat(stateMachine.getState().getIds(), contains(TestStates.S3));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExtendedState() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		stateMachine.start();

		stateMachine.getExtendedState().getVariables().put("foo", "bar");

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		persister.persist(stateMachine, "xxx");
		stateMachine.getExtendedState().getVariables().remove("foo");
		stateMachine = persister.restore(stateMachine, "xxx");

		assertThat(stateMachine.getExtendedState().get("foo", String.class), is("bar"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSubStates() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		persister.persist(stateMachine, "xxx");
		stateMachine = persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));

		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S3", "S31"));

		stateMachine = persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRegions() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		stateMachine.start();

		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S11", "S21", "S31"));

		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S12", "S21", "S31"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S12", "S22", "S31"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S12", "S22", "S32"));

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		persister.persist(stateMachine, "xxx");
		stateMachine = persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S12", "S22", "S32"));

		stateMachine.sendEvent("E4");
		stateMachine.sendEvent("E5");
		stateMachine.sendEvent("E6");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S13", "S23", "S33"));

		stateMachine = persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S12", "S22", "S32"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSubsInRegions() throws Exception {
		context.register(Config51.class, Config52.class);
		context.refresh();

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<String, String> stateMachine1 = context.getBean("machine1", StateMachine.class);
		StateMachine<String, String> stateMachine2 = context.getBean("machine2", StateMachine.class);
		stateMachine1.start();

		assertThat(stateMachine1.getState().getIds(), containsInAnyOrder("S11", "S111", "S21"));
		persister.persist(stateMachine1, "xxx");
		stateMachine2 = persister.restore(stateMachine2, "xxx");
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S11", "S111", "S21"));

		stateMachine1.sendEvent("E1");
		assertThat(stateMachine1.getState().getIds(), containsInAnyOrder("S12", "S21"));
		persister.persist(stateMachine1, "xxx");
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S11", "S111", "S21"));
		stateMachine2 = persister.restore(stateMachine2, "xxx");
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S12", "S21"));

		stateMachine1.sendEvent("E2");
		assertThat(stateMachine1.getState().getIds(), containsInAnyOrder("S12", "S22", "S221"));
		persister.persist(stateMachine1, "xxx");
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S12", "S21"));
		stateMachine2 = persister.restore(stateMachine2, "xxx");
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S12", "S22", "S221"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHistoryFlatShallow() throws Exception {
		// not sure if this test makes any sense as it was done for
		// gh182, but persisting history state which is transitient
		// is not exactly possible
		context.register(Config61.class, Config62.class);
		context.refresh();

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<String, String> stateMachine1 = context.getBean("machine1", StateMachine.class);
		StateMachine<String, String> stateMachine2 = context.getBean("machine2", StateMachine.class);
		// start gets in state S1
		stateMachine1.start();

		// event E1 takes into state S2
		stateMachine1.sendEvent("E1");
		assertThat(stateMachine1.getState().getIds(), contains("S2"));
		Object history = readHistoryState(stateMachine1);
		assertThat(history, is("S2"));

		// we persist with state S2 and history keeps same S2
		persister.persist(stateMachine1, "xxx");

		stateMachine2 = persister.restore(stateMachine2, "xxx");
		history = readHistoryState(stateMachine2);
		assertThat(history, is("S2"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testHistorySubShallow() throws Exception {
		// not sure if this test makes any sense as it was done for
		// gh182, but persisting history state which is transitient
		// is not exactly possible
		context.register(Config71.class, Config72.class);
		context.refresh();

		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<String, String> stateMachine1 = context.getBean("machine1", StateMachine.class);
		StateMachine<String, String> stateMachine2 = context.getBean("machine2", StateMachine.class);
		stateMachine1.start();

		stateMachine1.sendEvent("E1");
		assertThat(stateMachine1.getState().getIds(), containsInAnyOrder("S2", "S21"));

		stateMachine1.sendEvent("E3");
		assertThat(stateMachine1.getState().getIds(), containsInAnyOrder("S2", "S22"));
		persister.persist(stateMachine1, "xxx");

		stateMachine1.sendEvent("E2");
		assertThat(stateMachine1.getState().getIds(), containsInAnyOrder("S1"));


		stateMachine2 = persister.restore(stateMachine2, "xxx");
		Object history = TestUtils.readField("history", stateMachine1);
		assertThat(history, notNullValue());
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S2", "S22"));

		stateMachine2.sendEvent("E2");
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine2.sendEvent("EH3");
		assertThat(stateMachine2.getState().getIds(), containsInAnyOrder("S2", "S22"));
	}

	@Test
	public void testPersistWithEnd() throws Exception {
		context.register(Config8.class);
		context.refresh();
		InMemoryStateMachinePersist1 stateMachinePersist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);
		@SuppressWarnings("unchecked")
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);

		StateMachine<String,String> stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine, notNullValue());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));

		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
		assertThat(stateMachine.isComplete(), is(true));

		persister.persist(stateMachine, "xxx");

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));

		stateMachine = persister.restore(stateMachine, "xxx");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
		assertThat(stateMachine.isComplete(), is(true));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1")
					.state("S2")
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S3")
					.event("E2");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1")
					.state("S2")
					.state("S3")
					.and()
					.withStates()
						.parent("S2")
						.initial("S21")
						.state("S21")
						.state("S22")
					.and()
					.withStates()
						.parent("S3")
						.initial("S31")
						.state("S31")
						.state("S32");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S3")
					.event("E2")
					.and()
				.withExternal()
					.source("S21")
					.target("S22")
					.event("E3");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S11")
					.state("S11")
					.state("S12")
					.state("S13")
					.and()
				.withStates()
					.initial("S21")
					.state("S21")
					.state("S22")
					.state("S23")
					.and()
				.withStates()
					.initial("S31")
					.state("S31")
					.state("S32")
					.state("S33");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S11")
					.target("S12")
					.event("E1")
					.and()
				.withExternal()
					.source("S21")
					.target("S22")
					.event("E2")
					.and()
				.withExternal()
					.source("S31")
					.target("S32")
					.event("E3")
					.and()
				.withExternal()
					.source("S12")
					.target("S13")
					.event("E4")
					.and()
				.withExternal()
					.source("S22")
					.target("S23")
					.event("E5")
					.and()
				.withExternal()
					.source("S32")
					.target("S33")
					.event("E6");
		}
	}

	static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S11")
					.state("S11")
					.state("S12")
					.state("S13")
					.and()
					.withStates()
						.parent("S11")
						.initial("S111")
						.state("S111")
						.state("S112")
						.and()
				.withStates()
					.initial("S21")
					.state("S21")
					.state("S22")
					.state("S23")
					.and()
					.withStates()
						.parent("S22")
						.initial("S221")
						.state("S221")
						.state("S222");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S11")
					.target("S12")
					.event("E1")
					.and()
				.withExternal()
					.source("S21")
					.target("S22")
					.event("E2");
		}

	}

	@Configuration
	@EnableStateMachine(name = "machine1")
	static class Config51 extends Config5 {
	}

	@Configuration
	@EnableStateMachine(name = "machine2")
	static class Config52 extends Config5 {
	}

	@Configuration
	@EnableStateMachine(name = "machine1")
	static class Config61 extends Config6 {
	}

	@Configuration
	@EnableStateMachine(name = "machine2")
	static class Config62 extends Config6 {
	}

	static class Config6 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1")
					.state("S2")
					.state("S3")
					.state("S4")
					.history("SH", History.SHALLOW);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2").event("E1")
					.and()
				.withExternal()
					.source("S2").target("S3").event("E2")
					.and()
				.withExternal()
					.source("S1").target("S4").event("E3")
					.and()
				.withExternal()
					.source("S2").target("S4").event("E3")
					.and()
				.withExternal()
					.source("S3").target("S4").event("E3")
					.and()
				.withExternal()
					.source("S1").target("SH").event("EH")
					.and()
				.withExternal()
					.source("S2").target("SH").event("EH")
					.and()
				.withExternal()
					.source("S3").target("SH").event("EH");
		}
	}

	@Configuration
	@EnableStateMachine(name = "machine1")
	static class Config71 extends Config7 {
	}

	@Configuration
	@EnableStateMachine(name = "machine2")
	static class Config72 extends Config7 {
	}

	static class Config7 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1")
					.state("S2")
					.history("SH", History.SHALLOW)
					.and()
					.withStates()
						.parent("S2")
						.initial("S21")
						.state("S22")
						.history("S2H", History.SHALLOW);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2").event("E1")
					.and()
				.withExternal()
					.source("S2").target("S1").event("E2")
					.and()
				.withExternal()
					.source("S21").target("S22").event("E3")
					.and()
				.withExternal()
					.source("S1").target("SH").event("EH1")
					.and()
				.withExternal()
					.source("S2").target("SH").event("EH2")
					.and()
				.withExternal()
					.source("S1").target("S2H").event("EH3")
					.and()
				.withExternal()
					.source("S2").target("S2H").event("EH3");
		}
	}

	@Configuration
	@EnableStateMachineFactory
	static class Config8 extends StateMachineConfigurerAdapter<String, String> {

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
					.end("S2");
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

	static class InMemoryStateMachinePersist2 implements StateMachinePersist<TestStates, TestEvents, String> {

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

	private static Object readHistoryState(Object stateMachine) throws Exception {
		Object pseudo = TestUtils.readField("history", stateMachine);
		if (pseudo instanceof HistoryPseudoState) {
			Object state = TestUtils.readField("state", pseudo);
			if (state != null) {
				return TestUtils.callMethod("getId", state);
			}
		}
		return null;
	}
}
