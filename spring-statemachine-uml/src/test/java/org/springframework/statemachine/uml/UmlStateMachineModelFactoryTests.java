/*
 * Copyright 2016-2018 the original author or authors.
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
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
import org.springframework.statemachine.state.State;
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
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(2));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
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
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(2));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
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
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(4));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getParent(), nullValue());
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getParent(), nullValue());
			} else if (stateData.getState().equals("S11")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getParent(), is("S1"));
			} else if (stateData.getState().equals("S12")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getParent(), is("S1"));
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
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(4));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getRegion(), notNullValue());
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getRegion(), notNullValue());
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getRegion(), notNullValue());
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getRegion(), notNullValue());
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleFlatEnd() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat-end.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(3));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.isEnd(), is(false));
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.isEnd(), is(false));
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isEnd(), is(true));
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
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(8));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("S21")) {
				assertThat(stateData.isInitial(), is(true));
			} else if (stateData.getState().equals("S22")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("ENTRY")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getPseudoStateKind(), is(PseudoStateKind.ENTRY));
			} else if (stateData.getState().equals("EXIT")) {
				assertThat(stateData.getPseudoStateKind(), is(PseudoStateKind.EXIT));
				assertThat(stateData.isInitial(), is(false));
			} else {
				throw new IllegalArgumentException();
			}
		}
		assertThat(stateMachineModel.getTransitionsData().getEntrys().size(), is(1));
		assertThat(stateMachineModel.getTransitionsData().getExits().size(), is(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleFlatMachine() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction action1 = context.getBean("action1", LatchAction.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(action1.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineMachine() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1", "S11"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S1", "S12"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleRootRegionsMachine() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1", "S3"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S3"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S4"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleEntryExitMachine() throws Exception {
		context.register(Config5.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		stateMachine.sendEvent("E4");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S4"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice1() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice2() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s3").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S3"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice3() {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S4"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleForkJoin() {
		context.register(Config7.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("SI"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S20", "S30"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S30"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("SF"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin1() {
		context.register(Config20.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("SI"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S20", "S30"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S30"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S4"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin2() {
		context.register(Config20.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		stateMachine.getExtendedState().getVariables().put("foo", "bar");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("SI"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S20", "S30"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S30"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("SF"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryShallow() {
		context.register(Config8.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S20"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E4");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryDeep() {
		context.register(Config9.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S211"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S212"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E4");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S212"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction1() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E4").setHeader("junction", "s5").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S5"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction2() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S3"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E4").setHeader("junction", "s6").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S6"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction3() {
		context.register(Config10.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S4"));
		stateMachine.sendEvent("E4");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S7"));
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
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(s1Exit.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(s2Entry.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getExtendedState().getVariables().get("hellos2do"), is("hellos2dovalue"));
	}

	@Test
	public void testSimpleEventDefer() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-eventdefer.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(3));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getDeferred().size(), is(1));
				assertThat(stateData.getDeferred().iterator().next(), is("E2"));
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getDeferred().size(), is(0));
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getDeferred().size(), is(0));
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
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		Collection<TransitionData<String, String>> transitionDatas = stateMachineModel.getTransitionsData().getTransitions();
		assertThat(stateDatas.size(), is(2));
		assertThat(transitionDatas.size(), is(3));
		for (TransitionData<String, String> transitionData : transitionDatas) {
			if (transitionData.getEvent().equals("E1")) {
				assertThat(transitionData.getKind(), is(TransitionKind.EXTERNAL));
			} else if (transitionData.getEvent().equals("E2")) {
				assertThat(transitionData.getKind(), is(TransitionKind.LOCAL));
			} else if (transitionData.getEvent().equals("E3")) {
				assertThat(transitionData.getKind(), is(TransitionKind.INTERNAL));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryDefault() {
		context.register(Config12.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E4");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers1() throws Exception {
		context.register(Config13.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction s3Entry = context.getBean("s3Entry", LatchAction.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(s3Entry.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S3"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers2() throws Exception {
		context.register(Config13.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction s5Entry = context.getBean("s5Entry", LatchAction.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E2");
		assertThat(s5Entry.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S5"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny1() throws Exception {
		context.register(Config14.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny2() throws Exception {
		context.register(Config14.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S3"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testInitialActions() throws Exception {
		context.register(Config15.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction initialAction = context.getBean("initialAction", LatchAction.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		assertThat(initialAction.latch.await(1, TimeUnit.SECONDS), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsAllow() throws Exception {
		context.register(Config16.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").setHeader("foo", "bar").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
		assertThat(stateMachine.getExtendedState().get("myvar1", String.class), is("myvalue1"));
		assertThat(stateMachine.getExtendedState().get("myvar2", String.class), is("myvalue2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsDeny() throws Exception {
		context.register(Config16.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
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
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S20"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S30"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21", "S31"));
		stateMachine.sendEvent("E4");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S3"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleStateActions() throws Exception {
		context.register(Config22.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction e1Action = context.getBean("e1Action", LatchAction.class);
		LatchAction e2Action = context.getBean("e2Action", LatchAction.class);
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS), is(true));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
		assertThat(e2Action.latch.await(1, TimeUnit.SECONDS), is(true));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S3"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E20");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E30");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E21");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToSub() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E31");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E22");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E32");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E21");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S21"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S22"));

		listener.reset();
		stateMachine.sendEvent("E23");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(2));
		assertThat(listener.entered.size(), is(2));
		assertThat(listener.exited, containsInAnyOrder("S2", "S22"));
		assertThat(listener.entered, containsInAnyOrder("S2", "S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToParent() {
		context.register(Config23.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine, notNullValue());
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		stateMachine.start();
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S21"));

		listener.reset();
		stateMachine.sendEvent("E31");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S21"));
		assertThat(listener.entered, containsInAnyOrder("S22"));

		listener.reset();
		stateMachine.sendEvent("E33");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		assertThat(listener.exited.size(), is(1));
		assertThat(listener.entered.size(), is(1));
		assertThat(listener.exited, containsInAnyOrder("S22"));
		assertThat(listener.entered, containsInAnyOrder("S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleConnectionPointRefMachine() throws Exception {
		context.register(Config24.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent("E3");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S22"));
		stateMachine.sendEvent("E4");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S4"));
	}

	@Test
	public void testConnectionPointRef() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-connectionpointref.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(8));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("S21")) {
				assertThat(stateData.isInitial(), is(true));
			} else if (stateData.getState().equals("S22")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial(), is(false));
			} else if (stateData.getState().equals("ENTRY")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getPseudoStateKind(), is(PseudoStateKind.ENTRY));
			} else if (stateData.getState().equals("EXIT")) {
				assertThat(stateData.getPseudoStateKind(), is(PseudoStateKind.EXIT));
				assertThat(stateData.isInitial(), is(false));
			} else {
				throw new IllegalArgumentException();
			}
		}
		assertThat(stateMachineModel.getTransitionsData().getEntrys().size(), is(1));
		assertThat(stateMachineModel.getTransitionsData().getExits().size(), is(1));
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

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS), is(false));
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

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S4"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS), is(true));
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

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S6"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choice1ToChoice2.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS5.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choiceToS6.latch.await(1, TimeUnit.SECONDS), is(true));
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

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS), is(false));
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

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S4"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS), is(true));
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

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
		stateMachine.sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S6"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choice1ToChoice2.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(choiceToS5.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(choiceToS6.latch.await(1, TimeUnit.SECONDS), is(true));
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
