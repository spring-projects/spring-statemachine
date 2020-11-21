/*
 * Copyright 2015-2020 the original author or authors.
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

import java.util.function.Consumer;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

/**
 * Interface for a {@link StateMachine} event executor.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineExecutor<S, E> extends StateMachineReactiveLifecycle {

	/**
	 * Queue event.
	 *
	 * @param message the message
	 * @param callback the executor callback
	 * @return completion when event is queued
	 */
	Mono<Void> queueEvent(Mono<Message<E>> message, StateMachineExecutorCallback callback);

	/**
	 * Queue deferred event.
	 *
	 * @param message the message
	 */
	void queueDeferredEvent(Message<E> message);

	/**
	 * Execute and check all triggerless transitions.
	 *
	 * @param context the state context
	 * @param state the state
	 * @return completion when handled
	 */
	Mono<Void> executeTriggerlessTransitions(StateContext<S, E> context, State<S, E> state);

	/**
	 * Sets the if initial stage is enabled.
	 *
	 * @param enabled the new flag
	 */
	void setInitialEnabled(boolean enabled);

	/**
	 * Set initial forwarded event.
	 *
	 * @param message the forwarded message
	 * @see StateMachineAccess#setForwardedInitialEvent(Message)
	 */
	void setForwardedInitialEvent(Message<E> message);

	/**
	 * Sets the state machine executor transit.
	 *
	 * @param stateMachineExecutorTransit the state machine executor transit
	 */
	void setStateMachineExecutorTransit(StateMachineExecutorTransit<S, E> stateMachineExecutorTransit);

	/**
	 * Adds the state machine interceptor.
	 *
	 * @param interceptor the interceptor
	 */
	void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor);

	/**
	 * Callback interface when executor wants to handle transit.
	 */
	public interface StateMachineExecutorTransit<S, E> {

		/**
		 * Called when executor wants to do a transit.
		 *
		 * @param transition the transition
		 * @param stateContext the state context
		 * @param message the message
		 * @return completion when handled
		 */
		Mono<Void> transit(Transition<S, E> transition, StateContext<S, E> stateContext, Message<E> message);
	}

	/**
	 * Completion callback to notify back complete or error.
	 */
	public interface StateMachineExecutorCallback {
		void complete();
		void error(Throwable e);
	}

	static class MonoSinkStateMachineExecutorCallback implements Consumer<MonoSink<Void>>, StateMachineExecutorCallback {

		private boolean complete;
		private Throwable error;

		@Override
		public void complete() {
			complete = true;
		}

		@Override
		public void error(Throwable e) {
			error = e;
		}

		@Override
		public void accept(MonoSink<Void> t) {
			if (complete) {
				t.success();
			} else if (error != null) {
				t.error(error);
			} else {
				t.success();
			}
		}
	}

	public static class ExecutorExceptionHolder {

		private Throwable error;

		public void setError(Throwable error) {
			this.error = error;
		}

		public Throwable getError() {
			return error;
		}
	}
}
