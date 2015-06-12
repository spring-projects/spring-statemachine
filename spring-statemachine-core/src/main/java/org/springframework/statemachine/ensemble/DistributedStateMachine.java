/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.ensemble;

import java.util.Collection;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@code DistributedStateMachine} is wrapping a real {@link StateMachine} and works
 * together with a {@link StateMachineEnsemble} order to provide a distributed state
 * machine.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DistributedStateMachine<S, E> extends LifecycleObjectSupport implements StateMachine<S, E> {

	private final static Log log = LogFactory.getLog(DistributedStateMachine.class);
	private final String uuid = UUID.randomUUID().toString();
	private final StateMachineEnsemble<S, E> ensemble;
	private final StateMachine<S, E> delegate;
	private final LocalEnsembleListener listener;
	private final LocalStateMachineListener stateMachineListener;

	/**
	 * Instantiates a new distributed state machine.
	 *
	 * @param ensemble the state machine ensemble
	 * @param delegate the delegating state machine
	 */
	public DistributedStateMachine(StateMachineEnsemble<S, E> ensemble, StateMachine<S, E> delegate) {
		Assert.notNull(ensemble, "State machine ensemble must be set");
		Assert.notNull(delegate, "State machine delegate must be set");
		this.ensemble = ensemble;
		this.delegate = delegate;
		this.listener = new LocalEnsembleListener();
		this.stateMachineListener = new LocalStateMachineListener();
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
	}

	@Override
	protected void doStart() {
		ensemble.addEnsembleListener(listener);
		ensemble.join(this);
		delegate.addStateListener(stateMachineListener);
		super.doStart();
	}

	@Override
	protected void doStop() {
		ensemble.removeEnsembleListener(listener);
		super.doStop();
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		return delegate.sendEvent(MessageBuilder.fromMessage(event).setHeader("uuid", uuid).build());
	}

	@Override
	public boolean sendEvent(E event) {
		return sendEvent(MessageBuilder.withPayload(event).build());
	}

	@Override
	public State<S, E> getState() {
		return delegate.getState();
	}

	@Override
	public Collection<State<S, E>> getStates() {
		return delegate.getStates();
	}

	@Override
	public Collection<Transition<S, E>> getTransitions() {
		return delegate.getTransitions();
	}

	@Override
	public boolean isComplete() {
		return delegate.isComplete();
	}

	@Override
	public void addStateListener(StateMachineListener<S, E> listener) {
		delegate.addStateListener(listener);
	}

	@Override
	public void removeStateListener(StateMachineListener<S, E> listener) {
		delegate.removeStateListener(listener);
	}

	@Override
	public State<S, E> getInitialState() {
		return delegate.getInitialState();
	}

	@Override
	public ExtendedState getExtendedState() {
		return delegate.getExtendedState();
	}

	@Override
	public StateMachineAccessor<S, E> getStateMachineAccessor() {
		return delegate.getStateMachineAccessor();
	}

	private class LocalStateMachineListener extends StateMachineListenerAdapter<S, E> {

		@Override
		public void stateChanged(StateContext<S, E> context) {
			if (ObjectUtils.nullSafeEquals(uuid, context.getMessageHeader("uuid"))) {
				ensemble.setState(new DefaultStateMachineContext<S, E>(delegate, context.getTransition().getTarget()
						.getId(), context.getEvent(), context.getMessageHeaders(), context.getExtendedState()));
			}
		}
	}

	private class LocalEnsembleListener implements EnsembleListeger<S, E> {

		@Override
		public void stateMachineJoined(final StateMachineContext<S, E> context) {
			if (context != null) {
				// I'm now successfully joined, so set delegating
				// sm to current known state by a context.

				delegate.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S, E>>() {

					@Override
					public void apply(StateMachineAccess<S, E> function) {
						function.resetState(context.getState());
						function.setExtendedState(context.getExtendedState());
					}

				});
			}
			log.info("Requesting to start delegating state machine " + delegate);
			delegate.start();
		}

		@Override
		public void stateMachineLeft(StateMachineContext<S, E> context) {
			log.info("Requesting to stop delegating state machine " + delegate);
			delegate.stop();
		}

		@Override
		public void stateChanged(StateMachineContext<S, E> context) {
			delegate.sendEvent(MessageBuilder.withPayload(context.getEvent()).copyHeaders(context.getEventHeaders()).build());
		}

	}

}
