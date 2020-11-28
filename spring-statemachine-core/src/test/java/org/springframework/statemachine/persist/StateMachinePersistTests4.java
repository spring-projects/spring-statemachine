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
package org.springframework.statemachine.persist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.OrderedComposite;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.CompositePseudoStateListener;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class StateMachinePersistTests4 extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testPersistRegions() throws Exception {
		context.register(Config1.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);
		assertThat(stateMachine).isNotNull();
		assertThat(stateMachine.getId()).isEqualTo("testid");

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		persister.persist(stateMachine, "xxx2");

		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
		persister.persist(stateMachine, "xxx3");

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine).isNotNull();
		assertThat(stateMachine.getId()).isNull();
		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine).isNotNull();
		assertThat(stateMachine.getId()).isNull();
		stateMachine = persister.restore(stateMachine, "xxx2");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		stateMachine = stateMachineFactory.getStateMachine();
		assertThat(stateMachine).isNotNull();
		assertThat(stateMachine.getId()).isNull();
		stateMachine = persister.restore(stateMachine, "xxx3");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsNotEnteredJoinStates() throws Exception {
		context.register(Config1.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = stateMachineFactory.getStateMachine();
		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsNotEnteredJoinStatesRestoreTwice() throws Exception {
		context.register(Config1.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = stateMachineFactory.getStateMachine();
		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsPartialEnteredJoinStates() throws Exception {
		context.register(Config1.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsPartialEnteredJoinStatesRestoreTwice() throws Exception {
		context.register(Config1.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsNotEnteredJoinStatesWithEnds() throws Exception {
		context.register(Config2.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = stateMachineFactory.getStateMachine();
		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsNotEnteredJoinStatesRestoreTwiceWithEnds() throws Exception {
		context.register(Config2.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = stateMachineFactory.getStateMachine();
		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsPartialEnteredJoinStatesWithEnds() throws Exception {
		context.register(Config2.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}

	@Test
	public void testJoinAfterPersistRegionsPartialEnteredJoinStatesRestoreTwiceWithEnds() throws Exception {
		context.register(Config2.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);

		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		StateMachine<TestStates, TestEvents> stateMachine = stateMachineFactory.getStateMachine("testid");
		doStartAndAssert(stateMachine);

		doSendEventAndConsumeAll(stateMachine, TestEvents.E1);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E2);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		persister.persist(stateMachine, "xxx1");

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);

		stateMachine = persister.restore(stateMachine, "xxx1");
		assertThat(stateMachine.getId()).isEqualTo("testid");
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		doSendEventAndConsumeAll(stateMachine, TestEvents.E3);
		assertThat(stateMachine.getState().getIds()).containsOnly(TestStates.S4);
	}


	@Test
	public void testJoinFromSuperAfterPersistRegions() throws Exception {
		context.register(Config3.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);
		StateMachine<TestStates, TestEvents> machine = stateMachineFactory.getStateMachine("testid");
		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		listener.reset(1);
		assertThat(machine).isNotNull();
		doStartAndAssert(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(3);
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(3);
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		persister.persist(machine, "xxx1");
		machine = persister.restore(machine, "xxx1");

		listener.reset(1);
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		listener.reset(2);
		doSendEventAndConsumeAll(machine, TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);

		assertThat(machine.getState().getIds()).containsExactly(TestStates.S4);

		// try fresh machine
		machine = stateMachineFactory.getStateMachine("testid");
		machine = persister.restore(machine, "xxx1");
		machine.addStateListener(listener);

		listener.reset(1);
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		listener.reset(2);
		doSendEventAndConsumeAll(machine, TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);

		assertThat(machine.getState().getIds()).containsExactly(TestStates.S4);
	}

	@Test
	public void testJoinFromSuperAfterPersistRegionsPartial() throws Exception {
		context.register(Config3.class);
		context.refresh();

		StateMachineFactory<TestStates, TestEvents> stateMachineFactory = resolveFactory(context);
		StateMachine<TestStates, TestEvents> machine = stateMachineFactory.getStateMachine("testid");
		InMemoryStateMachinePersist stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<TestStates, TestEvents, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);

		TestListener listener = new TestListener();
		machine.addStateListener(listener);
		listener.reset(1);
		assertThat(machine).isNotNull();
		doStartAndAssert(machine);
		assertPseudoStatesHaveOneListener(machine);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(3);
		doSendEventAndConsumeAll(machine, TestEvents.E1);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(3);
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S20, TestStates.S30);

		listener.reset(1);
		doSendEventAndConsumeAll(machine, TestEvents.E2);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);

		persister.persist(machine, "xxx1");

		machine = persister.restore(machine, "xxx1");
		assertPseudoStatesHaveOneListener(machine);
		listener.reset(2);
		doSendEventAndConsumeAll(machine, TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S4);


		machine = persister.restore(machine, "xxx1");
		assertPseudoStatesHaveOneListener(machine);
		listener.reset(2);
		doSendEventAndConsumeAll(machine, TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S4);

		// try fresh machine
		machine = stateMachineFactory.getStateMachine("testid");
		machine = persister.restore(machine, "xxx1");
		machine.addStateListener(listener);
		assertThat(machine.getState().getIds()).containsOnly(TestStates.S2, TestStates.S21, TestStates.S30);
		listener.reset(2);
		doSendEventAndConsumeAll(machine, TestEvents.E3);
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(TestStates.S4);
		assertPseudoStatesHaveOneListener(machine);
	}

	private void assertPseudoStatesHaveOneListener(Object machine) throws Exception {
		Collection<State<?,?>> states = TestUtils.readField("states", machine);
		for (State<?,?> s : states) {
			PseudoState<?, ?> ps = s.getPseudoState();
			if (ps != null) {
				CompositePseudoStateListener<?, ?> pseudoStateListener = TestUtils.readField("pseudoStateListener", ps);
				OrderedComposite<?> listeners = TestUtils.readField("listeners", pseudoStateListener);
				List<?> list = TestUtils.readField("list", listeners);
				assertThat(list).hasSize(1);
			}
		}
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

	@Configuration
	@EnableStateMachineFactory
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

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
					.end(TestStates.S4)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.end(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30)
						.end(TestStates.S31);
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

	@Configuration
	@EnableStateMachineFactory
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.S2)
					.join(TestStates.S3)
					.state(TestStates.S4)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.end(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.end(TestStates.S31);
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
				.withJoin()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.SI)
					.event(TestEvents.E4);
		}

	}

	private static class TestListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;
		final List<Transition<TestStates, TestEvents>> transitions = new ArrayList<Transition<TestStates,TestEvents>>();
		final List<State<TestStates, TestEvents>> tos = new ArrayList<>();

		@Override
		public void stateChanged(State<TestStates, TestEvents> from, State<TestStates, TestEvents> to) {
			tos.add(to);
			stateChangedCount++;
			stateChangedLatch.countDown();
		}

		@Override
		public void transition(Transition<TestStates, TestEvents> transition) {
			transitions.add(transition);
			transitionLatch.countDown();
		}

		public void reset(int c1) {
			reset(c1, 0);
		}

		public void reset(int c1, int c2) {
			stateChangedLatch = new CountDownLatch(c1);
			transitionLatch = new CountDownLatch(c2);
			stateChangedCount = 0;
			transitions.clear();
			tos.clear();
		}
	}

	static class InMemoryStateMachinePersist implements StateMachinePersist<TestStates, TestEvents, String> {

		private final HashMap<String, StateMachineContext<TestStates, TestEvents>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<TestStates, TestEvents> context, String contextObj) throws Exception {
			contexts.put(contextObj, context);
		}

		@Override
		public StateMachineContext<TestStates, TestEvents> read(String contextObj) throws Exception {
			return contexts.get(contextObj);
		}
	}
}
