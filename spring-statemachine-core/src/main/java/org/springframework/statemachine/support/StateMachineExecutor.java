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
package org.springframework.statemachine.support;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.Trigger;

/**
 * Interface for a {@link StateMachine} event executor.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineExecutor<S, E> {

	/**
	 * Queue event.
	 *
	 * @param message the message
	 */
	void queueEvent(Message<E> message);

	/**
	 * Queue trigger.
	 *
	 * @param trigger the trigger
	 * @param message the message
	 */
	void queueTrigger(Trigger<S, E> trigger, Message<E> message);

	/**
	 * Queue deferred event.
	 *
	 * @param message the message
	 */
	void queueDeferredEvent(Message<E> message);

	/**
	 * Execute {@code StateMachineExecutor} logic.
	 */
	void execute();

	/**
	 * Sets the if initial stage is enabled.
	 *
	 * @param enabled the new flag
	 */
	void setInitialEnabled(boolean enabled);

	/**
	 * Start executor.
	 *
	 * @see LifecycleObjectSupport#start()
	 */
	void start();

	/**
	 * Stop executor.
	 *
	 * @see LifecycleObjectSupport#stop()
	 */
	void stop();

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
		 */
		void transit(Transition<S, E> transition, StateContext<S, E> stateContext, Message<E> message);

	}

}
