/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.statemachine.scxml;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

public class TestUtils {

	public static class LatchAction implements Action<String, String> {
		public final CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void execute(StateContext<String, String> context) {
			latch.countDown();
		}
	}

	public static class TestGuard implements Guard<String, String> {
		private final boolean result;

		public TestGuard(boolean result) {
			this.result = result;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return result;
		}
	}

	public static class TestExtendedStateListener extends org.springframework.statemachine.listener.StateMachineListenerAdapter<String, String> {
		public final CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void stateChanged(org.springframework.statemachine.state.State<String, String> from,
				org.springframework.statemachine.state.State<String, String> to) {
			latch.countDown();
		}
	}

	public static void doStartAndAssert(StateMachine<String, String> stateMachine) {
		stateMachine.startReactively().block();
		org.assertj.core.api.Assertions.assertThat(stateMachine.getState()).isNotNull();
	}

	public static void doSendEventAndConsumeAll(StateMachine<String, String> stateMachine, String event) {
		stateMachine.sendEvent(event).block();
	}

	public static void doSendEventAndConsumeAll(StateMachine<String, String> stateMachine,
			org.springframework.messaging.Message<String> message) {
		stateMachine.sendEvent(message).block();
	}

	public static void assertLatch(CountDownLatch latch, long timeoutSeconds) throws InterruptedException {
		org.assertj.core.api.Assertions.assertThat(latch.await(timeoutSeconds, TimeUnit.SECONDS)).isTrue();
	}
}

