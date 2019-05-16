/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.recipes;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler.PersistStateChangeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class PersistStateMachineHandlerTests {

	@Test
	public void testAcceptedStateChangeViaPersist() throws Exception {
		StateMachine<String,String> stateMachine = buildTestStateMachine();

		PersistStateMachineHandler handler = new PersistStateMachineHandler(stateMachine);
		handler.afterPropertiesSet();
		handler.start();

		TestPersistStateChangeListener listener = new TestPersistStateChangeListener();
		handler.addPersistStateChangeListener(listener);

		Message<String> event = MessageBuilder.withPayload("E2").build();
		boolean accepted = handler.handleEventWithState(event, "S1");
		assertThat(accepted, is(true));
		assertThat(listener.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
	}

	@Test
	public void testNotAcceptedStateChangeViaPersist() throws Exception {
		StateMachine<String,String> stateMachine = buildTestStateMachine();

		PersistStateMachineHandler handler = new PersistStateMachineHandler(stateMachine);
		handler.afterPropertiesSet();
		handler.start();

		TestPersistStateChangeListener listener = new TestPersistStateChangeListener();
		handler.addPersistStateChangeListener(listener);

		Message<String> event = MessageBuilder.withPayload("E1").build();
		boolean accepted = handler.handleEventWithState(event, "S1");
		assertThat(accepted, is(false));
		assertThat(listener.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1"));
	}

	@Test
	public void testChoice() throws Exception {
		StateMachine<String,String> stateMachine = buildTestStateMachine2();

		PersistStateMachineHandler handler = new PersistStateMachineHandler(stateMachine);
		handler.afterPropertiesSet();
		handler.start();

		TestPersistStateChangeListener listener = new TestPersistStateChangeListener();
		handler.addPersistStateChangeListener(listener);

		Message<String> event = MessageBuilder.withPayload("E1").build();

		boolean accepted = handler.handleEventWithState(event, "SI");
		assertThat(accepted, is(true));
		assertThat(listener.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener.states.size(), is(1));
		assertThat(listener.states.get(0).getId(), is("S2"));
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2"));
	}

	private static class TestPersistStateChangeListener implements PersistStateChangeListener {

		CountDownLatch latch = new CountDownLatch(1);
		List<State<String, String>> states = new ArrayList<>();

		@Override
		public void onPersist(State<String, String> state, Message<String> message,
				Transition<String, String> transition, StateMachine<String, String> stateMachine) {
			states.add(state);
			latch.countDown();
		}

	}

	private static StateMachine<String, String> buildTestStateMachine()
			throws Exception {
		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.taskExecutor(new SyncTaskExecutor())
				.autoStartup(true);

		builder.configureStates()
				.withStates()
					.initial("SI")
					.state("S1")
					.state("S2");

		builder.configureTransitions()
				.withExternal()
					.source("SI").target("S1").event("E1")
					.and()
				.withExternal()
					.source("S1").target("S2").event("E2");

		return builder.build();
	}

	private static StateMachine<String, String> buildTestStateMachine2()
			throws Exception {
		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.taskExecutor(new SyncTaskExecutor())
				.autoStartup(true);

		builder.configureStates()
				.withStates()
					.initial("SI")
					.choice("S1")
					.state("S2")
					.state("S3");

		builder.configureTransitions()
				.withExternal()
					.source("SI").target("S1").event("E1")
					.and()
				.withChoice()
					.source("S1")
					.last("S2");

		return builder.build();
	}
}
