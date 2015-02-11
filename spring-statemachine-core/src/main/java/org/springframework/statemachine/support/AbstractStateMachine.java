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

import java.util.ArrayList;
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
import org.springframework.core.OrderComparator;
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
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.Trigger;
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

	private final State<S,E> endState;

	private final Message<E> initialEvent;

	private final ExtendedState extendedState;

	private final Queue<Message<E>> eventQueue = new ConcurrentLinkedQueue<Message<E>>();

	private final LinkedList<Message<E>> deferList = new LinkedList<Message<E>>();

	private final CompositeStateMachineListener<S, E> stateListener = new CompositeStateMachineListener<S, E>();

	private volatile State<S,E> currentState;

	private volatile Runnable task;

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
		this(states, transitions, initialState, endState, null, null);
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
	 * @param endState the final state of this machine
	 * @param initialEvent the initial event of this machine
	 * @param extendedState the extended state of this machine
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, State<S, E> endState, Message<E> initialEvent, ExtendedState extendedState) {
		super();
		this.states = states;
		this.transitions = transitions;
		this.initialState = initialState;
		this.endState = endState;
		this.initialEvent = initialEvent;
		this.extendedState = extendedState;
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
	public void sendEvent(Message<E> event) {
		if (isComplete()) {
			return;
		}
		// TODO: machine header looks weird!
		event = MessageBuilder.fromMessage(event).setHeader("machine", this).build();
		if (log.isDebugEnabled()) {
			log.debug("Queue event " + event);
		}
		eventQueue.add(event);
		scheduleEventQueueProcessing();
	}

	@Override
	public void sendEvent(E event) {
		sendEvent(MessageBuilder.withPayload(event).build());
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		Assert.notNull(initialState, "Initial state must be set");
		Assert.state(initialState.getPseudoState() != null
				&& initialState.getPseudoState().getKind() == PseudoStateKind.INITIAL,
				"Initial state's pseudostate kind must be INITIAL");
	}

	@Override
	protected void doStart() {
		super.doStart();
		switchToState(initialState, initialEvent, null);
	}

	@Override
	protected void doStop() {
		super.doStop();
		currentState = null;
	}

	@Override
	public void addStateListener(StateMachineListener<State<S, E>, E> listener) {
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

	private void switchToState(State<S,E> state, Message<E> event, Transition<S,E> transition) {
		exitFromState(currentState, event, transition);
		notifyStateChanged(currentState, state);

		callHandlers(currentState, state, event);

		currentState = state;
		entryToState(state, event, transition);

		// TODO: should handle triggerles transition some how differently
		for (Transition<S,E> t : transitions) {
			State<S,E> source = t.getSource();
			State<S,E> target = t.getTarget();
			if (t.getTrigger() == null && source.equals(currentState)) {
				switchToState(target, event, t);
			}

		}

	}

	private void exitFromState(State<S, E> state, Message<E> event, Transition<S, E> transition) {
		if (state != null) {
			log.trace("Exit state=[" + state + "]");
			MessageHeaders messageHeaders = event != null ? event.getHeaders() : new MessageHeaders(
					new HashMap<String, Object>());
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(messageHeaders, extendedState, transition);
			state.exit(event != null ? event.getPayload() : null, stateContext);
		}
	}

	private void entryToState(State<S, E> state, Message<E> event, Transition<S, E> transition) {
		if (state != null) {
			log.trace("Enter state=[" + state + "]");
			MessageHeaders messageHeaders = event != null ? event.getHeaders() : new MessageHeaders(
					new HashMap<String, Object>());
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(messageHeaders, extendedState, transition);
			state.entry(event != null ? event.getPayload() : null, stateContext);
		}
	}

	private void processEventQueue() {
		log.debug("Process event queue");
		Message<E> queuedEvent = null;
		while ((queuedEvent = eventQueue.poll()) != null) {
			Message<E> defer = null;
			for (Transition<S,E> transition : transitions) {
				State<S,E> source = transition.getSource();
				State<S,E> target = transition.getTarget();
				Trigger<S, E> trigger = transition.getTrigger();

				if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
					if (trigger != null && trigger.evaluate(queuedEvent.getPayload())) {
						StateContext<S, E> stateContext = new DefaultStateContext<S, E>(queuedEvent.getHeaders(), extendedState, transition);
						boolean transit = transition.transit(stateContext);
						if (transit && transition.getKind() != TransitionKind.INTERNAL) {
							switchToState(target, queuedEvent, transition);
						}
						break;
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

	private void processDeferList() {
		log.debug("Process defer list");
		ListIterator<Message<E>> iterator = deferList.listIterator();
		while (iterator.hasNext()) {
			Message<E> event = iterator.next();
			for (Transition<S,E> transition : transitions) {
				State<S,E> source = transition.getSource();
				State<S,E> target = transition.getTarget();
				Trigger<S, E> trigger = transition.getTrigger();
				if (source.equals(currentState)) {
					if (trigger != null && trigger.evaluate(event.getPayload())) {
						StateContext<S, E> stateContext = new DefaultStateContext<S, E>(event.getHeaders(), extendedState, transition);
						boolean transit = transition.transit(stateContext);
						if (transit && transition.getKind() != TransitionKind.INTERNAL) {
							switchToState(target, event, transition);
						}
						iterator.remove();
					}
				}
			}
		}
	}

	private void scheduleEventQueueProcessing() {
		if (task == null) {
			task = new Runnable() {
				@Override
				public void run() {
					processEventQueue();
					processDeferList();
					task = null;
				}
			};
			getTaskExecutor().execute(task);
		}
	}

	private void callHandlers(State<S,E> sourceState, State<S,E> targetState, Message<E> event) {
		if (sourceState != null && targetState != null) {
			MessageHeaders messageHeaders = event != null ? event.getHeaders() : new MessageHeaders(
					new HashMap<String, Object>());
			StateContext<S, E> stateContext = new DefaultStateContext<S, E>(messageHeaders, extendedState, null);
			getStateMachineHandlerResults(getStateMachineHandlers(sourceState, targetState), stateContext);
		}

	}

	private List<Object> getStateMachineHandlerResults(List<StateMachineHandler> stateMachineHandlers, final StateContext<S, E> stateContext) {
		StateMachineRuntime runtime = new StateMachineRuntime() {
			@Override
			public StateContext<S, E> getStateContext() {
				return stateContext;
			}
		};
		List<Object> results = new ArrayList<Object>();
		for (StateMachineHandler handler : stateMachineHandlers) {
			results.add(handler.handle(runtime));
		}
		return results;
	}

	private List<StateMachineHandler> getStateMachineHandlers(State<S, E> sourceState, State<S, E> targetState) {
		BeanFactory beanFactory = getBeanFactory();

		// TODO think how to handle null bf
		if (beanFactory == null) {
			return Collections.emptyList();
		}
		Assert.state(beanFactory instanceof ListableBeanFactory, "Bean factory must be instance of ListableBeanFactory");
		Map<String, StateMachineOnTransitionHandler> handlers = ((ListableBeanFactory) beanFactory)
				.getBeansOfType(StateMachineOnTransitionHandler.class);
		List<StateMachineHandler> handlersList = new ArrayList<StateMachineHandler>();

		for (Entry<String, StateMachineOnTransitionHandler> entry : handlers.entrySet()) {
			OnTransition annotation = entry.getValue().getAnnotation();
			String source = annotation.source();
			String target = annotation.target();
			// TODO: need major fixes
			String s = sourceState.getIds().iterator().next().toString();
			String t = targetState.getIds().iterator().next().toString();
			if (s.equals(source) && t.equals(target)) {
				handlersList.add(entry.getValue());
			}
		}

		OrderComparator comparator = new OrderComparator();
		Collections.sort(handlersList, comparator);
		return handlersList;
	}

	private void notifyStateChanged(State<S,E> source, State<S,E> target) {
		stateListener.stateChanged(source, target);
		StateMachineEventPublisher eventPublisher = getStateMachineEventPublisher();
		if (eventPublisher != null) {
			eventPublisher.publishStateChanged(this, source, target);
		}
	}

}
