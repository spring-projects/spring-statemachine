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
package org.springframework.statemachine.test.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * A {@link StateMachineListener} which is used during the tests
 * to assert correct count of listener callbacks.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class LatchStateMachineListener<S, E> extends StateMachineListenerAdapter<S, E> {

	private final Object lock = new Object();

	private volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
	private volatile CountDownLatch stateEnteredLatch = new CountDownLatch(1);
	private volatile CountDownLatch stateExitedLatch = new CountDownLatch(1);
	private volatile CountDownLatch eventNotAcceptedLatch = new CountDownLatch(1);
	private volatile CountDownLatch transitionLatch = new CountDownLatch(1);
	private volatile CountDownLatch transitionStartedLatch = new CountDownLatch(1);
	private volatile CountDownLatch transitionEndedLatch = new CountDownLatch(1);
	private volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
	private volatile CountDownLatch stateMachineStoppedLatch = new CountDownLatch(1);
	private volatile CountDownLatch extendedStateChangedLatch = new CountDownLatch(1);

	private final List<StateChangedWrapper<S, E>> stateChanged = new ArrayList<StateChangedWrapper<S, E>>();
	private final List<State<S, E>> stateEntered = new ArrayList<State<S, E>>();
	private final List<State<S, E>> stateExited = new ArrayList<State<S, E>>();
	private final List<Message<E>> eventNotAccepted = new ArrayList<Message<E>>();
	private final List<Transition<S, E>> transition = new ArrayList<Transition<S, E>>();
	private final List<Transition<S, E>> transitionStarted = new ArrayList<Transition<S, E>>();
	private final List<Transition<S, E>> transitionEnded = new ArrayList<Transition<S, E>>();
	private final List<StateMachine<S, E>> stateMachineStarted = new ArrayList<StateMachine<S, E>>();
	private final List<StateMachine<S, E>> stateMachineStopped = new ArrayList<StateMachine<S, E>>();
	private final List<ExtendedStateChangedWrapper> extendedStateChanged = new ArrayList<ExtendedStateChangedWrapper>();

	@Override
	public void stateChanged(State<S, E> from, State<S, E> to) {
		synchronized (lock) {
			this.stateChanged.add(new StateChangedWrapper<>(from, to));
			this.stateChangedLatch.countDown();
		}
	}

	@Override
	public void stateEntered(State<S, E> state) {
		synchronized (lock) {
			this.stateEntered.add(state);
			this.stateEnteredLatch.countDown();
		}
	}

	@Override
	public void stateExited(State<S, E> state) {
		synchronized (lock) {
			this.stateExited.add(state);
			this.stateExitedLatch.countDown();
		}
	}

	@Override
	public void eventNotAccepted(Message<E> event) {
		synchronized (lock) {
			this.eventNotAccepted.add(event);
			this.eventNotAcceptedLatch.countDown();
		}
	}

	@Override
	public void transition(Transition<S, E> transition) {
		synchronized (lock) {
			this.transition.add(transition);
			this.transitionLatch.countDown();
		}
	}

	@Override
	public void transitionStarted(Transition<S, E> transition) {
		synchronized (lock) {
			this.transitionStarted.add(transition);
			this.transitionStartedLatch.countDown();
		}
	}

	@Override
	public void transitionEnded(Transition<S, E> transition) {
		synchronized (lock) {
			this.transitionEnded.add(transition);
			this.transitionEndedLatch.countDown();
		}
	}

	@Override
	public void stateMachineStarted(StateMachine<S, E> stateMachine) {
		synchronized (lock) {
			this.stateMachineStarted.add(stateMachine);
			this.stateMachineStartedLatch.countDown();
		}
	}

	@Override
	public void stateMachineStopped(StateMachine<S, E> stateMachine) {
		synchronized (lock) {
			this.stateMachineStopped.add(stateMachine);
			this.stateMachineStoppedLatch.countDown();
		}
	}

	@Override
	public void extendedStateChanged(Object key, Object value) {
		synchronized (lock) {
			this.extendedStateChanged.add(new ExtendedStateChangedWrapper(key, value));
			this.extendedStateChangedLatch.countDown();
		}
	}

	public void reset(int stateChangedCount, int stateEnteredCount, int stateExitedCount, int eventNotAcceptedCount,
			int transitionCount, int transitionStartedCount, int transitionEndedCount, int stateMachineStartedCount,
			int stateMachineStoppedCount, int extendedStateChangedCount) {
		synchronized (lock) {
			this.stateChangedLatch = new CountDownLatch(stateChangedCount);
			this.stateEnteredLatch = new CountDownLatch(stateEnteredCount);
			this.stateExitedLatch = new CountDownLatch(stateExitedCount);
			this.eventNotAcceptedLatch = new CountDownLatch(eventNotAcceptedCount);
			this.transitionLatch = new CountDownLatch(transitionCount);
			this.transitionStartedLatch = new CountDownLatch(transitionStartedCount);
			this.transitionEndedLatch = new CountDownLatch(transitionEndedCount);
			this.stateMachineStartedLatch = new CountDownLatch(stateMachineStartedCount);
			this.stateMachineStoppedLatch = new CountDownLatch(stateMachineStoppedCount);
			this.extendedStateChangedLatch = new CountDownLatch(extendedStateChangedCount);
			this.stateChanged.clear();
			this.stateEntered.clear();
			this.stateExited.clear();
			this.eventNotAccepted.clear();
			this.transition.clear();
			this.transitionStarted.clear();
			this.transitionEnded.clear();
			this.stateMachineStarted.clear();
			this.stateMachineStopped.clear();
			this.extendedStateChanged.clear();
		}
	}

	public CountDownLatch getStateChangedLatch() {
		return stateChangedLatch;
	}

	public CountDownLatch getStateEnteredLatch() {
		return stateEnteredLatch;
	}

	public CountDownLatch getStateExitedLatch() {
		return stateExitedLatch;
	}

	public CountDownLatch getEventNotAcceptedLatch() {
		return eventNotAcceptedLatch;
	}

	public CountDownLatch getTransitionLatch() {
		return transitionLatch;
	}

	public CountDownLatch getTransitionStartedLatch() {
		return transitionStartedLatch;
	}

	public CountDownLatch getTransitionEndedLatch() {
		return transitionEndedLatch;
	}

	public CountDownLatch getStateMachineStartedLatch() {
		return stateMachineStartedLatch;
	}

	public CountDownLatch getStateMachineStoppedLatch() {
		return stateMachineStoppedLatch;
	}

	public CountDownLatch getExtendedStateChangedLatch() {
		return extendedStateChangedLatch;
	}

	public List<StateChangedWrapper<S, E>> getStateChanged() {
		return stateChanged;
	}

	public List<State<S, E>> getStateEntered() {
		return stateEntered;
	}

	public List<State<S, E>> getStateExited() {
		return stateExited;
	}

	public List<Message<E>> getEventNotAccepted() {
		return eventNotAccepted;
	}

	public List<Transition<S, E>> getTransition() {
		return transition;
	}

	public List<Transition<S, E>> getTransitionStarted() {
		return transitionStarted;
	}

	public List<Transition<S, E>> getTransitionEnded() {
		return transitionEnded;
	}

	public List<StateMachine<S, E>> getStateMachineStarted() {
		return stateMachineStarted;
	}

	public List<StateMachine<S, E>> getStateMachineStopped() {
		return stateMachineStopped;
	}

	public List<ExtendedStateChangedWrapper> getExtendedStateChanged() {
		return extendedStateChanged;
	}

	public static class StateChangedWrapper<S, E> {
		final State<S, E> from;
		final State<S, E> to;

		public StateChangedWrapper(State<S, E> from, State<S, E> to) {
			this.from = from;
			this.to = to;
		}

	}

	public static class ExtendedStateChangedWrapper {
		final Object key;
		final Object value;

		public ExtendedStateChangedWrapper(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

	}

}
