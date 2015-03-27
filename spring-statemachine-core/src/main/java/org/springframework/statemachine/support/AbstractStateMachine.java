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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.Lifecycle;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.event.StateMachineEventPublisher;
import org.springframework.statemachine.listener.CompositeStateMachineListener;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.processor.StateMachineHandler;
import org.springframework.statemachine.processor.StateMachineOnTransitionHandler;
import org.springframework.statemachine.processor.StateMachineRuntime;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.DefaultTriggerContext;
import org.springframework.statemachine.trigger.TimerTrigger;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.statemachine.trigger.TriggerListener;
import org.springframework.util.Assert;

/**
 * Base implementation of a {@link StateMachine} loosely modelled from UML state
 * machine.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractStateMachine<S, E> extends LifecycleObjectSupport implements StateMachine<S, E> {

	private static final Log log = LogFactory.getLog(AbstractStateMachine.class);

	private final Collection<State<S,E>> states;

	private final Collection<Transition<S,E>> transitions;

	private final State<S,E> initialState;

	private final Transition<S, E> initialTransition;

	private final State<S,E> endState;

	private final Message<E> initialEvent;

	private final ExtendedState extendedState;

	private final Queue<Message<E>> eventQueue = new ConcurrentLinkedQueue<Message<E>>();

	private final LinkedList<Message<E>> deferList = new LinkedList<Message<E>>();

	private final CompositeStateMachineListener<S, E> stateListener = new CompositeStateMachineListener<S, E>();

	private volatile State<S,E> currentState;

	private volatile Runnable task;

	private final Map<String, StateMachineOnTransitionHandler<S, E>> handlers = new HashMap<String, StateMachineOnTransitionHandler<S,E>>();

	private volatile boolean handlersInitialized;

	private final Queue<TriggerQueueItem> triggerQueue = new ConcurrentLinkedQueue<TriggerQueueItem>();

	private final Map<Trigger<S, E>, Transition<S,E>> triggerToTransitionMap = new HashMap<Trigger<S,E>, Transition<S,E>>();

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
	 * @param states the states
	 * @param transitions the transitions
	 * @param initialState the initial state
	 * @param endState the end state
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, State<S, E> endState) {
		this(states, transitions, initialState, null, endState, null, new DefaultExtendedState());
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
		this(states, transitions, initialState, null, null, null, extendedState);
	}

	/**
	 * Instantiates a new abstract state machine.
	 *
	 * @param states the states of this machine
	 * @param transitions the transitions of this machine
	 * @param initialState the initial state of this machine
	 * @param endState the final state of this machine
	 * @param initialEvent the initial event of this machine
	 * @param extendedState the extended state of this machine
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, Transition<S, E> initialTransition, State<S, E> endState, Message<E> initialEvent, ExtendedState extendedState) {
		super();
		this.states = states;
		this.transitions = transitions;
		this.initialState = initialState;
		this.initialTransition = initialTransition;
		this.endState = endState;
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
				triggerToTransitionMap.put(trigger, transition);
			}
		}
		for (State<S, E> state : states) {
			if (state.isSubmachineState()) {
				StateMachine<S, E> submachine = ((AbstractState<S, E>)state).getSubmachine();
				submachine.addStateListener(new StateMachineListenerRelay());
			}
		}
	}

	@Override
	protected void doStart() {
		super.doStart();
		registerTriggerListener();
		switchToState(initialState, initialEvent, null);
		// TODO: for now execute outside of switchToState
		if (initialTransition != null) {
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(
					initialEvent != null ? initialEvent.getHeaders() : null, extendedState, initialTransition, this);
			initialTransition.transit(stateContext);
		}
	}

	@Override
	protected void doStop() {
		super.doStop();
		currentState = null;
	}

	@Override
	public void addStateListener(StateMachineListener<S, E> listener) {
		stateListener.register(listener);
	}

	@Override
	public boolean isComplete() {
		return (endState != null && endState.equals(currentState));
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

	protected boolean acceptEvent(Message<E> event) {

		boolean accepted = currentState.sendEvent(event);
		if (accepted) {
			return true;
		}

		if (log.isDebugEnabled()) {
			log.debug("Queue event " + event);
		}

		Message<E> defer = null;
		for (Transition<S,E> transition : transitions) {
			State<S,E> source = transition.getSource();
			Trigger<S, E> trigger = transition.getTrigger();

			if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(event.getPayload()))) {
					triggerQueue.add(new TriggerQueueItem(trigger, event));
					return true;
				} else if (source.getDeferredEvents() != null && source.getDeferredEvents().contains(event.getPayload())) {
					defer = event;
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

	private void switchToState(State<S,E> state, Message<E> event, Transition<S,E> transition) {
		setCurrentState(state, event, transition);

		// TODO: should handle triggerles transition some how differently
		for (Transition<S,E> t : transitions) {
			State<S,E> source = t.getSource();
			State<S,E> target = t.getTarget();
			if (t.getTrigger() == null && source.equals(currentState)) {
				switchToState(target, event, t);
			}
		}

	}

	void setCurrentState(State<S, E> state, Message<E> event, Transition<S, E> transition) {
		if (states.contains(state)) {
			exitFromState(currentState, event, transition);
			State<S, E> notifyFrom = currentState;
			currentState = state;
			entryToState(state, event, transition);
			notifyStateChanged(notifyFrom, state);
		} else if (currentState.isSubmachineState()) {
			// TODO: should find a better way to trick setting state for submachine
			//       without a need to access package protected method via casting
			StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
			((AbstractStateMachine<S, E>)submachine).setCurrentState(state, event, transition);
		}
	}

	private void exitFromState(State<S, E> state, Message<E> event, Transition<S, E> transition) {
		if (state != null) {
			log.trace("Exit state=[" + state + "]");
			MessageHeaders messageHeaders = event != null ? event.getHeaders() : new MessageHeaders(
					new HashMap<String, Object>());
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(messageHeaders, extendedState, transition, this);

			// TODO: we use this trick not to double notify
			State<S, E> toNotify = null;
			if (state.isSubmachineState()) {
				StateMachine<S, E> submachine = ((AbstractState<S, E>)state).getSubmachine();
				if (((LifecycleObjectSupport)submachine).isRunning()) {
					toNotify = submachine.getState();
				}
			}

			if (toNotify != null) {
				notifyStateExited(toNotify);
			}

			state.exit(event != null ? event.getPayload() : null, stateContext);
			notifyStateExited(state);

		}
	}

	private void entryToState(State<S, E> state, Message<E> event, Transition<S, E> transition) {
		if (state != null) {
			log.trace("Enter state=[" + state + "]");
			MessageHeaders messageHeaders = event != null ? event.getHeaders() : new MessageHeaders(
					new HashMap<String, Object>());
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(messageHeaders, extendedState, transition, this);
			notifyStateEntered(state);

			// TODO: we use this trick not to double notify
			State<S, E> toNotify = null;
			if (state.isSubmachineState()) {
				StateMachine<S, E> submachine = ((AbstractState<S, E>)state).getSubmachine();
				if (((LifecycleObjectSupport)submachine).isRunning()) {
					toNotify = submachine.getState();
				}
			}

			state.entry(event != null ? event.getPayload() : null, stateContext);
			if (toNotify != null) {
				notifyStateEntered(toNotify);
			}
		}
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
				}
			};
			getTaskExecutor().execute(task);
		}
	}

	private void processTriggerQueue() {
		log.debug("Process trigger queue");
		TriggerQueueItem queueItem = null;
		while ((queueItem = triggerQueue.poll()) != null) {
			Message<E> queuedEvent = queueItem.message;
			Transition<S, E> transition = triggerToTransitionMap.get(queueItem.trigger);
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(queuedEvent != null ? queuedEvent.getHeaders() : null, extendedState, transition, this);
			if (transition == null) {
				continue;
			}

			State<S,E> source = transition.getSource();
			if (source == null || currentState == null) {
				continue;
			}
			if (!StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				continue;
			}

			notifyTransitionStart(transition);
			callHandlers(transition.getSource(), transition.getTarget(), queuedEvent);
			boolean transit = transition.transit(stateContext);
			if (transit) {
				if (transition.getKind() != TransitionKind.INTERNAL) {
					switchToState(transition.getTarget(), queuedEvent, transition);
				}
				notifyTransition(transition);
			}
			notifyTransitionEnd(transition);
		}

	}

	private void callHandlers(State<S,E> sourceState, State<S,E> targetState, Message<E> event) {
		if (sourceState != null && targetState != null) {
			MessageHeaders messageHeaders = event != null ? event.getHeaders() : new MessageHeaders(
					new HashMap<String, Object>());
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(messageHeaders, extendedState, null, this);
			getStateMachineHandlerResults(getStateMachineHandlers(sourceState, targetState), stateContext);
		}
	}

	private List<Object> getStateMachineHandlerResults(List<StateMachineHandler<S, E>> stateMachineHandlers, final StateContext<S, E> stateContext) {
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

	private synchronized List<StateMachineHandler<S, E>> getStateMachineHandlers(State<S, E> sourceState, State<S, E> targetState) {
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

	private void notifyStateChanged(State<S,E> source, State<S,E> target) {
		stateListener.stateChanged(source, target);
		StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
		if (eventPublisher != null) {
			eventPublisher.publishStateChanged(this, source, target);
		}
	}

	private void notifyStateEntered(State<S,E> state) {
		stateListener.stateEntered(state);
		StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
		if (eventPublisher != null) {
			eventPublisher.publishStateEntered(this, state);
		}
	}

	private void notifyStateExited(State<S,E> state) {
		stateListener.stateExited(state);
		StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
		if (eventPublisher != null) {
			eventPublisher.publishStateExited(this, state);
		}
	}

	private void notifyTransitionStart(Transition<S,E> transition) {
		stateListener.transitionStarted(transition);
		StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
		if (eventPublisher != null) {
			eventPublisher.publishTransitionStart(this, transition);
		}
	}

	private void notifyTransition(Transition<S,E> transition) {
		stateListener.transition(transition);
		StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
		if (eventPublisher != null) {
			eventPublisher.publishTransitionEnd(this, transition);
		}
	}

	private void notifyTransitionEnd(Transition<S,E> transition) {
		stateListener.transitionEnded(transition);
		StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
		if (eventPublisher != null) {
			eventPublisher.publishTransition(this, transition);
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

	/**
	 * This class is used to relay listener events from a submachines which works
	 * as its own listener context. User only connects to main root machine and
	 * expects to get events for all machines from there.
	 */
	private class StateMachineListenerRelay implements StateMachineListener<S,E> {

		@Override
		public void stateChanged(State<S, E> from, State<S, E> to) {
			stateListener.stateChanged(from, to);
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

	}

}
