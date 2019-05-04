/*
 * Copyright 2015-2019 the original author or authors.
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
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.Lifecycle;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.ExtendedState.ExtendedStateChangeListener;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.ActionListener;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.ForkPseudoState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.JoinPseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateContext;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.PseudoStateListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateListenerAdapter;
import org.springframework.statemachine.support.StateMachineExecutor.StateMachineExecutorTransit;
import org.springframework.statemachine.transition.InitialTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionConflictPolicy;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.DefaultTriggerContext;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

	private TransitionConflictPolicy transitionConflictPolicy;

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

	private final UUID uuid;

	private String id;

	private volatile Message<E> forwardedInitialEvent;

	private StateMachine<S, E> parentMachine;

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
		this(states, transitions, initialState, null, null, extendedState, null);
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
	 * @param uuid the given uuid for this machine
	 */
	public AbstractStateMachine(Collection<State<S, E>> states, Collection<Transition<S, E>> transitions,
			State<S, E> initialState, Transition<S, E> initialTransition, Message<E> initialEvent,
								ExtendedState extendedState, UUID uuid) {
		super();
		this.uuid = uuid == null ? UUID.randomUUID() : uuid;
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
		State<S, E> s = lastState;
		if (s != null && isComplete()) {
			return s;
		} else {
			return currentState;
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

	/**
	 * @param history to set internal history state.
	 */
	public void setHistoryState(PseudoState<S, E> history) {
		this.history = history;
	}

	/**
	 * @return history state attribute.
	 */
	public PseudoState<S, E> getHistoryState() {
		return history;
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		return sendEvent(Mono.just(event))
			.switchIfEmpty(Flux.just(StateMachineEventResult.<S, E>from(this, event, ResultType.DENIED)))
			.reduce(false, (a, r) -> !(a | r.getResultType() == ResultType.DENIED))
			.block();
	}

	@Override
	protected void notifyEventNotAccepted(StateContext<S, E> stateContext) {
		if (parentMachine == null) {
			super.notifyEventNotAccepted(stateContext);
		}
	}

	@Override
	public boolean sendEvent(E event) {
		return sendEvent(MessageBuilder.withPayload(event).build());
	}

	@Override
	public Flux<StateMachineEventResult<S, E>> sendEvents(Flux<Message<E>> events) {
		return events.flatMap(e -> handleEvent(e));
	}

	@Override
	public Flux<StateMachineEventResult<S, E>> sendEvent(Mono<Message<E>> event) {
		return event.flatMapMany(e -> handleEvent(e));
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

		for (final State<S, E> state : states) {

			state.addStateListener(new StateListenerAdapter<S, E>() {
				@Override
				public void onComplete(StateContext<S, E> context) {
					log.debug("State onComplete: state=[" + state + "] context=[" + context + "]");
					((AbstractStateMachine<S, E>)getRelayStateMachine()).executeTriggerlessTransitions(AbstractStateMachine.this, context, state).subscribe();
				};
			});

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

		ReactiveStateMachineExecutor<S, E> executor = new ReactiveStateMachineExecutor<S, E>(this, getRelayStateMachine(), transitions,
				triggerToTransitionMap, triggerlessTransitions, initialTransition, initialEvent, transitionConflictPolicy);
		if (getBeanFactory() != null) {
			executor.setBeanFactory(getBeanFactory());
		}
		if (getTaskExecutor() != null){
			// parent machine is set when we're on substates(not regions)
			// so then force sync executor which makes things a bit more reliable
			// as state execution should anyway get synched with plain substates.
			if(parentMachine != null) {
				executor.setTaskExecutor(new SyncTaskExecutor());
			} else {
				executor.setTaskExecutor(getTaskExecutor());
			}
		}
		executor.afterPropertiesSet();
		executor.setStateMachineExecutorTransit(new StateMachineExecutorTransit<S, E>() {

			@Override
			public Mono<Void> transit(Transition<S, E> t, StateContext<S, E> ctx, Message<E> message) {
				Mono<Void> mono = Mono.empty();
				long now = System.currentTimeMillis();
				// TODO: fix above stateContext as it's not used
				notifyTransitionStart(buildStateContext(Stage.TRANSITION_START, message, t, getRelayStateMachine()));
				try {
					t.executeTransitionActions(ctx);
				} catch (Exception e) {
					// aborting, executor should stop possible loop checking possible transitions
					// causing infinite execution
					log.warn("Aborting as transition " + t, e);
					throw new StateMachineException("Aborting as transition " + t + " caused error ", e);
				}
				notifyTransition(buildStateContext(Stage.TRANSITION, message, t, getRelayStateMachine()));
				if (t.getTarget().getPseudoState() != null && t.getTarget().getPseudoState().getKind() == PseudoStateKind.JOIN) {
					exitFromState(t.getSource(), message, t, getRelayStateMachine());
				} else {
					if (t.getKind() == TransitionKind.INITIAL) {
						mono = switchToState(t.getTarget(), message, t, getRelayStateMachine()).thenEmpty(Mono.defer(() -> {
							notifyStateMachineStarted(buildStateContext(Stage.STATEMACHINE_START, message, t, getRelayStateMachine()));
							return Mono.empty();
						}));
					} else if (t.getKind() != TransitionKind.INTERNAL) {
						mono = switchToState(t.getTarget(), message, t, getRelayStateMachine());
					}
				}
				// TODO: looks like events should be called here and anno processing earlier
				notifyTransitionEnd(buildStateContext(Stage.TRANSITION_END, message, t, getRelayStateMachine()));
				notifyTransitionMonitor(getRelayStateMachine(), t, System.currentTimeMillis() - now);
				return mono;
			}
		});
		stateMachineExecutor = executor;

		for (Transition<S, E> t : getTransitions()) {
			t.addActionListener(new ActionListener<S, E>() {

				@Override
				public void onExecute(StateMachine<S, E> stateMachine, Action<S, E> action, long duration) {
					notifyActionMonitor(stateMachine, action, duration);
				}
			});
		}
		for (State<S, E> s : getStates()) {
			s.addActionListener(new ActionListener<S, E>() {
				@Override
				public void onExecute(StateMachine<S, E> stateMachine, Action<S, E> action, long duration) {
					notifyActionMonitor(stateMachine, action, duration);
				}
			});
		}
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
	protected Mono<Void> doPreStartReactively() {
		return Mono.defer(() -> {
			if (currentState != null) {
				return Mono.fromRunnable(() -> {
					super.doStart();
					if (log.isDebugEnabled()) {
						log.debug("State already set, disabling initial");
					}
					registerPseudoStateListener();
					stateMachineExecutor.setInitialEnabled(false);
				})
				.and(stateMachineExecutor.startReactively())
				.doOnSuccess(x -> {
					StateContext<S, E> stateContext = buildStateContext(Stage.STATEMACHINE_START, null, null, getRelayStateMachine());
					notifyStateMachineStarted(stateContext);
				})
				.and(Mono.defer(() -> {
					if (currentState != null && currentState.isSubmachineState()) {
						StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
						return submachine.startReactively();
					} else if (currentState != null && currentState.isOrthogonal()) {
						Collection<Region<S, E>> regions = ((AbstractState<S, E>)currentState).getRegions();
						return Flux.fromIterable(regions).flatMap(r -> r.startReactively()).then();
					}
					return Mono.empty();
				}))
				;
			} else {
				return Mono.fromRunnable(() -> {
					super.doStart();
					registerPseudoStateListener();

					if (initialEnabled != null && !initialEnabled) {
						if (log.isDebugEnabled()) {
							log.debug("Initial disable asked, disabling initial");
						}
						stateMachineExecutor.setInitialEnabled(false);
					} else {
						stateMachineExecutor.setForwardedInitialEvent(forwardedInitialEvent);
					}
				})
				.and(stateMachineExecutor.startReactively())
				;
			}
		});
	}

	@Override
	protected Mono<Void> doPostStartReactively() {
		return isComplete() ? stopReactively() : super.doPostStartReactively();
	}

	@Override
	protected Mono<Void> doPreStopReactively() {
		return Mono.fromRunnable(() -> {
			notifyStateMachineStopped(buildStateContext(Stage.STATEMACHINE_STOP, null, null, this));
			// stash current state before we null it so that
			// we can still return where we 'were' when machine is stopped
			lastState = currentState;
			currentState = null;
			initialEnabled = null;
			log.debug("Stop complete " + this);
		})
		.then(stateMachineExecutor.stopReactively())
		;

		// TODO: REACTOR, doing this other way around will dispose triggerDisposable in
		//       ReactiveStateMachineExecutor and we get cancel before runnable is ran.

//		return stateMachineExecutor.stopReactively().then(Mono.fromRunnable(() -> {
//			notifyStateMachineStopped(buildStateContext(Stage.STATEMACHINE_STOP, null, null, this));
//			// stash current state before we null it so that
//			// we can still return where we 'were' when machine is stopped
//			lastState = currentState;
//			currentState = null;
//			initialEnabled = null;
//			log.debug("Stop complete " + this);
//		}));
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
	public void setParentMachine(StateMachine<S, E> parentMachine) {
		this.parentMachine = parentMachine;
	}

	@Override
	public void setForwardedInitialEvent(Message<E> message) {
		forwardedInitialEvent = message;
	}

	/**
	 * Sets the transition conflict policy.
	 *
	 * @param transitionConflictPolicy the new transition conflict policy
	 */
	public void setTransitionConflightPolicy(TransitionConflictPolicy transitionConflictPolicy) {
		this.transitionConflictPolicy = transitionConflictPolicy;
	}

	private Flux<StateMachineEventResult<S, E>> handleEvent(Message<E> message) {
		if (hasStateMachineError()) {
			return Flux.just(StateMachineEventResult.<S, E>from(this, message, ResultType.DENIED));
		}
		return Mono.just(message)
			.map(m -> getStateMachineInterceptors().preEvent(m, this))
			.onErrorResume(error -> Mono.empty())
			.flatMapMany(m -> acceptEvent(m))
			.doOnNext(notifyOnDenied());
	}

	private Consumer<StateMachineEventResult<S, E>> notifyOnDenied() {
		return r -> {
			if (r.getResultType() == ResultType.DENIED) {
				notifyEventNotAccepted(buildStateContext(Stage.EVENT_NOT_ACCEPTED, r.getMessage(), null,
				getRelayStateMachine(), getState(), null));
			}
		};
	}

	private Flux<StateMachineEventResult<S, E>> acceptEvent(Message<E> message) {
		return Flux.defer(() -> {
			State<S, E> cs = currentState;
			if (cs != null) {
				if (cs.shouldDefer(message)) {
					stateMachineExecutor.queueDeferredEvent(message);
					return Flux.just(StateMachineEventResult.<S, E>from(this, message, ResultType.DEFERRED));
				}
				return cs.sendEvent(message).collectList().flatMapMany(l -> {
					Flux<StateMachineEventResult<S, E>> ret = Flux.fromIterable(l);
					if (!l.stream().anyMatch(er -> er.getResultType() == ResultType.ACCEPTED)) {
						ret = ret.concatWith(Mono.defer(() -> {
							for (Transition<S,E> transition : transitions) {
								State<S,E> source = transition.getSource();
								Trigger<S, E> trigger = transition.getTrigger();
								if (cs != null && StateMachineUtils.containsAtleastOne(source.getIds(), cs.getIds())) {
									if (trigger != null && trigger.evaluate(new DefaultTriggerContext<S, E>(message.getPayload()))) {
										return stateMachineExecutor.queueEvent(Mono.just(message)).thenReturn(StateMachineEventResult.<S, E>from(this, message, ResultType.ACCEPTED));
									}
								}
							}
							return Mono.just(StateMachineEventResult.<S, E>from(this, message, ResultType.DENIED));
						}));
					}
					return ret;
				});
			}
			return Flux.just(StateMachineEventResult.<S, E>from(this, message, ResultType.DENIED));
		});
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

	@SuppressWarnings("rawtypes")
	@Override
	public void resetStateMachine(StateMachineContext<S, E> stateMachineContext) {
		// TODO: this function needs a serious rewrite
		if (stateMachineContext == null) {
			log.info("Got null context, resetting to initial state, clearing extended state and machine id");
			currentState = initialState;
			extendedState.getVariables().clear();
			setId(null);
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

				boolean enumMatch = false;
				if (state instanceof Enum && ss.getId() instanceof Enum && state.getClass() == ss.getId().getClass()
						&& ((Enum) ss.getId()).ordinal() == ((Enum) state).ordinal()) {
					enumMatch = true;
				}

				if (state != null && (ss.getIds().contains(state) || enumMatch) ) {
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
					}

					if (log.isDebugEnabled()) {
						log.debug("State reseted: stateMachine=[" + this + "] stateMachineContext=[" + stateMachineContext + "]");
					}
					stateSet = true;
					break;
				} else if (stateMachineContext.getChilds() != null && !stateMachineContext.getChilds().isEmpty()) {
					// we're here because root machine only have regions
					if (s.isOrthogonal()) {
						Collection<Region<S, E>> regions = ((AbstractState<S, E>)s).getRegions();

						for (Region<S, E> region : regions) {
							for (final StateMachineContext<S, E> child : stateMachineContext.getChilds()) {
								// only call if reqion id matches with context id
								if (ObjectUtils.nullSafeEquals(region.getId(), child.getId())) {
									((StateMachine<S, E>)region).getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<S,E>>() {

										@Override
										public void apply(StateMachineAccess<S, E> function) {
											function.resetStateMachine(child);
										}
									});
								}
							}
						}
					} else {
						for (final StateMachineContext<S, E> child : stateMachineContext.getChilds()) {
							S state2 = child.getState();
							boolean enumMatch2 = false;
							if (state2 instanceof Enum && ss.getId() instanceof Enum
									&& state.getClass() == ss.getId().getClass()
									&& ((Enum) ss.getId()).ordinal() == ((Enum) state2).ordinal()) {
								enumMatch2 = true;
							}

							if (state2 != null && (ss.getIds().contains(state2) || enumMatch2) ) {
								currentState = s;
								lastState = currentState;
								stateSet = true;
								break;
							}
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
			if (StateMachineUtils.isPseudoState(s, PseudoStateKind.JOIN)) {
				JoinPseudoState<S, E> jps = (JoinPseudoState<S, E>) s.getPseudoState();
				Collection<S> ids = currentState.getIds();
				jps.reset(ids);
			}

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
			this.extendedState.getVariables().clear();
			this.extendedState.getVariables().putAll(stateMachineContext.getExtendedState().getVariables());
		}
		if (currentState instanceof Lifecycle) {
			((Lifecycle)currentState).start();
		}
	}

	@Override
	public void addStateMachineInterceptor(StateMachineInterceptor<S, E> interceptor) {
		getStateMachineInterceptors().add(interceptor);
		stateMachineExecutor.addStateMachineInterceptor(interceptor);
	}

	@Override
	public void addStateMachineMonitor(StateMachineMonitor<S, E> monitor) {
		getStateMachineMonitor().register(monitor);
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

	protected Mono<Void> executeTriggerlessTransitions(StateMachine<S, E> stateMachine, StateContext<S, E> stateContext, State<S, E> state) {
		Mono<Void> mono = this.stateMachineExecutor.executeTriggerlessTransitions(stateContext, state);
		State<S, E> cs = currentState;
		if (cs != null && cs.isOrthogonal()) {
			Collection<Region<S, E>> regions = ((AbstractState<S, E>)cs).getRegions();
			Mono<Void> m = Flux.fromIterable(regions)
				.flatMap(r -> ((AbstractStateMachine<S, E>)r).executeTriggerlessTransitions(this, stateContext, state))
				.then();
			mono = mono.then(m);
		} else if (cs != null && cs.isSubmachineState()) {
			StateMachine<S, E> submachine = ((AbstractState<S, E>)cs).getSubmachine();
			Mono<Void> m = ((AbstractStateMachine<S, E>)submachine).executeTriggerlessTransitions(this, stateContext, state);
			mono = mono.then(m);
		}
		return mono;
	}

	protected StateMachineExecutor<S, E> getStateMachineExecutor() {
		return stateMachineExecutor;
	}

	private boolean callPreStateChangeInterceptors(State<S,E> state, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		try {
			getStateMachineInterceptors().preStateChange(state, message, transition, this, stateMachine);
		} catch (Exception e) {
			log.info("Interceptors threw exception, skipping state change", e);
			return false;
		}
		return true;
	}

	private void callPostStateChangeInterceptors(State<S,E> state, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		try {
			getStateMachineInterceptors().postStateChange(state, message, transition, this, stateMachine);
		} catch (Exception e) {
			log.warn("Interceptors threw exception in post state change", e);
		}
	}

	private boolean isInitialTransition(Transition<S,E> transition) {
		return transition != null && transition.getKind() == TransitionKind.INITIAL;
	}

	private Mono<Void> switchToState(State<S,E> state, Message<E> message, Transition<S,E> transition, StateMachine<S, E> stateMachine) {
		return Mono.defer(() -> {
			if (!isInitialTransition(transition) && !StateMachineUtils.isTransientPseudoState(state)
					&& !callPreStateChangeInterceptors(state, message, transition, stateMachine)) {
				return Mono.empty();
			}

			StateContext<S, E> stateContext = buildStateContext(Stage.STATE_CHANGED, message, transition, stateMachine);
			State<S,E> toState = followLinkedPseudoStates(state, stateContext);
			PseudoStateKind kind = state.getPseudoState() != null ? state.getPseudoState().getKind() : null;

			if (kind != null && (kind != PseudoStateKind.INITIAL && kind != PseudoStateKind.JOIN
					&& kind != PseudoStateKind.FORK && kind != PseudoStateKind.END)) {
				callPreStateChangeInterceptors(toState, message, transition, stateMachine);
			}

			kind = toState.getPseudoState() != null ? toState.getPseudoState().getKind() : null;
			if (kind == PseudoStateKind.FORK) {
				Mono<Void> ret1 = exitCurrentState(toState, message, transition, stateMachine);
				ForkPseudoState<S, E> fps = (ForkPseudoState<S, E>) toState.getPseudoState();
				Mono<Void> ret2 = Flux.fromIterable(fps.getForks())
					.flatMap(f -> {
						callPreStateChangeInterceptors(f, message, transition, stateMachine);
						return setCurrentState(f, message, transition, false, stateMachine, null, fps.getForks());
					})
					.then()
					;
				return ret1.then(ret2);
			} else {
				Collection<State<S, E>> targets = new ArrayList<>();
				targets.add(toState);
				return setCurrentState(toState, message, transition, true, stateMachine, null, targets);
			}
		})
		.then(Mono.defer(() -> {
			return shouldComplete() ? stopReactively() : Mono.empty();
		}))
		;
	}

	private boolean shouldComplete() {
		return StateMachineUtils.isPseudoState(currentState, PseudoStateKind.END);
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
				List<PseudoStateListener<S, E>> listeners = new ArrayList<PseudoStateListener<S, E>>();
				listeners.add(new PseudoStateListener<S, E>() {
					@Override
					public void onContext(PseudoStateContext<S, E> context) {
						PseudoState<S, E> pseudoState = context.getPseudoState();
						State<S, E> toStateOrig = findStateWithPseudoState(pseudoState);
						StateContext<S, E> stateContext = buildStateContext(Stage.STATE_EXIT, null, null, getRelayStateMachine());
						State<S, E> toState = followLinkedPseudoStates(toStateOrig, stateContext);
						// TODO: try to find matching transition based on direct link.
						// should make this built-in in pseudostates
						Transition<S, E> transition = findTransition(toStateOrig, toState);
						switchToState(toState, null, transition, getRelayStateMachine()).subscribe();
						pseudoState.exit(stateContext);
					}
				});
				// setting instead adding makes sure existing listeners are removed
				p.setPseudoStateListeners(listeners);
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

	Mono<Void> setCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, boolean exit, StateMachine<S, E> stateMachine) {
		return setCurrentState(state, message, transition, exit, stateMachine, null, null);
	}

	Mono<Void> setCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, boolean exit,
			StateMachine<S, E> stateMachine, Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		return setCurrentStateInternal3(state, message, transition, exit, stateMachine, sources, targets);
	}

	private Mono<Void> setCurrentStateInternal3(State<S, E> state, Message<E> message, Transition<S, E> transition, boolean exit,
			StateMachine<S, E> stateMachine, Collection<State<S, E>> sources, Collection<State<S, E>> targets) {

		java.util.function.Function<State<S, E>, State<S, E>> mapFromTargetSub = in -> {
			if (transition != null) {
				boolean isTargetSubOf = StateMachineUtils.isSubstate(state, transition.getSource());
				if (isTargetSubOf && currentState == transition.getTarget()) {
					return transition.getSource();
				}
			}
			return in;
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleExit = in -> {
			if (exit) {
				return exitCurrentState(in, message, transition, stateMachine, sources, targets)
						.then(Mono.just(in))
						;
			}
			return Mono.just(in);
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleStart = in -> {
			if (!isRunning() && !isComplete()) {
				return startReactively().then(Mono.just(in));
			}
			return Mono.just(in);
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleEntry1 = in -> {
			State<S, E> notifyFrom = currentState;
			currentState = in;
			return entryToState(in, message, transition, stateMachine)
				.then(Mono.just(in))
				.doOnNext(s -> {
					if (!StateMachineUtils.isPseudoState(s, PseudoStateKind.JOIN)) {
						notifyStateChanged(buildStateContext(Stage.STATE_CHANGED, message, null, getRelayStateMachine(), notifyFrom, s));
					}
				});
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleEntry2 = in -> {
			State<S, E> notifyFrom = currentState;
			State<S, E> findDeep = findDeepParent(in);
			currentState = findDeep;
			return entryToState(findDeep, message, transition, stateMachine)
				.then(Mono.just(in))
				.doOnNext(s -> {
					if (!StateMachineUtils.isPseudoState(s, PseudoStateKind.JOIN)) {
						notifyStateChanged(buildStateContext(Stage.STATE_CHANGED, message, null, getRelayStateMachine(), notifyFrom, findDeep));
					}
				});
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleStop = s -> {
			if (stateMachine != this && isComplete()) {
				return stopReactively().then(Mono.just(s));
			}
			return Mono.just(s);
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleSubmachineOrRegions = in -> {
			return Mono.just(in)
				.flatMap(s -> {
					if (currentState == findDeepParent(s)) {
						boolean isTargetSubOf = transition != null && StateMachineUtils.isSubstate(state, transition.getSource());
						if (currentState.isSubmachineState()) {
							StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
							// need to check complete as submachine may now return non null
							if (!submachine.isComplete() && submachine.getState() == s) {
								State<S, E> findDeep = findDeepParent(s);
								if (currentState == findDeep) {
									Mono<State<S, E>> mono = Mono.just(s);
									if (isTargetSubOf) {
										mono = mono.flatMap(ss -> entryToState(currentState, message, transition, stateMachine).then(Mono.just(ss)));
									}
									currentState = findDeep;
									mono = mono.flatMap(ss -> ((AbstractStateMachine<S, E>)submachine).setCurrentState(ss, message, transition, false, stateMachine)).then(Mono.empty());
									return mono;
								}
							}
						} else if (currentState.isOrthogonal()) {
							Collection<Region<S, E>> regions = ((AbstractState<S, E>)currentState).getRegions();
							State<S, E> findDeep = findDeepParent(s);
							for (Region<S, E> region : regions) {
								if (region.getState() == s) {
									if (currentState == findDeep) {
										Mono<State<S, E>> mono = Mono.just(s);
										if (isTargetSubOf) {
											mono = mono.flatMap(ss -> entryToState(currentState, message, transition, stateMachine).then(Mono.just(ss)));
										}
										currentState = findDeep;
										mono = mono.flatMap(ss -> ((AbstractStateMachine<S, E>)region).setCurrentState(s, message, transition, false, stateMachine)).then(Mono.empty());
										return mono;
									}
								}
							}
						}
					}
					return Mono.just(s);
				})
				.flatMap(s -> {
					Mono<State<S, E>> mono = Mono.just(s);
					boolean shouldTryEntry = findDeepParent(s) != currentState;
					if (!shouldTryEntry && (transition.getSource() == currentState && StateMachineUtils.isSubstate(currentState, transition.getTarget()))) {
						shouldTryEntry = true;
					}
					currentState = findDeepParent(s);
					if (shouldTryEntry) {
						mono = mono.flatMap(ss -> entryToState(currentState, message, transition, stateMachine, sources, targets)).then(Mono.just(s));
					}

					if (currentState.isSubmachineState()) {
						StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
						mono = mono.flatMap(ss -> ((AbstractStateMachine<S, E>)submachine).setCurrentState(s, message, transition, false, stateMachine).then(Mono.just(ss)));
					} else if (currentState.isOrthogonal()) {
						Collection<Region<S, E>> regions = ((AbstractState<S, E>)currentState).getRegions();
						Mono<State<S, E>> ret = Flux.fromIterable(regions)
							.flatMap(region -> ((AbstractStateMachine<S, E>)region).setCurrentState(s, message, transition, false, stateMachine))
							.then(Mono.just(s));
						mono = mono.then(ret);
					}
					return mono;
				});
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleStage1 = in -> {
			return Mono.just(in)
				.map(mapFromTargetSub)
				.filter(s -> states.contains(s))
				.flatMap(handleExit)
				.flatMap(handleEntry1)
				.flatMap(handleStart)
				.then(Mono.just(in));
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleStage2 = in -> {
			return Mono.just(in)
				.filter(s -> currentState == null && !states.contains(s) && StateMachineUtils.isSubstate(findDeepParent(s), state))
				.map(mapFromTargetSub)
				.flatMap(handleExit)
				.flatMap(handleEntry2)
				.flatMap(handleStart)
				.then(Mono.just(in));
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleStage3 = in -> {
			return Mono.just(in)
				.map(mapFromTargetSub)
				.filter(s -> currentState != null && !states.contains(s) && findDeepParent(state) != null)
				.flatMap(handleExit)
				.flatMap(handleSubmachineOrRegions)
				.then(Mono.just(in));
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleStage4 = in -> {
			return Mono.just(in)
				.filter(s -> history != null && transition.getKind() != TransitionKind.INITIAL)
				.map(mapFromTargetSub)
				.doOnNext(s -> {
					// do not set history if this is initial transition as
					// it would break history state set via reset as
					// we get here i.e. when machine is started in reset.
					// and it really doesn't make sense to set initial state for history
					// if we get here via initial transition
					if (history.getKind() == PseudoStateKind.HISTORY_SHALLOW) {
						State<S, E> findDeep = findDeepParent(state);
						((HistoryPseudoState<S, E>)history).setState(findDeep);
					} else if (history.getKind() == PseudoStateKind.HISTORY_DEEP){
						((HistoryPseudoState<S, E>)history).setState(s);
					}
				})
				.then(Mono.just(in));
		};

		java.util.function.Function<State<S, E>, ? extends Mono<State<S, E>>> handleStage5 = in -> {
			return Mono.just(in).flatMap(handleStop);
		};

		return Mono.just(state)
			.flatMap(handleStage1)
			.flatMap(handleStage2)
			.flatMap(handleStage3)
			.flatMap(handleStage4)
			.flatMap(handleStage5)
			.then();
	}

	Mono<Void> exitCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		return exitCurrentState(state, message, transition, stateMachine, null, null);
	}

	Mono<Void> exitCurrentState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine,
			Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		if (currentState == null) {
			return Mono.empty();
		}
		if (currentState.isSubmachineState()) {
			StateMachine<S, E> submachine = ((AbstractState<S, E>)currentState).getSubmachine();
			Mono<Void> ret1 = ((AbstractStateMachine<S, E>)submachine).exitCurrentState(state, message, transition, stateMachine);
			Mono<Void> ret2 = exitFromState(currentState, message, transition, stateMachine, sources, targets);
			return ret1.then(ret2);
		} else if (currentState.isOrthogonal()) {
			Collection<Region<S,E>> regions = ((AbstractState<S, E>)currentState).getRegions();
			return Flux.fromIterable(regions)
				.filter(r -> r.getStates().contains(state))
				.flatMap(r -> exitFromState(r.getState(), message, transition, stateMachine, sources, targets))
				.then()
				.and(exitFromState(currentState, message, transition, stateMachine, sources, targets));
		} else {
			return exitFromState(currentState, message, transition, stateMachine, sources, targets);
		}
	}

	private Mono<Void> exitFromState(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine) {
		return exitFromState(state, message, transition, stateMachine, null, null);
	}

	private Mono<Void> exitFromState(State<S, E> state, Message<E> message, Transition<S, E> transition,
			StateMachine<S, E> stateMachine, Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		if (state == null) {
			return Mono.empty();
		}
		if (log.isTraceEnabled()) {
			log.trace("Trying Exit state=[" + state + "]");
		}
		StateContext<S, E> stateContext = buildStateContext(Stage.STATE_EXIT, message, transition, stateMachine);

		if (transition != null) {
			State<S, E> findDeep = findDeepParent(transition.getTarget());
			boolean isTargetSubOfOtherState = findDeep != null && findDeep != currentState;
			boolean isSubOfSource = StateMachineUtils.isSubstate(transition.getSource(), currentState);
			boolean isSubOfTarget = StateMachineUtils.isSubstate(transition.getTarget(), currentState);

			if (transition.getKind() == TransitionKind.LOCAL && StateMachineUtils.isSubstate(transition.getSource(), transition.getTarget()) && transition.getSource() == currentState) {
				return Mono.empty();
			} else if (transition.getKind() == TransitionKind.LOCAL && StateMachineUtils.isSubstate(transition.getTarget(), transition.getSource()) && transition.getTarget() == currentState) {
				return Mono.empty();
			}

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
					return Mono.empty();
				}
				if (StateMachineUtils.isSubstate(currentState, transition.getTarget())) {
					// don't exit if we're targeting fork within a substate or other
					// pseudostate if we're within region.
					if (currentState.isSubmachineState()
							&& StateMachineUtils.isPseudoState(transition.getTarget(), PseudoStateKind.FORK)) {
						return Mono.empty();
					} else if (currentState.isOrthogonal()) {
						return Mono.empty();
					}
				}
			} else if (findDeep != null && findDeep != state && findDeep.getStates().contains(state)) {
			} else if (!isSubOfSource && !isSubOfTarget) {
				return Mono.empty();
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Exit state=[" + state + "]");
		}
		notifyStateExited(buildStateContext(Stage.STATE_EXIT, message, null, getRelayStateMachine(), state, null));
		return state.exit(stateContext);
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

	private Mono<Void> entryToState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine) {
		return entryToState(state, message, transition, stateMachine, null, null);
	}

	private Mono<Void> entryToState(State<S, E> state, Message<E> message, Transition<S, E> transition, StateMachine<S, E> stateMachine,
			Collection<State<S, E>> sources, Collection<State<S, E>> targets) {
		if (state == null) {
			return Mono.empty();
		}

		// call post interceptors here instead end of switchToState
		// as anonymous transition would cause post calls to happen on wrong order
		callPostStateChangeInterceptors(state, message, transition, stateMachine);

		log.debug("Trying Enter state=[" + state + "]");
		if (log.isTraceEnabled()) {
			log.trace("Trying Enter state=[" + state + "]");
		}
		StateContext<S, E> stateContext = buildStateContext(Stage.STATE_ENTRY, message, transition, stateMachine, sources, targets);

		if (transition != null) {
			State<S, E> findDeep1 = findDeepParent(transition.getTarget());
			State<S, E> findDeep2 = findDeepParent(transition.getSource());
			boolean isComingFromOtherSubmachine = findDeep1 != null && findDeep2 != null && findDeep2 != currentState;

			boolean isSubOfSource = StateMachineUtils.isSubstate(transition.getSource(), currentState);
			boolean isSubOfTarget = StateMachineUtils.isSubstate(transition.getTarget(), currentState);

			if (transition.getKind() == TransitionKind.LOCAL && StateMachineUtils.isSubstate(transition.getSource(), transition.getTarget())
					&& transition.getSource() == currentState) {
				return Mono.empty();
			} else if (transition.getKind() == TransitionKind.LOCAL && StateMachineUtils.isSubstate(transition.getTarget(), transition.getSource())
					&& transition.getTarget() == currentState) {
				return Mono.empty();
			}

			if (currentState == transition.getSource() && currentState == transition.getTarget()) {
			} else if (!isSubOfSource && !isSubOfTarget && currentState == transition.getTarget()) {
			} else if (isComingFromOtherSubmachine) {
			} else if (!isSubOfSource && !isSubOfTarget && findDeep2 == null) {
			} else if (isSubOfSource && !isSubOfTarget && currentState == transition.getTarget()) {
				if (isDirectSubstate(transition.getSource(), transition.getTarget()) && transition.getKind() != TransitionKind.LOCAL
						&& isInitial(transition.getTarget())) {
					return Mono.empty();
				}
			} else if (!isSubOfSource && !isSubOfTarget
					&& (transition.getSource() == currentState && StateMachineUtils.isSubstate(currentState, transition.getTarget()))) {
			} else if (!isSubOfSource && !isSubOfTarget) {
				if (!StateMachineUtils.isTransientPseudoState(transition.getTarget())) {
					return Mono.empty();
				}
			}
		}

		// with linked joins, we need to enter state but should not notify.
		// state entries are needed to track join logic.
		if (!StateMachineUtils.isPseudoState(state, PseudoStateKind.JOIN)) {
			notifyStateEntered(buildStateContext(Stage.STATE_ENTRY, message, transition, getRelayStateMachine(), null, state));
		}
		if (log.isDebugEnabled()) {
			log.debug("Enter state=[" + state + "]");
		}
		return state.entry(stateContext);
	}

	private static <S, E> boolean isInitial(State<S, E> state) {
		return state.getPseudoState() != null && state.getPseudoState().getKind() == PseudoStateKind.INITIAL;
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
