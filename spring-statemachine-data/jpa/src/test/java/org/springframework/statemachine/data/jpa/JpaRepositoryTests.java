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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.RepositoryStateMachineModelFactory;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.support.StateMachineJackson2RepositoryPopulatorFactoryBean;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.ObjectUtils;

public class JpaRepositoryTests extends AbstractJpaRepositoryTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testRepository1() {
		context.register(Config.class);
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
		context.register(Config.class);
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
		context.register(Config.class);
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
		context.register(Config.class);
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
		context.register(Config.class);
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

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine2() throws Exception {
		context.register(Config2.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2").and()
					.step().sendEvent("E2").expectStates("S3").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine3() throws Exception {
		context.register(Config3.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S20").and()
					.step().sendEvent("E2").expectStates("S2", "S21").and()
					.step().sendEvent("E3").expectStates("S1").and()
					.step().sendEvent("E4").expectStates("S2", "S21").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine4() throws Exception {
		context.register(Config4.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S20").and()
					.step().sendEvent("E2").expectStates("S2", "S21").and()
					.step().sendEvent("E3").expectStates("S1").and()
					.step().sendEvent("E4").expectStates("S2", "S21").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine5() throws Exception {
		context.register(Config5.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S10", "S20").and()
					.step().sendEvent("E1").expectStates("S11", "S21").and()
					.step().sendEvent("E2").expectStates("S10", "S21").and()
					.step().sendEvent("E3").expectStates("S10", "S20").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6First() throws Exception {
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s30").build()).expectStates("S30").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6Then1() throws Exception {
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s31").build()).expectStates("S31").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6Then2() throws Exception {
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s32").build()).expectStates("S32").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6Last() throws Exception {
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").build()).expectStates("S33").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8First() throws Exception {
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("junction", "s30").build()).expectStates("S30").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8Then1() throws Exception {
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("junction", "s31").build()).expectStates("S31").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8Then2() throws Exception {
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("junction", "s32").build()).expectStates("S32").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8Last() throws Exception {
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").build()).expectStates("S33").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine9() throws Exception {
		context.register(Config9.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("ENTRY1").expectStates("S2", "S22").and()
					.step().sendEvent("EXIT1").expectStates("S4").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine10() throws Exception {
		context.register(Config10.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent("E1").expectStates("S2", "S21", "S31").and()
					.build();
		plan.test();

		State<String, String> endState = null;
		Iterator<State<String, String>> iterator = stateMachine.getStates().iterator();
		while (iterator.hasNext()) {
			State<String, String> next = iterator.next();
			if (next.getId().equals("SF")) {
				endState = next;
				break;
			}
		}
		assertThat(endState, notNullValue());
		assertThat(endState.getPseudoState(), notNullValue());
		assertThat(endState.getPseudoState().getKind(), is(PseudoStateKind.END));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine11() throws Exception {
		context.register(Config11.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent("E1").expectStates("S2", "S20", "S30").and()
					.step().sendEvent("E2").expectStates("S2", "S21", "S30").and()
					.step().sendEvent("E3").expectStates("S4").and()
					.step().sendEvent("E4").expectStates("SI").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine12() throws Exception {
		context.register(Config12.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("READY").and()
					.step().sendEvent("E3").expectStates("S3").and()
					.step().sendEvent("E1").expectStates("S3").and()
					.step().sendEvent("E6").expectStates("S1").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine13() throws Exception {
		context.register(Config13.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S21").and()
					.step().sendEvent("E3").expectStates("S2", "S22").and()
					.step().sendEvent("E2").expectStates("S3").and()
					.build();
		plan.test();
	}

	@Test
	public void testPopulate1() {
		context.register(Config2.class);
		context.refresh();
		JpaStateRepository stateRepository = context.getBean(JpaStateRepository.class);
		JpaTransitionRepository transitionRepository = context.getBean(JpaTransitionRepository.class);
		assertThat(stateRepository.count(), is(3l));
		assertThat(transitionRepository.count(), is(3l));
	}

	@Test
	public void testPopulate2() {
		context.register(Config7.class);
		context.refresh();
		JpaStateRepository stateRepository = context.getBean(JpaStateRepository.class);
		JpaTransitionRepository transitionRepository = context.getBean(JpaTransitionRepository.class);
		JpaGuardRepository guardRepository = context.getBean(JpaGuardRepository.class);
		JpaActionRepository actionRepository = context.getBean(JpaActionRepository.class);
		assertThat(stateRepository.count(), is(2l));
		assertThat(transitionRepository.count(), is(1l));
		assertThat(guardRepository.count(), is(1l));
		assertThat(actionRepository.count(), is(2l));

		JpaRepositoryTransition transition = transitionRepository.findAll().iterator().next();
		assertThat(transition.getActions().size(), is(2));
		assertThat(transition.getGuard(), notNullValue());
		assertThat(transition.getGuard().getSpel(), is("true"));
		JpaRepositoryAction[] actions = transition.getActions().toArray(new JpaRepositoryAction[0]);
		assertThat(Arrays.asList(actions[0].getSpel(), actions[1].getSpel()), containsInAnyOrder("true", "false"));
	}

	@Test
	public void testAutowire() {
		context.register(Config.class, WireConfig.class);
		context.refresh();
	}

	@EnableAutoConfiguration
	static class Config {
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

	@EnableAutoConfiguration
	static class Config2 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data2.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config3 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data3.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config4 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data4.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config5 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data5.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config6 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data6.json")});
			return factoryBean;
		}

		@Bean
		public Guard<String, String> s30Guard() {
			return new ChoiceGuard("s30");
		}

		@Bean
		public Guard<String, String> s31Guard() {
			return new ChoiceGuard("s31");
		}

		@Bean
		public Guard<String, String> s32Guard() {
			return new ChoiceGuard("s32");
		}

	}

	@EnableAutoConfiguration
	static class Config7 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data7.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config8 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data8.json")});
			return factoryBean;
		}

		@Bean
		public Guard<String, String> s30Guard() {
			return new JunctionGuard("s30");
		}

		@Bean
		public Guard<String, String> s31Guard() {
			return new JunctionGuard("s31");
		}

		@Bean
		public Guard<String, String> s32Guard() {
			return new JunctionGuard("s32");
		}

	}

	@EnableAutoConfiguration
	static class Config9 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data9.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config10 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data10.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config11 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data11.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config12 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data12.json")});
			return factoryBean;
		}
	}

	@EnableAutoConfiguration
	static class Config13 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data13.json")});
			return factoryBean;
		}
	}

	@Configuration
	@EnableStateMachineFactory
	public static class FactoryConfig extends StateMachineConfigurerAdapter<String, String> {

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

	private static class ChoiceGuard implements Guard<String, String> {

		private final String match;

		public ChoiceGuard(String match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("choice", String.class));
		}
	}

	private static class JunctionGuard implements Guard<String, String> {

		private final String match;

		public JunctionGuard(String match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("junction", String.class));
		}
	}
}
