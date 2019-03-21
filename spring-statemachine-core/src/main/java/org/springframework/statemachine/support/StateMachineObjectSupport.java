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

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.OrderComparator;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.event.StateMachineEventPublisher;
import org.springframework.statemachine.listener.CompositeStateMachineListener;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.Assert;

/**
 * Support and helper class for base state machine implementation.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class StateMachineObjectSupport<S, E> extends LifecycleObjectSupport implements BeanNameAware {

	private static final Log log = LogFactory.getLog(StateMachineObjectSupport.class);

	private final CompositeStateMachineListener<S, E> stateListener = new CompositeStateMachineListener<S, E>();

	/** Context application event publisher if exist */
	private volatile StateMachineEventPublisher stateMachineEventPublisher;

	/** Flag for application context events */
	private boolean contextEventsEnabled = true;

	private final StateMachineInterceptorList<S, E> interceptors =
			new StateMachineInterceptorList<S, E>();

	private String beanName;

	@Override
	public void setBeanName(String name) {
		beanName = name;
	}

	/**
	 * Returns a bean name known to context per contract
	 * with {@link BeanNameAware}.
	 *
	 * @return a bean name
	 */
	protected String getBeanName() {
		return beanName;
	}

	/**
	 * Gets the state machine event publisher.
	 *
	 * @return the state machine event publisher
	 */
	protected StateMachineEventPublisher getStateMachineEventPublisher() {
		if(stateMachineEventPublisher == null && getBeanFactory() != null) {
			if(log.isTraceEnabled()) {
				log.trace("getting stateMachineEventPublisher service from bean factory " + getBeanFactory());
			}
			stateMachineEventPublisher = StateMachineContextUtils.getEventPublisher(getBeanFactory());
		}
		return stateMachineEventPublisher;
	}

	/**
	 * Sets the state machine event publisher.
	 *
	 * @param stateMachineEventPublisher the new state machine event publisher
	 */
	public void setStateMachineEventPublisher(StateMachineEventPublisher stateMachineEventPublisher) {
		Assert.notNull(stateMachineEventPublisher, "StateMachineEventPublisher cannot be null");
		this.stateMachineEventPublisher = stateMachineEventPublisher;
	}

	/**
	 * Set if context application events are enabled. Events
	 * are enabled by default. Set this to false if you don't
	 * want state machine to send application context events.
	 *
	 * @param contextEventsEnabled the enabled flag
	 */
	public void setContextEventsEnabled(boolean contextEventsEnabled) {
		this.contextEventsEnabled = contextEventsEnabled;
	}

	protected CompositeStateMachineListener<S, E> getStateListener() {
		return stateListener;
	}

	protected void notifyStateChanged(State<S,E> source, State<S,E> target) {
		stateListener.stateChanged(source, target);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishStateChanged(this, source, target);
			}
		}
	}

	protected void notifyStateEntered(State<S,E> state) {
		stateListener.stateEntered(state);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishStateEntered(this, state);
			}
		}
	}

	protected void notifyStateExited(State<S,E> state) {
		stateListener.stateExited(state);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishStateExited(this, state);
			}
		}
	}

	protected void notifyEventNotAccepted(Message<E> event) {
		stateListener.eventNotAccepted(event);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishEventNotAccepted(this, event);
			}
		}
	}

	protected void notifyTransitionStart(Transition<S,E> transition) {
		stateListener.transitionStarted(transition);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishTransitionStart(this, transition);
			}
		}
	}

	protected void notifyTransition(Transition<S,E> transition) {
		stateListener.transition(transition);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishTransition(this, transition);
			}
		}
	}

	protected void notifyTransitionEnd(Transition<S,E> transition) {
		stateListener.transitionEnded(transition);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishTransitionEnd(this, transition);
			}
		}
	}

	protected void notifyStateMachineStarted(StateMachine<S, E> stateMachine) {
		stateListener.stateMachineStarted(stateMachine);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishStateMachineStart(this, stateMachine);
			}
		}
	}

	protected void notifyStateMachineStopped(StateMachine<S, E> stateMachine) {
		stateListener.stateMachineStopped(stateMachine);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishStateMachineStop(this, stateMachine);
			}
		}
	}

	protected void notifyStateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
		stateListener.stateMachineError(stateMachine, exception);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishStateMachineError(this, stateMachine, exception);
			}
		}
	}

	protected void notifyExtendedStateChanged(Object key, Object value) {
		stateListener.extendedStateChanged(key, value);
		if (contextEventsEnabled) {
			StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
			if (eventPublisher != null) {
				eventPublisher.publishExtendedStateChanged(this, key, value);
			}
		}
	}

	protected void stateChangedInRelay() {
		// TODO: this is a temporary tweak to know when state is
		//       changed in a submachine/regions order to give
		//       state machine a change to request executor login again
		//       which is needed when we use multiple thread. with multiple
		//       threads submachines may do their stuff after thread handling
		//       main machine has already finished its execution logic, thus
		//       re-scheduling is needed.
	}

	protected StateMachineInterceptorList<S, E> getStateMachineInterceptors() {
		return interceptors;
	}

	protected void setStateMachineInterceptors(List<StateMachineInterceptor<S,E>> interceptors) {
		Collections.sort(interceptors, new OrderComparator());
		this.interceptors.set(interceptors);
	}

	/**
	 * This class is used to relay listener events from a submachines which works
	 * as its own listener context. User only connects to main root machine and
	 * expects to get events for all machines from there.
	 */
	protected class StateMachineListenerRelay implements StateMachineListener<S,E> {

		@Override
		public void stateChanged(State<S, E> from, State<S, E> to) {
			stateListener.stateChanged(from, to);
			stateChangedInRelay();
		}

		@Override
		public void stateEntered(State<S, E> state) {
			stateListener.stateEntered(state);
		}

		@Override
		public void stateExited(State<S, E> state) {
			stateListener.stateExited(state);
		}

		@Override
		public void eventNotAccepted(Message<E> event) {
			stateListener.eventNotAccepted(event);
		}

		@Override
		public void transition(Transition<S, E> transition) {
			stateListener.transition(transition);
		}

		@Override
		public void transitionStarted(Transition<S, E> transition) {
			stateListener.transitionStarted(transition);
		}

		@Override
		public void transitionEnded(Transition<S, E> transition) {
			stateListener.transitionEnded(transition);
		}

		@Override
		public void stateMachineStarted(StateMachine<S, E> stateMachine) {
			stateListener.stateMachineStarted(stateMachine);
		}

		@Override
		public void stateMachineStopped(StateMachine<S, E> stateMachine) {
			stateListener.stateMachineStopped(stateMachine);
		}

		@Override
		public void stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
			stateListener.stateMachineError(stateMachine, exception);
		}

		@Override
		public void extendedStateChanged(Object key, Object value) {
			stateListener.extendedStateChanged(key, value);
		}

	}

}
