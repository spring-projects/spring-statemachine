/*
 * Copyright 2017-2018 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.ActionListener;
import org.springframework.statemachine.action.CompositeActionListener;
import org.springframework.statemachine.action.StateDoActionPolicy;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineUtils;
import org.springframework.statemachine.trigger.Trigger;

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
	private final Collection<? extends Action<S, E>> entryActions;
	private final Collection<? extends Action<S, E>> exitActions;
	private final Collection<? extends Action<S, E>> stateActions;
	private final Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
	private final StateMachine<S, E> submachine;
	private List<Trigger<S, E>> triggers = new ArrayList<Trigger<S, E>>();
	private final CompositeStateListener<S, E> stateListener = new CompositeStateListener<S, E>();
	private final List<ScheduledAction> scheduledActions = new ArrayList<>();
	private CompositeActionListener<S, E> actionListener;
	private final List<StateMachineListener<S, E>> completionListeners = new CopyOnWriteArrayList<>();
	private StateDoActionPolicy stateDoActionPolicy;
	private Long stateDoActionPolicyTimeout;

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
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions) {
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
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState) {
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
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState, StateMachine<S, E> submachine) {
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
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState, Collection<Region<S, E>> regions) {
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
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, PseudoState<S, E> pseudoState, Collection<Region<S, E>> regions,
			StateMachine<S, E> submachine) {
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
	public AbstractState(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions, Collection<? extends Action<S, E>> stateActions,
			PseudoState<S, E> pseudoState, Collection<Region<S, E>> regions, StateMachine<S, E> submachine) {
		this.id = id;
		this.deferred = deferred != null ? deferred : Collections.<E>emptySet();
		this.entryActions = entryActions != null ? entryActions : Collections.<Action<S, E>>emptySet();
		this.exitActions = exitActions != null ? exitActions : Collections.<Action<S, E>>emptySet();
		this.stateActions = stateActions != null ? stateActions : Collections.<Action<S, E>>emptySet();
		this.pseudoState = pseudoState;

		// use of private ctor should prevent user to
		// add regions and a submachine which is not allowed.
		if (regions != null) {
			this.regions.addAll(regions);
		}
		this.submachine = submachine;
	}

	@Override
	public boolean sendEvent(Message<E> event) {
		return false;
	}

	@Override
	public boolean shouldDefer(Message<E> event) {
		return deferred.contains(event.getPayload());
	}

	@Override
	public void exit(StateContext<S, E> context) {
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
		completionListeners.clear();
		cancelStateActions();
		stateListener.onExit(context);
		disarmTriggers();
	}

	@Override
	public void entry(StateContext<S, E> context) {
		if (submachine != null) {
			final StateMachineListener<S, E> l = new StateMachineListenerAdapter<S, E>() {

				@Override
				public void stateContext(StateContext<S, E> stateContext) {
					if (stateContext.getStage() == Stage.STATEMACHINE_STOP) {
						if (stateContext.getStateMachine() == submachine && submachine.isComplete()) {
							completionListeners.remove(this);
							submachine.removeStateListener(this);
							if (completionListeners.isEmpty()) {
								notifyStateOnComplete(stateContext);
							}
						}
					}
				}
			};
			submachine.addStateListener(l);
		} else if (!regions.isEmpty()) {
			for (final Region<S, E> region : regions) {
				final StateMachineListener<S, E> l = new StateMachineListenerAdapter<S, E>() {

					@Override
					public void stateContext(StateContext<S, E> stateContext) {
						if (stateContext.getStage() == Stage.STATEMACHINE_STOP) {
							if (stateContext.getStateMachine() == region && region.isComplete()) {
								completionListeners.remove(this);
								region.removeStateListener(this);
								if (completionListeners.isEmpty()) {
									notifyStateOnComplete(stateContext);
								}
							}
						}
					}
				};
				completionListeners.add(l);
				region.addStateListener(l);
			}
		}

		stateListener.onEntry(context);
		armTriggers();
		scheduleStateActions(context);
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
	public Collection<? extends Action<S, E>> getEntryActions() {
		return entryActions;
	}

	@Override
	public Collection<? extends Action<S, E>> getStateActions() {
		return stateActions;
	}

	@Override
	public Collection<? extends Action<S, E>> getExitActions() {
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
	protected void doStart() {
		armTriggers();
	}

	@Override
	protected void doStop() {
		disarmTriggers();
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

	/**
	 * Cancel existing state actions and clear list.
	 */
	protected void cancelStateActions() {
		if (log.isDebugEnabled()) {
			log.debug("Handling finish of state actions, scheduledActions size is " + scheduledActions.size());
		}
		for (ScheduledAction task : scheduledActions) {
			if (task.timeout != null) {
				if (log.isDebugEnabled()) {
					log.debug("Timeouting scheduled state do action " + task);
				}
				try {
					task.future.get(task.timeout, TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					if (log.isDebugEnabled()) {
						log.debug("Cancelling scheduled state do action after timeout " + task);
					}
					task.future.cancel(true);
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Cancelling scheduled state do action immediately " + task);
				}
				task.future.cancel(true);
			}
		}
		scheduledActions.clear();
	}

	/**
	 * Schedule state actions and store futures into list to
	 * be cancelled.
	 *
	 * @param context the context
	 */
	protected void scheduleStateActions(StateContext<S, E> context) {
		AtomicInteger completionCount = null;
		if (isSimple()) {
			completionCount = new AtomicInteger(stateActions.size());
		}
		for (Action<S, E> action : stateActions) {
			ScheduledFuture<?> future = scheduleAction(action, context, completionCount);
			if (log.isDebugEnabled()) {
				log.debug("Scheduling state do action " + action + " with future " + future);
			}
			if (future != null) {
				scheduledActions.add(new ScheduledAction(future, resolveDoActionTimeout(context)));
			}
		}
		if (isSimple() && stateActions.size() == 0) {
			notifyStateOnComplete(context);
		}
	}

	/**
	 * Execute action and notify action listener if set.
	 *
	 * @param action the action
	 * @param context the context
	 */
	protected void executeAction(Action<S, E> action, StateContext<S, E> context) {
		long now = System.currentTimeMillis();
		action.execute(context);
		if (this.actionListener != null) {
			try {
				this.actionListener.onExecute(context.getStateMachine(), action, System.currentTimeMillis() - now);
			} catch (Exception e) {
				log.warn("Error with actionListener", e);
			}
		}
	}

	/**
	 * Schedule action and return future which can be used to cancel it.
	 *
	 * @param action the action
	 * @param context the context
	 * @param completionCount the completion count tracker
	 * @return the scheduled future
	 */
	protected ScheduledFuture<?> scheduleAction(final Action<S, E> action, final StateContext<S, E> context,
			final AtomicInteger completionCount) {
		TaskScheduler taskScheduler = getTaskScheduler();
		if (taskScheduler == null) {
			log.error("Unable to schedule action as taskSchedule is not set, action=[" + action + "]");
			return null;
		}
		ScheduledFuture<?> future = taskScheduler.schedule(new Runnable() {

			@Override
			public void run() {
				executeAction(action, context);
				if (completionCount != null && completionCount.decrementAndGet() <= 0) {
					notifyStateOnComplete(context);
				}
			}
		}, new Date());
		return future;
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
		ScheduledFuture<?> future;
		Long timeout;

		public ScheduledAction(ScheduledFuture<?> future, Long timeout) {
			this.future = future;
			this.timeout = timeout;
		}
		@Override
		public String toString() {
			return "ScheduledTask [future=" + future + ", timeout=" + timeout + "]";
		}
	}

	@Override
	public String toString() {
		return "AbstractState [id=" + id + ", pseudoState=" + pseudoState + ", deferred=" + deferred + ", entryActions="
				+ entryActions + ", exitActions=" + exitActions + ", stateActions=" + stateActions + ", regions="
				+ regions + ", submachine=" + submachine + "]";
	}
}
