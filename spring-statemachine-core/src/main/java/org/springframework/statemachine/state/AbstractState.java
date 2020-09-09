/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.statemachine.state;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Subscription;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.action.ActionListener;
import org.springframework.statemachine.action.CompositeActionListener;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineUtils;
import org.springframework.statemachine.trigger.Trigger;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Base implementation of a {@link State}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractState<S, E> extends LifecycleObjectSupport implements State<S, E> {

	private static final Log log = LogFactory.getLog(AbstractState.class);
	private final S id;
	private final PseudoState<S, E> pseudoState;
	private final Collection<E> deferred;
	private final Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions;
	private final Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions;
	private final Collection<Function<StateContext<S, E>, Mono<Void>>> stateActions;
	private final List<ScheduledAction> scheduledActions = new ArrayList<>();
	private final Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
	private final StateMachine<S, E> submachine;
	private List<Trigger<S, E>> triggers = new ArrayList<Trigger<S, E>>();
	private final CompositeStateListener<S, E> stateListener = new CompositeStateListener<S, E>();
	private CompositeActionListener<S, E> actionListener;
	private final List<StateMachineListener<S, E>> completionListeners = new CopyOnWriteArrayList<>();
	private StateDoActionPolicy stateDoActionPolicy;
	private Long stateDoActionPolicyTimeout;
	private final Queue<Disposable> disposables = new ConcurrentLinkedDeque<>();

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(S id, PseudoState<S, E> pseudoState) {
		this(id, null, null, null, pseudoState);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 */
	public AbstractState(S id, Collection<E> deferred) {
		this(id, deferred, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public AbstractState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions) {
		this(id, deferred, entryActions, exitActions, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 */
	public AbstractState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions, PseudoState<S, E> pseudoState) {
		this(id, deferred, entryActions, exitActions, pseudoState, null, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param submachine the submachine
	 */
	public AbstractState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions, PseudoState<S, E> pseudoState,
			StateMachine<S, E> submachine) {
		this(id, deferred, entryActions, exitActions, pseudoState, null, submachine);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 */
	public AbstractState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions, PseudoState<S, E> pseudoState,
			Collection<Region<S, E>> regions) {
		this(id, deferred, entryActions, exitActions, pseudoState, regions, null);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 * @param submachine the submachine
	 */
	public AbstractState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions, PseudoState<S, E> pseudoState,
			Collection<Region<S, E>> regions, StateMachine<S, E> submachine) {
		this(id, deferred, entryActions, exitActions, null, pseudoState, regions, submachine);
	}

	/**
	 * Instantiates a new abstract state.
	 *
	 * @param id the state identifier
	 * @param deferred the deferred
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 * @param stateActions the state actions
	 * @param pseudoState the pseudo state
	 * @param regions the regions
	 * @param submachine the submachine
	 */
	public AbstractState(S id, Collection<E> deferred,
			Collection<Function<StateContext<S, E>, Mono<Void>>> entryActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> exitActions,
			Collection<Function<StateContext<S, E>, Mono<Void>>> stateActions, PseudoState<S, E> pseudoState,
			Collection<Region<S, E>> regions, StateMachine<S, E> submachine) {
		this.id = id;
		this.deferred = deferred != null ? deferred : Collections.<E>emptySet();
		this.entryActions = entryActions != null ? entryActions : Collections.emptySet();
		this.exitActions = exitActions != null ? exitActions : Collections.emptySet();
		this.stateActions = stateActions != null ? stateActions : Collections.emptySet();
		this.pseudoState = pseudoState;

		// use of private ctor should prevent user to
		// add regions and a submachine which is not allowed.
		if (regions != null) {
			this.regions.addAll(regions);
		}
		this.submachine = submachine;
	}

	@Override
	public Flux<StateMachineEventResult<S, E>> sendEvent(Message<E> event) {
		return Flux.empty();
	}

	@Override
	public boolean shouldDefer(Message<E> event) {
		return deferred.contains(event.getPayload());
	}

	@Override
	public Mono<Void> exit(StateContext<S, E> context) {
		return Mono.<Void>defer(() -> {
			if (submachine != null) {
				for (StateMachineListener<S, E> l : completionListeners) {
					submachine.removeStateListener(l);
				}
			} else if (!regions.isEmpty()) {
				for (Region<S, E> region : regions) {
					for (StateMachineListener<S, E> l : completionListeners) {
						region.removeStateListener(l);
					}
				}
			}
			return Mono.empty();
		})
		.then(Mono.<Void>fromRunnable(() -> {
			completionListeners.clear();
		}))
		.then(cancelStateActions())
		.then(Mono.<Void>fromRunnable(() -> {
			stateListener.onExit(context);
			disarmTriggers();
		}))
		.doFinally(signal -> disposeDisposables());
	}

	@Override
	public Mono<Void> entry(StateContext<S, E> context) {
		return Mono.defer(() -> {
			if (submachine != null) {
				Disposable disposable = Mono.just(submachine)
					.flatMap(submachine -> completionStateListenerSink(submachine))
					// TODO: REACTOR this is causing cancel which breaks some things
					// .then(handleStateDoOnComplete(context))
					.then(Mono.fromRunnable(() -> notifyStateOnComplete(context)))
					.subscribe();
				disposables.add(disposable);
			} else if (!regions.isEmpty()) {
				// TODO: REACTOR we should handle disposable
				Flux.fromIterable(regions)
					.flatMap(region -> completionStateListenerSink(region))
					.then(handleStateDoOnComplete(context))
					.then(Mono.fromRunnable(() -> notifyStateOnComplete(context)))
					.subscribe();
			}
			stateListener.onEntry(context);
			armTriggers();
			return Mono.empty();
		})
		.then(scheduleStateActions(context));
	}

	@Override
	public S getId() {
		return id;
	}

	@Override
	public abstract Collection<S> getIds();

	@Override
	public abstract Collection<State<S, E>> getStates();

	@Override
	public PseudoState<S, E> getPseudoState() {
		return pseudoState;
	}

	@Override
	public Collection<E> getDeferredEvents() {
		return deferred;
	}

	@Override
	public Collection<Function<StateContext<S, E>, Mono<Void>>> getEntryActions() {
		return entryActions;
	}

	@Override
	public Collection<Function<StateContext<S, E>, Mono<Void>>> getStateActions() {
		return stateActions;
	}

	@Override
	public Collection<Function<StateContext<S, E>, Mono<Void>>> getExitActions() {
		return exitActions;
	}

	@Override
	public boolean isComposite() {
		return !regions.isEmpty();
	}

	@Override
	public boolean isOrthogonal() {
		return regions.size() > 1;
	}

	@Override
	public boolean isSimple() {
		return !isSubmachineState() && !isComposite();
	}

	@Override
	public boolean isSubmachineState() {
		return submachine != null;
	}

	@Override
	public void addStateListener(StateListener<S, E> listener) {
		stateListener.register(listener);
	}

	@Override
	public void removeStateListener(StateListener<S, E> listener) {
		stateListener.unregister(listener);
	}

	@Override
	public void addActionListener(ActionListener<S, E> listener) {
		synchronized (this) {
			if (this.actionListener == null) {
				this.actionListener = new CompositeActionListener<>();
			}
			this.actionListener.register(listener);
		}
	}

	@Override
	public void removeActionListener(ActionListener<S, E> listener) {
		synchronized (this) {
			if (this.actionListener != null) {
				this.actionListener.unregister(listener);
			}
		}
	}

	@Override
	protected Mono<Void> doPreStartReactively() {
		return Mono.fromRunnable(() -> armTriggers());
	}

	@Override
	protected Mono<Void> doPreStopReactively() {
		return Mono.fromRunnable(() -> disarmTriggers());
	}

	/**
	 * Gets the submachine.
	 *
	 * @return the submachine or null if not set
	 */
	public StateMachine<S, E> getSubmachine() {
		return submachine;
	}

	/**
	 * Gets the regions.
	 *
	 * @return the regions or empty collection if no regions
	 */
	public Collection<Region<S, E>> getRegions() {
		return regions;
	}

	/**
	 * Sets the triggers.
	 *
	 * @param triggers the triggers
	 */
	public void setTriggers(List<Trigger<S, E>> triggers) {
		if (triggers != null) {
			this.triggers = triggers;
		} else {
			this.triggers.clear();
		}
	}

	/**
	 * Gets the triggers.
	 *
	 * @return the triggers
	 */
	public List<Trigger<S, E>> getTriggers() {
		return triggers;
	}

	public void setStateDoActionPolicy(StateDoActionPolicy stateDoActionPolicy) {
		this.stateDoActionPolicy = stateDoActionPolicy;
	}

	public void setStateDoActionPolicyTimeout(Long stateDoActionPolicyTimeout) {
		this.stateDoActionPolicyTimeout = stateDoActionPolicyTimeout;
	}

	/**
	 * Arm triggers.
	 */
	protected void armTriggers() {
		for (Trigger<S, E> trigger : triggers) {
			trigger.arm();
		}
	}

	/**
	 * Disarm triggers.
	 */
	protected void disarmTriggers() {
		for (Trigger<S, E> trigger : triggers) {
			trigger.disarm();
		}
	}

	private Mono<Void> completionStateListenerSink(Region<S, E> region) {
		return Mono.create(sink -> {
			final StateMachineListener<S, E> listener = new StateMachineListenerAdapter<S, E>() {

				@Override
				public void stateContext(StateContext<S, E> stateContext) {
					if (stateContext.getStage() == Stage.STATEMACHINE_STOP) {
						if (stateContext.getStateMachine() == region && region.isComplete()) {
							completionListeners.remove(this);
							region.removeStateListener(this);
							sink.success();
						}
					}
				}
			};
			completionListeners.add(listener);
			region.addStateListener(listener);
			});
	}

	private void disposeDisposables() {
		Disposable disposable;
		while ((disposable = disposables.poll()) != null) {
			disposable.dispose();
		}
	}

	private Mono<Void> scheduleStateActions(StateContext<S, E> context) {
		return Mono.defer(() -> {
			final AtomicInteger completionCount = new AtomicInteger(stateActions.size());
			Long timeout = resolveDoActionTimeout(context);
			return Flux.fromIterable(stateActions)
				.doOnNext(stateAction -> {
					executeAction(stateAction, context)
						.onErrorResume(t -> Mono.empty())
						.subscribeOn(Schedulers.parallel())
						.doOnSubscribe(subscription -> {
							if (log.isDebugEnabled()) {
								log.debug("Adding new scheduled action with subscription=" + subscription);
							}
							scheduledActions.add(new ScheduledAction(subscription, timeout, System.currentTimeMillis()));
						})
						.then(handleCompleteOrEmpty1(context, completionCount))
						.subscribe();
				})
				.then(handleCompleteOrEmpty2(context, completionCount));
		});
	}

	private Mono<Void> handleCompleteOrEmpty1(StateContext<S, E> context, AtomicInteger completionCount) {
		return Mono.defer(() -> {
			log.debug("handleCompleteOrEmpty1 " + completionCount + " " + stateActions);
			if (completionCount.decrementAndGet() <= 0 && stateActions.size() > 0) {
				return handleStateDoOnComplete(context)
					.then(Mono.fromRunnable(() -> notifyStateOnComplete(context)));
			} else {
				return Mono.empty();
			}
		});
	}

	private Mono<Void> handleCompleteOrEmpty2(StateContext<S, E> context, AtomicInteger completionCount) {
		return Mono.defer(() -> {
			if (isSimple() && stateActions.size() == 0) {
				return handleStateDoOnComplete(context)
					.then(Mono.fromRunnable(() -> notifyStateOnComplete(context)));
			} else {
				return Mono.empty();
			}
		});
	}

	private Mono<Void> cancelStateActions() {
		return Flux.fromIterable(scheduledActions)
			// state action tells us how long it needs for timeout, delay
			.flatMap(stateAction -> {
				// check delay and prevent unnecessary thread switch with Mono.delay()
				if (stateAction.getNeededDelayNow().toMillis() > 0) {
					return Mono.delay(stateAction.getNeededDelayNow()).thenReturn(stateAction);
				} else {
					return Mono.just(stateAction);
				}
			})
			// then dispose which i.e. should interrupt blocking threads or cancel reactive code
			.doOnNext(stateAction -> {
				if (stateAction.subscription != null) {
					log.debug("About to dispose subscription " + stateAction.subscription);
					stateAction.subscription.cancel();
				}
			})
			// we're done, clear state scheduled state actions
			.thenEmpty(Mono.fromRunnable(() -> {
				scheduledActions.clear();
			}));
	}

	/**
	 * Execute action and notify action listener if set.
	 *
	 * @param action the action
	 * @param context the context
	 * @return mono for completion
	 */
	protected Mono<Void> executeAction(Function<StateContext<S, E>, Mono<Void>> action, StateContext<S, E> context) {
		return Mono.just(action)
			.flatMap(a -> {
				long now = System.currentTimeMillis();
				return a.apply(context)
					.thenEmpty(Mono.fromRunnable(() -> {
						if (this.actionListener != null) {
							try {
								this.actionListener.onExecute(context.getStateMachine(), action, System.currentTimeMillis() - now);
							} catch (Exception e) {
								log.warn("Error with actionListener", e);
							}
						}
					}));
			});
	}

	protected Mono<Void> handleStateDoOnComplete(StateContext<S, E> context) {
		return stateListener.doOnComplete(context);
	}

	protected void notifyStateOnComplete(StateContext<S, E> context) {
		stateListener.onComplete(context);
	}

	private Long resolveDoActionTimeout(StateContext<S, E> context) {
		Long timeout = null;
		if (stateDoActionPolicy == StateDoActionPolicy.TIMEOUT_CANCEL) {
			timeout = StateMachineUtils.getMessageHeaderDoActionTimeout(context);
			if (timeout == null) {
				timeout = stateDoActionPolicyTimeout;
			}
		}
		return timeout;
	}

	private static class ScheduledAction {
		Subscription subscription;
		Long timeout;
		Long subscribeTime;

		ScheduledAction(Subscription subscription, Long timeout, Long subscribeTime) {
			this.subscription = subscription;
			this.timeout = timeout;
			this.subscribeTime = subscribeTime;
		}

		Duration getNeededDelayNow() {
			long delay = 0;
			if (subscribeTime != null && timeout != null) {
				long now = System.currentTimeMillis();
				long tocancel = subscribeTime + timeout;
				delay = now > tocancel ? 0 : tocancel - now;
			}
			return Duration.ofMillis(delay);
		}
	}

	@Override
	public String toString() {
		return "AbstractState [id=" + id + ", pseudoState=" + pseudoState + ", deferred=" + deferred + ", entryActions="
				+ entryActions + ", exitActions=" + exitActions + ", stateActions=" + stateActions + ", regions="
				+ regions + ", submachine=" + submachine + "]";
	}
}
