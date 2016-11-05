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
package org.springframework.statemachine.data.jpa;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.data.AbstractRepositoryTests;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
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
	}

}
