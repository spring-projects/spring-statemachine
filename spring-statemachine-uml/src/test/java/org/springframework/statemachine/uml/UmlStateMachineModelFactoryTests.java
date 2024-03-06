/*
 * Copyright 2016-2020 the original author or authors.
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
package org.springframework.statemachine.uml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.ObjectUtils;

public class UmlStateMachineModelFactoryTests extends AbstractUmlTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testSimpleFlat1() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.registerAction("action1", new LatchAction());
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(2);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleFlat2() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
		DefaultStateMachineComponentResolver<String, String> resolver = new DefaultStateMachineComponentResolver<>();
		resolver.registerAction("action1", new LatchAction());
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setStateMachineComponentResolver(resolver);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(2);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleSubmachine() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-submachine.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(4);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getParent()).isNull();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getParent()).isNull();
			} else if (stateData.getState().equals("S11")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getParent()).isEqualTo("S1");
			} else if (stateData.getState().equals("S12")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getParent()).isEqualTo("S1");
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleRootRegions() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(4);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getRegion()).isNotNull();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	/**
	 * Test {@link StateMachine} vs {@link StateMachineModel} consistency.<BR/>
	 * In this (failing) test, one can notice that the statemachine instance has a duplicated transition "S1->S2" as illustrated here<BR/>
	 * <img src="data:image/png;base64,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"/>
	 */
	@Disabled
	@Test
	public void testStateMachineVsStateMachineModelConsistency() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();

		try {
			// build statemachine from model
			UmlStateMachineModelFactory umlStateMachineModelFactory = new UmlStateMachineModelFactory(("classpath:org/springframework/statemachine/uml/simple-root-regions.uml"));
			StateMachineBuilder.Builder<String, String> stateMachineBuilder = StateMachineBuilder.builder();
			stateMachineBuilder.configureModel().withModel().factory(umlStateMachineModelFactory);
			stateMachineBuilder.configureConfiguration().withConfiguration();
			StateMachine<String, String> stateMachine = stateMachineBuilder.build();

			// get the "root" state of this state machines
			State<String, String> rootState = stateMachine.getStates().stream().findFirst().get();
			assertThat(rootState).isInstanceOf(RegionState.class);
			RegionState<String, String> rootRegionState = ((RegionState<String, String>) rootState);

			// compare statemachine and stateMachineModel

			// states in Region1
			AbstractStateMachine region1InStatemachine = (AbstractStateMachine)
					((List) rootRegionState.getRegions()).stream()
							.filter(region -> ((AbstractStateMachine) region).getId().contains("Region1"))
							.findFirst().get();

			List statesOfRegion1InStateMachine = region1InStatemachine.getStates().stream()
					.map(o -> ((State) o).getId().toString())
					.sorted().toList();

			List<String> statesOfRegion1InStateMachineModel = stateMachineModel.getStatesData().getStateData().stream()
					.filter(stateData -> "Region1".equals(stateData.getRegion().toString()))
					.map(stateData -> stateData.getState().toString())
					.sorted().toList();

			assertThat(statesOfRegion1InStateMachine).isEqualTo(statesOfRegion1InStateMachineModel);

			// states in Region2
			AbstractStateMachine region2InStatemachine = (AbstractStateMachine)
					((List) rootRegionState.getRegions()).stream()
							.filter(region -> ((AbstractStateMachine) region).getId().contains("Region2"))
							.findFirst().get();

			List statesOfRegion2InStateMachine = region2InStatemachine.getStates().stream()
					.map(o -> ((State) o).getId().toString())
					.sorted().toList();

			List<String> statesOfRegion2InStateMachineModel = stateMachineModel.getStatesData().getStateData().stream()
					.filter(stateData -> "Region2".equals(stateData.getRegion().toString()))
					.map(stateData -> stateData.getState().toString())
					.sorted().toList();

			assertThat(statesOfRegion2InStateMachine).isEqualTo(statesOfRegion2InStateMachineModel);

			// transitions in Region1
			List transitionsOfRegion1InStateMachine = region1InStatemachine.getTransitions().stream()
					.map(o -> ((Transition) o).getSource().getId().toString() + "->" + ((Transition) o).getTarget().getId().toString())
					.sorted().toList();

			List<String> transitionsOfRegion1InStateMachineModel = stateMachineModel.getTransitionsData().getTransitions().stream()
					// let's exclude "initial" transition
					.filter(transitionData -> !transitionData.getSource().startsWith("initial"))
					.filter(transitionData -> statesOfRegion1InStateMachine.contains(transitionData.getSource())
							|| statesOfRegion1InStateMachine.contains(transitionData.getTarget()))
					.map(transitionData -> transitionData.getSource() + "->" + transitionData.getTarget())
					.sorted().toList();

			assertThat(transitionsOfRegion1InStateMachine).isEqualTo(transitionsOfRegion1InStateMachineModel);

			// transitions in Region2
			List transitionsOfRegion2InStateMachine = region2InStatemachine.getTransitions().stream()
					.map(o -> ((Transition) o).getSource().getId().toString() + "->" + ((Transition) o).getTarget().getId().toString())
					.sorted().toList();

			List<String> transitionsOfRegion2InStateMachineModel = stateMachineModel.getTransitionsData().getTransitions().stream()
					// let's exclude "initial" transition
					.filter(transitionData -> !transitionData.getSource().startsWith("initial"))
					.filter(transitionData -> statesOfRegion2InStateMachine.contains(transitionData.getSource())
							|| statesOfRegion2InStateMachine.contains(transitionData.getTarget()))
					.map(transitionData -> transitionData.getSource() + "->" + transitionData.getTarget())
					.sorted().toList();

			// WOW! this is failing! Why is transition "S3->S4" present in both Region1 AND Region2 ?!?
			// Does this indicates an issue in UmlStateMachineModelFactory ???
			// Expected :["S1->S2"]
			// Actual   :["S1->S2", "S3->S4"]
			assertThat(transitionsOfRegion2InStateMachine).isEqualTo(transitionsOfRegion2InStateMachineModel);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testSimpleFlatEnd() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat-end.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(3);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.isEnd()).isFalse();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.isEnd()).isFalse();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isEnd()).isTrue();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleEntryExit() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-entryexit.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(8);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S21")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S22")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("ENTRY")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.ENTRY);
			} else if (stateData.getState().equals("EXIT")) {
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.EXIT);
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
		assertThat(stateMachineModel.getTransitionsData().getEntrys().size()).isEqualTo(1);
		assertThat(stateMachineModel.getTransitionsData().getExits().size()).isEqualTo(1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleFlatMachine() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction action1 = context.getBean("action1", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(action1.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineMachine() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1", "S11");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S1", "S12");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleRootRegionsMachine() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1", "S3");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S3");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleEntryExitMachine() throws Exception {
		context.register(Config5.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice1() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice2() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s3").build());
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice3() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMissingNameChoice() {
		context.register(Config6MissingName.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleForkJoin() {
		context.register(Config7.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("SI");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20", "S30");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("SF");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin1() {
		context.register(Config20.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("SI");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20", "S30");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin2() {
		context.register(Config20.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		stateMachine.getExtendedState().getVariables().put("foo", "bar");
		assertThat(stateMachine.getState().getIds()).contains("SI");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20", "S30");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("SF");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryShallow() {
		context.register(Config8.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryDeep() {
		context.register(Config9.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S211");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S212");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S212");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction1() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E4").setHeader("junction", "s5").build());
		assertThat(stateMachine.getState().getIds()).contains("S5");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction2() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S3");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E4").setHeader("junction", "s6").build());
		assertThat(stateMachine.getState().getIds()).contains("S6");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction3() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S4");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S7");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleActions() throws Exception {
		context.register(Config11.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		TestExtendedStateListener listener = new TestExtendedStateListener();
		stateMachine.addStateListener(listener);
		LatchAction e1Action = context.getBean("e1Action", LatchAction.class);
		LatchAction s1Exit = context.getBean("s1Exit", LatchAction.class);
		LatchAction s2Entry = context.getBean("s2Entry", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(s1Exit.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(s2Entry.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getExtendedState().getVariables().get("hellos2do")).isEqualTo("hellos2dovalue");
	}

	@Test
	public void testSimpleEventDefer() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-eventdefer.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(3);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getDeferred().size()).isEqualTo(1);
				assertThat(stateData.getDeferred().iterator().next()).isEqualTo("E2");
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getDeferred().size()).isEqualTo(0);
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getDeferred().size()).isEqualTo(0);
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleTransitionTypes() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-transitiontypes.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitionDatas = stateMachineModel.getTransitionsData().getTransitions();
		assertThat(stateDatas.size()).isEqualTo(2);
		assertThat(transitionDatas.size()).isEqualTo(4);
		for (TransitionData<String, String> transitionData : transitionDatas) {
			if (transitionData.getEvent() != null) {
				if (transitionData.getEvent().equals("E1")) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.EXTERNAL);
				} else if (transitionData.getEvent().equals("E2")) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.LOCAL);
				} else if (transitionData.getEvent().equals("E3")) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.INTERNAL);
				} else {
					throw new IllegalArgumentException();
				}
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryDefault() {
		context.register(Config12.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers1() throws Exception {
		context.register(Config13.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction s3Entry = context.getBean("s3Entry", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(s3Entry.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers2() throws Exception {
		context.register(Config13.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction s5Entry = context.getBean("s5Entry", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(s5Entry.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains("S5");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny1() throws Exception {
		context.register(Config14.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S1");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny2() throws Exception {
		context.register(Config14.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testInitialActions() throws Exception {
		context.register(Config15.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction initialAction = context.getBean("initialAction", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		assertThat(initialAction.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsAllow() throws Exception {
		context.register(Config16.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("foo", "bar").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(stateMachine.getExtendedState().get("myvar1", String.class)).isEqualTo("myvalue1");
		assertThat(stateMachine.getExtendedState().get("myvar2", String.class)).isEqualTo("myvalue2");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsDeny() throws Exception {
		context.register(Config16.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S1");
	}

	@Test
	public void testSimpleFlatMultipleToEnds() throws Exception {
		context.register(Config17.class);
		context.refresh();
	}

	@Test
	public void testSimpleFlatMultipleToEndsViachoices() throws Exception {
		context.register(Config18.class);
		context.refresh();
	}

	@Test
	public void testBrokenModelShadowEntries() throws Exception {
		context.register(Config19.class);
		context.refresh();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineRef() throws Exception {
		context.register(Config21.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S20");
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S30");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21", "S31");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleStateActions() throws Exception {
		context.register(Config22.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction e1Action = context.getBean("e1Action", LatchAction.class);
		LatchAction e2Action = context.getBean("e2Action", LatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(e2Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(stateMachine, "E2");
		assertThat(stateMachine.getState().getIds()).contains("S3");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E20");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E30");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E21");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E31");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E22");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E32");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S21");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E21");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S21");
		assertThat(listener.entered).contains("S2", "S22");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E23");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains("S2", "S22");
		assertThat(listener.entered).contains("S2", "S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, "E1");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S21");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E31");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S21");
		assertThat(listener.entered).contains("S22");

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, "E33");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains("S22");
		assertThat(listener.entered).contains("S22");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleConnectionPointRefMachine() throws Exception {
		context.register(Config24.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, "E3");
		assertThat(stateMachine.getState().getIds()).contains("S2", "S22");
		doSendEventAndConsumeAll(stateMachine, "E4");
		assertThat(stateMachine.getState().getIds()).contains("S4");
	}

	@Test
	public void testConnectionPointRef() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-connectionpointref.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(8);
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S21")) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals("S22")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals("ENTRY")) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.ENTRY);
			} else if (stateData.getState().equals("EXIT")) {
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.EXIT);
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
		assertThat(stateMachineModel.getTransitionsData().getEntrys().size()).isEqualTo(1);
		assertThat(stateMachineModel.getTransitionsData().getExits().size()).isEqualTo(1);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice1() throws InterruptedException {
		context.register(Config25.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice2() throws InterruptedException {
		context.register(Config25.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds()).contains("S4");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice3() throws InterruptedException {
		context.register(Config25.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);
		LatchAction choice1ToChoice2 = context.getBean("choice1ToChoice2", LatchAction.class);
		LatchAction choiceToS5 = context.getBean("choiceToS5", LatchAction.class);
		LatchAction choiceToS6 = context.getBean("choiceToS6", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds()).contains("S6");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choice1ToChoice2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS5.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS6.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction1() throws InterruptedException {
		context.register(Config26.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction2() throws InterruptedException {
		context.register(Config26.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds()).contains("S4");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction3() throws InterruptedException {
		context.register(Config26.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		LatchAction s1ToChoice = context.getBean("s1ToChoice", LatchAction.class);
		LatchAction choiceToS2 = context.getBean("choiceToS2", LatchAction.class);
		LatchAction choiceToS4 = context.getBean("choiceToS4", LatchAction.class);
		LatchAction choice1ToChoice2 = context.getBean("choice1ToChoice2", LatchAction.class);
		LatchAction choiceToS5 = context.getBean("choiceToS5", LatchAction.class);
		LatchAction choiceToS6 = context.getBean("choiceToS6", LatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds()).contains("S6");

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choice1ToChoice2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS5.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS6.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	public void testPseudostateInSubmachineHaveCorrectParent() {
		context.refresh();
		Resource model = new ClassPathResource("org/springframework/statemachine/uml/pseudostate-in-submachine.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model);
		assertThat(model.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();

		assertThat(stateDatas.size()).isEqualTo(4);

		StateData<String, String> choiceStateData = stateDatas.stream()
				.filter(sd -> "CHOICE".equals(sd.getState()))
				.findFirst()
				.get();
		assertThat(choiceStateData).isNotNull();
		assertThat(choiceStateData.getParent()).isEqualTo("S1");
	}

	@Test
	public void testPseudostateInSubmachinerefHaveCorrectParent() {
		context.refresh();
		Resource model = new ClassPathResource("org/springframework/statemachine/uml/pseudostate-in-submachineref.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model);
		assertThat(model.exists()).isTrue();
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();

		assertThat(stateDatas.size()).isEqualTo(4);

		StateData<String, String> choiceStateData = stateDatas.stream()
				.filter(sd -> "CHOICE".equals(sd.getState()))
				.findFirst()
				.get();
		assertThat(choiceStateData).isNotNull();
		assertThat(choiceStateData.getParent()).isEqualTo("S1");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTransitionEffectSpel() {
		context.register(Config27.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("S1");
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds()).contains("S2");
		assertThat(stateMachine.getExtendedState().get("key", String.class)).isEqualTo("value");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportedSubMachine() {
		context.register(Config28.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains("MAIN2", "CHILD2");
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public Action<String, String> action1() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-submachine.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-entryexit.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config6 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-choice.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config6MissingName extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/missingname-choice.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config7 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-forkjoin.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config8 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-history-shallow.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config9 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-history-deep.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config10 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-junction.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public JunctionGuard s5Guard() {
			return new JunctionGuard("s5");
		}

		@Bean
		public JunctionGuard s6Guard() {
			return new JunctionGuard("s6");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config11 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-actions.uml");
		}

		@Bean
		public LatchAction s1Exit() {
			return new LatchAction();
		}

		@Bean
		public LatchAction s2Entry() {
			return new LatchAction();
		}

		@Bean
		public LatchAction e1Action() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config12 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-history-default.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config13 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-timers.uml");
		}

		@Bean
		public LatchAction s3Entry() {
			return new LatchAction();
		}

		@Bean
		public LatchAction s5Entry() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config14 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-guards.uml");
		}

		@Bean
		public SimpleGuard denyGuard() {
			return new SimpleGuard(false);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config15 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/initial-actions.uml");
		}

		@Bean
		public LatchAction initialAction() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config16 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-spels.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config17 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-flat-multiple-to-end.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config18 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-flat-multiple-to-end-viachoices.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config19 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/broken-model-shadowentries.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config20 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/multijoin-forkjoin.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config21 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-submachineref.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config22 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-state-actions.uml");
		}

		@Bean
		public LatchAction e1Action() {
			return new LatchAction();
		}

		@Bean
		public LatchAction e2Action() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config23 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/uml/simple-localtransition.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config24 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-connectionpointref.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config25 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/action-with-transition-choice.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}

		@Bean
		public ChoiceGuard s5Guard() {
			return new ChoiceGuard("s5");
		}

		@Bean
		public ChoiceGuard choice2Guard() {
			return new ChoiceGuard("choice2");
		}

		@Bean
		public LatchAction s1ToChoice() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS4() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choice1ToChoice2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS5() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS6() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config26 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/action-with-transition-junction.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}

		@Bean
		public ChoiceGuard s5Guard() {
			return new ChoiceGuard("s5");
		}

		@Bean
		public ChoiceGuard choice2Guard() {
			return new ChoiceGuard("choice2");
		}

		@Bean
		public LatchAction s1ToChoice() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS4() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choice1ToChoice2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS5() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS6() {
			return new LatchAction();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config27 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/transition-effect-spel.uml");
			return new UmlStateMachineModelFactory(model);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config28 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource mainModel = new ClassPathResource("org/springframework/statemachine/uml/import-main/import-main.uml");
			Resource subModel = new ClassPathResource("org/springframework/statemachine/uml/import-sub/import-sub.uml");
			return new UmlStateMachineModelFactory(mainModel, new Resource[] { subModel });
		}
	}

	public static class LatchAction implements Action<String, String> {
		CountDownLatch latch = new CountDownLatch(1);
		@Override
		public void execute(StateContext<String, String> context) {
			latch.countDown();
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

	private static class SimpleGuard implements Guard<String, String> {

		private final boolean deny;

		public SimpleGuard(boolean deny) {
			this.deny = deny;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return deny;
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

	private static class TestListener extends StateMachineListenerAdapter<String, String> {

		final ArrayList<String> entered = new ArrayList<>();
		final ArrayList<String> exited = new ArrayList<>();

		@Override
		public void stateEntered(State<String, String> state) {
			entered.add(state.getId());
		}

		@Override
		public void stateExited(State<String, String> state) {
			exited.add(state.getId());
		}

		public void reset() {
			entered.clear();
			exited.clear();
		}
	}

	private static class TestExtendedStateListener extends StateMachineListenerAdapter<String, String> {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void extendedStateChanged(Object key, Object value) {
			if (ObjectUtils.nullSafeEquals(key, "hellos2do")) {
				latch.countDown();
			}
		}
	}
}
