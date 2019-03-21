/*
 * Copyright 2015-2017 the original author or authors.
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
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.event.StateMachineEventPublisher;
import org.springframework.statemachine.listener.CompositeStateMachineListener;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.monitor.CompositeStateMachineMonitor;
import org.springframework.statemachine.processor.StateMachineHandlerCallHelper;
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
	private final CompositeStateMachineMonitor<S, E> stateMachineMonitor = new CompositeStateMachineMonitor<S, E>();

	/** Context application event publisher if exist */
	private volatile StateMachineEventPublisher stateMachineEventPublisher;

	/** Flag for application context events */
	private boolean contextEventsEnabled = true;

	private final StateMachineInterceptorList<S, E> interceptors = new StateMachineInterceptorList<S, E>();
	private String beanName;
	private volatile boolean handlersInitialized;
	private final StateMachineHandlerCallHelper<S, E> stateMachineHandlerCallHelper = new StateMachineHandlerCallHelper<S, E>();

	@Override
	protected void doStart() {
		super.doStart();
		if (!handlersInitialized) {
			try {
				stateMachineHandlerCallHelper.setBeanFactory(getBeanFactory());
				stateMachineHandlerCallHelper.afterPropertiesSet();
			} catch (Exception e) {
				log.error("Unable to initialize annotation handlers", e);
			} finally {
				handlersInitialized = true;
			}
		}
	}

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

	protected CompositeStateMachineMonitor<S, E> getStateMachineMonitor() {
		return stateMachineMonitor;
	}

	protected void notifyStateChanged(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnStateChanged(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnStateChanged(stateContext.getStateMachine().getId(), stateContext);
			stateListener.stateChanged(stateContext.getSource(), stateContext.getTarget());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishStateChanged(this, stateContext.getSource(), stateContext.getTarget());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyStateChanged", e);
		}
	}

	protected void notifyStateEntered(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnStateEntry(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnStateEntry(stateContext.getStateMachine().getId(), stateContext);
			stateListener.stateEntered(stateContext.getTarget());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishStateEntered(this, stateContext.getTarget());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyStateEntered", e);
		}
	}

	protected void notifyStateExited(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnStateExit(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnStateExit(stateContext.getStateMachine().getId(), stateContext);
			stateListener.stateExited(stateContext.getSource());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishStateExited(this, stateContext.getSource());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyStateExited", e);
		}
	}

	protected void notifyEventNotAccepted(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnEventNotAccepted(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnEventNotAccepted(stateContext.getStateMachine().getId(), stateContext);
			stateListener.eventNotAccepted(stateContext.getMessage());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishEventNotAccepted(this, stateContext.getMessage());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyEventNotAccepted", e);
		}
	}

	protected void notifyTransitionStart(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnTransitionStart(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnTransitionStart(stateContext.getStateMachine().getId(), stateContext);
			stateListener.transitionStarted(stateContext.getTransition());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishTransitionStart(this, stateContext.getTransition());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyTransitionStart", e);
		}
	}

	protected void notifyTransition(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnTransition(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnTransition(stateContext.getStateMachine().getId(), stateContext);
			stateListener.transition(stateContext.getTransition());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishTransition(this, stateContext.getTransition());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyTransition", e);
		}
	}

	protected void notifyTransitionEnd(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnTransitionEnd(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnTransitionEnd(stateContext.getStateMachine().getId(), stateContext);
			stateListener.transitionEnded(stateContext.getTransition());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishTransitionEnd(this, stateContext.getTransition());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyTransitionEnd", e);
		}
	}

	protected void notifyStateMachineStarted(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnStateMachineStart(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnStateMachineStart(stateContext.getStateMachine().getId(), stateContext);
			stateListener.stateMachineStarted(stateContext.getStateMachine());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishStateMachineStart(this, stateContext.getStateMachine());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyStateMachineStarted", e);
		}
	}

	protected void notifyStateMachineStopped(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnStateMachineStop(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnStateMachineStop(stateContext.getStateMachine().getId(), stateContext);
			stateListener.stateMachineStopped(stateContext.getStateMachine());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishStateMachineStop(this, stateContext.getStateMachine());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyStateMachineStopped", e);
		}
	}

	protected void notifyStateMachineError(StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnStateMachineError(getBeanName(), stateContext);
			stateMachineHandlerCallHelper.callOnStateMachineError(stateContext.getStateMachine().getId(), stateContext);
			stateListener.stateMachineError(stateContext.getStateMachine(), stateContext.getException());
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishStateMachineError(this, stateContext.getStateMachine(), stateContext.getException());
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyStateMachineError", e);
		}
	}

	protected void notifyExtendedStateChanged(Object key, Object value, StateContext<S, E> stateContext) {
		try {
			stateMachineHandlerCallHelper.callOnExtendedStateChanged(getBeanName(), key, value, stateContext);
			stateMachineHandlerCallHelper.callOnExtendedStateChanged(stateContext.getStateMachine().getId(), key, value, stateContext);
			stateListener.extendedStateChanged(key, value);
			stateListener.stateContext(stateContext);
			if (contextEventsEnabled) {
				StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
				if (eventPublisher != null) {
					eventPublisher.publishExtendedStateChanged(this, key, value);
				}
			}
		} catch (Throwable e) {
			log.warn("Error during notifyExtendedStateChanged", e);
		}
	}

	protected void notifyTransitionMonitor(StateMachine<S, E> stateMachine, Transition<S, E> transition, long duration) {
		try {
			stateMachineMonitor.transition(stateMachine, transition, duration);
		} catch (Exception e) {
			log.warn("Error during notifyTransitionMonitor", e);
		}
	}

	protected void notifyActionMonitor(StateMachine<S, E> stateMachine, Action<S, E> action, long duration) {
		try {
			stateMachineMonitor.action(stateMachine, action, duration);
		} catch (Exception e) {
			log.warn("Error during notifyTransitionMonitor", e);
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

		@Override
		public void stateContext(StateContext<S, E> stateContext) {
			stateListener.stateContext(stateContext);
		}

	}

}
