/*
 * Copyright 2015-2021 the original author or authors.
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
package org.springframework.statemachine.access;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StateMachineAccessTests {

	@Test
	public void testDoWithAllRegionsSetRelay() {
		MockStateMachine mock = new MockStateMachine();
		final StateMachine<String, String> stateMachine = mock;
		stateMachine.getStateMachineAccessor().doWithAllRegions(function -> function.setRelay(stateMachine));

		assertThat(mock.relay).isSameAs(stateMachine);
	}

	@Test
	public void testGetAllRegionsSetRelay() {
		MockStateMachine mock = new MockStateMachine();
		final StateMachine<String, String> stateMachine = mock;
		stateMachine.getStateMachineAccessor().withAllRegions().forEach(access -> access.setRelay(stateMachine));

		assertThat(mock.relay).isSameAs(stateMachine);
	}

	private static class MockStateMachine implements StateMachine<String, String>, StateMachineAccess<String, String> {

		StateMachine<String, String> relay;

		@Override
		public StateMachineAccessor<String, String> getStateMachineAccessor() {
			return new StateMachineAccessor<String, String>() {

				@Override
				public void doWithAllRegions(Consumer<StateMachineAccess<String, String>> stateMachineAccess) {
					stateMachineAccess.accept(MockStateMachine.this);
				}

				@Override
				public List<StateMachineAccess<String, String>> withAllRegions() {
					List<StateMachineAccess<String, String>> list = new ArrayList<StateMachineAccess<String,String>>();
					list.add(MockStateMachine.this);
					return list;
				}

				@Override
				public void doWithRegion(Consumer<StateMachineAccess<String, String>> stateMachineAccess) {
				}

				@Override
				public StateMachineAccess<String, String> withRegion() {
					return null;
				}
			};
		}

		@Override
		public void addStateMachineInterceptor(StateMachineInterceptor<String, String> interceptor) {
		}

		@Override
		public void addStateMachineMonitor(StateMachineMonitor<String, String> monitor) {
		}

		@Override
		public void setRelay(StateMachine<String, String> stateMachine) {
			this.relay = stateMachine;
		}

		@Override
		public void setParentMachine(StateMachine<String, String> stateMachine) {
		}

		@Override
		public void resetStateMachine(StateMachineContext<String, String> stateMachineContext) {
		}

		@Override
		public Mono<Void> resetStateMachineReactively(StateMachineContext<String, String> stateMachineContext) {
			return Mono.empty();
		}

		@Override
		public Mono<Void> startReactively() {
			return null;
		}

		@Override
		public Mono<Void> stopReactively() {
			return null;
		}

		@Override
		@SuppressWarnings({"all", "deprecation"})
		public void start() {
		}

		@Override
		@SuppressWarnings({"all", "deprecation"})
		public void stop() {
		}

		@Override
		@SuppressWarnings({"all", "deprecation"})
		public boolean sendEvent(Message<String> event) {
			return false;
		}

		@Override
		@SuppressWarnings({"all", "deprecation"})
		public boolean sendEvent(String event) {
			return false;
		}

		@Override
		public Flux<StateMachineEventResult<String, String>> sendEvent(Mono<Message<String>> event) {
			return null;
		}

		@Override
		public Mono<List<StateMachineEventResult<String, String>>> sendEventCollect(Mono<Message<String>> event) {
			return null;
		}

		@Override
		public Flux<StateMachineEventResult<String, String>> sendEvents(Flux<Message<String>> events) {
			return null;
		}

		@Override
		public State<String, String> getState() {
			return null;
		}

		@Override
		public Collection<State<String, String>> getStates() {
			return null;
		}

		@Override
		public Collection<Transition<String, String>> getTransitions() {
			return null;
		}

		@Override
		public boolean isComplete() {
			return false;
		}

		@Override
		public void setStateMachineError(Exception exception) {
		}

		@Override
		public boolean hasStateMachineError() {
			return false;
		}

		@Override
		public void addStateListener(StateMachineListener<String, String> listener) {
		}

		@Override
		public void removeStateListener(StateMachineListener<String, String> listener) {
		}

		@Override
		public State<String, String> getInitialState() {
			return null;
		}

		@Override
		public ExtendedState getExtendedState() {
			return null;
		}

		@Override
		public void setInitialEnabled(boolean enabled) {
		}

		@Override
		public UUID getUuid() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public void setForwardedInitialEvent(Message<String> message) {
		}

	}

}
