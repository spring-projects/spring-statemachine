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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.ExtendedState.ExtendedStateChangeListener;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.processor.StateMachineHandler;
import org.springframework.statemachine.processor.StateMachineOnTransitionHandler;
import org.springframework.statemachine.processor.StateMachineRuntime;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.ForkPseudoState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateContext;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.PseudoStateListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineExecutor.StateMachineExecutorTransit;
import org.springframework.statemachine.transition.InitialTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.DefaultTriggerContext;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Base implementation of a {@link StateMachine} loosely modelled from UML state
 * machine.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractStateMachine<S, E> extends StateMachineObjectSupport<S, E> implements StateMachine<S, E>, StateMachineAccess<S, E> {

	private static final Log log = LogFactory.getLog(AbstractStateMachine.class);

	private final Collection<State<S,E>> states;

	private final Collection<Transition<S,E>> transitions;

	private final State<S,E> initialState;

	private final Transition<S, E> initialTransition;

	private final Message<E> initialEvent;

	private ExtendedState extendedState;

	private volatile State<S,E> currentState;

	private volatile Exception currentError;

	private volatile PseudoState<S, E> history;

	private final Map<String, StateMachineOnTransitionHandler<S, E>> handlers = new HashMap<String, StateMachineOnTransitionHandler<S,E>>();

	private volatile boolean handlersInitialized;

	private final Map<Trigger<S, E>, Transition<S,E>> triggerToTransitionMap = new HashMap<Trigger<S,E>, Transition<S,E>>();

	private final List<Transition<S, E>> triggerlessTransitions = new ArrayList<Transition<S,E>>();

	private StateMachine<S, E> relay;

	private StateMachineExecutor<S, E> stateMachineExecutor;

	private Boolean initialEnabled = null;

	private String id = UUID.randomUUID().toString();

	private volatile Message<E> forwardedInitialEvent;

	/**
	 * Instantiates a new abstract state machine.
	 *
	 * @param states the states of this machine
	 * @param transitions the transitions of this machine
	 * @param initialState the initial state of this machine
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState) {
		this(states, transitions, initialState, new DefaultExtendedState());
	}

	/**
	 * Instantiates a new abstract state machine.
	 *
	 * @param states the states of this machine
	 * @param transitions the transitions of this machine
	 * @param initialState the initial state of this machine
	 * @param extendedState the extended state of this machine
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, ExtendedState extendedState) {
		this(states, transitions, initialState, null, null, extendedState);
	}

	/**
	 * Instantiates a new abstract state machine.
	 *
	 * @param states the states of this machine
	 * @param transitions the transitions of this machine
	 * @param initialState the initial state of this machine
	 * @param initialTransition the initial transition
	 * @param initialEvent the initial event of this machine
	 * @param extendedState the extended state of this machine
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, Transition<S, E> initialTransition, Message<E> initialEvent, ExtendedState extendedState) {
		super();
		this.states = states;
		this.transitions = transitions;
		this.initialState = initialState;
		this.initialEvent = initialEvent;
		this.extendedState = extendedState != null ? extendedState : new DefaultExtendedState();
		if (initialTransition == null) {
			this.initialTransition = new InitialTransition<S, E>(initialState);
		} else {
			this.initialTransition = initialTransition;
		}
	}

	@Override
	public State<S,E> getState() {
		return currentState;
	}

	@Override
	public State<S,E> getInitialState() {
		return initialState;
	}

	@Override
	public ExtendedState getExtendedState() {
		return extendedState;
	}

	public void setHistoryState(PseudoState<S, E> history) {
		this.history = history;
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		if (hasStateMachineError()) {
			// TODO: should we throw exception?
			notifyEventNotAccepted(event);
			return false;
		}
		if (isComplete() || !isRunning()) {
			notifyEventNotAccepted(event);
			return false;
		}
		boolean accepted = acceptEvent(event);
		stateMachineExecutor.execute();
		if (!accepted) {
			notifyEventNotAccepted(event);
		}
		return accepted;
	}

	@Override
	public boolean sendEvent(E event) {
		return sendEvent(MessageBuilder.withPayload(event).build());
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		Assert.notNull(initialState, "Initial state must be set");
		Assert.state(initialState.getPseudoState() != null
				&& initialState.getPseudoState().getKind() == PseudoStateKind.INITIAL,
				"Initial state's pseudostate kind must be INITIAL");

		extendedState.setExtendedStateChangeListener(new ExtendedStateChangeListener() {
			@Override
			public void changed(Object key, Object value) {
				notifyExtendedStateChanged(key, value);
			}
		});

		// process given transitions
		for (Transition<S, E> transition : transitions) {
			Trigger<S, E> trigger = transition.getTrigger();
			if (trigger != null) {
				// we have same triggers with different transitions
				triggerToTransitionMap.put(trigger, transition);
			} else {
				triggerlessTransitions.add(transition);
			}
		}

		for (State<S, E> state : states) {
			if (state.isSubmachineState()) {
				StateMachine<S, E> submachine = ((AbstractState<S, E>)state).getSubmachine();
				submachine.addStateListener(new StateMachineListenerRelay());
			} else if (state.isOrthogonal()) {
				Collection<Region<S, E>> regions = ((AbstractState<S, E>)state).getRegions();
				for (Region<S, E> region : regions) {
					region.addStateListener(new StateMachineListenerRelay());
				}
			}
			if (state.getPseudoState() != null
					&& (state.getPseudoState().getKind() == PseudoStateKind.HISTORY_DEEP || state.getPseudoState()
							.getKind() == PseudoStateKind.HISTORY_DEEP)) {
				history = state.getPseudoState();
			}
		}

		DefaultStateMachineExecutor<S, E> executor = new DefaultStateMachineExecutor<S, E>(this, getRelayStateMachine(), transitions,
				triggerToTransitionMap, triggerlessTransitions, initialTransition, initialEvent);
		if (getBeanFactory() != null) {
			executor.setBeanFactory(getBeanFactory());
		}
		if (getTaskExecutor() != null){
			executor.setTaskExecutor(getTaskExecutor());
		}
		executor.afterPropertiesSet();
		executor.setStateMachineExecutorTransit(new StateMachineExecutorTransit<S, E>() {

			@Override
			public void transit(Transition<S, E> t, StateContext<S, E> stateContext, Message<E> queuedMessage) {
				notifyTransitionStart(t);
				callHandlers(t.getSource(), t.getTarget(), queuedMessage);
				if (t.getKind() == TransitionKind.INITIAL) {
					switchToState(t.getTarget(), queuedMessage, t, getRelayStateMachine());
					notifyStateMachineStarted(getRelayStateMachine());
				} else if (t.getKind() != TransitionKind.INTERNAL) {
					switchToState(t.getTarget(), queuedMessage, t, getRelayStateMachine());
				}
				notifyTransition(t);
				notifyTransitionEnd(t);
			}
		});
		stateMachineExecutor = executor;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		// last change to set factory because this maybe be called per
		// BeanFactoryAware if machine is created as Bean and configurers
		// didn't set it.
		if (getBeanFactory() == null) {
			super.setBeanFactory(beanFactory);
			if (stateMachineExecutor instanceof BeanFactoryAware) {
				((BeanFactoryAware)stateMachineExecutor).setBeanFactory(beanFactory);
			}
		}
	}

	@Override
	protected void doStart() {
		// if state is set assume nothing to do
		if (currentState != null) {
			if (log.isDebugEnabled()) {
				log.debug("State already set, disabling initial");
			}
			stateMachineExecutor.setInitialEnabled(false);
			stateMachineExecutor.start();
			// assume that state was set/reseted so we need to
			// dispatch started event which would net getting
			// dispatched via executor
			notifyStateMachineStarted(getRelayStateMachine());
			return;
		}
		registerPseudoStateListener();

		if (initialEnabled != null && !initialEnabled) {
			if (log.isDebugEnabled()) {
				log.debug("Initial disable asked, disabling initial");
			}
			stateMachineExecutor.setInitialEnabled(false);
		} else {
			stateMachineExecutor.setForwardedInitialEvent(forwardedInitialEvent);
		}

		// start fires first execution which should execute initial transition
		stateMachineExecutor.start();
	}

	@Override
	protected void doStop() {
		stateMachineExecutor.stop();
		notifyStateMachineStopped(this);
		currentState = null;
		initialEnabled = null;
	}

	@Override
	protected void doDestroy() {
		// if lifecycle methods has not been called, make
		// sure we get into those if only destroy() is called.
		stop();
	}

	@Override
	public void setStateMachineError(Exception exception) {
		if (exception == null) {
			currentError = null;
		} else {
			exception = getStateMachineInterceptors().stateMachineError(this, exception);
			currentError = exception;
		}
		if (currentError != null) {
			notifyStateMachineError(this, currentError);
		}
	}

	@Override
	public boolean hasStateMachineError() {
		return currentError != null;
	}

	@Override
	public void addStateListener(StateMachineListener<S, E> listener) {
		getStateListener().register(listener);
	}

	@Override
	public void removeStateListener(StateMachineListener<S, E> listener) {
		getStateListener().unregister(listener);
	}

	@Override
	public boolean isComplete() {
		if (currentState == null) {
			return !isRunning();
		} else {
			return currentState != null && currentState.getPseudoState() != null
					&& currentState.getPseudoState().getKind() == PseudoStateKind.END;
		}
	}

	/**
	 * Gets the {@link State}s defined in this machine. Returned collection is
	 * an unmodifiable copy because states in a state machine are immutable.
	 *
	 * @return immutable copy of existing states
	 */
	@Override
	public Collection<State<S, E>> getStates() {
		return Collections.unmodifiableCollection(states);
	}

	@Override
	public Collection<Transition<S, E>> getTransitions() {
		return transitions;
	}


	@Override
	public void setInitialEnabled(boolean enabled) {
		initialEnabled = enabled;
	}

	@SuppressWarnings("unchecked")
	@Override
	public StateMachineAccessor<S, E> getStateMachineAccessor() {
		// TODO: needs cleaning and perhaps not an anonymous function
		return new StateMachineAccessor<S, E>() {

			@Override
			public void doWithAllRegions(StateMachineFunction<StateMachineAccess<S, E>> stateMachineAccess) {
				stateMachineAccess.apply(AbstractStateMachine.this);
				for (State<S, E> state : states) {
					if (state.isSubmachineState()) {
						StateMachine<S, E> submachine = ((AbstractState<S, E>) state).getSubmachine();
						submachine.getStateMachineAccessor().doWithAllRegions(stateMachineAccess);
					} else if (state.isOrthogonal()) {
						Collection<Region<S, E>> regions = ((AbstractState<S, E>) state).getRegions();
						for (Region<S, E> region : regions) {
							((StateMachine<S, E>)region).getStateMachineAccessor().doWithAllRegions(stateMachineAccess);
						}
					}
				}
			}

			@Override
			public List<StateMachineAccess<S, E>> withAllRegions() {
				List<StateMachineAccess<S, E>> list = new ArrayList<StateMachineAccess<S, E>>();
				list.add(AbstractStateMachine.this);
				for (State<S, E> state : states) {
					if (state.isSubmachineState()) {
						StateMachine<S, E> submachine = ((AbstractState<S, E>) state).getSubmachine();
						if (submachine instanceof StateMachineAccess) {
							list.add((StateMachineAccess<S, E>)submachine);
						}
					} else if (state.isOrthogonal()) {
						Collection<Region<S, E>> regions = ((AbstractState<S, E>) state).getRegions();
						for (Region<S, E> region : regions) {
							list.add((StateMachineAccess<S, E>) region);
						}
					}
				}
				return list;
			}

			@Override
			public void doWithRegion(StateMachineFunction<StateMachineAccess<S, E>> stateMachineAccess) {
				stateMachineAccess.apply(AbstractStateMachine.this);
			}

			@Override
			public StateMachineAccess<S, E> withRegion() {
				return AbstractStateMachine.this;
			}
		};
	}

	@Override
	public void setRelay(StateMachine<S, E> stateMachine) {
		this.relay = stateMachine;
	}

	@Override
	protected void stateChangedInRelay() {
		// TODO: temp tweak, see super
		stateMachineExecutor.execute();
	}

	@Override
	public void setForwardedInitialEvent(Message<E> message) {
		forwardedInitialEvent = message;
	}

	private StateMachine<S, E> getRelayStateMachine() {
		return relay != null ? relay : this;
	}

	@Override
	public String toString() {
		ArrayList<State<S, E>> all = new ArrayList<State<S,E>>();
		for (State<S, E> s : states) {
			all.addAll(s.getStates());
		}
		StringBuilder buf = new StringBuilder();
		for (State<S, E> s : all) {
			buf.append(s.getId() + " ");
		}
		buf.append(" / ");
		if (currentState != null) {
			buf.append(StringUtils.collectionToCommaDelimitedString(currentState.getIds()));
		}
		buf.append(" / id=");
		buf.append(id);
		return buf.toString();
	}

	@Override
	public void resetStateMachine(StateMachineContext<S, E> stateMachineContext) {
		if (stateMachineContext == null) {
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("Request to reset state machine: stateMachine=[" + this + "] stateMachineContext=[" + stateMachineContext + "]");
		}
		S state = stateMachineContext.getState();
		if (state == null) {
			return;
		}
		boolean stateSet = false;
		for (State<S, E> s : getStates()) {
			for (State<S, E> ss : s.getStates()) {
				if (ss.getIds().contains(state)) {
					currentState = s;
					// TODO: not sure about starting submachine/regions here, though
					//       needed if we only transit to super state or reset regions
					if (s.isSubmachineState()) {
						StateMachine<S, E> submachine = ((AbstractState<S, E>)s).getSubmachine();
						for (final StateMachineContext<S, E> child : stateMachineContext.getChilds()) {
							submachine.getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<S,E>>() {

								@Override
								public void apply(StateMachineAccess<S, E> function) {
									function.resetStateMachine(child);
								}
							});
						}
						submachine.start();
					} else if (s.isOrthogonal() && stateMachineContext.getChilds() != null) {
						Collection<Region<S, E>> regions = ((AbstractState<S, E>)s).getRegions();
						for (Region<S, E> region : regions) {
							for (final StateMachineContext<S, E> child : stateMachineContext.getChilds()) {
								((StateMachine<S, E>)region).getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<S,E>>() {

									@Override
									public void apply(StateMachineAccess<S, E> function) {
										function.resetStateMachine(child);
									}
								});
							}
						}
						for (Region<S, E> region : regions) {
							region.start();
						}
					}

					if (log.isDebugEnabled()) {
						log.debug("State reseted: stateMachine=[" + this + "] stateMachineContext=[" + stateMachineContext + "]");
					}
					stateSet = true;
					break;
				}
			}
			if (stateSet) {
				break;
			}
		}
		if (stateSet && stateMachineContext.getExtendedState() != null) {
			this.extendedState = stateMachineContext.getExtendedState();
		}
	}

	@Override
	public void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor) {
		getStateMachineInterceptors().add(interceptor);
		stateMachineExecutor.addStateMachineInterceptor(interceptor);
	}

	@Override
	public String getId() {
		return id;
	}

	protected synchronized boolean acceptEvent(Message<E> message) {
		if ((currentState != null && currentState.shouldDefer(message))) {
			log.info("Current state " + currentState + " deferred event " + message);
			stateMachineExecutor.queueDeferredEvent(message);
			return true;
		}
		if ((currentState != null && currentState.sendEvent(message))) {
			return true;
		}

		if (log.isDebugEnabled()) {
			log.debug("Queue event " + message + " " + this);
		}

		for (Transition<S,E> transition : transitions) {
			State<S,E> source = transition.getSource();
			Trigger<S, E> trigger = transition.getTrigger();

			if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(message.getPayload()))) {
					stateMachineExecutor.queueEvent(message);
					return true;
				}
			}
		}
		// if we're about to not accept event, check defer again in case
		// state was changed between original check and now
		if ((currentState != null && currentState.shouldDefer(message))) {
			log.info("Current state " + currentState + " deferred event " + message);
			stateMachineExecutor.queueDeferredEvent(message);
			return true;
		}
		return false;
	}

	private boolean callPreStateChangeInterceptors(State<S,E> state, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		try {
			getStateMachineInterceptors().preStateChange(state, message, transition, stateMachine);
		} catch (Exception e) {
			log.info("Interceptors threw exception, skipping state change", e);
			return false;
		}
		return true;
	}

	private void callPostStateChangeInterceptors(State<S,E> state, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		try {
			getStateMachineInterceptors().postStateChange(state, message, transition, stateMachine);
		} catch (Exception e) {
		}
	}

	private boolean isInitialTransition(Transition<S,E> transition) {
		return transition != null && transition.getKind() == TransitionKind.INITIAL;
	}

	private void switchToState(State<S,E> state, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		if (!isInitialTransition(transition) && !callPreStateChangeInterceptors(state, message, transition, stateMachine)) {
			return;
		}
		// TODO: need to make below more clear when
		//       we figure out rest of a pseudostates
		PseudoStateKind kind = state.getPseudoState() != null ? state.getPseudoState().getKind() : null;
		if (kind == PseudoStateKind.CHOICE || kind == PseudoStateKind.HISTORY_SHALLOW
				|| kind == PseudoStateKind.HISTORY_DEEP) {
			StateContext<S, E> stateContext = buildStateContext(message, transition, stateMachine);
			State<S, E> toState = state.getPseudoState().entry(stateContext);

			if (kind == PseudoStateKind.CHOICE) {
				callPreStateChangeInterceptors(toState, message, transition, stateMachine);
			}

			setCurrentState(toState, message, transition, true, stateMachine);
		} else if (kind == PseudoStateKind.FORK) {
			ForkPseudoState<S, E> fps = (ForkPseudoState<S, E>) state.getPseudoState();
			for (State<S, E> ss : fps.getForks()) {
				callPreStateChangeInterceptors(ss, message, transition, stateMachine);
				setCurrentState(ss, message, transition, false, stateMachine);
			}
		} else {
			setCurrentState(state, message, transition, true, stateMachine);
		}

		callPostStateChangeInterceptors(state, message, transition, stateMachine);

		stateMachineExecutor.execute();
		if (isComplete()) {
			stop();
		}
	}

	private void registerPseudoStateListener() {
		for (State<S, E> state : states) {
			PseudoState<S, E> p = state.getPseudoState();
			if (p != null) {
				p.addPseudoStateListener(new PseudoStateListener<S, E>() {
					@Override
					public void onContext(PseudoStateContext<S, E> context) {
						PseudoState<S, E> pseudoState = context.getPseudoState();
						State<S, E> toState = findStateWithPseudoState(pseudoState);
						StateContext<S, E> stateContext = buildStateContext(null, null, getRelayStateMachine());
						pseudoState.exit(stateContext);
						switchToState(toState, null, null, getRelayStateMachine());
					}
				});
			}
		}
	}

	private State<S, E> findStateWithPseudoState(PseudoState<S, E> pseudoState) {
		for (State<S, E> s : states) {
			if (s.getPseudoState() == pseudoState) {
				return s;
			}
		}
		return null;
	}

	private StateContext<S, E> buildStateContext(Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		E event = message != null ? message.getPayload() : null;
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		return new DefaultStateContext<S, E>(event, messageHeaders, extendedState, transition, stateMachine);
	}

	private State<S, E> findDeepParent(State<S, E> state) {
		for (State<S, E> s : states) {
			if (s.getStates().contains(state)) {
				return s;
			}
		}
		return null;
	}

	synchronized void setCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, boolean exit, StateMachine<S, E> stateMachine) {

		State<S, E> findDeep = findDeepParent(state);
		boolean isTargetSubOf = false;
		if (transition != null) {
			isTargetSubOf = StateMachineUtils.isSubstate(state, transition.getSource());
			if (isTargetSubOf && currentState == transition.getTarget()) {
				state = transition.getSource();
			}
		}

		boolean nonDeepStatePresent = false;

		if (states.contains(state)) {
			if (exit) {
				exitCurrentState(state, message, transition, stateMachine);
			}
			State<S, E> notifyFrom = currentState;
			currentState = state;
			if (!isRunning()) {
				start();
			}
			entryToState(state, message, transition, stateMachine);
			notifyStateChanged(notifyFrom, state);
			nonDeepStatePresent = true;
		} else if (currentState == null && StateMachineUtils.isSubstate(findDeep, state)) {
			if (exit) {
				exitCurrentState(findDeep, message, transition, stateMachine);
			}
			State<S, E> notifyFrom = currentState;
			currentState = findDeep;
			if (!isRunning()) {
				start();
			}
			entryToState(findDeep, message, transition, stateMachine);
			notifyStateChanged(notifyFrom, findDeep);
		}

		if (currentState != null && !nonDeepStatePresent) {
			if (findDeep != null) {
				if (exit) {
					exitCurrentState(state, message, transition, stateMachine);
				}
				if (currentState == findDeep) {

					if (currentState.isSubmachineState()) {
						StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
						if (submachine.getState() == state) {
							if (currentState == findDeep) {
								if (isTargetSubOf) {
									entryToState(currentState, message, transition, stateMachine);
								}
								currentState = findDeep;
								((AbstractStateMachine<S, E>)submachine).setCurrentState(state, message, transition, false, stateMachine);
								return;
							}
						}
					} else if (currentState.isOrthogonal()) {
						Collection<Region<S, E>> regions = ((AbstractState<S, E>)currentState).getRegions();
						for (Region<S, E> region : regions) {
							if (region.getState() == state) {
								if (currentState == findDeep) {
									if (isTargetSubOf) {
										entryToState(currentState, message, transition, stateMachine);
									}
									currentState = findDeep;
									((AbstractStateMachine<S, E>)region).setCurrentState(state, message, transition, false, stateMachine);
									return;
								}
							}

						}
					}
				}
				boolean shouldTryEntry = findDeep != currentState;
				if (!shouldTryEntry && (transition.getSource() == currentState && StateMachineUtils.isSubstate(currentState, transition.getTarget()))) {
					shouldTryEntry = true;
				}
				currentState = findDeep;
				if (shouldTryEntry) {
					entryToState(currentState, message, transition, stateMachine);
				}

				if (currentState.isSubmachineState()) {
					StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
					((AbstractStateMachine<S, E>)submachine).setCurrentState(state, message, transition, false, stateMachine);
				} else if (currentState.isOrthogonal()) {
					Collection<Region<S, E>> regions = ((AbstractState<S, E>)currentState).getRegions();
					for (Region<S, E> region : regions) {
						((AbstractStateMachine<S, E>)region).setCurrentState(state, message, transition, false, stateMachine);
					}
				}
			}
		}
		if (history != null) {
			if (history.getKind() == PseudoStateKind.HISTORY_SHALLOW) {
				((HistoryPseudoState<S, E>)history).setState(findDeep);
			} else if (history.getKind() == PseudoStateKind.HISTORY_DEEP){
				((HistoryPseudoState<S, E>)history).setState(state);
			}
		}
	}

	void exitCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		if (currentState == null) {
			return;
		}
		if (currentState.isSubmachineState()) {
			StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
			((AbstractStateMachine<S, E>)submachine).exitCurrentState(state, message, transition, stateMachine);
			exitFromState(currentState, message, transition, stateMachine);
		} else {
			exitFromState(currentState, message, transition, stateMachine);
		}
	}

	private void exitFromState(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine) {
		if (state == null) {
			return;
		}
		log.trace("Trying Exit state=[" + state + "]");
		StateContext<S, E> stateContext = buildStateContext(message, transition, stateMachine);

		if (transition != null) {

			State<S, E> findDeep = findDeepParent(transition.getTarget());
			boolean isTargetSubOfOtherState = findDeep != null && findDeep != currentState;
			boolean isSubOfSource = StateMachineUtils.isSubstate(transition.getSource(), currentState);
			boolean isSubOfTarget = StateMachineUtils.isSubstate(transition.getTarget(), currentState);

			// TODO: this and entry below should be done via a separate
			// voter of some sort which would reveal transition path
			// we could make a choice on.
			if (currentState == transition.getSource() && currentState == transition.getTarget()) {
			} else if (!isSubOfSource && !isSubOfTarget && currentState == transition.getSource()) {
			} else if (!isSubOfSource && !isSubOfTarget && currentState == transition.getTarget()) {
			} else if (isTargetSubOfOtherState) {
			} else if (!isSubOfSource && !isSubOfTarget && findDeep == null) {
			} else if (!isSubOfSource && !isSubOfTarget && (transition.getSource() == currentState && StateMachineUtils.isSubstate(currentState, transition.getTarget()))) {
			} else if (!isSubOfSource && !isSubOfTarget) {
				return;
			}

		}

		log.debug("Exit state=[" + state + "]");
		state.exit(stateContext);
		notifyStateExited(state);
	}

	private void entryToState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		if (state == null) {
			return;
		}
		log.trace("Trying Enter state=[" + state + "]");
		StateContext<S, E> stateContext = buildStateContext(message, transition, stateMachine);

		if (transition != null) {
			State<S, E> findDeep1 = findDeepParent(transition.getTarget());
			State<S, E> findDeep2 = findDeepParent(transition.getSource());
			boolean isComingFromOtherSubmachine = findDeep1 != null && findDeep2 != null && findDeep2 != currentState;

			boolean isSubOfSource = StateMachineUtils.isSubstate(transition.getSource(), currentState);
			boolean isSubOfTarget = StateMachineUtils.isSubstate(transition.getTarget(), currentState);
			if (currentState == transition.getSource() && currentState == transition.getTarget()) {
			} else if (!isSubOfSource && !isSubOfTarget && currentState == transition.getTarget()) {
			} else if (isComingFromOtherSubmachine) {
			} else if (!isSubOfSource && !isSubOfTarget && findDeep2 == null) {
			} else if (isSubOfSource && !isSubOfTarget && currentState == transition.getTarget()) {
			} else if (!isSubOfSource && !isSubOfTarget && (transition.getSource() == currentState && StateMachineUtils.isSubstate(currentState, transition.getTarget()))) {
			} else if (!isSubOfSource && !isSubOfTarget) {
				return;
			}
		}

		notifyStateEntered(state);
		log.debug("Enter state=[" + state + "]");
		state.entry(stateContext);
	}

	private void callHandlers(State<S,E> sourceState, State<S,E> targetState, Message<E> message) {
		StateContext<S, E> stateContext = buildStateContext(message, null, getRelayStateMachine());
		getStateMachineHandlerResults(getStateMachineHandlers(sourceState, targetState), stateContext);
	}

	private List<Object> getStateMachineHandlerResults(List<StateMachineHandler<S, E>> stateMachineHandlers,
			final StateContext<S, E> stateContext) {
		StateMachineRuntime<S, E> runtime = new StateMachineRuntime<S, E>() {
			@Override
			public StateContext<S, E> getStateContext() {
				return stateContext;
			}
		};
		List<Object> results = new ArrayList<Object>();
		for (StateMachineHandler<S, E> handler : stateMachineHandlers) {
			results.add(handler.handle(runtime));
		}
		return results;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private synchronized List<StateMachineHandler<S, E>> getStateMachineHandlers(State<S, E> sourceState,
			State<S, E> targetState) {
		BeanFactory beanFactory = getBeanFactory();

		// TODO think how to handle null bf
		if (beanFactory == null) {
			return Collections.emptyList();
		}
		Assert.state(beanFactory instanceof ListableBeanFactory, "Bean factory must be instance of ListableBeanFactory");

		if (!handlersInitialized) {
			Map<String, StateMachineOnTransitionHandler> handlersMap = ((ListableBeanFactory) beanFactory)
					.getBeansOfType(StateMachineOnTransitionHandler.class);
			for (Entry<String, StateMachineOnTransitionHandler> entry : handlersMap.entrySet()) {
				handlers.put(entry.getKey(), entry.getValue());
			}
			handlersInitialized = true;
		}

		List<StateMachineHandler<S, E>> handlersList = new ArrayList<StateMachineHandler<S, E>>();

		for (Entry<String, StateMachineOnTransitionHandler<S, E>> entry : handlers.entrySet()) {
			// add only matching names from beanName and WithStateMachine name field
			WithStateMachine withStateMachine = AnnotationUtils.findAnnotation(entry.getValue().getBeanClass(), WithStateMachine.class);
			if (withStateMachine == null || !ObjectUtils.nullSafeEquals(withStateMachine.name(), getBeanName())) {
				continue;
			}
			OnTransition metaAnnotation = entry.getValue().getMetaAnnotation();
			Annotation annotation = entry.getValue().getAnnotation();
			if (transitionHandlerMatch(metaAnnotation, annotation, sourceState, targetState)) {
				handlersList.add(entry.getValue());
			}
		}

		OrderComparator comparator = new OrderComparator();
		Collections.sort(handlersList, comparator);
		return handlersList;
	}

	private boolean transitionHandlerMatch(OnTransition metaAnnotation, Annotation annotation, State<S, E> sourceState, State<S, E> targetState) {
		String[] msources = metaAnnotation.source();
		String[] mtargets = metaAnnotation.target();

		Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
		Object source = annotationAttributes.get("source");
		Object target = annotationAttributes.get("target");

		Collection<String> scoll = StateMachineUtils.toStringCollection(source);
		if (scoll.isEmpty()) {
			scoll = Arrays.asList(msources);
		}
		Collection<String> tcoll = StateMachineUtils.toStringCollection(target);
		if (tcoll.isEmpty()) {
			tcoll = Arrays.asList(mtargets);
		}

		boolean handle = false;
		if (!scoll.isEmpty() && !tcoll.isEmpty()) {
			if (sourceState != null
					&& targetState != null
					&& StateMachineUtils.containsAtleastOne(scoll,
							StateMachineUtils.toStringCollection(sourceState.getIds()))
					&& StateMachineUtils.containsAtleastOne(tcoll,
							StateMachineUtils.toStringCollection(targetState.getIds()))) {
				handle = true;
			}
		} else if (!scoll.isEmpty()) {
			if (sourceState != null
					&& StateMachineUtils.containsAtleastOne(scoll,
							StateMachineUtils.toStringCollection(sourceState.getIds()))) {
				handle = true;
			}
		} else if (!tcoll.isEmpty()) {
			if (targetState != null
					&& StateMachineUtils.containsAtleastOne(tcoll,
							StateMachineUtils.toStringCollection(targetState.getIds()))) {
				handle = true;
			}
		} else if (scoll.isEmpty() && tcoll.isEmpty()) {
			handle = true;
		}

		return handle;
	}

}
