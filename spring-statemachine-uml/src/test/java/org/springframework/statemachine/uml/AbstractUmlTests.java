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
package org.springframework.statemachine.uml;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.uml.support.GenericTypeConverter;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;

public abstract class AbstractUmlTests<S, E> {

	protected AnnotationConfigApplicationContext context;
	private final GenericTypeConverter<S> stateConverter;
	private final GenericTypeConverter<E> eventConverter;

	protected AbstractUmlTests(GenericTypeConverter<S> stateConverter, GenericTypeConverter<E> eventConverter) {
		this.stateConverter = stateConverter;
		this.eventConverter = eventConverter;
	}

	@BeforeEach
	public void setup() {
		context = buildContext();
	}

	@AfterEach
	public void clean() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	protected AnnotationConfigApplicationContext buildContext() {
		return null;
	}

	@Test
	public void testSimpleFlat1() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		builder.registerAction("action1", createLatchAction());
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(2);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	protected abstract Action<S, E> createLatchAction();

	@Test
	public void testSimpleFlat2() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
		DefaultStateMachineComponentResolver<S, E> resolver = new DefaultStateMachineComponentResolver<>();
		resolver.registerAction("action1", createLatchAction());
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		builder.setStateMachineComponentResolver(resolver);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(2);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
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
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(4);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getParent()).isNull();
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getParent()).isNull();
			} else if (stateData.getState().equals(stateConverter.convert("S11"))) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getParent()).isEqualTo(stateConverter.convert("S1"));
			} else if (stateData.getState().equals(stateConverter.convert("S12"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getParent()).isEqualTo(stateConverter.convert("S1"));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleRootRegions() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(4);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals(stateConverter.convert("S3"))) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getRegion()).isNotNull();
			} else if (stateData.getState().equals(stateConverter.convert("S4"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getRegion()).isNotNull();
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleFlatEnd() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat-end.uml");
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(3);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.isEnd()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.isEnd()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S3"))) {
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
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(8);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S21"))) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals(stateConverter.convert("S22"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S3"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S4"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("ENTRY"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.ENTRY);
			} else if (stateData.getState().equals(stateConverter.convert("EXIT"))) {
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
		registerConfig2();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		AbstractLatchAction action1 = context.getBean("action1", AbstractLatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(action1.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
	}

	protected abstract void registerConfig2();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineMachine() throws Exception {
		registerConfig3();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"), stateConverter.convert("S11"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"), stateConverter.convert("S12"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
	}

	protected abstract void registerConfig3();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleRootRegionsMachine() throws Exception {
		registerConfig4();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"), stateConverter.convert("S3"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S3"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S4"));
	}

	protected abstract void registerConfig4();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleEntryExitMachine() throws Exception {
		registerConfig5();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E4"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S4"));
	}

	protected abstract void registerConfig5();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice1() {
		registerConfig6();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
	}

	protected abstract void registerConfig6();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice2() {
		registerConfig6();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("choice", "s3").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S3"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleChoice3() {
		registerConfig6();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S4"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMissingNameChoice() {
		registerConfig6MissingName();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
	}

	protected abstract void registerConfig6MissingName();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleForkJoin() {
		registerConfig7();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("SI"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S20"), stateConverter.convert("S30"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S30"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("SF"));
	}

	protected abstract void registerConfig7();

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin1() {
		registerConfig20();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("SI"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S20"), stateConverter.convert("S30"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S30"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S4"));
	}

	protected abstract void registerConfig20();

	@Test
	@SuppressWarnings("unchecked")
	public void testMultiJoinForkJoin2() {
		registerConfig20();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		stateMachine.getExtendedState().getVariables().put("foo", "bar");
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("SI"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S20"), stateConverter.convert("S30"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S30"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("SF"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryShallow() {
		registerConfig8();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S20"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E4"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
	}

	protected abstract void registerConfig8();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleHistoryDeep() {
		registerConfig9();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S211"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S212"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E4"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S212"));
	}

	protected abstract void registerConfig9();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction1() {
		registerConfig10();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E4")).setHeader("junction", "s5").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S5"));
	}

	protected abstract void registerConfig10();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction2() {
		registerConfig10();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S3"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E4")).setHeader("junction", "s6").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S6"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleJunction3() {
		registerConfig10();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S4"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E4"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S7"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleActions() throws Exception {
		registerConfig11();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		TestExtendedStateListener listener = new TestExtendedStateListener();
		stateMachine.addStateListener(listener);
		AbstractLatchAction e1Action = context.getBean("e1Action", AbstractLatchAction.class);
		AbstractLatchAction s1Exit = context.getBean("s1Exit", AbstractLatchAction.class);
		AbstractLatchAction s2Entry = context.getBean("s2Entry", AbstractLatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(s1Exit.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(s2Entry.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getExtendedState().getVariables().get("hellos2do")).isEqualTo("hellos2dovalue");
	}

	protected abstract void registerConfig11();

	@Test
	public void testSimpleEventDefer() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-eventdefer.uml");
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(3);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
				assertThat(stateData.getDeferred().size()).isEqualTo(1);
				assertThat(stateData.getDeferred().iterator().next()).isEqualTo(eventConverter.convert("E2"));
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getDeferred().size()).isEqualTo(0);
			} else if (stateData.getState().equals(stateConverter.convert("S3"))) {
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
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		Collection<TransitionData<S, E>> transitionDatas = stateMachineModel.getTransitionsData().getTransitions();
		assertThat(stateDatas.size()).isEqualTo(2);
		assertThat(transitionDatas.size()).isEqualTo(4);
		for (TransitionData<S, E> transitionData : transitionDatas) {
			if (transitionData.getEvent() != null) {
				if (transitionData.getEvent().equals(eventConverter.convert("E1"))) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.EXTERNAL);
				} else if (transitionData.getEvent().equals(eventConverter.convert("E2"))) {
					assertThat(transitionData.getKind()).isEqualTo(TransitionKind.LOCAL);
				} else if (transitionData.getEvent().equals(eventConverter.convert("E3"))) {
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
		registerConfig12();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E4"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
	}

	protected abstract void registerConfig12();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers1() throws Exception {
		registerConfig13();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		AbstractLatchAction s3Entry = context.getBean("s3Entry", AbstractLatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(s3Entry.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S3"));
	}

	protected abstract void registerConfig13();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleTimers2() throws Exception {
		registerConfig13();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		AbstractLatchAction s5Entry = context.getBean("s5Entry", AbstractLatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(s5Entry.latch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S5"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny1() throws Exception {
		registerConfig14();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
	}

	protected abstract void registerConfig14();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleGuardsDeny2() throws Exception {
		registerConfig14();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S3"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testInitialActions() throws Exception {
		registerConfig15();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		AbstractLatchAction initialAction = context.getBean("initialAction", AbstractLatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		assertThat(initialAction.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	protected abstract void registerConfig15();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsAllow() throws Exception {
		registerConfig16();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("foo", "bar").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
		assertThat(stateMachine.getExtendedState().get("myvar1", String.class)).isEqualTo("myvalue1");
		assertThat(stateMachine.getExtendedState().get("myvar2", String.class)).isEqualTo("myvalue2");
	}

	protected abstract void registerConfig16();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSpelsDeny() throws Exception {
		registerConfig16();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
	}

	@Test
	public void testSimpleFlatMultipleToEnds() throws Exception {
		registerConfig17();
		context.refresh();
	}

	protected abstract void registerConfig17();

	@Test
	public void testSimpleFlatMultipleToEndsViachoices() throws Exception {
		registerConfig18();
		context.refresh();
	}

	protected abstract void registerConfig18();

	@Test
	public void testBrokenModelShadowEntries() throws Exception {
		registerConfig19();
		context.refresh();
	}

	protected abstract void registerConfig19();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineRef() throws Exception {
		registerConfig21();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S20"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S30"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"), stateConverter.convert("S31"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E4"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S3"));
	}

	protected abstract void registerConfig21();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleStateActions() throws Exception {
		registerConfig22();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		AbstractLatchAction e1Action = context.getBean("e1Action", AbstractLatchAction.class);
		AbstractLatchAction e2Action = context.getBean("e2Action", AbstractLatchAction.class);
		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		assertThat(e1Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
		assertThat(e2Action.latch.await(1, TimeUnit.SECONDS)).isTrue();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E2"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S3"));
	}

	protected abstract void registerConfig22();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToSub() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		String ids = "" + stateMachine.getState().getIds();
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E20"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
	}

	protected abstract void registerConfig23();

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToSub() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E30"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains(stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToSub() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E21"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToSub() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E31"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains(stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalSuperDoesEntryExitToParent() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E22"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalSuperDoesNotEntryExitToParent() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E32"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains(stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S21"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionExternalToNonInitialSuperDoesEntryExitToParent() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E21"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E23"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		assertThat(listener.exited.size()).isEqualTo(2);
		assertThat(listener.entered.size()).isEqualTo(2);
		assertThat(listener.exited).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		assertThat(listener.entered).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleLocaltransitionLocalToNonInitialSuperDoesNotEntryExitToParent() {
		registerConfig23();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);
		assertThat(stateMachine).isNotNull();
		TestListener listener = new TestListener();
		stateMachine.addStateListener(listener);
		doStartAndAssert(stateMachine);
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E1"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S21"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E31"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains(stateConverter.convert("S21"));
		assertThat(listener.entered).contains(stateConverter.convert("S22"));

		listener.reset();
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E33"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		assertThat(listener.exited.size()).isEqualTo(1);
		assertThat(listener.entered.size()).isEqualTo(1);
		assertThat(listener.exited).contains(stateConverter.convert("S22"));
		assertThat(listener.entered).contains(stateConverter.convert("S22"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleConnectionPointRefMachine() throws Exception {
		registerConfig24();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E3"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"), stateConverter.convert("S22"));
		doSendEventAndConsumeAll(stateMachine, eventConverter.convert("E4"));
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S4"));
	}

	protected abstract void registerConfig24();

	@Test
	public void testConnectionPointRef() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-connectionpointref.uml");
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model1, stateConverter, eventConverter);
		builder.setBeanFactory(context);
		assertThat(model1.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size()).isEqualTo(8);
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getState().equals(stateConverter.convert("S1"))) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals(stateConverter.convert("S2"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S21"))) {
				assertThat(stateData.isInitial()).isTrue();
			} else if (stateData.getState().equals(stateConverter.convert("S22"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S3"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("S4"))) {
				assertThat(stateData.isInitial()).isFalse();
			} else if (stateData.getState().equals(stateConverter.convert("ENTRY"))) {
				assertThat(stateData.isInitial()).isFalse();
				assertThat(stateData.getPseudoStateKind()).isEqualTo(PseudoStateKind.ENTRY);
			} else if (stateData.getState().equals(stateConverter.convert("EXIT"))) {
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
		registerConfig25();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		AbstractLatchAction s1ToChoice = context.getBean("s1ToChoice", AbstractLatchAction.class);
		AbstractLatchAction choiceToS2 = context.getBean("choiceToS2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS4 = context.getBean("choiceToS4", AbstractLatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
	}

	protected abstract void registerConfig25();

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice2() throws InterruptedException {
		registerConfig25();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		AbstractLatchAction s1ToChoice = context.getBean("s1ToChoice", AbstractLatchAction.class);
		AbstractLatchAction choiceToS2 = context.getBean("choiceToS2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS4 = context.getBean("choiceToS4", AbstractLatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S4"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionChoice3() throws InterruptedException {
		registerConfig25();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		AbstractLatchAction s1ToChoice = context.getBean("s1ToChoice", AbstractLatchAction.class);
		AbstractLatchAction choiceToS2 = context.getBean("choiceToS2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS4 = context.getBean("choiceToS4", AbstractLatchAction.class);
		AbstractLatchAction choice1ToChoice2 = context.getBean("choice1ToChoice2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS5 = context.getBean("choiceToS5", AbstractLatchAction.class);
		AbstractLatchAction choiceToS6 = context.getBean("choiceToS6", AbstractLatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S6"));

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
		registerConfig26();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		AbstractLatchAction s1ToChoice = context.getBean("s1ToChoice", AbstractLatchAction.class);
		AbstractLatchAction choiceToS2 = context.getBean("choiceToS2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS4 = context.getBean("choiceToS4", AbstractLatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("choice", "s2").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isFalse();
	}

	protected abstract void registerConfig26();

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction2() throws InterruptedException {
		registerConfig26();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		AbstractLatchAction s1ToChoice = context.getBean("s1ToChoice", AbstractLatchAction.class);
		AbstractLatchAction choiceToS2 = context.getBean("choiceToS2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS4 = context.getBean("choiceToS4", AbstractLatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S4"));

		assertThat(s1ToChoice.latch.await(1, TimeUnit.SECONDS)).isTrue();
		assertThat(choiceToS2.latch.await(1, TimeUnit.SECONDS)).isFalse();
		assertThat(choiceToS4.latch.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testActionWithTransitionJunction3() throws InterruptedException {
		registerConfig26();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		AbstractLatchAction s1ToChoice = context.getBean("s1ToChoice", AbstractLatchAction.class);
		AbstractLatchAction choiceToS2 = context.getBean("choiceToS2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS4 = context.getBean("choiceToS4", AbstractLatchAction.class);
		AbstractLatchAction choice1ToChoice2 = context.getBean("choice1ToChoice2", AbstractLatchAction.class);
		AbstractLatchAction choiceToS5 = context.getBean("choiceToS5", AbstractLatchAction.class);
		AbstractLatchAction choiceToS6 = context.getBean("choiceToS6", AbstractLatchAction.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).setHeader("choice", "choice2").build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S6"));

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
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model, stateConverter, eventConverter);
		assertThat(model.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();

		assertThat(stateDatas.size()).isEqualTo(4);

		StateData<S, E> choiceStateData = stateDatas.stream()
				.filter(sd -> stateConverter.convert("CHOICE").equals(sd.getState()))
				.findFirst()
				.get();
		assertThat(choiceStateData).isNotNull();
		assertThat(choiceStateData.getParent()).isEqualTo(stateConverter.convert("S1"));
	}

	@Test
	public void testPseudostateInSubmachinerefHaveCorrectParent() {
		context.refresh();
		Resource model = new ClassPathResource("org/springframework/statemachine/uml/pseudostate-in-submachineref.uml");
		GenericUmlStateMachineModelFactory<S, E> builder = new GenericUmlStateMachineModelFactory<S, E>(model, stateConverter, eventConverter);
		assertThat(model.exists()).isTrue();
		StateMachineModel<S, E> stateMachineModel = builder.build();
		assertThat(stateMachineModel).isNotNull();
		Collection<StateData<S, E>> stateDatas = stateMachineModel.getStatesData().getStateData();

		assertThat(stateDatas.size()).isEqualTo(4);

		StateData<S, E> choiceStateData = stateDatas.stream()
				.filter(sd -> stateConverter.convert("CHOICE").equals(sd.getState()))
				.findFirst()
				.get();
		assertThat(choiceStateData).isNotNull();
		assertThat(choiceStateData.getParent()).isEqualTo(stateConverter.convert("S1"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testTransitionEffectSpel() {
		registerConfig27();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S1"));
		doSendEventAndConsumeAll(stateMachine, MessageBuilder.withPayload(eventConverter.convert("E1")).build());
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("S2"));
		assertThat(stateMachine.getExtendedState().get("key", String.class)).isEqualTo("value");
	}

	protected abstract void registerConfig27();

	@Test
	@SuppressWarnings("unchecked")
	public void testImportedSubMachine() {
		registerConfig28();
		context.refresh();
		StateMachine<S, E> stateMachine = context.getBean(StateMachine.class);

		doStartAndAssert(stateMachine);
		assertThat(stateMachine.getState().getIds()).contains(stateConverter.convert("MAIN2"), stateConverter.convert("CHILD2"));
	}

	protected abstract void registerConfig28();


	public class TestListener<S, E> extends StateMachineListenerAdapter<S, E> {

		final ArrayList<S> entered = new ArrayList<>();
		final ArrayList<S> exited = new ArrayList<>();

		@Override
		public void stateEntered(State<S, E> state) {
			entered.add(state.getId());
		}

		@Override
		public void stateExited(State<S, E> state) {
			exited.add(state.getId());
		}

		public void reset() {
			entered.clear();
			exited.clear();
		}
	}

	private static class TestExtendedStateListener<S, E> extends StateMachineListenerAdapter<S, E> {

		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void extendedStateChanged(Object key, Object value) {
			if (ObjectUtils.nullSafeEquals(key, "hellos2do")) {
				latch.countDown();
			}
		}
	}

}
