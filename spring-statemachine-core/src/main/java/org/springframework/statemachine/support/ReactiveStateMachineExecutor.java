/*
 * Copyright 2019 the original author or authors.
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;
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

import reactor.core.Disposable;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

public class ReactiveStateMachineExecutor<S, E> extends LifecycleObjectSupport implements StateMachineExecutor<S, E> {

	private static final Log log = LogFactory.getLog(ReactiveStateMachineExecutor.class);
	private final StateMachine<S, E> stateMachine;
	private final StateMachine<S, E> relayStateMachine;
	private final Map<Trigger<S, E>, Transition<S,E>> triggerToTransitionMap;
	private final List<Transition<S, E>> triggerlessTransitions;
	private final Collection<Transition<S,E>> transitions;
	private final Transition<S, E> initialTransition;
	private final Message<E> initialEvent;
	private final TransitionComparator<S, E> transitionComparator;
	private final TransitionConflictPolicy transitionConflictPolicy;
	// TODO deferList is never cleared
	private final Queue<Message<E>> deferList = new ConcurrentLinkedQueue<Message<E>>();
	private final AtomicBoolean initialHandled = new AtomicBoolean(false);
	private final StateMachineInterceptorList<S, E> interceptors = new StateMachineInterceptorList<S, E>();

	private volatile Message<E> forwardedInitialEvent;
	private volatile Message<E> queuedMessage = null;
	private StateMachineExecutorTransit<S, E> stateMachineExecutorTransit;

	private EmitterProcessor<TriggerQueueItem> triggerProcessor = EmitterProcessor.create(false);
	private FluxSink<TriggerQueueItem> triggerSink;
	private Flux<Void> triggerFlux;
	private Disposable triggerDisposable;

	public ReactiveStateMachineExecutor(StateMachine<S, E> stateMachine, StateMachine<S, E> relayStateMachine,
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
		this.triggerlessTransitions.sort(transitionComparator);
		registerTriggerListener();
	}

	@Override
	protected void onInit() throws Exception {
		triggerSink = triggerProcessor.sink();
		triggerFlux = Flux.from(triggerProcessor)
			.flatMap(trigger -> handleTrigger(trigger));
	}

	@Override
	protected Mono<Void> doPreStartReactively() {
		return Mono.defer(() -> {
			Mono<Void> mono = Mono.empty();
			startTriggers();

			if (triggerDisposable == null) {
				triggerDisposable = triggerFlux.subscribe();
			}

			if (!initialHandled.getAndSet(true)) {
				ArrayList<Transition<S, E>> trans = new ArrayList<Transition<S, E>>();
				trans.add(initialTransition);
				// TODO: should we merge if initial event is actually used?
				if (initialEvent != null) {
					mono = mono.then(handleInitialTrans(initialTransition, initialEvent));
				} else {
					mono = mono.then(handleInitialTrans(initialTransition, forwardedInitialEvent));
				}
			}
			mono = mono.then(handleTriggerlessTransitions(null, null));
			return mono;
		});
	}

	@Override
	protected Mono<Void> doPreStopReactively() {
		return Mono.fromRunnable(() -> {
			stopTriggers();
			if (triggerDisposable != null) {
				triggerDisposable.dispose();
				triggerDisposable = null;
			}
			initialHandled.set(false);
		})
		;
	}

	@Override
	public void queueTrigger(Trigger<S, E> trigger, Message<E> message) {
		if (log.isDebugEnabled()) {
			log.debug("Queue trigger " + trigger);
		}
		triggerSink.next(new TriggerQueueItem(trigger, message));
	}

	@Override
	public void queueDeferredEvent(Message<E> message) {
		// TODO Auto-generated method stub
		if (log.isDebugEnabled()) {
			log.debug("Deferring message " + message);
		}
		deferList.add(message);
	}

	@Override
	public Mono<Void> executeTriggerlessTransitions(StateContext<S, E> context, State<S, E> state) {
		if (stateMachine.getState() != null) {
			return handleTriggerlessTransitions(context, state);
		}
		return Mono.empty();
	}

	@Override
	public void setInitialEnabled(boolean enabled) {
		initialHandled.set(!enabled);
	}

	@Override
	public void setForwardedInitialEvent(Message<E> message) {
		forwardedInitialEvent = message;
	}

	@Override
	public void setStateMachineExecutorTransit(StateMachineExecutorTransit<S, E> stateMachineExecutorTransit) {
		this.stateMachineExecutorTransit = stateMachineExecutorTransit;
	}

	@Override
	public void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor) {
		interceptors.add(interceptor);
	}

	@Override
	public Mono<Void> queueEvent(Mono<Message<E>> message) {
		Flux<Message<E>> messages = Flux.merge(message, Flux.fromIterable(deferList));
		return messages
			.flatMap(m -> handleEvent(m))
			.doOnNext(i -> {
				triggerSink.next(i);
			})
			.then();
	}

	private Mono<TriggerQueueItem> handleEvent(Message<E> queuedEvent) {
		if (log.isDebugEnabled()) {
			log.debug("Handling message " + queuedEvent);
		}
		return Mono.defer(() -> {
			State<S,E> currentState = stateMachine.getState();
			if ((currentState != null && currentState.shouldDefer(queuedEvent))) {
				log.info("Current state " + currentState + " deferred event " + queuedEvent);
				return Mono.just(new TriggerQueueItem(null, queuedEvent));
			}
			for (Transition<S,E> transition : transitions) {
				State<S,E> source = transition.getSource();
				Trigger<S, E> trigger = transition.getTrigger();

				if (StateMachineUtils.containsAtleastOne(source.getIds(), currentState.getIds())) {
					if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(queuedEvent.getPayload()))) {
						return Mono.just(new TriggerQueueItem(trigger, queuedEvent));
					}
				}
			}
			return Mono.empty();
		});
	}

	private Mono<Void> handleTrigger(TriggerQueueItem queueItem) {
		return Mono.defer(() -> {
			Mono<Void> ret = null;
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
				trans.sort(transitionComparator);
				ret = handleTriggerTrans(trans, queuedMessage).then();
			}

			List<Transition<S, E>> transWithGuards = new ArrayList<>();
			for (Transition<S, E> t : triggerlessTransitions) {
				if (((AbstractTransition<S, E>)t).getGuard() != null) {
					transWithGuards.add(t);
				}
			}

			if (ret == null) {
				ret = Mono.empty();
			}
			return ret;
		});
	}


	private Mono<Void> handleInitialTrans(Transition<S, E> tran, Message<E> queuedMessage) {
		StateContext<S, E> stateContext = buildStateContext(queuedMessage, tran, relayStateMachine);
		tran.transit(stateContext);
		return stateMachineExecutorTransit.transit(tran, stateContext, queuedMessage);
	}

	private Mono<Void> handleTriggerlessTransitions(StateContext<S, E> context, State<S, E> state) {
		Flux<Mono<Boolean>> monoFlux = Flux.generate((sink) -> {
			sink.next(handleTriggerTrans(triggerlessTransitions, context != null ? context.getMessage() : null, state));
		});
		Flux<Boolean> flux = Flux.concat(monoFlux);
		return flux.takeUntil(b -> !b).then();
	}

	private final Set<Transition<S, E>> joinSyncTransitions = new HashSet<>();
	private final Set<State<S, E>> joinSyncStates = new HashSet<>();

	private Mono<Boolean> handleTriggerTrans(List<Transition<S, E>> trans, Message<E> queuedMessage) {
		return handleTriggerTrans(trans, queuedMessage, null);
	}

	private Mono<Boolean> handleTriggerTrans(List<Transition<S, E>> trans, Message<E> queuedMessage, State<S, E> completion) {
		return Mono.defer(() -> {
			Mono<Boolean> mono = Mono.just(false);
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
						List<List<State<S,E>>> joins = ((JoinPseudoState<S, E>)t.getTarget().getPseudoState()).getJoins();
						for (List<State<S,E>> j : joins) {
							joinSyncStates.addAll(j);
						}
					}
					joinSyncTransitions.add(t);
					boolean removed = joinSyncStates.remove(t.getSource());
					boolean joincomplete = removed & joinSyncStates.isEmpty();
					if (joincomplete) {
						for (Transition<S, E> tt : joinSyncTransitions) {
							StateContext<S, E> stateContext = buildStateContext(queuedMessage, tt, relayStateMachine);
							tt.transit(stateContext);
							stateMachineExecutorTransit.transit(tt, stateContext, queuedMessage).block();
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
						mono = stateMachineExecutorTransit.transit(t, stateContext, queuedMessage).then(Mono.just(true));
					} catch (Exception e) {
						interceptors.postTransition(stateContext);
					}
					interceptors.postTransition(stateContext);
					break;
				}
			}
			return mono;
		});
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
						queueTrigger(trigger, null);
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
