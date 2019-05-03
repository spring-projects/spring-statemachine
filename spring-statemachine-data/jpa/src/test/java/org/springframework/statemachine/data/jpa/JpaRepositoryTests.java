/*
 * Copyright 2016-2019 the original author or authors.
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
package org.springframework.statemachine.data.jpa;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.AbstractRepositoryTests;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateMachineRepository;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.transition.TransitionKind;

/**
 * JPA repository config tests.
 *
 * @author Janne Valkealahti
 */
public class JpaRepositoryTests extends AbstractRepositoryTests {

	@Test
	public void testRepository1() {
		context.register(TestConfig.class);
		context.refresh();

		JpaStateRepository statesRepository = context.getBean(JpaStateRepository.class);
		JpaRepositoryState stateS1 = new JpaRepositoryState("S1");
		JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
		assertThat(statesRepository.count(), is(0l));

		statesRepository.save(stateS1);
		statesRepository.save(stateS2);
		assertThat(statesRepository.count(), is(2l));

		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);
		JpaRepositoryTransition transition = new JpaRepositoryTransition(stateS1, stateS2, "E1");
		transition.setKind(TransitionKind.EXTERNAL);
		transitionsRepository.save(transition);

		assertThat(statesRepository.count(), is(2l));

		JpaRepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource().getState(), is("S1"));
		assertThat(transition2.getTarget().getState(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));
		assertThat(transition2.getKind(), is(TransitionKind.EXTERNAL));

		context.close();
	}

	@Test
	public void testRepository2() {
		context.register(TestConfig.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		StateRepository<JpaRepositoryState> statesRepository1 = context.getBean(StateRepository.class);
		JpaRepositoryState state1 = new JpaRepositoryState("S1");
		statesRepository1.save(state1);
		JpaRepositoryState state2 = new JpaRepositoryState("S2");
		statesRepository1.save(state2);
		@SuppressWarnings("unchecked")
		StateRepository<? extends RepositoryState> statesRepository2 = context.getBean(StateRepository.class);
		Iterable<? extends RepositoryState> findAll = statesRepository2.findAll();
		assertThat(findAll.iterator().next().getState(), is("S1"));

		@SuppressWarnings("unchecked")
		TransitionRepository<RepositoryTransition> transitionsRepository = context.getBean(TransitionRepository.class);
		RepositoryTransition transition = new JpaRepositoryTransition(state1, state2, "E1");
		transitionsRepository.save(transition);
		RepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource().getState(), is("S1"));
		assertThat(transition2.getTarget().getState(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));

		context.close();
	}

	@Test
	public void testRepository3() {
		context.register(TestConfig.class);
		context.refresh();

		JpaStateRepository statesRepository = context.getBean(JpaStateRepository.class);
		JpaRepositoryState state1 = new JpaRepositoryState("machine1", "S1", true);
		statesRepository.save(state1);
		JpaRepositoryState state2 = new JpaRepositoryState("machine2", "S2", false);
		statesRepository.save(state2);
		JpaRepositoryState state3 = new JpaRepositoryState("machine1", "S3", true);
		statesRepository.save(state3);
		JpaRepositoryState state4 = new JpaRepositoryState("machine2", "S4", false);
		statesRepository.save(state4);

		List<JpaRepositoryState> findByMachineId1 = statesRepository.findByMachineId("machine1");
		List<JpaRepositoryState> findByMachineId2 = statesRepository.findByMachineId("machine2");
		assertThat(findByMachineId1.size(), is(2));
		assertThat(findByMachineId2.size(), is(2));
		assertThat(findByMachineId1.get(0).getMachineId(), is("machine1"));
		assertThat(findByMachineId2.get(0).getMachineId(), is("machine2"));


		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);
		JpaRepositoryTransition transition1 = new JpaRepositoryTransition("machine1", state1, state2, "E1");
		JpaRepositoryTransition transition2 = new JpaRepositoryTransition("machine2", state3, state4, "E2");
		transitionsRepository.save(transition1);
		transitionsRepository.save(transition2);
		List<JpaRepositoryTransition> findByMachineId3 = transitionsRepository.findByMachineId("machine1");
		List<JpaRepositoryTransition> findByMachineId4 = transitionsRepository.findByMachineId("machine2");

		assertThat(findByMachineId3.size(), is(1));
		assertThat(findByMachineId4.size(), is(1));
		assertThat(findByMachineId3.get(0).getMachineId(), is("machine1"));
		assertThat(findByMachineId4.get(0).getMachineId(), is("machine2"));

		context.close();
	}

	@Test
	public void testRepository4() {
		context.register(TestConfig.class);
		context.refresh();

		JpaActionRepository actionsRepository = context.getBean(JpaActionRepository.class);
		JpaRepositoryAction action1 = new JpaRepositoryAction();
		action1.setSpel("spel1");
		action1.setName("action1");
		actionsRepository.save(action1);

		assertThat(actionsRepository.count(), is(1l));
		JpaRepositoryAction action11 = actionsRepository.findAll().iterator().next();
		assertThat(action1.getSpel(), is(action11.getSpel()));
		assertThat(action1.getName(), is(action11.getName()));
	}

	@Test
	public void testRepository5() {
		context.register(TestConfig.class);
		context.refresh();

		JpaStateRepository statesRepository = context.getBean(JpaStateRepository.class);
		JpaRepositoryState stateS1 = new JpaRepositoryState("S1");
		JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
		statesRepository.save(stateS1);
		statesRepository.save(stateS2);

		JpaActionRepository actionsRepository = context.getBean(JpaActionRepository.class);

		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);
		JpaRepositoryTransition transition = new JpaRepositoryTransition(stateS1, stateS2, "E1");

		JpaActionRepository actionRepository = context.getBean(JpaActionRepository.class);
		JpaRepositoryAction action1 = new JpaRepositoryAction();
		action1.setName("action1");
		actionRepository.save(action1);

		Set<JpaRepositoryAction> actions = new HashSet<>(Arrays.asList(action1));
		transition.setActions(actions);

		transitionsRepository.save(transition);
		JpaRepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource().getState(), is("S1"));
		assertThat(transition2.getTarget().getState(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));

		assertThat(actionsRepository.count(), is(1l));
		JpaRepositoryAction action11 = actionsRepository.findAll().iterator().next();
		assertThat(action1.getName(), is(action11.getName()));


		assertThat(transition2.getActions().size(), is(1));
	}

	@Test
	public void testRepository6() {
		context.register(TestConfig.class);
		context.refresh();

		JpaActionRepository actionsRepository = context.getBean(JpaActionRepository.class);
		JpaStateRepository statesRepository = context.getBean(JpaStateRepository.class);
		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);

		JpaRepositoryAction action1 = new JpaRepositoryAction();
		action1.setName("action1");
		actionsRepository.save(action1);
		assertThat(actionsRepository.count(), is(1l));

		JpaRepositoryAction action2 = new JpaRepositoryAction();
		action2.setName("action2");
		actionsRepository.save(action2);
		assertThat(actionsRepository.count(), is(2l));

		JpaRepositoryState stateS1 = new JpaRepositoryState("S1");
		stateS1.setEntryActions(new HashSet<>(Arrays.asList(action1, action2)));
		stateS1.setExitActions(new HashSet<>(Arrays.asList(action1, action2)));
		JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
		stateS2.setParentState(stateS1);
		stateS2.setStateActions(new HashSet<>(Arrays.asList(action1, action2)));
		JpaRepositoryState stateS3 = new JpaRepositoryState("S3");
		stateS3.setParentState(stateS1);
		stateS3.setExitActions(new HashSet<>(Arrays.asList(action1, action2)));
		statesRepository.save(stateS1);
		statesRepository.save(stateS2);
		statesRepository.save(stateS3);

		JpaRepositoryTransition transition1 = new JpaRepositoryTransition(stateS1, stateS2, "E1");
		transition1.setActions(new HashSet<>(Arrays.asList(action1, action2)));
		transitionsRepository.save(transition1);
		assertThat(transitionsRepository.count(), is(1l));

		JpaRepositoryTransition transition2 = new JpaRepositoryTransition(stateS2, stateS3, "E2");
		transition2.setActions(new HashSet<>(Arrays.asList(action1, action2)));
		transitionsRepository.save(transition2);
		assertThat(transitionsRepository.count(), is(2l));
	}

	@Test
	public void testRepository7() {
		context.register(TestConfig.class);
		context.refresh();

		JpaStateMachineRepository stateMachineRepository = context.getBean(JpaStateMachineRepository.class);

		JpaRepositoryStateMachine machine1 = new JpaRepositoryStateMachine();
		machine1.setMachineId("machine1");
		machine1.setState("S1");
		machine1.setStateMachineContext(new byte[] { 0 });

		assertThat(stateMachineRepository.count(), is(0l));
		stateMachineRepository.save(machine1);
		assertThat(stateMachineRepository.count(), is(1l));

		JpaRepositoryStateMachine machine1x = stateMachineRepository.findById("machine1").get();
		assertThat(machine1x.getMachineId(), is(machine1.getMachineId()));
		assertThat(machine1x.getState(), is(machine1.getState()));
		assertThat(machine1x.getStateMachineContext().length, is(1));
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Override
	protected Class<?>[] getRegisteredClasses() {
		return new Class<?>[] { TestConfig.class };
	}

	@Test
	public void testAutowire() {
		context.register(TestConfig.class, WireConfig.class);
		context.refresh();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStateMachinePersistWithStrings() {
		context.register(TestConfig.class, ConfigWithStrings.class);
		context.refresh();

		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getId(), is("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getId(), is("S2"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getId(), is("S1"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStateMachinePersistWithEnums() {
		context.register(TestConfig.class, ConfigWithEnums.class);
		context.refresh();

		StateMachine<PersistTestStates, PersistTestEvents> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getId(), is(PersistTestStates.S1));
		stateMachine.sendEvent(PersistTestEvents.E1);
		assertThat(stateMachine.getState().getId(), is(PersistTestStates.S2));
		stateMachine.sendEvent(PersistTestEvents.E2);
		assertThat(stateMachine.getState().getId(), is(PersistTestStates.S1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testStateMachinePersistWithRootRegions() {
		context.register(TestConfig.class, ConfigWithRootRegions.class);
		context.refresh();
		JpaStateMachineRepository stateMachineRepository = context.getBean(JpaStateMachineRepository.class);

		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S10", "S20"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S11", "S21"));

		assertThat(stateMachineRepository.count(), is(3l));

		List<String> ids = StreamSupport.stream(stateMachineRepository.findAll().spliterator(), false)
				.map(jrsm -> jrsm.getMachineId()).collect(Collectors.toList());
		assertThat(ids.size(), is(3));

		// [null#238e8cc0-a932-4583-b696-2c057e5ebefe, null#486e20be-853e-4e4d-9a68-c62c061469ef, testid]
		assertThat(ids, containsInAnyOrder("testid", "testid#R1", "testid#R2"));

	}

	@EnableAutoConfiguration
	static class TestConfig {
	}

	@Configuration
	static class WireConfig {

		@Autowired
		StateRepository<JpaRepositoryState> statesRepository1;

		@Autowired
		TransitionRepository<JpaRepositoryTransition> statesRepository11;

		@SuppressWarnings("rawtypes")
		@Autowired
		StateRepository statesRepository2;

		@Autowired
		JpaStateRepository statesRepository3;

		@Autowired
		StateRepository<? extends RepositoryState> statesRepository4;

		@Autowired
		JpaStateMachineRepository jpaStateMachineRepository1;

		@Autowired
		StateMachineRepository<JpaRepositoryStateMachine> jpaStateMachineRepository2;

	}

	@Configuration
	@EnableStateMachine
	public static class ConfigWithStrings extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private JpaStateMachineRepository jpaStateMachineRepository;

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.machineId("xxx1")
				.and()
				.withPersistence()
					.runtimePersister(stateMachineRuntimePersister());
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
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S1")
					.event("E2");
		}

		@Bean
		public StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister() {
			return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class ConfigWithEnums extends StateMachineConfigurerAdapter<PersistTestStates, PersistTestEvents> {

		@Autowired
		private JpaStateMachineRepository jpaStateMachineRepository;

		@Override
		public void configure(StateMachineConfigurationConfigurer<PersistTestStates, PersistTestEvents> config) throws Exception {
			config
				.withConfiguration()
					.machineId("xxx2")
				.and()
				.withPersistence()
					.runtimePersister(stateMachineRuntimePersister());
		}

		@Override
		public void configure(StateMachineStateConfigurer<PersistTestStates, PersistTestEvents> states) throws Exception {
			states
				.withStates()
					.initial(PersistTestStates.S1)
					.state(PersistTestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<PersistTestStates, PersistTestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(PersistTestStates.S1)
					.target(PersistTestStates.S2)
					.event(PersistTestEvents.E1)
					.and()
				.withExternal()
					.source(PersistTestStates.S2)
					.target(PersistTestStates.S1)
					.event(PersistTestEvents.E2);
		}

		@Bean
		public StateMachineRuntimePersister<PersistTestStates, PersistTestEvents, String> stateMachineRuntimePersister() {
			return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
		}
	}

	public enum PersistTestStates {
		S1, S2;
	}

	public enum PersistTestEvents {
		E1, E2;
	}

	@Configuration
	@EnableStateMachine
	static class ConfigWithRootRegions extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private JpaStateMachineRepository jpaStateMachineRepository;

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.machineId("testid")
					.and()
				.withPersistence()
					.runtimePersister(stateMachineRuntimePersister());
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.region("R1")
					.initial("S10")
					.state("S10")
					.state("S11")
					.and()
				.withStates()
					.region("R2")
					.initial("S20")
					.state("S20")
					.state("S21");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S10")
					.target("S11")
					.event("E1")
					.and()
				.withExternal()
					.source("S20")
					.target("S21")
					.event("E1");
		}

		@Bean
		public StateMachineRuntimePersister<String, String, String> stateMachineRuntimePersister() {
			return new JpaPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
		}
	}
}
