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
package org.springframework.statemachine.support;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.Lifecycle;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.OnTransition;
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
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.DefaultTriggerContext;
import org.springframework.statemachine.trigger.TimerTrigger;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.statemachine.trigger.TriggerListener;
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
public abstract class AbstractStateMachine<S, E> extends StateMachineObjectSupport<S, E> implements StateMachine<S, E> {

	private static final Log log = LogFactory.getLog(AbstractStateMachine.class);

	private final Collection<State<S,E>> states;

	private final Collection<Transition<S,E>> transitions;

	private final State<S,E> initialState;

	private final Transition<S, E> initialTransition;

	private final Message<E> initialEvent;

	private final ExtendedState extendedState;

	private final Queue<Message<E>> eventQueue = new ConcurrentLinkedQueue<Message<E>>();

	private final LinkedList<Message<E>> deferList = new LinkedList<Message<E>>();

	private volatile State<S,E> currentState;

	private volatile PseudoState<S, E> history;

	private volatile Runnable task;

	private final AtomicBoolean requestTask = new AtomicBoolean(false);

	private final Map<String, StateMachineOnTransitionHandler<S, E>> handlers = new HashMap<String, StateMachineOnTransitionHandler<S,E>>();

	private volatile boolean handlersInitialized;

	private final Queue<TriggerQueueItem> triggerQueue = new ConcurrentLinkedQueue<TriggerQueueItem>();

	private final Map<Trigger<S, E>, Transition<S,E>> triggerToTransitionMap = new HashMap<Trigger<S,E>, Transition<S,E>>();

	private final List<Transition<S, E>> triggerlessTransitions = new ArrayList<Transition<S,E>>();

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
	 * @param initialEvent the initial event of this machine
	 * @param extendedState the extended state of this machine
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, Transition<S, E> initialTransition, Message<E> initialEvent, ExtendedState extendedState) {
		super();
		this.states = states;
		this.transitions = transitions;
		this.initialState = initialState;
		this.initialTransition = initialTransition;
		this.initialEvent = initialEvent;
		this.extendedState = extendedState != null ? extendedState : new DefaultExtendedState();
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
		if (isComplete() || !isRunning()) {
			return false;
		}
		boolean accepted = acceptEvent(event);
		scheduleEventQueueProcessing();
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
	}

	@Override
	protected void doStart() {
		// if state is set assume nothing to do
		if (currentState != null) {
			return;
		}
		registerTriggerListener();
		registerPseudoStateListener();
		switchToState(initialState, initialEvent, null, this);
		// TODO: for now execute outside of switchToState
		if (initialTransition != null) {
			StateContext<S, E> stateContext = buildStateContext(initialEvent, initialTransition, this);
			initialTransition.transit(stateContext);
		}
		notifyStateMachineStarted(this);
	}

	@Override
	protected void doStop() {
		notifyStateMachineStopped(this);
		currentState = null;
	}

	@Override
	public void addStateListener(StateMachineListener<S, E> listener) {
		getStateListener().register(listener);
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
		return buf.toString();
	}

	protected boolean acceptEvent(Message<E> message) {

		boolean accepted = currentState.sendEvent(message);
		if (accepted) {
			return true;
		}

		if (log.isDebugEnabled()) {
			log.debug("Queue event " + message);
		}

		Message<E> defer = null;
		for (Transition<S,E> transition : transitions) {
			State<S,E> source = transition.getSource();
			Trigger<S, E> trigger = transition.getTrigger();

			if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(message.getPayload()))) {
					triggerQueue.add(new TriggerQueueItem(trigger, message));
					return true;
				} else if (source.getDeferredEvents() != null && source.getDeferredEvents().contains(message.getPayload())) {
					defer = message;
				}
			}
		}
		if (defer != null) {
			log.info("Deferring event " + defer);
			deferList.addLast(defer);
			return true;
		}

		return false;
	}

	private void switchToState(State<S,E> state, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		// TODO: need to make below more clear when
		//       we figure out rest of a pseudostates
		PseudoStateKind kind = state.getPseudoState() != null ? state.getPseudoState().getKind() : null;
		if (kind == PseudoStateKind.CHOICE || kind == PseudoStateKind.HISTORY_SHALLOW
				|| kind == PseudoStateKind.HISTORY_DEEP) {
			StateContext<S, E> stateContext = buildStateContext(message, transition, stateMachine);
			State<S, E> toState = state.getPseudoState().entry(stateContext);
			setCurrentState(toState, message, transition, true, stateMachine);
		} else if (kind == PseudoStateKind.FORK) {
			ForkPseudoState<S, E> fps = (ForkPseudoState<S, E>) state.getPseudoState();
			for (State<S, E> ss : fps.getForks()) {
				setCurrentState(ss, message, transition, false, stateMachine);
			}
		} else {
			setCurrentState(state, message, transition, true, stateMachine);
		}

		scheduleEventQueueProcessing();
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
						pseudoState.exit(null);
						switchToState(toState, null, null, AbstractStateMachine.this);
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

	void setCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, boolean exit, StateMachine<S, E> stateMachine) {
		State<S, E> findDeep = findDeepParent(state);
		boolean isTargetSubOf = false;
		if (transition != null) {
			isTargetSubOf = StateMachineUtils.isSubstate(state, transition.getSource());
			if (isTargetSubOf && currentState == transition.getTarget()) {
				state = transition.getSource();
			}
		}

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
		} else if (currentState != null) {
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
				currentState = findDeep;
				entryToState(currentState, message, transition, stateMachine);

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
			boolean isTargetSubOfSource = StateMachineUtils.isSubstate(transition.getSource(), transition.getTarget());
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
			} else if (!isSubOfSource && !isSubOfTarget) {
				return;
			}

			if (transition.getSource() == currentState && isTargetSubOfSource) {
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
				return;
			} else if (!isSubOfSource && !isSubOfTarget) {
				return;
			}
		}

		notifyStateEntered(state);
		log.debug("Enter state=[" + state + "]");
		state.entry(stateContext);
	}

	private void processEventQueue() {
		log.debug("Process event queue");
		Message<E> queuedEvent = null;
		while ((queuedEvent = eventQueue.poll()) != null) {
			Message<E> defer = null;
			for (Transition<S,E> transition : transitions) {
				State<S,E> source = transition.getSource();
				Trigger<S, E> trigger = transition.getTrigger();

				if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
					if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(queuedEvent.getPayload()))) {
						triggerQueue.add(new TriggerQueueItem(trigger, queuedEvent));
					} else if (source.getDeferredEvents() != null && source.getDeferredEvents().contains(queuedEvent.getPayload())) {
						defer = queuedEvent;
					}
				}
			}
			if (defer != null) {
				log.info("Deferring event " + defer);
				deferList.addLast(defer);
			}
		}
	}

	private boolean processDeferList() {
		log.debug("Process defer list");
		boolean triggered = false;
		ListIterator<Message<E>> iterator = deferList.listIterator();
		while (iterator.hasNext()) {
			Message<E> event = iterator.next();
			for (Transition<S,E> transition : transitions) {
				State<S,E> source = transition.getSource();
				Trigger<S, E> trigger = transition.getTrigger();
				if (source.equals(currentState)) {
					if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(event.getPayload()))) {
						triggerQueue.add(new TriggerQueueItem(trigger, event));
						iterator.remove();
						triggered = true;
					}
				}
			}
		}
		return triggered;
	}

	private void scheduleEventQueueProcessing() {
		TaskExecutor executor = getTaskExecutor();
		if (executor == null) {
			return;
		}
		if (task == null) {
			task = new Runnable() {
				@Override
				public void run() {
					processEventQueue();
					processTriggerQueue();
					while (processDeferList()) {
						processTriggerQueue();
					}
					task = null;
					if (requestTask.getAndSet(false)) {
						scheduleEventQueueProcessing();
					}
				}
			};
			executor.execute(task);
		} else {
			requestTask.set(true);
		}
	}

	private void processTriggerQueue() {
		log.debug("Process trigger queue");
		TriggerQueueItem queueItem = null;
		while ((queueItem = triggerQueue.poll()) != null) {

			if (currentState == null) {
				continue;
			}

			Message<E> queuedMessage = queueItem.message;
			E event = queuedMessage != null ? queuedMessage.getPayload() : null;

			// need all transitions trigger could match, event trigger may match multiple
			// need to go up from substates and ask if trigger transit, if not check super
			ArrayList<Transition<S, E>> trans = new ArrayList<Transition<S,E>>();

			if (event != null) {
				ArrayList<S> ids = new ArrayList<S>(currentState.getIds());
				Collections.reverse(ids);
				for (S id : ids) {
					for (Entry<Trigger<S, E>, Transition<S, E>> e : triggerToTransitionMap.entrySet()) {
						Trigger<S, E> tri = e.getKey();
						E ee = tri.getEvent();
						Transition<S, E> tra = e.getValue();
						if (event == ee) {
							if (tra.getSource().getId() == id && !trans.contains(tra)) {
								trans.add(tra);
								continue;
							}
						}
					}
				}
			}

			// most likely timer
			if (trans.isEmpty()) {
				trans.add(triggerToTransitionMap.get(queueItem.trigger));
			}

			// go through candidates and transit max one
			handleTriggerTrans(trans, queuedMessage);
		}
		if (currentState != null) {
			// handle triggerless transitions
			handleTriggerTrans(triggerlessTransitions, null);
		}
	}

	private void handleTriggerTrans(List<Transition<S, E>> trans, Message<E> queuedMessage) {
		for (Transition<S, E> t : trans) {
			StateContext<S, E> stateContext = buildStateContext(queuedMessage, t, this);
			if (t == null) {
				continue;
			}
			State<S,E> source = t.getSource();
			if (source == null) {
				continue;
			}
			if (!StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				continue;
			}
			boolean transit = t.transit(stateContext);
			if (transit) {
				// TODO: should change trasition api so that we can ask
				//       if transition will transit so that we can post
				//       accurate notifyTransitionStart
				notifyTransitionStart(t);
				callHandlers(t.getSource(), t.getTarget(), queuedMessage);
				if (t.getKind() != TransitionKind.INTERNAL) {
					switchToState(t.getTarget(), queuedMessage, t, this);
				}
				notifyTransition(t);
				notifyTransitionEnd(t);
				break;
			}

		}

	}

	private void callHandlers(State<S,E> sourceState, State<S,E> targetState, Message<E> message) {
		if (sourceState != null && targetState != null) {
			StateContext<S, E> stateContext = buildStateContext(message, null, this);
			getStateMachineHandlerResults(getStateMachineHandlers(sourceState, targetState), stateContext);
		}
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

	private synchronized List<StateMachineHandler<S, E>> getStateMachineHandlers(State<S, E> sourceState,
			State<S, E> targetState) {
		BeanFactory beanFactory = getBeanFactory();

		// TODO think how to handle null bf
		if (beanFactory == null) {
			return Collections.emptyList();
		}
		Assert.state(beanFactory instanceof ListableBeanFactory, "Bean factory must be instance of ListableBeanFactory");

		if (!handlersInitialized) {
			Map<String, StateMachineOnTransitionHandler> handlersx = ((ListableBeanFactory) beanFactory)
					.getBeansOfType(StateMachineOnTransitionHandler.class);
			for (Entry<String, StateMachineOnTransitionHandler> entry : handlersx.entrySet()) {
				handlers.put(entry.getKey(), entry.getValue());
			}
			handlersInitialized = true;
		}

		List<StateMachineHandler<S, E>> handlersList = new ArrayList<StateMachineHandler<S, E>>();

		for (Entry<String, StateMachineOnTransitionHandler<S, E>> entry : handlers.entrySet()) {
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
			if (StateMachineUtils.containsAtleastOne(scoll, StateMachineUtils.toStringCollection(sourceState.getIds()))
					&& StateMachineUtils.containsAtleastOne(tcoll,
							StateMachineUtils.toStringCollection(targetState.getIds()))) {
				handle = true;
			}
		} else if (!scoll.isEmpty()) {
			if (StateMachineUtils.containsAtleastOne(scoll, StateMachineUtils.toStringCollection(sourceState.getIds()))) {
				handle = true;
			}
		} else if (!tcoll.isEmpty()) {
			if (StateMachineUtils.containsAtleastOne(tcoll, StateMachineUtils.toStringCollection(targetState.getIds()))) {
				handle = true;
			}
		}

		return handle;
	}

	private void registerTriggerListener() {
		for (final Trigger<S, E> trigger : triggerToTransitionMap.keySet()) {
			if (trigger instanceof TimerTrigger) {
				((TimerTrigger<?, ?>)trigger).addTriggerListener(new TriggerListener() {
					@Override
					public void triggered() {
						log.debug("TimedTrigger triggered " + trigger);
						triggerQueue.add(new TriggerQueueItem(trigger, null));
						scheduleEventQueueProcessing();
					}
				});
			}
			if (trigger instanceof Lifecycle) {
				((Lifecycle)trigger).start();
			}
		}
	}

	private class TriggerQueueItem {
		Trigger<S, E> trigger;
		Message<E> message;
		public TriggerQueueItem(Trigger<S, E> trigger, Message<E> message) {
			this.trigger = trigger;
			this.message = message;
		}
	}

}
