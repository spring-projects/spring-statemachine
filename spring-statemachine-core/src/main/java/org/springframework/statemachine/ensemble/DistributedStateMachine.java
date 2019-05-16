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
package org.springframework.statemachine.ensemble;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@code DistributedStateMachine} is wrapping a real {@link StateMachine} and works
 * together with a {@link StateMachineEnsemble} order to provide a distributed state
 * machine.
 *
 * Every distributed state machine will enter its initial state regardless of
 * a distributed state status.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DistributedStateMachine<S, E> extends LifecycleObjectSupport implements StateMachine<S, E> {

	private final static Log log = LogFactory.getLog(DistributedStateMachine.class);
	private final StateMachineEnsemble<S, E> ensemble;
	private final StateMachine<S, E> delegate;
	private final LocalEnsembleListener listener = new LocalEnsembleListener();
	private final LocalStateMachineInterceptor interceptor = new LocalStateMachineInterceptor();

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
	}

	@Override
	protected void onInit() throws Exception {
		// TODO: should we register with all, not just top one?
		delegate.getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<S, E>>() {

			@Override
			public void apply(StateMachineAccess<S, E> function) {
				function.addStateMachineInterceptor(interceptor);
			}
		});

	}

	@Override
	protected void doStart() {
		ensemble.addEnsembleListener(listener);
		ensemble.join(this);
		super.doStart();
	}

	@Override
	protected void doStop() {
		ensemble.removeEnsembleListener(listener);
		ensemble.leave(this);
		super.doStop();
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		// adding state machine id to the message so that
		// listeners can know from where a state change originates
		return delegate.sendEvent(MessageBuilder.fromMessage(event)
				.setHeader(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER, delegate.getId()).build());
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
	public void setStateMachineError(Exception exception) {
		delegate.setStateMachineError(exception);
	}

	@Override
	public boolean hasStateMachineError() {
		return delegate.hasStateMachineError();
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

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public String toString() {
		return "DistributedStateMachine [delegate=" + delegate + "]";
	}

	/**
	 * We intercept state changes order to attempt to update global
	 * distributed state. This attempt is sent to an ensemble which will
	 * tell us if that attempt was successful.
	 */
	private class LocalStateMachineInterceptor implements StateMachineInterceptor<S, E> {

		@Override
		public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
				StateMachine<S, E> stateMachine) {
			if (log.isTraceEnabled()) {
				log.trace("Received preStateChange from " + stateMachine + " for delegate " + delegate);
			}
			// only handle if state change originates from this dist machine
			if (message != null
					&& ObjectUtils.nullSafeEquals(delegate.getId(),
							message.getHeaders().get(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER))) {
				ensemble.setState(new DefaultStateMachineContext<S, E>(transition.getTarget().getId(), message
						.getPayload(), message.getHeaders(), stateMachine.getExtendedState()));
			}
		}

		@Override
		public void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
				StateMachine<S, E> stateMachine) {
		}

		@Override
		public StateContext<S, E> preTransition(StateContext<S, E> stateContext) {
			return stateContext;
		}

		@Override
		public StateContext<S, E> postTransition(StateContext<S, E> stateContext) {
			// only handle if state change originates from this dist machine
			if (stateContext.getTransition() != null
					&& stateContext.getTransition().getKind() == TransitionKind.INTERNAL
					&& ObjectUtils.nullSafeEquals(delegate.getId(),
							stateContext.getMessageHeader(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER))) {
				StateMachineContext<S, E> current = ensemble.getState();
				if (current != null) {
					ensemble.setState(new DefaultStateMachineContext<S, E>(
							current.getState(), stateContext.getEvent(), stateContext
									.getMessageHeaders(), stateContext.getStateMachine().getExtendedState()));
				} else if (stateContext.getStateMachine().getState() != null) {
					// if current ensemble state is null, get it from sm itself
					ensemble.setState(new DefaultStateMachineContext<S, E>(stateContext.getStateMachine().getState()
							.getId(), stateContext.getEvent(), stateContext.getMessageHeaders(), stateContext
							.getStateMachine().getExtendedState()));
				}
			}
			return stateContext;
		}

		@Override
		public Exception stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
			return exception;
		}

	}

	/**
	 * Bridge for instructing delegating machine based on what
	 * is happening in an ensemble.
	 */
	private class LocalEnsembleListener implements EnsembleListener<S, E> {

		@Override
		public void stateMachineJoined(final StateMachine<S, E> stateMachine, final StateMachineContext<S, E> context) {
			if (log.isDebugEnabled()) {
				log.debug("Event stateMachineJoined stateMachine=[" + stateMachine + "] context=[" + context + "]");
			}
			if (stateMachine != null && stateMachine == DistributedStateMachine.this) {
				delegate.stop();
				setStateMachineError(null);
				if (context != null) {
					// I'm now successfully joined, so set delegating
					// sm to current known state by a context.

					if (log.isDebugEnabled()) {
						log.debug("Joining with context " + context);
					}

					delegate.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S, E>>() {

						@Override
						public void apply(StateMachineAccess<S, E> function) {
							function.resetStateMachine(context);
						}

					});
				}
				log.info("Requesting to start delegating state machine " + delegate);
				log.info("Delegating machine id " + delegate.getId());
				delegate.start();
			}
		}

		@Override
		public void stateMachineLeft(StateMachine<S, E> stateMachine, StateMachineContext<S, E> context) {
			if (stateMachine != null && stateMachine == DistributedStateMachine.this) {
				log.info("Requesting to stop delegating state machine " + delegate);
				delegate.stop();
			}
		}

		@Override
		public void stateChanged(StateMachineContext<S, E> context) {
			// do not pass if state change was originated from this dist machine
			if (!ObjectUtils.nullSafeEquals(delegate.getId(),
					context.getEventHeaders().get(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER))) {
				delegate.sendEvent(MessageBuilder.withPayload(context.getEvent())
						.copyHeaders(context.getEventHeaders()).build());
			}
		}

		@Override
		public void ensembleError(StateMachineEnsembleException exception) {
			log.error("Ensemble error", exception);
			setStateMachineError(exception);
			throw exception;
		}

	}

}
