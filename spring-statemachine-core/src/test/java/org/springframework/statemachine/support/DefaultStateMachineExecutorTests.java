/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineExecutor.StateMachineExecutorTransit;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.EventTrigger;
import org.springframework.statemachine.trigger.TimerTrigger;
import org.springframework.statemachine.trigger.Trigger;

public class DefaultStateMachineExecutorTests {

	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleExecute() throws Exception {

		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		Message<String> message = MessageBuilder.withPayload("E1").build();

		EventTrigger<String, String> triggerE1 = new EventTrigger<String, String>("E1");

		State<String, String> stateS1 = mock(State.class);
		when(stateS1.getId()).thenReturn("S1");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S1"));
		State<String, String> stateS2 = mock(State.class);
		when(stateS2.getId()).thenReturn("S2");
		when(stateS2.getIds()).thenReturn(Arrays.asList("S2"));

		Transition<String, String> transitionS1S2 = mock(Transition.class);
		when(transitionS1S2.getSource()).thenReturn(stateS1);
		when(transitionS1S2.getTarget()).thenReturn(stateS2);
		when(transitionS1S2.getTrigger()).thenReturn(triggerE1);
		when(transitionS1S2.transit(any())).thenReturn(true);

		StateMachine<String, String> stateMachine = mock(StateMachine.class);
		when(stateMachine.getState()).thenReturn(stateS1);

		Collection<Transition<String, String>> transitions = new ArrayList<>();
		transitions.add(transitionS1S2);

		Map<Trigger<String, String>, Transition<String, String>> triggerToTransitionMap = new HashMap<>();
		triggerToTransitionMap.put(triggerE1, transitionS1S2);

		List<Transition<String, String>> triggerlessTransitions = new ArrayList<>();

		Transition<String, String> initialTransition = mock(Transition.class);
		Message<String> initialEvent = null;

		DefaultStateMachineExecutor<String, String> executor = new DefaultStateMachineExecutor<>(
				stateMachine,
				stateMachine,
				transitions,
				triggerToTransitionMap,
				triggerlessTransitions,
				initialTransition,
				initialEvent,
				null);

		executor.setTaskExecutor(taskExecutor);

		TestStateMachineExecutorTransit transit = new TestStateMachineExecutorTransit();
		transit.reset(2);
		executor.setStateMachineExecutorTransit(transit);
		executor.start();

		executor.queueEvent(message);
		executor.execute();

		assertThat(transit.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(transit.transitions.size(), is(2));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleTimer() throws Exception {
		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler();

		EventTrigger<String, String> triggerE1 = new EventTrigger<String, String>("E1");

		TimerTrigger<String, String> triggerTimer = new TimerTrigger<>(1000, 1);
		triggerTimer.setTaskScheduler(taskScheduler);

		State<String, String> stateS1 = mock(State.class);
		when(stateS1.getId()).thenReturn("S1");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S1"));
		State<String, String> stateS2 = mock(State.class);
		when(stateS1.getId()).thenReturn("S2");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S2"));
		State<String, String> stateS3 = mock(State.class);
		when(stateS1.getId()).thenReturn("S3");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S3"));

		Transition<String, String> transitionS1S2 = mock(Transition.class);
		when(transitionS1S2.getSource()).thenReturn(stateS1);
		when(transitionS1S2.getTarget()).thenReturn(stateS2);
		when(transitionS1S2.getTrigger()).thenReturn(triggerE1);
		when(transitionS1S2.transit(any())).thenReturn(true);

		Transition<String, String> transitionS1S3 = mock(Transition.class);
		when(transitionS1S3.getSource()).thenReturn(stateS1);
		when(transitionS1S3.getTarget()).thenReturn(stateS3);
		when(transitionS1S3.getTrigger()).thenReturn(triggerTimer);
		when(transitionS1S3.transit(any())).thenReturn(true);


		StateMachine<String, String> stateMachine = mock(StateMachine.class);
		when(stateMachine.getState()).thenReturn(stateS1);

		Collection<Transition<String, String>> transitions = new ArrayList<>();
		transitions.add(transitionS1S2);

		Map<Trigger<String, String>, Transition<String, String>> triggerToTransitionMap = new HashMap<>();
		triggerToTransitionMap.put(triggerE1, transitionS1S2);
		triggerToTransitionMap.put(triggerTimer, transitionS1S3);

		List<Transition<String, String>> triggerlessTransitions = new ArrayList<>();

		Transition<String, String> initialTransition = mock(Transition.class);
		Message<String> initialEvent = null;

		DefaultStateMachineExecutor<String, String> executor = new DefaultStateMachineExecutor<>(
				stateMachine,
				stateMachine,
				transitions,
				triggerToTransitionMap,
				triggerlessTransitions,
				initialTransition,
				initialEvent,
				null);

		executor.setTaskExecutor(taskExecutor);

		TestStateMachineExecutorTransit transit = new TestStateMachineExecutorTransit();
		transit.reset(2);
		executor.setStateMachineExecutorTransit(transit);
		executor.start();

		triggerTimer.start();
		triggerTimer.arm();

		assertThat(transit.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(transit.transitions.size(), is(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDeadlock() throws Exception {
		// gh-315
		// nasty, with deadlock you can't use junit timeout
		// as then test is run on different thread, thus test doesn't fail.
		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		ConcurrentTaskScheduler taskScheduler = new ConcurrentTaskScheduler();

		EventTrigger<String, String> triggerE1 = new EventTrigger<String, String>("E1");

		TimerTrigger<String, String> triggerTimer = new TimerTrigger<>(1000);
		triggerTimer.setTaskScheduler(taskScheduler);

		State<String, String> stateS1 = mock(State.class);
		when(stateS1.getId()).thenReturn("S1");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S1"));
		State<String, String> stateS2 = mock(State.class);
		when(stateS1.getId()).thenReturn("S2");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S2"));
		State<String, String> stateS3 = mock(State.class);
		when(stateS1.getId()).thenReturn("S3");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S3"));

		Transition<String, String> transitionS1S2 = mock(Transition.class);
		when(transitionS1S2.getSource()).thenReturn(stateS1);
		when(transitionS1S2.getTarget()).thenReturn(stateS2);
		when(transitionS1S2.getTrigger()).thenReturn(triggerE1);
		when(transitionS1S2.transit(any())).thenReturn(true);

		Transition<String, String> transitionS1S3 = mock(Transition.class);
		when(transitionS1S3.getSource()).thenReturn(stateS1);
		when(transitionS1S3.getTarget()).thenReturn(stateS3);
		when(transitionS1S3.getTrigger()).thenReturn(triggerTimer);
		when(transitionS1S3.transit(any())).thenReturn(true);


		StateMachine<String, String> stateMachine = mock(StateMachine.class);
		when(stateMachine.getState()).thenReturn(stateS1);

		Collection<Transition<String, String>> transitions = new ArrayList<>();
		transitions.add(transitionS1S2);

		Map<Trigger<String, String>, Transition<String, String>> triggerToTransitionMap = new HashMap<>();
		triggerToTransitionMap.put(triggerE1, transitionS1S2);
		triggerToTransitionMap.put(triggerTimer, transitionS1S3);

		List<Transition<String, String>> triggerlessTransitions = new ArrayList<>();

		Transition<String, String> initialTransition = mock(Transition.class);
		Message<String> initialEvent = null;

		DefaultStateMachineExecutor<String, String> executor = new DefaultStateMachineExecutor<>(
				stateMachine,
				stateMachine,
				transitions,
				triggerToTransitionMap,
				triggerlessTransitions,
				initialTransition,
				initialEvent,
				null);

		executor.setTaskExecutor(taskExecutor);

		TestStateMachineExecutorTransit transit = new TestStateMachineExecutorTransit();
		transit.reset(2);
		executor.setStateMachineExecutorTransit(transit);
		executor.start();

		triggerTimer.start();
		assertThat(transit.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(transit.transitions.size(), is(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEventPolling() throws Exception {
		// event polling should continue even if an event is no more relevant.
		SyncTaskExecutor taskExecutor = new SyncTaskExecutor();
		final CountDownLatch latch = new CountDownLatch(1);
		EventTrigger<String, String> triggerE1 = new EventTrigger<String, String>("E1");

		State<String, String> stateS1 = mock(State.class);
		when(stateS1.getId()).thenReturn("S1");
		when(stateS1.getIds()).thenReturn(Arrays.asList("S1"));
		State<String, String> stateS2 = mock(State.class);
		when(stateS2.getId()).thenReturn("S2");
		when(stateS2.getIds()).thenReturn(Arrays.asList("S2"));

		StateMachine<String, String> stateMachine = mock(StateMachine.class);
		when(stateMachine.getState()).thenReturn(stateS1);

		Transition<String, String> transitionS1S2 = mock(Transition.class);
		when(transitionS1S2.getSource()).thenReturn(stateS1);
		when(transitionS1S2.getTarget()).thenReturn(stateS2);
		when(transitionS1S2.getTrigger()).thenReturn(triggerE1);
		when(transitionS1S2.transit(any())).thenAnswer(x -> {
			when(stateMachine.getState()).thenReturn(stateS2);
			return true;
		});

		Collection<Transition<String, String>> transitions = new ArrayList<>();
		transitions.add(transitionS1S2);

		Map<Trigger<String, String>, Transition<String, String>> triggerToTransitionMap = new HashMap<>();
		triggerToTransitionMap.put(triggerE1, transitionS1S2);

		List<Transition<String, String>> triggerlessTransitions = new ArrayList<>();

		Transition<String, String> initialTransition = mock(Transition.class);
		Message<String> initialEvent = null;

		DefaultStateMachineExecutor<String, String> executor = new DefaultStateMachineExecutor<>(
				stateMachine,
				stateMachine,
				transitions,
				triggerToTransitionMap,
				triggerlessTransitions,
				initialTransition,
				initialEvent,
				null);

		executor.setTaskExecutor(taskExecutor);

		executor.setStateMachineExecutorTransit((x, y, z) -> latch.countDown());
		executor.start();
		//E2 should not stuck the event polling as it is not relevant.
		executor.queueEvent(new GenericMessage<>("E2"));
		executor.queueEvent(new GenericMessage<>("E1"));
		executor.execute();
		latch.await(1, TimeUnit.SECONDS);
		assertThat(stateMachine.getState().getId(), is(stateS2.getId()));
	}

	private static class TestStateMachineExecutorTransit implements StateMachineExecutorTransit<String, String> {

		ArrayList<Transition<String, String>> transitions = new ArrayList<>();
		CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void transit(Transition<String, String> transition, StateContext<String, String> stateContext, Message<String> message) {
			transitions.add(transition);
			latch.countDown();
		}

		void reset(int i) {
			latch = new CountDownLatch(i);
			transitions.clear();
		}
	}
}
