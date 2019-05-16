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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Support class working with a {@link StateMachineInterceptor}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineInterceptorList<S, E> {

	private final List<StateMachineInterceptor<S, E>> interceptors = new CopyOnWriteArrayList<StateMachineInterceptor<S, E>>();

	/**
	 * Sets the interceptors, clears any existing interceptors.
	 *
	 * @param interceptors the list of interceptors
	 * @return <tt>true</tt> if interceptor list changed as a result of the
	 *         call
	 */
	public boolean set(List<StateMachineInterceptor<S, E>> interceptors) {
		synchronized (interceptors) {
			interceptors.clear();
			return interceptors.addAll(interceptors);
		}
	}

	/**
	 * Adds interceptor to the list.
	 *
	 * @param interceptor the interceptor
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 */
	public boolean add(StateMachineInterceptor<S, E> interceptor) {
		return interceptors.add(interceptor);
	}

	/**
	 * Removes interceptor from the list.
	 *
	 * @param interceptor the interceptor
	 * @return <tt>true</tt> (as specified by {@link Collection#remove})
	 */
	public boolean remove(StateMachineInterceptor<S, E> interceptor) {
		return interceptors.remove(interceptor);
	}

	/**
	 * Pre state change.
	 *
	 * @param state the state
	 * @param message the message
	 * @param transition the transition
	 * @param stateMachine the state machine
	 */
	public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine) {
		for (StateMachineInterceptor<S, E> interceptor : interceptors) {
			interceptor.preStateChange(state, message, transition, stateMachine);
		}
	}

	/**
	 * Post state change.
	 *
	 * @param state the state
	 * @param message the message
	 * @param transition the transition
	 * @param stateMachine the state machine
	 */
	public void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine) {
		for (StateMachineInterceptor<S, E> interceptor : interceptors) {
			interceptor.postStateChange(state, message, transition, stateMachine);
		}
	}

	/**
	 * Pre transition.
	 *
	 * @param stateContext the state context
	 * @return the state context
	 */
	public StateContext<S, E> preTransition(StateContext<S, E> stateContext) {
		for (StateMachineInterceptor<S, E> interceptor : interceptors) {
			if ((stateContext = interceptor.preTransition(stateContext)) == null) {
				break;
			}
		}
		return stateContext;
	}

	/**
	 * Post transition.
	 *
	 * @param stateContext the state context
	 * @return the state context
	 */
	public StateContext<S, E> postTransition(StateContext<S, E> stateContext) {
		for (StateMachineInterceptor<S, E> interceptor : interceptors) {
			if ((stateContext = interceptor.postTransition(stateContext)) == null) {
				break;
			}
		}
		return stateContext;
	}

	/**
	 * State machine error.
	 *
	 * @param stateMachine the state machine
	 * @param exception the exception
	 * @return the exception
	 */
	public Exception stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
		for (StateMachineInterceptor<S, E> interceptor : interceptors) {
			if ((exception = interceptor.stateMachineError(stateMachine, exception)) == null) {
				break;
			}
		}
		return exception;
	}

	@Override
	public String toString() {
		return "StateMachineInterceptorList [interceptors=" + interceptors + "]";
	}

}
