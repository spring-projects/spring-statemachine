/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.Message;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfig;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.builders.StateMachineTransitions.ChoiceData;
import org.springframework.statemachine.config.builders.StateMachineTransitions.TransitionData;
import org.springframework.statemachine.ensemble.DistributedStateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.ChoicePseudoState;
import org.springframework.statemachine.state.ChoicePseudoState.ChoiceStateData;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.ForkPseudoState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.JoinPseudoState;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.tree.Tree;
import org.springframework.statemachine.support.tree.Tree.Node;
import org.springframework.statemachine.support.tree.TreeTraverser;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultInternalTransition;
import org.springframework.statemachine.transition.InitialTransition;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.EventTrigger;
import org.springframework.statemachine.trigger.TimerTrigger;
import org.springframework.statemachine.trigger.Trigger;
import org.springframework.util.ObjectUtils;

/**
 * Base {@link StateMachineFactory} implementation building {@link StateMachine}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractStateMachineFactory<S, E> extends LifecycleObjectSupport implements
		StateMachineFactory<S, E>, BeanNameAware {

	private final StateMachineTransitions<S, E> stateMachineTransitions;

	private final StateMachineStates<S, E> stateMachineStates;

	private final StateMachineConfigurationConfig<S,E> stateMachineConfigurationConfig;

	private Boolean contextEvents;

	private boolean handleAutostartup = false;

	private String beanName;

	/**
	 * Instantiates a new enum state machine factory.
	 *
	 * @param stateMachineConfigurationConfig the state machine generic config
	 * @param stateMachineTransitions the state machine transitions
	 * @param stateMachineStates the state machine states
	 */
	public AbstractStateMachineFactory(StateMachineConfigurationConfig<S, E> stateMachineConfigurationConfig,
			StateMachineTransitions<S, E> stateMachineTransitions, StateMachineStates<S, E> stateMachineStates) {
		this.stateMachineConfigurationConfig = stateMachineConfigurationConfig;
		this.stateMachineTransitions = stateMachineTransitions;
		this.stateMachineStates = stateMachineStates;
	}
	
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public StateMachine<S, E> getStateMachine() {

		// shared
		DefaultExtendedState defaultExtendedState = new DefaultExtendedState();

		StateMachine<S, E> machine = null;

		// we store mappings from state id's to states which gets
		// created during the process. This is needed for transitions to
		// find a correct mappings because they use state id's, not actual
		// states.
		final Map<S, State<S, E>> stateMap = new HashMap<S, State<S, E>>();
		Stack<MachineStackItem<S, E>> regionStack = new Stack<MachineStackItem<S, E>>();
		Stack<StateData<S, E>> stateStack = new Stack<StateData<S, E>>();
		Map<Object, StateMachine<S, E>> machineMap = new HashMap<Object, StateMachine<S,E>>();

		Iterator<Node<StateData<S, E>>> iterator = buildStateDataIterator();
		while (iterator.hasNext()) {
			Node<StateData<S, E>> node = iterator.next();
			StateData<S, E> stateData = node.getData();
			StateData<S, E> peek = stateStack.isEmpty() ? null : stateStack.peek();

			// simply push and continue
			if (stateStack.isEmpty()) {
				stateStack.push(stateData);
				continue;
			}

			boolean stackContainsSameParent = false;
			Iterator<StateData<S, E>> ii = stateStack.iterator();
			while (ii.hasNext()) {
				StateData<S, E> sd = ii.next();
				if (stateData != null && ObjectUtils.nullSafeEquals(stateData.getState(), sd.getParent())) {
					stackContainsSameParent = true;
					break;
				}
			}

			if (stateData != null && !stackContainsSameParent) {
				stateStack.push(stateData);
				continue;
			}

			Collection<StateData<S, E>> stateDatas = popSameParents(stateStack);
			int initialCount = getInitialCount(stateDatas);
			Collection<Collection<StateData<S, E>>> regionsStateDatas = splitIntoRegions(stateDatas);
			Collection<TransitionData<S, E>> transitionsData = getTransitionData(iterator.hasNext(), stateDatas);

			if (initialCount > 1) {
				for (Collection<StateData<S, E>> regionStateDatas : regionsStateDatas) {
					machine = buildMachine(machineMap, stateMap, regionStateDatas, transitionsData, resolveBeanFactory(),
							contextEvents, defaultExtendedState, stateMachineTransitions, resolveTaskExecutor(),
							resolveTaskScheduler());
					regionStack.push(new MachineStackItem<S, E>(machine));
				}

				Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
				while (!regionStack.isEmpty()) {
					MachineStackItem<S, E> pop = regionStack.pop();
					regions.add(pop.machine);
				}
				S parent = (S)peek.getParent();
				RegionState<S, E> rstate = buildRegionStateInternal(parent, regions, null, stateData != null ? stateData.getEntryActions() : null,
						stateData != null ? stateData.getExitActions() : null, new DefaultPseudoState<S, E>(PseudoStateKind.INITIAL));
				if (stateData != null) {
					stateMap.put(stateData.getState(), rstate);
				} else {
					// TODO: don't like that we create a last machine here
					Collection<State<S, E>> states = new ArrayList<State<S, E>>();
					states.add(rstate);
					Transition<S, E> initialTransition = new InitialTransition<S, E>(rstate);
					StateMachine<S, E> m = buildStateMachineInternal(states, new ArrayList<Transition<S, E>>(), rstate,
							initialTransition, null, defaultExtendedState, null, contextEvents, resolveBeanFactory(),
							resolveTaskExecutor(), resolveTaskScheduler(), beanName);
					machine = m;
				}
			} else {
				machine = buildMachine(machineMap, stateMap, stateDatas, transitionsData, resolveBeanFactory(),
						contextEvents, defaultExtendedState, stateMachineTransitions, resolveTaskExecutor(), resolveTaskScheduler());
				if (peek.isInitial() || (!peek.isInitial() && !machineMap.containsKey(peek.getParent()))) {
					machineMap.put(peek.getParent(), machine);
				}
			}

			stateStack.push(stateData);
		}

		// setup autostart for top-level machine
		if (machine instanceof LifecycleObjectSupport) {
			((LifecycleObjectSupport)machine).setAutoStartup(stateMachineConfigurationConfig.isAutoStart());
		}
		
		// set top-level machine as relay
		final StateMachine<S, E> fmachine = machine;
		fmachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S, E>>() {

			@Override
			public void apply(StateMachineAccess<S, E> function) {
				function.setRelay(fmachine);
			}

		});

		// setup distributed state machine if needed.
		// we wrap previously build machine with a distributed
		// state machine and set it to use given ensemble.
		if (stateMachineConfigurationConfig.getStateMachineEnsemble() != null) {
			DistributedStateMachine<S, E> distributedStateMachine = new DistributedStateMachine<S, E>(
					stateMachineConfigurationConfig.getStateMachineEnsemble(), machine);
			distributedStateMachine.setAutoStartup(stateMachineConfigurationConfig.isAutoStart());
			distributedStateMachine.afterPropertiesSet();
			machine = distributedStateMachine;
		}

		for (StateMachineListener<S, E> listener : stateMachineConfigurationConfig.getStateMachineListeners()) {
			machine.addStateListener(listener);
		}

		return delegateAutoStartup(machine);
	}

	/**
	 * Instructs this factory to handle auto-start flag manually
	 * by calling lifecycle start method.
	 *
	 * @param handleAutostartup the new handle autostartup
	 */
	public void setHandleAutostartup(boolean handleAutostartup) {
		this.handleAutostartup = handleAutostartup;
	}

	/**
	 * Instructs this factory to enable application context events.
	 *
	 * @param contextEvents the new context events enabled
	 */
	public void setContextEventsEnabled(Boolean contextEvents) {
		this.contextEvents = contextEvents;
	}

	private StateMachine<S, E> delegateAutoStartup(StateMachine<S, E> delegate) {
		if (handleAutostartup && delegate instanceof SmartLifecycle && ((SmartLifecycle) delegate).isAutoStartup()) {
			((SmartLifecycle)delegate).start();
		}
		return delegate;
	}

	private BeanFactory resolveBeanFactory() {
		if (stateMachineConfigurationConfig.getBeanFactory() != null) {
			return stateMachineConfigurationConfig.getBeanFactory();
		} else {
			return getBeanFactory();
		}
	}

	private TaskExecutor resolveTaskExecutor() {
		if (stateMachineConfigurationConfig.getTaskExecutor() != null) {
			return stateMachineConfigurationConfig.getTaskExecutor();
		} else {
			return getTaskExecutor();
		}
	}

	private TaskScheduler resolveTaskScheduler() {
		if (stateMachineConfigurationConfig.getTaskScheduler() != null) {
			return stateMachineConfigurationConfig.getTaskScheduler();
		} else {
			return getTaskScheduler();
		}
	}

	private int getInitialCount(Collection<StateData<S, E>> stateDatas) {
		int count = 0;
		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.isInitial()) {
				count++;
			}
		}
		return count;
	}

	private Collection<Collection<StateData<S, E>>> splitIntoRegions(Collection<StateData<S, E>> stateDatas) {
		Map<Object, Collection<StateData<S, E>>> map = new HashMap<Object, Collection<StateData<S, E>>>();
		for (StateData<S, E> stateData : stateDatas) {
			Collection<StateData<S, E>> c = map.get(stateData.getRegion());
			if (c == null) {
				c = new ArrayList<StateData<S,E>>();
			}
			c.add(stateData);
			map.put(stateData.getRegion(), c);
		}
		return map.values();
	}

	private Collection<TransitionData<S, E>> getTransitionData(boolean roots, Collection<StateData<S, E>> stateDatas) {
		if (roots) {
			return resolveTransitionData(stateMachineTransitions.getTransitions(), stateDatas);
		} else {
			return resolveTransitionData2(stateMachineTransitions.getTransitions());
		}
	}

	private static <S, E> Collection<StateData<S, E>> popSameParents(Stack<StateData<S, E>> stack) {
		Collection<StateData<S, E>> data = new ArrayList<StateData<S, E>>();
		Object parent = null;
		if (!stack.isEmpty()) {
			parent = stack.peek().getParent();
		}
		while (!stack.isEmpty() && ObjectUtils.nullSafeEquals(parent, stack.peek().getParent())) {
			data.add(stack.pop());
		}
		return data;
	}


	private static class MachineStackItem<S, E> {

		StateMachine<S, E> machine;

		public MachineStackItem(StateMachine<S, E> machine) {
			this.machine = machine;
		}

	}

	private Collection<TransitionData<S, E>> resolveTransitionData(Collection<TransitionData<S, E>> in, Collection<StateData<S, E>> stateDatas) {
		ArrayList<TransitionData<S, E>> out = new ArrayList<TransitionData<S,E>>();

		Collection<Object> states = new ArrayList<Object>();
		for (StateData<S, E> stateData : stateDatas) {
			states.add(stateData.getParent());
		}

		for (TransitionData<S, E> transitionData : in) {
			S state = transitionData.getState();
			if (state != null && states.contains(state)) {
				out.add(transitionData);
			}
		}

		return out;
	}

	private Collection<TransitionData<S, E>> resolveTransitionData2(Collection<TransitionData<S, E>> in) {
		ArrayList<TransitionData<S, E>> out = new ArrayList<TransitionData<S,E>>();
		for (TransitionData<S, E> transitionData : in) {
			if (transitionData.getState() == null) {
				out.add(transitionData);
			}
		}
		return out;
	}


	private StateMachine<S, E> buildMachine(
			Map<Object, StateMachine<S, E>> machineMap, Map<S, State<S, E>> stateMap,
			Collection<StateData<S, E>> stateDatas, Collection<TransitionData<S, E>> transitionsData,
			BeanFactory beanFactory, Boolean contextEvents, DefaultExtendedState defaultExtendedState,
			StateMachineTransitions<S, E> stateMachineTransitions, TaskExecutor taskExecutor,
			TaskScheduler taskScheduler) {
		State<S, E> state = null;
		State<S, E> initialState = null;
		PseudoState<S, E> historyState = null;
		Action<S, E> initialAction = null;
		Collection<State<S, E>> states = new ArrayList<State<S,E>>();

		// for now loop twice and build states for
		// non initial/end pseudostates last

		for (StateData<S, E> stateData : stateDatas) {
			StateMachine<S, E> stateMachine = machineMap.get(stateData.getState());
			state = stateMap.get(stateData.getState());
			if (state != null) {
				states.add(state);
				if (stateData.isInitial()) {
					initialState = state;
				}
				continue;
			}
			if (stateMachine != null) {
				PseudoState<S, E> pseudoState = null;
				if (stateData.isInitial()) {
					pseudoState = new DefaultPseudoState<S, E>(PseudoStateKind.INITIAL);
				}
				state = new StateMachineState<S, E>(stateData.getState(), stateMachine, stateData.getDeferred(),
						stateData.getEntryActions(), stateData.getExitActions(), pseudoState);
				// TODO: below if/else doesn't feel right
				if (stateDatas.size() > 1 && stateData.isInitial()) {
					initialState = state;
					initialAction = stateData.getInitialAction();
				} else if (stateDatas.size() == 1) {
					initialState = state;
					initialAction = stateData.getInitialAction();
				}
				states.add(state);
			} else {
				PseudoState<S, E> pseudoState = null;
				if (stateData.isInitial()) {
					pseudoState = new DefaultPseudoState<S, E>(PseudoStateKind.INITIAL);
				} else if (stateData.isEnd()) {
					pseudoState = new DefaultPseudoState<S, E>(PseudoStateKind.END);
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.HISTORY_SHALLOW) {
					pseudoState = new HistoryPseudoState<S, E>(PseudoStateKind.HISTORY_SHALLOW);
					historyState = pseudoState;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.HISTORY_DEEP) {
					pseudoState = new HistoryPseudoState<S, E>(PseudoStateKind.HISTORY_DEEP);
					historyState = pseudoState;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.JOIN) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.FORK) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.CHOICE) {
					continue;
				}
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), pseudoState);
				if (stateData.isInitial()) {
					initialState = state;
					initialAction = stateData.getInitialAction();
				}
				states.add(state);

			}
			stateMap.put(stateData.getState(), state);
		}

		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getPseudoStateKind() == PseudoStateKind.CHOICE) {
				S s = stateData.getState();
				List<ChoiceData<S, E>> list = stateMachineTransitions.getChoices().get(s);
				List<ChoiceStateData<S, E>> choices = new ArrayList<ChoiceStateData<S, E>>();
				for (ChoiceData<S, E> c : list) {
					choices.add(new ChoiceStateData<S, E>(stateMap.get(c.getTarget()), c.getGuard()));
				}
				PseudoState<S, E> pseudoState = new ChoicePseudoState<S, E>(choices);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), pseudoState);
				states.add(state);
				stateMap.put(stateData.getState(), state);
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.FORK) {
				S s = stateData.getState();
				List<S> list = stateMachineTransitions.getForks().get(s);
				List<State<S, E>> forks = new ArrayList<State<S,E>>();
				for (S fs : list) {
					forks.add(stateMap.get(fs));
				}
				PseudoState<S, E> pseudoState = new ForkPseudoState<S, E>(forks);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), pseudoState);
				states.add(state);
				stateMap.put(stateData.getState(), state);
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.JOIN) {
				S s = stateData.getState();
				List<S> list = stateMachineTransitions.getJoins().get(s);
				List<State<S, E>> joins = new ArrayList<State<S,E>>();

				// if join source is a regionstate, get
				// it's end states from regions
				if (list.size() == 1) {
					State<S, E> ss1 = stateMap.get(list.get(0));
					if (ss1 instanceof RegionState) {
						Collection<Region<S, E>> regions = ((RegionState<S, E>)ss1).getRegions();
						for (Region<S, E> r : regions) {
							Collection<State<S, E>> ss2 = r.getStates();
							for (State<S, E> ss3 : ss2) {
								if (ss3.getPseudoState() != null && ss3.getPseudoState().getKind() == PseudoStateKind.END) {
									joins.add(ss3);
									continue;
								}
							}
						}
					}
				} else {
					for (S fs : list) {
						joins.add(stateMap.get(fs));
					}
				}
				JoinPseudoState<S, E> pseudoState = new JoinPseudoState<S, E>(joins);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), pseudoState);
				states.add(state);
				stateMap.put(stateData.getState(), state);

				// find joins sources and associate
				for (Entry<S, State<S, E>> e : stateMap.entrySet()) {
					State<S, E> value = e.getValue();
					if (value.isOrthogonal()) {
						Collection<State<S, E>> states2 = value.getStates();
						if (states2.containsAll(joins)) {
							((RegionState<S, E>)value).setJoin(pseudoState);
						}
					}
				}
			}
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData) {
			S source = transitionData.getSource();
			S target = transitionData.getTarget();
			E event = transitionData.getEvent();
			Long period = transitionData.getPeriod();

			Trigger<S, E> trigger = null;
			if (event != null) {
				trigger = new EventTrigger<S, E>(event);
			} else if (period != null) {
				TimerTrigger<S, E> t = new TimerTrigger<S, E>(period);
				if (beanFactory != null) {
					t.setBeanFactory(beanFactory);
				}
				trigger = t;
			}

			if (transitionData.getKind() == TransitionKind.EXTERNAL) {
				// TODO can we do this?
				if (stateMap.get(source) == null || stateMap.get(target) == null) {
					continue;
				}
				DefaultExternalTransition<S, E> transition = new DefaultExternalTransition<S, E>(stateMap.get(source),
						stateMap.get(target), transitionData.getActions(), event, transitionData.getGuard(), trigger);
				transitions.add(transition);

			} else if (transitionData.getKind() == TransitionKind.INTERNAL) {
				DefaultInternalTransition<S, E> transition = new DefaultInternalTransition<S, E>(stateMap.get(source),
						transitionData.getActions(), event, transitionData.getGuard(), trigger);
				transitions.add(transition);
			}
		}

		Transition<S, E> initialTransition = new InitialTransition<S, E>(initialState, initialAction);
		StateMachine<S, E> machine = buildStateMachineInternal(states, transitions, initialState, initialTransition,
				null, defaultExtendedState, historyState, contextEvents, beanFactory, taskExecutor, taskScheduler,
				beanName);
		return machine;
	}

	protected abstract StateMachine<S, E> buildStateMachineInternal(Collection<State<S, E>> states,
			Collection<Transition<S, E>> transitions, State<S, E> initialState, Transition<S, E> initialTransition,
			Message<E> initialEvent, ExtendedState extendedState, PseudoState<S, E> historyState,
			Boolean contextEventsEnabled, BeanFactory beanFactory, TaskExecutor taskExecutor,
			TaskScheduler taskScheduler, String beanName);

	protected abstract State<S, E> buildStateInternal(S id, Collection<E> deferred, Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState<S, E> pseudoState);

	private Iterator<Node<StateData<S, E>>> buildStateDataIterator() {
		Tree<StateData<S, E>> tree = new Tree<StateData<S, E>>();

		for (StateData<S, E> stateData : stateMachineStates.getStateDatas()) {
			Object id = stateData.getState();
			Object parent = stateData.getParent();
			tree.add(stateData, id, parent);
		}

		TreeTraverser<Node<StateData<S, E>>> traverser = new TreeTraverser<Node<StateData<S, E>>>() {
		    @Override
		    public Iterable<Node<StateData<S, E>>> children(Node<StateData<S, E>> root) {
		        return root.getChildren();
		    }
		};

		Iterable<Node<StateData<S, E>>> postOrderTraversal = traverser.postOrderTraversal(tree.getRoot());
		Iterator<Node<StateData<S, E>>> iterator = postOrderTraversal.iterator();
		return iterator;
	}

	protected abstract RegionState<S, E> buildRegionStateInternal(S id, Collection<Region<S, E>> regions, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState<S, E> pseudoState);

}
