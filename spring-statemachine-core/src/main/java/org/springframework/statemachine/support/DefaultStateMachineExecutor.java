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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.DefaultTriggerContext;
import org.springframework.statemachine.trigger.TimerTrigger;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.statemachine.trigger.TriggerListener;

/**
 * Default implementation of a {@link StateMachineExecutor}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateMachineExecutor<S, E> extends LifecycleObjectSupport implements StateMachineExecutor<S, E> {

	private static final Log log = LogFactory.getLog(DefaultStateMachineExecutor.class);

	private final StateMachine<S, E> stateMachine;

	private final StateMachine<S, E> relayStateMachine;

	private final ExtendedState extendedState;

	private final Queue<Message<E>> eventQueue = new ConcurrentLinkedQueue<Message<E>>();

	private final LinkedList<Message<E>> deferList = new LinkedList<Message<E>>();

	private final Queue<TriggerQueueItem> triggerQueue = new ConcurrentLinkedQueue<TriggerQueueItem>();

	private final Collection<Transition<S,E>> transitions;

	private final AtomicBoolean requestTask = new AtomicBoolean(false);

	private final Map<Trigger<S, E>, Transition<S,E>> triggerToTransitionMap;

	private final List<Transition<S, E>> triggerlessTransitions;

	private final Transition<S, E> initialTransition;

	private final Message<E> initialEvent;

	private final AtomicBoolean initialHandled = new AtomicBoolean(false);

	private volatile Runnable task;

	private StateMachineExecutorTransit<S, E> stateMachineExecutorTransit;

    private final StateMachineInterceptorList<S, E> interceptors =
            new StateMachineInterceptorList<S, E>();

	/**
	 * Instantiates a new default state machine executor.
	 *
	 * @param stateMachine the state machine
	 * @param relayStateMachine the relay state machine
	 * @param extendedState the extended state
	 * @param transitions the transitions
	 * @param triggerToTransitionMap the trigger to transition map
	 * @param triggerlessTransitions the triggerless transitions
	 */
	public DefaultStateMachineExecutor(StateMachine<S, E> stateMachine, StateMachine<S, E> relayStateMachine,
			ExtendedState extendedState, Collection<Transition<S, E>> transitions, Map<Trigger<S, E>, Transition<S, E>> triggerToTransitionMap,
			List<Transition<S, E>> triggerlessTransitions, Transition<S, E> initialTransition, Message<E> initialEvent) {
		this.stateMachine = stateMachine;
		this.relayStateMachine = relayStateMachine;
		this.extendedState = extendedState;
		this.triggerToTransitionMap = triggerToTransitionMap;
		this.triggerlessTransitions = triggerlessTransitions;
		this.transitions = transitions;
		this.initialTransition = initialTransition;
		this.initialEvent = initialEvent;
		registerTriggerListener();
	}

	@Override
	public void queueEvent(Message<E> message) {
		eventQueue.add(message);
	}

	@Override
	public void queueTrigger(Trigger<S, E> trigger, Message<E> message) {
		triggerQueue.add(new TriggerQueueItem(trigger, message));
	}

	@Override
	public void queueDeferredEvent(Message<E> message) {
		deferList.addLast(message);
	}

	@Override
	public void execute() {
		scheduleEventQueueProcessing();
	}

	@Override
	public void setStateMachineExecutorTransit(StateMachineExecutorTransit<S, E> stateMachineExecutorTransit) {
		this.stateMachineExecutorTransit = stateMachineExecutorTransit;
	}

	@Override
	protected void doStart() {
		super.doStart();
		execute();
	}

	@Override
	protected void doStop() {
		super.doStop();
		initialHandled.set(false);
	}

	@Override
	public void setInitialEnabled(boolean enabled) {
		// TODO: should prob handle case where this is enabled
		//       when executor is running
		initialHandled.set(!enabled);
	}

	@Override
	public void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor) {
		interceptors.add(interceptor);
	}

	private void handleTriggerTrans(List<Transition<S, E>> trans, Message<E> queuedMessage) {
		for (Transition<S, E> t : trans) {
			if (t == null) {
				continue;
			}
			State<S,E> source = t.getSource();
			if (source == null) {
				continue;
			}
			State<S,E> currentState = stateMachine.getState();
			if (!StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				continue;
			}

			StateContext<S, E> stateContext = buildStateContext(queuedMessage, t, relayStateMachine);
			stateContext = interceptors.preTransition(stateContext);
			if (stateContext == null) {
				break;
			}

			boolean transit = t.transit(stateContext);
			if (transit) {
				stateMachineExecutorTransit.transit(t, stateContext, queuedMessage);
				interceptors.postTransition(stateContext);
				break;
			}
		}
	}

	private void handleInitialTrans(Transition<S, E> tran, Message<E> queuedMessage) {
		StateContext<S, E> stateContext = buildStateContext(queuedMessage, tran, relayStateMachine);
		tran.transit(stateContext);
		stateMachineExecutorTransit.transit(tran, stateContext, queuedMessage);
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

	private void processEventQueue() {
		log.debug("Process event queue");
		Message<E> queuedEvent = null;
		State<S,E> currentState = stateMachine.getState();
		while ((queuedEvent = eventQueue.poll()) != null) {
			Message<E> defer = null;
			for (Transition<S, E> transition : transitions) {
				State<S, E> source = transition.getSource();
				Trigger<S, E> trigger = transition.getTrigger();

				if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
					if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(queuedEvent.getPayload()))) {
						triggerQueue.add(new TriggerQueueItem(trigger, queuedEvent));
					} else if (source.getDeferredEvents() != null
							&& source.getDeferredEvents().contains(queuedEvent.getPayload())) {
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

	private void processTriggerQueue() {
		if (!isRunning()) {
			return;
		}
		if (!initialHandled.getAndSet(true)) {
			ArrayList<Transition<S, E>> trans = new ArrayList<Transition<S, E>>();
			trans.add(initialTransition);
			handleInitialTrans(initialTransition, initialEvent);
			return;
		}
		log.debug("Process trigger queue");
		TriggerQueueItem queueItem = null;
		while ((queueItem = triggerQueue.poll()) != null) {

			State<S,E> currentState = stateMachine.getState();

			if (currentState == null) {
				continue;
			}

			Message<E> queuedMessage = queueItem.message;
			E event = queuedMessage != null ? queuedMessage.getPayload() : null;

			// need all transitions trigger could match, event trigger may match
			// multiple
			// need to go up from substates and ask if trigger transit, if not
			// check super
			ArrayList<Transition<S, E>> trans = new ArrayList<Transition<S, E>>();

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
		if (stateMachine.getState() != null) {
			// handle triggerless transitions
			handleTriggerTrans(triggerlessTransitions, null);
		}
	}

	private boolean processDeferList() {
		log.debug("Process defer list");
		boolean triggered = false;
		ListIterator<Message<E>> iterator = deferList.listIterator();
		State<S,E> currentState = stateMachine.getState();
		while (iterator.hasNext()) {
			Message<E> event = iterator.next();
			for (Transition<S, E> transition : transitions) {
				State<S, E> source = transition.getSource();
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

	private StateContext<S, E> buildStateContext(Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		E event = message != null ? message.getPayload() : null;

		// TODO: maybe a direct use of MessageHeaders is wring, combine
		//       payload and headers as a message?

		// add sm id to headers so that user of a StateContext can
		// see who initiated this transition
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		Map<String, Object> map = new HashMap<String, Object>(messageHeaders);
		map.put(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER, stateMachine.getId());
		return new DefaultStateContext<S, E>(event, new MessageHeaders(map), extendedState, transition, stateMachine);
	}

	private void registerTriggerListener() {
		for (final Trigger<S, E> trigger : triggerToTransitionMap.keySet()) {
			if (trigger instanceof TimerTrigger) {
				((TimerTrigger<?, ?>) trigger).addTriggerListener(new TriggerListener() {
					@Override
					public void triggered() {
						log.debug("TimedTrigger triggered " + trigger);
						triggerQueue.add(new TriggerQueueItem(trigger, null));
						scheduleEventQueueProcessing();
					}
				});
			}
			if (trigger instanceof Lifecycle) {
				((Lifecycle) trigger).start();
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
