/*
 * Copyright 2015-2016 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.ExtendedState.ExtendedStateChangeListener;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.listener.StateMachineListener;
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

	// using this to log last state when machine stops, as
	// it's a bit difficult to keep currentState non-null after stop.
	private volatile State<S,E> lastState;

	private volatile Exception currentError;

	private volatile PseudoState<S, E> history;

	private final Map<Trigger<S, E>, Transition<S,E>> triggerToTransitionMap = new HashMap<Trigger<S,E>, Transition<S,E>>();

	private final List<Transition<S, E>> triggerlessTransitions = new ArrayList<Transition<S,E>>();

	private StateMachine<S, E> relay;

	private StateMachineExecutor<S, E> stateMachineExecutor;

	private Boolean initialEnabled = null;

	private UUID uuid = UUID.randomUUID();

	private String id;

	private volatile Message<E> forwardedInitialEvent;

	private final Object lock = new Object();

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
		// if we're complete assume we're stopped
		// and state was stashed into lastState
		synchronized (lock) {
			if (lastState != null && isComplete()) {
				return lastState;
			} else {
				return currentState;
			}
		}
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

	public PseudoState<S, E> getHistoryState() {
		return history;
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		if (hasStateMachineError()) {
			// TODO: should we throw exception?
			notifyEventNotAccepted(buildStateContext(Stage.EVENT_NOT_ACCEPTED, event, null, getRelayStateMachine(), getState(), null));
			return false;
		}

		try {
			event = getStateMachineInterceptors().preEvent(event, this);
		} catch (Exception e) {
			log.info("Event " + event + " threw exception in interceptors, not accepting event");
			notifyEventNotAccepted(buildStateContext(Stage.EVENT_NOT_ACCEPTED, event, null, getRelayStateMachine(), getState(), null));
			return false;
		}

		if (isComplete() || !isRunning()) {
			notifyEventNotAccepted(buildStateContext(Stage.EVENT_NOT_ACCEPTED, event, null, getRelayStateMachine(), getState(), null));
			return false;
		}
		boolean accepted = acceptEvent(event);
		stateMachineExecutor.execute();
		if (!accepted) {
			notifyEventNotAccepted(buildStateContext(Stage.EVENT_NOT_ACCEPTED, event, null, getRelayStateMachine(), getState(), null));
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
		lastState = null;
		extendedState.setExtendedStateChangeListener(new ExtendedStateChangeListener() {
			@Override
			public void changed(Object key, Object value) {
				notifyExtendedStateChanged(key, value, buildStateContext(Stage.EXTENDED_STATE_CHANGED, null, null, getRelayStateMachine()));
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
			public void transit(Transition<S, E> t, StateContext<S, E> ctx, Message<E> message) {
				// TODO: fix above stateContext as it's not used
				notifyTransitionStart(buildStateContext(Stage.TRANSITION_START, message, t, getRelayStateMachine()));
				notifyTransition(buildStateContext(Stage.TRANSITION, message, t, getRelayStateMachine()));
				if (t.getTarget().getPseudoState() != null && t.getTarget().getPseudoState().getKind() == PseudoStateKind.JOIN) {
					exitFromState(t.getSource(), message, t, getRelayStateMachine());
				} else {
					if (t.getKind() == TransitionKind.INITIAL) {
						switchToState(t.getTarget(), message, t, getRelayStateMachine());
						notifyStateMachineStarted(buildStateContext(Stage.STATEMACHINE_START, message, t, getRelayStateMachine()));
					} else if (t.getKind() != TransitionKind.INTERNAL) {
						switchToState(t.getTarget(), message, t, getRelayStateMachine());
					}
				}
				// TODO: looks like events should be called here and anno processing earlier
				notifyTransitionEnd(buildStateContext(Stage.TRANSITION_END, message, t, getRelayStateMachine()));
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
		super.doStart();
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
			StateContext<S, E> stateContext = buildStateContext(Stage.STATEMACHINE_START, null, null, getRelayStateMachine());
			notifyStateMachineStarted(stateContext);
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
		synchronized (lock) {
			stateMachineExecutor.stop();
			notifyStateMachineStopped(buildStateContext(Stage.STATEMACHINE_STOP, null, null, this));
			// stash current state before we null it so that
			// we can still return where we 'were' when machine is stopped
			lastState = currentState;
			currentState = null;
			initialEnabled = null;
		}
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
			notifyStateMachineError(buildStateContext(Stage.STATEMACHINE_ERROR, null, null, this, currentError));
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
		State<S, E> s = currentState;
		if (s == null) {
			return !isRunning();
		} else {
			return s != null && s.getPseudoState() != null
					&& s.getPseudoState().getKind() == PseudoStateKind.END;
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
		buf.append(" / uuid=");
		buf.append(uuid);
		buf.append(" / id=");
		buf.append(id);
		return buf.toString();
	}

	@Override
	public void resetStateMachine(StateMachineContext<S, E> stateMachineContext) {
		// TODO: this function needs a serious rewrite
		if (stateMachineContext == null) {
			log.info("Got null context, resetting to initial state and clearing extended state");
			currentState = initialState;
			extendedState.getVariables().clear();
			return;
		}
		if (log.isDebugEnabled()) {
			log.debug("Request to reset state machine: stateMachine=[" + this + "] stateMachineContext=[" + stateMachineContext + "]");
		}
		setId(stateMachineContext.getId());
		S state = stateMachineContext.getState();
		boolean stateSet = false;
		// handle state reset
		for (State<S, E> s : getStates()) {
			for (State<S, E> ss : s.getStates()) {
				if (state != null && ss.getIds().contains(state)) {
					currentState = s;
					// setting lastState here is needed for restore
					lastState = currentState;
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
				} else if (!stateMachineContext.getChilds().isEmpty()) {
					// we're here because root machine only have regions
					if (s.isOrthogonal()) {
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
				}
			}
			if (stateSet) {
				break;
			}
		}

		// handle history reset here as above state reset loop breaks out
		if (history != null && stateMachineContext.getHistoryStates() != null) {
			// setting history for 'this' machine
			State<S, E> h = null;
			for (State<S, E> hh : getStates()) {
				if (hh.getId().equals(stateMachineContext.getHistoryStates().get(null))) {
					h = hh;
					break;
				}
			}
			if (h != null) {
				((HistoryPseudoState<S, E>) history).setState(h);
			}
		}
		for (State<S, E> s : getStates()) {
			// setting history for 'submachines'
			if (s.isSubmachineState()) {
				StateMachine<S, E> submachine = ((AbstractState<S, E>) s).getSubmachine();
				PseudoState<S, E> submachineHistory = ((AbstractStateMachine<S, E>) submachine).getHistoryState();
				if (submachineHistory != null) {
					State<S, E> h = null;
					for (State<S, E> hh : submachine.getStates()) {
						if (hh.getId().equals(stateMachineContext.getHistoryStates().get(s.getId()))) {
							h = hh;
							break;
						}
					}
					if (h != null) {
						((HistoryPseudoState<S, E>) submachineHistory).setState(h);
					}
				}

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
	public UUID getUuid() {
		return uuid;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Sets the machine id.
	 *
	 * @param id the new machine id
	 */
	public void setId(String id) {
		this.id = id;
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

		StateContext<S, E> stateContext = buildStateContext(Stage.STATE_CHANGED, message, transition, stateMachine);
		State<S,E> toState = followLinkedPseudoStates(state, stateContext);
		PseudoStateKind kind = state.getPseudoState() != null ? state.getPseudoState().getKind() : null;
		if (kind != null && (kind != PseudoStateKind.INITIAL && kind != PseudoStateKind.JOIN && kind != PseudoStateKind.FORK)) {
			callPreStateChangeInterceptors(toState, message, transition, stateMachine);
		}

		// need to check for from original state passed in
		kind = toState.getPseudoState() != null ? toState.getPseudoState().getKind() : null;
		if (kind == PseudoStateKind.FORK) {
			exitCurrentState(toState, message, transition, stateMachine);
			ForkPseudoState<S, E> fps = (ForkPseudoState<S, E>) toState.getPseudoState();
			for (State<S, E> ss : fps.getForks()) {
				callPreStateChangeInterceptors(ss, message, transition, stateMachine);
				setCurrentState(ss, message, transition, false, stateMachine, null, fps.getForks());
			}
		} else {
			Collection<State<S, E>> targets = new ArrayList<>();
			targets.add(toState);
			setCurrentState(toState, message, transition, true, stateMachine, null, targets);
		}

		callPostStateChangeInterceptors(state, message, transition, stateMachine);

		stateMachineExecutor.execute();
		if (isComplete()) {
			stop();
		}
	}

	private State<S,E> followLinkedPseudoStates(State<S,E> state, StateContext<S, E> stateContext) {
		PseudoStateKind kind = state.getPseudoState() != null ? state.getPseudoState().getKind() : null;
		if (kind == PseudoStateKind.INITIAL ||  kind == PseudoStateKind.FORK) {
			return state;
		} else if (kind != null) {
			State<S,E> toState = state.getPseudoState().entry(stateContext);
			if (toState == null) {
				return state;
			} else {
				return followLinkedPseudoStates(toState, stateContext);
			}
		} else {
			return state;
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
						State<S, E> toStateOrig = findStateWithPseudoState(pseudoState);
						StateContext<S, E> stateContext = buildStateContext(Stage.STATE_EXIT, null, null, getRelayStateMachine());
						State<S, E> toState = followLinkedPseudoStates(toStateOrig, stateContext);
						// TODO: try to find matching transition based on direct link.
						// should make this built-in in pseudostates
						Transition<S, E> transition = findTransition(toStateOrig, toState);
						switchToState(toState, null, transition, getRelayStateMachine());
						pseudoState.exit(stateContext);
					}
				});
			}
		}
	}

	private Transition<S, E> findTransition(State<S, E> from, State<S, E> to) {
		for (Transition<S, E> transition : transitions) {
			if (transition.getSource() == from && transition.getTarget() == to) {
				return transition;
			}
		}
		return null;
	}

	private State<S, E> findStateWithPseudoState(PseudoState<S, E> pseudoState) {
		for (State<S, E> s : states) {
			if (s.getPseudoState() == pseudoState) {
				return s;
			}
		}
		return null;
	}

	private StateContext<S, E> buildStateContext(Stage stage, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		return new DefaultStateContext<S, E>(stage, message, messageHeaders, extendedState, transition, stateMachine, null, null, null);
	}

	private StateContext<S, E> buildStateContext(Stage stage, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine, Exception exception) {
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		return new DefaultStateContext<S, E>(stage, message, messageHeaders, extendedState, transition, stateMachine, null, null, exception);
	}

	private StateContext<S, E> buildStateContext(Stage stage, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine, State<S, E> source, State<S, E> target) {
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		return new DefaultStateContext<S, E>(stage, message, messageHeaders, extendedState, transition, stateMachine, source, target, null);
	}

	private StateContext<S, E> buildStateContext(Stage stage, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine, Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		return new DefaultStateContext<S, E>(stage, message, messageHeaders, extendedState, transition, stateMachine, null, null, sources, targets, null);
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
		setCurrentState(state, message, transition, exit, stateMachine, null, null);
	}

	synchronized void setCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, boolean exit,
			StateMachine<S, E> stateMachine, Collection<State<S, E>> sources, Collection<State<S, E>> targets) {

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
				exitCurrentState(state, message, transition, stateMachine, sources, targets);
			}
			State<S, E> notifyFrom = currentState;
			currentState = state;
			if (!isRunning()) {
				start();
			}
			entryToState(state, message, transition, stateMachine);
			notifyStateChanged(buildStateContext(Stage.STATE_CHANGED, message, null, getRelayStateMachine(), notifyFrom, state));
			nonDeepStatePresent = true;
		} else if (currentState == null && StateMachineUtils.isSubstate(findDeep, state)) {
			if (exit) {
				exitCurrentState(findDeep, message, transition, stateMachine, sources, targets);
			}
			State<S, E> notifyFrom = currentState;
			currentState = findDeep;
			if (!isRunning()) {
				start();
			}
			entryToState(findDeep, message, transition, stateMachine);
			notifyStateChanged(buildStateContext(Stage.STATE_CHANGED, message, null, getRelayStateMachine(), notifyFrom, findDeep));
		}

		if (currentState != null && !nonDeepStatePresent) {
			if (findDeep != null) {
				if (exit) {
					exitCurrentState(state, message, transition, stateMachine, sources, targets);
				}
				if (currentState == findDeep) {

					if (currentState.isSubmachineState()) {
						StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
						// need to check complete as submachine may now return non null
						if (!submachine.isComplete() && submachine.getState() == state) {
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
					entryToState(currentState, message, transition, stateMachine, sources, targets);
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
		if (history != null && transition.getKind() != TransitionKind.INITIAL) {
			// do not set history if this is initial transition as
			// it would break history state set via reset as
			// we get here i.e. when machine is started in reset.
			// and it really doesn't make sense to set initial state for history
			// if we get here via initial transition
			if (history.getKind() == PseudoStateKind.HISTORY_SHALLOW) {
				((HistoryPseudoState<S, E>)history).setState(findDeep);
			} else if (history.getKind() == PseudoStateKind.HISTORY_DEEP){
				((HistoryPseudoState<S, E>)history).setState(state);
			}
		}
		// if state was set from parent and we're now complete
		// also initiate stop
		if (stateMachine != this && isComplete()) {
			stop();
		}
	}

	void exitCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		exitCurrentState(state, message, transition, stateMachine, null, null);
	}

	void exitCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine,
			Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		if (currentState == null) {
			return;
		}
		if (currentState.isSubmachineState()) {
			StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
			((AbstractStateMachine<S, E>)submachine).exitCurrentState(state, message, transition, stateMachine);
			exitFromState(currentState, message, transition, stateMachine, sources, targets);
		} else if (currentState.isOrthogonal()) {
			Collection<Region<S,E>> regions = ((AbstractState<S, E>)currentState).getRegions();
			for (Region<S,E> r : regions) {
				if (r.getStates().contains(state)) {
					exitFromState(r.getState(), message, transition, stateMachine, sources, targets);
				}
			}
			exitFromState(currentState, message, transition, stateMachine, sources, targets);
		} else {
			exitFromState(currentState, message, transition, stateMachine, sources, targets);
		}
	}

	private void exitFromState(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine) {
		exitFromState(state, message, transition, stateMachine, null, null);
	}

	private void exitFromState(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine, Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		if (state == null) {
			return;
		}
		log.trace("Trying Exit state=[" + state + "]");
		StateContext<S, E> stateContext = buildStateContext(Stage.STATE_EXIT, message, transition, stateMachine);

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
			} else if (StateMachineUtils.isNormalPseudoState(transition.getTarget())) {
				if (isPseudoStateSubstate(findDeep, targets)) {
					return;
				}
			} else if (findDeep != null && findDeep != state && findDeep.getStates().contains(state)) {
			} else if (!isSubOfSource && !isSubOfTarget) {
				return;
			}

		}

		log.debug("Exit state=[" + state + "]");
		state.exit(stateContext);

		notifyStateExited(buildStateContext(Stage.STATE_EXIT, message, null, getRelayStateMachine(), state, null));
	}

	private boolean isPseudoStateSubstate(State<S, E> left, Collection<State<S, E>> rights) {
		if (rights == null || left == null) {
			return false;
		}
		for (State<S, E> s : rights) {
			if (StateMachineUtils.isSubstate(left, s)) {
				return true;
			}
		}
		return false;
	}

	private void entryToState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		entryToState(state, message, transition, stateMachine, null, null);
	}

	private void entryToState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine,
			Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		if (state == null) {
			return;
		}
		log.trace("Trying Enter state=[" + state + "]");
		StateContext<S, E> stateContext = buildStateContext(Stage.STATE_ENTRY, message, transition, stateMachine, sources, targets);

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
				if (isDirectSubstate(transition.getSource(), transition.getTarget())) {
					return;
				}
			} else if (!isSubOfSource && !isSubOfTarget && (transition.getSource() == currentState && StateMachineUtils.isSubstate(currentState, transition.getTarget()))) {
			} else if (!isSubOfSource && !isSubOfTarget) {
				return;
			}
		}

		notifyStateEntered(buildStateContext(Stage.STATE_ENTRY, message, transition, getRelayStateMachine(), null, state));
		log.debug("Enter state=[" + state + "]");
		state.entry(stateContext);
	}

	private static <S, E> boolean isDirectSubstate(State<S, E> left, State<S, E> right) {
		// Checks if right hand side is a direct substate of a left hand side.
		if (left != null && left.isSubmachineState()) {
			StateMachine<S, E> submachine = ((AbstractState<S, E>)left).getSubmachine();
			return submachine.getStates().contains(right);
		} else {
			return false;
		}
	}
}
