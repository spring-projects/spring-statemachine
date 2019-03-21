/*
 * Copyright 2015-2018 the original author or authors.
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.state.JoinPseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.AbstractTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionConflictPolicy;
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

	private final AtomicReference<Runnable> taskRef = new AtomicReference<Runnable>();

	private StateMachineExecutorTransit<S, E> stateMachineExecutorTransit;

	private final StateMachineInterceptorList<S, E> interceptors =
			new StateMachineInterceptorList<S, E>();

	private volatile Message<E> forwardedInitialEvent;

	private volatile Message<E> queuedMessage = null;

	private final ReentrantLock lock = new ReentrantLock();

	private final TransitionComparator<S, E> transitionComparator;

	private final TransitionConflictPolicy transitionConflictPolicy;

	/**
	 * Instantiates a new default state machine executor.
	 *
	 * @param stateMachine the state machine
	 * @param relayStateMachine the relay state machine
	 * @param transitions the transitions
	 * @param triggerToTransitionMap the trigger to transition map
	 * @param triggerlessTransitions the triggerless transitions
	 * @param initialTransition the initial transition
	 * @param initialEvent the initial event
	 * @param transitionConflictPolicy the transition conflict policy
	 */
	public DefaultStateMachineExecutor(StateMachine<S, E> stateMachine, StateMachine<S, E> relayStateMachine,
			Collection<Transition<S, E>> transitions, Map<Trigger<S, E>, Transition<S, E>> triggerToTransitionMap,
			List<Transition<S, E>> triggerlessTransitions, Transition<S, E> initialTransition, Message<E> initialEvent,
			TransitionConflictPolicy transitionConflictPolicy) {
		this.stateMachine = stateMachine;
		this.relayStateMachine = relayStateMachine;
		this.triggerToTransitionMap = triggerToTransitionMap;
		this.triggerlessTransitions = triggerlessTransitions;
		this.transitions = transitions;
		this.initialTransition = initialTransition;
		this.initialEvent = initialEvent;
		this.transitionComparator = new TransitionComparator<S, E>(transitionConflictPolicy);
		this.transitionConflictPolicy = transitionConflictPolicy;
		// anonymous transitions are fixed, sort those now
		Collections.sort(this.triggerlessTransitions, transitionComparator);
		registerTriggerListener();
	}

	@Override
	public void queueEvent(Message<E> message) {
		eventQueue.add(message);
	}

	@Override
	public void queueTrigger(Trigger<S, E> trigger, Message<E> message) {
		if (log.isDebugEnabled()) {
			log.debug("Queue trigger " + trigger);
		}
		triggerQueue.add(new TriggerQueueItem(trigger, message));
	}

	@Override
	public void queueDeferredEvent(Message<E> message) {
		if (log.isDebugEnabled()) {
			log.debug("Deferring message " + message);
		}
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
		startTriggers();
		execute();
	}

	@Override
	protected void doStop() {
		stopTriggers();
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
	public void setForwardedInitialEvent(Message<E> message) {
		forwardedInitialEvent = message;
	}

	@Override
	public void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor) {
		interceptors.add(interceptor);
	}

	@Override
	public Lock getLock() {
		return lock;
	}

	private final Set<Transition<S, E>> joinSyncTransitions = new HashSet<>();
	private final Set<State<S, E>> joinSyncStates = new HashSet<>();

	private boolean handleTriggerTrans(List<Transition<S, E>> trans, Message<E> queuedMessage) {
		return handleTriggerTrans(trans, queuedMessage, null);
	}

	private boolean handleTriggerTrans(List<Transition<S, E>> trans, Message<E> queuedMessage, State<S, E> completion) {
		boolean transit = false;
		for (Transition<S, E> t : trans) {
			if (t == null) {
				continue;
			}
			State<S,E> source = t.getSource();
			if (source == null) {
				continue;
			}
			State<S,E> currentState = stateMachine.getState();
			if (currentState == null) {
				continue;
			}
			if (!StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
				continue;
			}

			if (transitionConflictPolicy != TransitionConflictPolicy.PARENT && completion != null && !source.getId().equals(completion.getId())) {
				if (source.isOrthogonal()) {
					continue;
				}
				else if (!StateMachineUtils.isSubstate(source, completion)) {
					continue;

				}
			}

			// special handling of join
			if (StateMachineUtils.isPseudoState(t.getTarget(), PseudoStateKind.JOIN)) {
				if (joinSyncStates.isEmpty()) {
					List<State<S, E>> joins = ((JoinPseudoState<S, E>)t.getTarget().getPseudoState()).getJoins();
					joinSyncStates.addAll(joins);
				}
				joinSyncTransitions.add(t);
				boolean removed = joinSyncStates.remove(t.getSource());
				boolean joincomplete = removed & joinSyncStates.isEmpty();
				if (joincomplete) {
					for (Transition<S, E> tt : joinSyncTransitions) {
						StateContext<S, E> stateContext = buildStateContext(queuedMessage, tt, relayStateMachine);
						tt.transit(stateContext);
						stateMachineExecutorTransit.transit(tt, stateContext, queuedMessage);
					}
					joinSyncTransitions.clear();
					break;
				} else {
					continue;
				}
			}

			StateContext<S, E> stateContext = buildStateContext(queuedMessage, t, relayStateMachine);
			try {
				stateContext = interceptors.preTransition(stateContext);
			} catch (Exception e) {
				// currently expect that if exception is
				// thrown, this transition will not match.
				// i.e. security may throw AccessDeniedException
				log.info("Interceptors threw exception", e);
				stateContext = null;
			}
			if (stateContext == null) {
				break;
			}

			try {
				transit = t.transit(stateContext);
			} catch (Exception e) {
				log.warn("Aborting as transition " + t, e);
			}
			if (transit) {
				// if executor transit is raising exception, stop here
				try {
					stateMachineExecutorTransit.transit(t, stateContext, queuedMessage);
				} catch (Exception e) {
					interceptors.postTransition(stateContext);
					return false;
				}
				interceptors.postTransition(stateContext);
				break;
			}
		}
		return transit;
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

		// TODO: it'd be nice not to create runnable if
		//       current ref is null, we use atomic reference
		//       to play safe with concurrency.
		Runnable task = new Runnable() {
			@Override
			public void run() {
				// lock operation, see AbstractStateMachine
				// how this is used.
				lock.lock();
				try {
					boolean eventProcessed = false;
					while (processEventQueue()) {
						eventProcessed = true;
						processTriggerQueue();
						while (processDeferList()) {
							processTriggerQueue();
						}
					}
					if (!eventProcessed) {
						processTriggerQueue();
						while (processDeferList()) {
							processTriggerQueue();
						}
					}

					if (requestTask.getAndSet(false)) {
						scheduleEventQueueProcessing();
					}
					taskRef.set(null);
				} finally {
					lock.unlock();
				}

				// do second attempt which should reduse risk
				// of threading causing failed run to completion
				if (requestTask.getAndSet(false)) {
					scheduleEventQueueProcessing();
				}
			}
		};

		if (taskRef.compareAndSet(null, task)) {
			executor.execute(task);
		} else {
			requestTask.set(true);
		}
	}

	private boolean processEventQueue() {
		if (log.isDebugEnabled()) {
			log.debug("Process event queue, size=" + eventQueue.size());
		}
		Message<E> queuedEvent = eventQueue.poll();
		State<S,E> currentState = stateMachine.getState();
		if (queuedEvent != null) {
			if ((currentState != null && currentState.shouldDefer(queuedEvent))) {
				log.info("Current state " + currentState + " deferred event " + queuedEvent);
				queueDeferredEvent(queuedEvent);
				return true;
			}
			for (Transition<S,E> transition : transitions) {
				State<S,E> source = transition.getSource();
				Trigger<S, E> trigger = transition.getTrigger();

				if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
					if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(queuedEvent.getPayload()))) {
						queueTrigger(trigger, queuedEvent);
						return true;
					}
				}
			}
			return true;
		}
		return false;
	}

	private void processTriggerQueue() {
		while(processTriggerQueueUntilConsumed()) {
			if (log.isTraceEnabled()) {
				log.trace("calling next processTriggerQueueUntilConsumed");
			}
		}
	}

	private boolean processTriggerQueueUntilConsumed() {
		if (!isRunning()) {
			return false;
		}
		if (!initialHandled.getAndSet(true)) {
			ArrayList<Transition<S, E>> trans = new ArrayList<Transition<S, E>>();
			trans.add(initialTransition);
			// TODO: should we merge if initial event is actually used?
			if (initialEvent != null) {
				handleInitialTrans(initialTransition, initialEvent);
			} else {
				handleInitialTrans(initialTransition, forwardedInitialEvent);
			}
			return false;
		}
		if (log.isDebugEnabled()) {
			log.debug("Process trigger queue, size=" + triggerQueue.size() + " " + this);
		}
		TriggerQueueItem queueItem = triggerQueue.poll();
		// keep message here so that we can
		// pass it to triggerless transitions
		State<S,E> currentState = stateMachine.getState();
		if (queueItem != null && currentState != null) {
			if (log.isDebugEnabled()) {
				log.debug("Process trigger item " + queueItem + " " + this);
			}
			// queued message is kept on a class level order to let
			// triggerless transition to receive this message if it doesn't
			// kick in in this poll loop.
			queuedMessage = queueItem.message;
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
						if (event.equals(ee)) {
							if (tra.getSource().getId().equals(id) && !trans.contains(tra)) {
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

			// go through candidates and transit max one, sort before handling
			Collections.sort(trans, transitionComparator);
			handleTriggerTrans(trans, queuedMessage);
		}

		List<Transition<S, E>> transWithGuards = new ArrayList<>();
		for (Transition<S, E> t : triggerlessTransitions) {
			if (((AbstractTransition<S, E>)t).getGuard() != null) {
				transWithGuards.add(t);
			}
		}

		if (stateMachine.getState() != null) {
			// loop triggerless transitions here so that
			// all "chained" transitions will get queue message
			boolean transit = false;
			do {
				transit = handleTriggerTrans(transWithGuards, queuedMessage);
			} while (transit);
		}
		return !triggerQueue.isEmpty();
	}

	@Override
	public void executeTriggerlessTransitions(StateContext<S, E> context, State<S, E> state) {
		if (stateMachine.getState() != null) {
			handleTriggerTrans(triggerlessTransitions, context.getMessage(), state);
		}
	}

	private synchronized boolean processDeferList() {
		if (log.isDebugEnabled()) {
			log.debug("Process defer list, size=" + deferList.size());
		}
		ListIterator<Message<E>> iterator = deferList.listIterator();
		State<S,E> currentState = stateMachine.getState();
		while (iterator.hasNext()) {
			Message<E> event = iterator.next();
			if (currentState.shouldDefer(event)) {
				// if current state still defers, just continue with others
				continue;
			}
			for (Transition<S, E> transition : transitions) {
				State<S, E> source = transition.getSource();
				Trigger<S, E> trigger = transition.getTrigger();
				if (source.equals(currentState)) {
					if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(event.getPayload()))) {
						triggerQueue.add(new TriggerQueueItem(trigger, event));
						iterator.remove();
						// bail out when first deferred message is causing a trigger to fire
						return true;
					}
				}
			}
		}
		return false;
	}

	private StateContext<S, E> buildStateContext(Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		// TODO: maybe a direct use of MessageHeaders is wring, combine
		//       payload and headers as a message?

		// add sm id to headers so that user of a StateContext can
		// see who initiated this transition
		MessageHeaders messageHeaders = message != null ? message.getHeaders() : new MessageHeaders(
				new HashMap<String, Object>());
		Map<String, Object> map = new HashMap<String, Object>(messageHeaders);
		if (!map.containsKey(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER)) {
			// don't set sm id if it's already present because
			// we want to keep the originating sm id
			map.put(StateMachineSystemConstants.STATEMACHINE_IDENTIFIER, stateMachine.getUuid());
		}
		return new DefaultStateContext<S, E>(Stage.TRANSITION, message, new MessageHeaders(map), stateMachine.getExtendedState(), transition, stateMachine, null, null, null);
	}

	private void registerTriggerListener() {
		for (final Trigger<S, E> trigger : triggerToTransitionMap.keySet()) {
			if (trigger instanceof TimerTrigger) {
				((TimerTrigger<?, ?>) trigger).addTriggerListener(new TriggerListener() {
					@Override
					public void triggered() {
						if (log.isDebugEnabled()) {
							log.debug("TimedTrigger triggered " + trigger);
						}
						triggerQueue.add(new TriggerQueueItem(trigger, null));
						// isRunning() is also called in scheduleEventQueueProcessing()
						// but we may get into lifecycle deadlock if we schedule here
						// from a different thread. may happen if timer fires immediately
						// and we're not exactly gone through start sequence.
						// however this trigger is most likely getting processed as
						// it was added to trigger queue.
						if (isRunning()) {
							scheduleEventQueueProcessing();
						}
					}
				});
			}
		}
	}

	private void startTriggers() {
		for (final Trigger<S, E> trigger : triggerToTransitionMap.keySet()) {
			if (trigger instanceof Lifecycle) {
				((Lifecycle) trigger).start();
			}
		}
	}

	private void stopTriggers() {
		for (final Trigger<S, E> trigger : triggerToTransitionMap.keySet()) {
			if (trigger instanceof Lifecycle) {
				((Lifecycle) trigger).stop();
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
