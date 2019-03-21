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
package org.springframework.statemachine.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.statemachine.config.model.ChoiceData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.EntryData;
import org.springframework.statemachine.config.model.ExitData;
import org.springframework.statemachine.config.model.HistoryData;
import org.springframework.statemachine.config.model.JunctionData;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.config.model.verifier.CompositeStateMachineModelVerifier;
import org.springframework.statemachine.config.model.verifier.StateMachineModelVerifier;
import org.springframework.statemachine.ensemble.DistributedStateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.security.StateMachineSecurityInterceptor;
import org.springframework.statemachine.state.AbstractState;
import org.springframework.statemachine.state.ChoicePseudoState;
import org.springframework.statemachine.state.ChoicePseudoState.ChoiceStateData;
import org.springframework.statemachine.state.DefaultPseudoState;
import org.springframework.statemachine.state.EntryPseudoState;
import org.springframework.statemachine.state.ExitPseudoState;
import org.springframework.statemachine.state.ForkPseudoState;
import org.springframework.statemachine.state.HistoryPseudoState;
import org.springframework.statemachine.state.JoinPseudoState;
import org.springframework.statemachine.state.JoinPseudoState.JoinStateData;
import org.springframework.statemachine.state.JunctionPseudoState;
import org.springframework.statemachine.state.JunctionPseudoState.JunctionStateData;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.RegionState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.state.StateHolder;
import org.springframework.statemachine.state.StateMachineState;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.support.tree.Tree;
import org.springframework.statemachine.support.tree.Tree.Node;
import org.springframework.statemachine.support.tree.TreeTraverser;
import org.springframework.statemachine.transition.DefaultExternalTransition;
import org.springframework.statemachine.transition.DefaultInternalTransition;
import org.springframework.statemachine.transition.DefaultLocalTransition;
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

	private final Log log = LogFactory.getLog(AbstractStateMachineFactory.class);

	private final StateMachineModel<S, E> defaultStateMachineModel;

	private final StateMachineModelFactory<S, E> stateMachineModelFactory;

	private Boolean contextEvents;

	private boolean handleAutostartup = false;

	private String beanName;

	private StateMachineMonitor<S, E> defaultStateMachineMonitor;

	/**
	 * Instantiates a new abstract state machine factory.
	 *
	 * @param defaultStateMachineModel the default state machine model
	 * @param stateMachineModelFactory the state machine model factory
	 */
	public AbstractStateMachineFactory(StateMachineModel<S, E> defaultStateMachineModel, StateMachineModelFactory<S, E> stateMachineModelFactory) {
		this.stateMachineModelFactory = stateMachineModelFactory;
		this.defaultStateMachineModel = defaultStateMachineModel;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public StateMachine<S, E> getStateMachine() {
		return getStateMachine(null, null);
	}

	@Override
	public StateMachine<S, E> getStateMachine(String machineId) {
		return getStateMachine(null, machineId);
	}

	@Override
	public StateMachine<S, E> getStateMachine(UUID uuid) {
		return getStateMachine(uuid, null);
	}

	/**
	 * Main constructor that create a {@link StateMachine}.
	 *
	 * @param uuid for internal usage. Can be null, in that case a random one will be generated.
	 * @param machineId represent a user Id, up to you to set what you want.
	 * @return a {@link StateMachine}
	 */
	@SuppressWarnings("unchecked")
	public StateMachine<S, E> getStateMachine(UUID uuid, String machineId) {
		ArrayList<StateMachine<S, E>> machines = new ArrayList<>();

		StateMachineModel<S, E> stateMachineModel = resolveStateMachineModel(machineId);
		if (stateMachineModel.getConfigurationData().isVerifierEnabled()) {
			StateMachineModelVerifier<S, E> verifier = stateMachineModel.getConfigurationData().getVerifier();
			if (verifier == null) {
				verifier = new CompositeStateMachineModelVerifier<S, E>();
			}
			verifier.verify(stateMachineModel);
		}

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
		List<HolderListItem<S, E>> holderList = new ArrayList<>();

		Iterator<Node<StateData<S, E>>> iterator = buildStateDataIterator(stateMachineModel);
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
			Collection<TransitionData<S, E>> transitionsData = getTransitionData(iterator.hasNext(), stateDatas, stateMachineModel);

			if (initialCount > 1) {
				for (Collection<StateData<S, E>> regionStateDatas : regionsStateDatas) {
					// try to build reqion id's
					Object rId = regionStateDatas.iterator().next().getRegion();
					String mId = machineId != null ? machineId : stateMachineModel.getConfigurationData().getMachineId();
					mId = mId + "#" + (rId != null ? rId.toString() : "");

					machine = buildMachine(machineMap, stateMap, holderList, regionStateDatas, transitionsData, resolveBeanFactory(stateMachineModel),
							contextEvents, defaultExtendedState, stateMachineModel.getTransitionsData(), resolveTaskExecutor(stateMachineModel),
							resolveTaskScheduler(stateMachineModel), mId, null, stateMachineModel);
					regionStack.push(new MachineStackItem<S, E>(machine));
					machines.add(machine);
				}

				Collection<Region<S, E>> regions = new ArrayList<Region<S, E>>();
				while (!regionStack.isEmpty()) {
					MachineStackItem<S, E> pop = regionStack.pop();
					regions.add(pop.machine);
				}
				S parent = (S)peek.getParent();
				RegionState<S, E> rstate = buildRegionStateInternal(parent, regions, null,
						stateData != null ? stateData.getEntryActions() : null,
						stateData != null ? stateData.getExitActions() : null,
						new DefaultPseudoState<S, E>(PseudoStateKind.INITIAL), stateMachineModel);
				if (stateData != null) {
					stateMap.put(stateData.getState(), rstate);
				} else {
					// TODO: don't like that we create a last machine here
					Collection<State<S, E>> states = new ArrayList<State<S, E>>();
					states.add(rstate);
					Transition<S, E> initialTransition = new InitialTransition<S, E>(rstate);
					StateMachine<S, E> m = buildStateMachineInternal(states, new ArrayList<Transition<S, E>>(), rstate, initialTransition,
							null, defaultExtendedState, null, contextEvents, resolveBeanFactory(stateMachineModel), resolveTaskExecutor(stateMachineModel),
							resolveTaskScheduler(stateMachineModel), beanName,
							machineId != null ? machineId : stateMachineModel.getConfigurationData().getMachineId(),
							uuid, stateMachineModel);
					machine = m;
					machines.add(m);
				}
			} else {
				machine = buildMachine(machineMap, stateMap, holderList, stateDatas, transitionsData, resolveBeanFactory(stateMachineModel), contextEvents,
						defaultExtendedState, stateMachineModel.getTransitionsData(), resolveTaskExecutor(stateMachineModel), resolveTaskScheduler(stateMachineModel),
						machineId, uuid, stateMachineModel);
				machines.add(machine);
				if (peek.isInitial() || (!peek.isInitial() && !machineMap.containsKey(peek.getParent()))) {
					machineMap.put(peek.getParent(), machine);
				}
			}

			stateStack.push(stateData);
		}

		// setup autostart for top-level machine
		if (machine instanceof LifecycleObjectSupport) {
			((LifecycleObjectSupport)machine).setAutoStartup(stateMachineModel.getConfigurationData().isAutoStart());
		}

		// set top-level machine as relay
		final StateMachine<S, E> fmachine = machine;
		fmachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S, E>>() {

			@Override
			public void apply(StateMachineAccess<S, E> function) {
				function.setRelay(fmachine);
			}
		});

		// add monitoring hooks
		final StateMachineMonitor<S, E> stateMachineMonitor = stateMachineModel.getConfigurationData().getStateMachineMonitor();
		if (stateMachineMonitor != null || defaultStateMachineMonitor != null) {
			fmachine.getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<S ,E>>() {

				@Override
				public void apply(StateMachineAccess<S, E> function) {
					if (defaultStateMachineMonitor != null) {
						function.addStateMachineMonitor(defaultStateMachineMonitor);
					}
					if (stateMachineMonitor != null) {
						function.addStateMachineMonitor(stateMachineMonitor);
					}
				}
			});
		}

		// set parent machines for each built machine
		for (Entry<Object, StateMachine<S, E>> mme : machineMap.entrySet()) {
			StateMachine<S, E> m = null;
			if (mme.getKey() != null) {
				Object sParent = null;
				for (StateData<S, E> sd : stateMachineModel.getStatesData().getStateData()) {
					if (ObjectUtils.nullSafeEquals(sd.getState(), mme.getKey())) {
						sParent = sd.getParent();
						break;
					}
				}
				m = machineMap.get(sParent);
			}
			final StateMachine<S, E> mm = m;
			mme.getValue().getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<S ,E>>(){

				@Override
				public void apply(StateMachineAccess<S, E> function) {
					function.setParentMachine(mm);
				}
			});
		}

		// init built machines
		for (StateMachine<S, E> m : machines) {
			((LifecycleObjectSupport)m).afterPropertiesSet();
		}


		// TODO: should error out if sec is enabled but spring-security is not in cp
		if (stateMachineModel.getConfigurationData().isSecurityEnabled()) {
			final StateMachineSecurityInterceptor<S, E> securityInterceptor = new StateMachineSecurityInterceptor<S, E>(
					stateMachineModel.getConfigurationData().getTransitionSecurityAccessDecisionManager(),
					stateMachineModel.getConfigurationData().getEventSecurityAccessDecisionManager(),
					stateMachineModel.getConfigurationData().getEventSecurityRule());
			log.info("Adding security interceptor " + securityInterceptor);
			fmachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S, E>>() {

				@Override
				public void apply(StateMachineAccess<S, E> function) {
					function.addStateMachineInterceptor(securityInterceptor);
				}
			});
		}

		// setup distributed state machine if needed.
		// we wrap previously build machine with a distributed
		// state machine and set it to use given ensemble.
		if (stateMachineModel.getConfigurationData().getStateMachineEnsemble() != null) {
			DistributedStateMachine<S, E> distributedStateMachine = new DistributedStateMachine<S, E>(
					stateMachineModel.getConfigurationData().getStateMachineEnsemble(), machine);
			distributedStateMachine.setAutoStartup(stateMachineModel.getConfigurationData().isAutoStart());
			distributedStateMachine.afterPropertiesSet();
			machine = distributedStateMachine;
		}

		for (StateMachineListener<S, E> listener : stateMachineModel.getConfigurationData().getStateMachineListeners()) {
			machine.addStateListener(listener);
		}

		List<StateMachineInterceptor<S,E>> interceptors = stateMachineModel.getConfigurationData().getStateMachineInterceptors();
		if (interceptors != null) {
			for (final StateMachineInterceptor<S, E> interceptor : interceptors) {
				// add persisting interceptor hooks to all regions
				RegionPersistingInterceptorAdapter<S, E> adapter = new RegionPersistingInterceptorAdapter<>(interceptor, machine);
				machine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S,E>>() {
					@Override
					public void apply(StateMachineAccess<S, E> function) {
						function.addStateMachineInterceptor(adapter);
					}
				});
			}
		}

		// go through holders and fix state references which
		// were not known at a time holder was created
		for (HolderListItem<S, E> holderItem : holderList) {
			holderItem.value.setState(stateMap.get(holderItem.key));
		}

		return delegateAutoStartup(machine);
	}

	private static class RegionPersistingInterceptorAdapter<S, E> extends StateMachineInterceptorAdapter<S, E> {

		private final StateMachineInterceptor<S, E> interceptor;
		private final StateMachine<S, E> rootStateMachine;

		public RegionPersistingInterceptorAdapter(StateMachineInterceptor<S, E> interceptor, StateMachine<S, E> rootStateMachine) {
			this.interceptor = interceptor;
			this.rootStateMachine = rootStateMachine;
		}

		@Override
		public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
				StateMachine<S, E> stateMachine) {
			interceptor.preStateChange(state, message, transition, stateMachine, rootStateMachine);
		}

		@Override
		public void preStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
				StateMachine<S, E> stateMachine, StateMachine<S, E> rootStateMachine) {
			interceptor.preStateChange(state, message, transition, stateMachine, rootStateMachine);
		}

		@Override
		public void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
				StateMachine<S, E> stateMachine) {
			interceptor.postStateChange(state, message, transition, stateMachine, rootStateMachine);
		}

		@Override
		public void postStateChange(State<S, E> state, Message<E> message, Transition<S, E> transition,
				StateMachine<S, E> stateMachine, StateMachine<S, E> rootStateMachine) {
			interceptor.postStateChange(state, message, transition, stateMachine, rootStateMachine);
		}

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

	/**
	 * Set state machine monitor.
	 *
	 * @param stateMachineMonitor the state machine monitor
	 */
	public void setStateMachineMonitor(StateMachineMonitor<S, E> stateMachineMonitor) {
		this.defaultStateMachineMonitor = stateMachineMonitor;
	}

	private StateMachine<S, E> delegateAutoStartup(StateMachine<S, E> delegate) {
		if (handleAutostartup && delegate instanceof SmartLifecycle && ((SmartLifecycle) delegate).isAutoStartup()) {
			AutostartListener<S, E> autostartListener = new AutostartListener<>();
			delegate.addStateListener(autostartListener);
			((SmartLifecycle)delegate).start();
			try {
				autostartListener.latch.await(30, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.warn("Waited 30 seconds for machine to start as autostart was requested, machine may not be ready");
			} finally {
				delegate.removeStateListener(autostartListener);
			}
		}
		return delegate;
	}

	protected BeanFactory resolveBeanFactory(StateMachineModel<S, E> stateMachineModel) {
		if (stateMachineModel.getConfigurationData().getBeanFactory() != null) {
			return stateMachineModel.getConfigurationData().getBeanFactory();
		} else {
			return getBeanFactory();
		}
	}

	protected TaskExecutor resolveTaskExecutor(StateMachineModel<S, E> stateMachineModel) {
		if (stateMachineModel.getConfigurationData().getTaskExecutor() != null) {
			return stateMachineModel.getConfigurationData().getTaskExecutor();
		} else {
			return getTaskExecutor();
		}
	}

	protected TaskScheduler resolveTaskScheduler(StateMachineModel<S, E> stateMachineModel) {
		if (stateMachineModel.getConfigurationData().getTaskScheduler() != null) {
			return stateMachineModel.getConfigurationData().getTaskScheduler();
		} else {
			return getTaskScheduler();
		}
	}

	protected StateMachineModel<S, E> resolveStateMachineModel(String machineId) {
		if (stateMachineModelFactory == null) {
			return defaultStateMachineModel;
		} else {
			StateMachineModel<S, E> m = stateMachineModelFactory.build(machineId);
			if (m.getConfigurationData() == null) {
				// if model doesn't have explicit configuration data,
				// get it from default model
				return new DefaultStateMachineModel<>(defaultStateMachineModel.getConfigurationData(), m.getStatesData(),
						m.getTransitionsData());
			} else {
				return m;
			}
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

	private Collection<TransitionData<S, E>> getTransitionData(boolean roots, Collection<StateData<S, E>> stateDatas, StateMachineModel<S, E> stateMachineModel) {
		if (roots) {
			return resolveTransitionData(stateMachineModel.getTransitionsData().getTransitions(), stateDatas);
		} else {
			return resolveTransitionData2(stateMachineModel.getTransitionsData().getTransitions());
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


	@SuppressWarnings("unchecked")
	private StateMachine<S, E> buildMachine(Map<Object, StateMachine<S, E>> machineMap, Map<S, State<S, E>> stateMap,
			List<HolderListItem<S, E>> holderList, Collection<StateData<S, E>> stateDatas, Collection<TransitionData<S, E>> transitionsData,
			BeanFactory beanFactory, Boolean contextEvents, DefaultExtendedState defaultExtendedState,
			TransitionsData<S, E> stateMachineTransitions, TaskExecutor taskExecutor, TaskScheduler taskScheduler, String machineId,
			UUID uuid, StateMachineModel<S, E> stateMachineModel) {
		State<S, E> state = null;
		State<S, E> initialState = null;
		PseudoState<S, E> historyState = null;
		Action<S, E> initialAction = null;
		Collection<State<S, E>> states = new ArrayList<State<S,E>>();

		// for now loop twice and build states for
		// non initial/end pseudostates last

		for (StateData<S, E> stateData : stateDatas) {
			StateMachine<S, E> stateMachine = machineMap.get(stateData.getState());
			if (stateMachine == null) {
				// get a submachine from state data if we didn't have
				// it already. stays null if we don't have one.
				stateMachine = stateData.getSubmachine();
				if (stateMachine == null && stateData.getSubmachineFactory() != null) {
					stateMachine = stateData.getSubmachineFactory().getStateMachine(machineId);
				}
			}
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
				StateMachineState<S, E> stateMachineState = new StateMachineState<S, E>(stateData.getState(),
						stateMachine, stateData.getDeferred(), stateData.getEntryActions(), stateData.getExitActions(),
						pseudoState);
				stateMachineState
						.setStateDoActionPolicy(stateMachineModel.getConfigurationData().getStateDoActionPolicy());
				stateMachineState.setStateDoActionPolicyTimeout(
						stateMachineModel.getConfigurationData().getStateDoActionPolicyTimeout());
				state = stateMachineState;

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
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.HISTORY_DEEP) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.JOIN) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.FORK) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.CHOICE) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.JUNCTION) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.ENTRY) {
					continue;
				} else if (stateData.getPseudoStateKind() == PseudoStateKind.EXIT) {
					continue;
				}
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
				if (stateData.isInitial()) {
					initialState = state;
					initialAction = stateData.getInitialAction();
				}
				states.add(state);

			}
			stateMap.put(stateData.getState(), state);
		}

		for (StateData<S, E> stateData : stateDatas) {
			if (stateData.getPseudoStateKind() == PseudoStateKind.HISTORY_SHALLOW) {
				State<S, E> defaultState = null;
				S s = stateData.getState();
				Collection<HistoryData<S,E>> historys = stateMachineTransitions.getHistorys();
				for (HistoryData<S,E> history : historys) {
					if (history.getSource().equals(s)) {
						defaultState = stateMap.get(history.getTarget());
					}
				}
				StateHolder<S, E> defaultStateHolder = new StateHolder<S, E>(defaultState);
				StateHolder<S, E> containingStateHolder = new StateHolder<S, E>(stateMap.get(stateData.getParent()));
				if (containingStateHolder.getState() == null) {
					holderList.add(new HolderListItem<S, E>((S)stateData.getParent(), containingStateHolder));
				}
				PseudoState<S, E> pseudoState = new HistoryPseudoState<S, E>(PseudoStateKind.HISTORY_SHALLOW, defaultStateHolder, containingStateHolder);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
				states.add(state);
				stateMap.put(stateData.getState(), state);
				historyState = pseudoState;
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.HISTORY_DEEP) {
				State<S, E> defaultState = null;
				S s = stateData.getState();
				Collection<HistoryData<S,E>> historys = stateMachineTransitions.getHistorys();
				for (HistoryData<S,E> history : historys) {
					if (history.getSource().equals(s)) {
						defaultState = stateMap.get(history.getTarget());
					}
				}
				StateHolder<S, E> defaultStateHolder = new StateHolder<S, E>(defaultState);
				StateHolder<S, E> containingStateHolder = new StateHolder<S, E>(stateMap.get(stateData.getParent()));
				if (containingStateHolder.getState() == null) {
					holderList.add(new HolderListItem<S, E>((S)stateData.getParent(), containingStateHolder));
				}
				PseudoState<S, E> pseudoState = new HistoryPseudoState<S, E>(PseudoStateKind.HISTORY_DEEP, defaultStateHolder, containingStateHolder);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
				states.add(state);
				stateMap.put(stateData.getState(), state);
				historyState = pseudoState;
			}

			if (stateData.getPseudoStateKind() == PseudoStateKind.CHOICE) {
				S s = stateData.getState();
				List<ChoiceData<S, E>> list = stateMachineTransitions.getChoices().get(s);
				List<ChoiceStateData<S, E>> choices = new ArrayList<ChoiceStateData<S, E>>();
				for (ChoiceData<S, E> c : list) {
					StateHolder<S, E> holder = new StateHolder<S, E>(stateMap.get(c.getTarget()));
					if (holder.getState() == null) {
						holderList.add(new HolderListItem<S, E>(c.getTarget(), holder));
					}
					choices.add(new ChoiceStateData<S, E>(holder, c.getGuard(), c.getActions()));
				}
				PseudoState<S, E> pseudoState = new ChoicePseudoState<S, E>(choices);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
				states.add(state);
				stateMap.put(stateData.getState(), state);
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.JUNCTION) {
				S s = stateData.getState();
				List<JunctionData<S, E>> list = stateMachineTransitions.getJunctions().get(s);
				List<JunctionStateData<S, E>> junctions = new ArrayList<JunctionStateData<S, E>>();
				for (JunctionData<S, E> c : list) {
					StateHolder<S, E> holder = new StateHolder<S, E>(stateMap.get(c.getTarget()));
					if (holder.getState() == null) {
						holderList.add(new HolderListItem<S, E>(c.getTarget(), holder));
					}
					junctions.add(new JunctionStateData<S, E>(holder, c.getGuard(), c.getActions()));
				}
				PseudoState<S, E> pseudoState = new JunctionPseudoState<S, E>(junctions);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
				states.add(state);
				stateMap.put(stateData.getState(), state);
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.ENTRY) {
				S s = stateData.getState();
				Collection<EntryData<S, E>> entrys = stateMachineTransitions.getEntrys();
				for (EntryData<S, E> entry : entrys) {
					if (s.equals(entry.getSource())) {
						PseudoState<S, E> pseudoState = new EntryPseudoState<S, E>(stateMap.get(entry.getTarget()));
						state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
								stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
						states.add(state);
						stateMap.put(stateData.getState(), state);
						break;
					}
				}
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.EXIT) {
				S s = stateData.getState();
				Collection<ExitData<S, E>> exits = stateMachineTransitions.getExits();
				for (ExitData<S, E> entry : exits) {
					if (s.equals(entry.getSource())) {
						StateHolder<S, E> holder = new StateHolder<S, E>(stateMap.get(entry.getTarget()));
						if (holder.getState() == null) {
							holderList.add(new HolderListItem<S, E>(entry.getTarget(), holder));
						}
						PseudoState<S, E> pseudoState = new ExitPseudoState<S, E>(holder);
						state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
								stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
						states.add(state);
						stateMap.put(stateData.getState(), state);
						break;
					}
				}
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.FORK) {
				S s = stateData.getState();
				List<S> list = stateMachineTransitions.getForks().get(s);
				List<State<S, E>> forks = new ArrayList<State<S,E>>();
				for (S fs : list) {
					forks.add(stateMap.get(fs));
				}
				PseudoState<S, E> pseudoState = new ForkPseudoState<S, E>(forks);
				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
				states.add(state);
				stateMap.put(stateData.getState(), state);
			} else if (stateData.getPseudoStateKind() == PseudoStateKind.JOIN) {
				S s = stateData.getState();
				List<S> list = stateMachineTransitions.getJoins().get(s);
				List<List<State<S, E>>> joins = new ArrayList<List<State<S, E>>>();

				// if join source is an orthogonal state, assume we're joining by region end states
				// and actually use list of states from each region to support case where one region
				// defines multiple end states.
				if (list.size() == 1) {
					State<S, E> ss1 = stateMap.get(list.get(0));
					if (ss1 instanceof RegionState) {
						Collection<Region<S, E>> regions = ((RegionState<S, E>)ss1).getRegions();
						for (Region<S, E> r : regions) {
							List<State<S, E>> j = new ArrayList<State<S, E>>();
							Collection<State<S, E>> ss2 = r.getStates();
							for (State<S, E> ss3 : ss2) {
								if (ss3.getPseudoState() != null && ss3.getPseudoState().getKind() == PseudoStateKind.END) {
									j.add(ss3);
								}
							}
							joins.add(j);
						}
					}
				} else {
					for (S fs : list) {
						joins.add(Collections.singletonList(stateMap.get(fs)));
					}
				}

				List<JoinStateData<S, E>> joinTargets = new ArrayList<JoinStateData<S, E>>();
				Collection<TransitionData<S, E>> transitions = stateMachineTransitions.getTransitions();
				for (TransitionData<S, E> tt : transitions) {
					if (tt.getSource() == s) {
						StateHolder<S, E> holder = new StateHolder<S, E>(stateMap.get(tt.getTarget()));
						if (holder.getState() == null) {
							holderList.add(new HolderListItem<S, E>(tt.getTarget(), holder));
						}
						joinTargets.add(new JoinStateData<S, E>(holder, tt.getGuard()));
					}
				}
				JoinPseudoState<S, E> pseudoState = new JoinPseudoState<S, E>(joins, joinTargets);

				state = buildStateInternal(stateData.getState(), stateData.getDeferred(), stateData.getEntryActions(),
						stateData.getExitActions(), stateData.getStateActions(), pseudoState, stateMachineModel);
				states.add(state);
				stateMap.put(stateData.getState(), state);
			}
		}

		Collection<Transition<S, E>> transitions = new ArrayList<Transition<S, E>>();
		for (TransitionData<S, E> transitionData : transitionsData) {
			S source = transitionData.getSource();
			S target = transitionData.getTarget();
			E event = transitionData.getEvent();
			Long period = transitionData.getPeriod();
			Integer count = transitionData.getCount();

			Trigger<S, E> trigger = null;
			if (event != null) {
				trigger = new EventTrigger<S, E>(event);
			} else if (period != null) {
				TimerTrigger<S, E> t = new TimerTrigger<S, E>(period, count != null ? count : 0);
				if (beanFactory != null) {
					t.setBeanFactory(beanFactory);
				}
				if (taskExecutor != null) {
					t.setTaskExecutor(taskExecutor);
				}
				if (taskScheduler != null) {
					t.setTaskScheduler(taskScheduler);
				}
				trigger = t;
				((AbstractState<S, E>)stateMap.get(source)).getTriggers().add(trigger);
			}

			if (transitionData.getKind() == TransitionKind.EXTERNAL) {
				// TODO can we do this?
				if (stateMap.get(source) == null || stateMap.get(target) == null) {
					continue;
				}
				DefaultExternalTransition<S, E> transition = new DefaultExternalTransition<S, E>(stateMap.get(source),
						stateMap.get(target), transitionData.getActions(), event, transitionData.getGuard(), trigger,
						transitionData.getSecurityRule());
				transitions.add(transition);

			} else if (transitionData.getKind() == TransitionKind.LOCAL) {
				// TODO can we do this?
				if (stateMap.get(source) == null || stateMap.get(target) == null) {
					continue;
				}
				DefaultLocalTransition<S, E> transition = new DefaultLocalTransition<S, E>(stateMap.get(source),
						stateMap.get(target), transitionData.getActions(), event, transitionData.getGuard(), trigger,
						transitionData.getSecurityRule());
				transitions.add(transition);
			} else if (transitionData.getKind() == TransitionKind.INTERNAL) {
				DefaultInternalTransition<S, E> transition = new DefaultInternalTransition<S, E>(stateMap.get(source),
						transitionData.getActions(), event, transitionData.getGuard(), trigger,
						transitionData.getSecurityRule());
				transitions.add(transition);
			}
		}

		if (stateMachineTransitions.getJoins() != null) {
			for (Entry<S, List<S>> entry : stateMachineTransitions.getJoins().entrySet()) {
				if (stateMap.get(entry.getKey()) != null) {
					List<S> entryList = entry.getValue();
					for (S entryState : entryList) {
						State<S, E> source = stateMap.get(entryState);
						if (source != null && !source.isOrthogonal()) {
							State<S, E> target = stateMap.get(entry.getKey());
							DefaultExternalTransition<S, E> transition = new DefaultExternalTransition<S, E>(
									source, target, null, null, null, null, null);
							transitions.add(transition);
						}
					}
				}
			}
		}

		Transition<S, E> initialTransition = new InitialTransition<S, E>(initialState, initialAction);
		StateMachine<S, E> machine = buildStateMachineInternal(states, transitions, initialState, initialTransition,
				null, defaultExtendedState, historyState, contextEvents, beanFactory, taskExecutor, taskScheduler,
				beanName, machineId != null ? machineId : stateMachineModel.getConfigurationData().getMachineId(), uuid, stateMachineModel);
		return machine;
	}

	protected abstract StateMachine<S, E> buildStateMachineInternal(Collection<State<S, E>> states,
			Collection<Transition<S, E>> transitions, State<S, E> initialState, Transition<S, E> initialTransition, Message<E> initialEvent,
			ExtendedState extendedState, PseudoState<S, E> historyState, Boolean contextEventsEnabled, BeanFactory beanFactory,
			TaskExecutor taskExecutor, TaskScheduler taskScheduler, String beanName, String machineId, UUID uuid,
			StateMachineModel<S, E> stateMachineModel);

	protected abstract State<S, E> buildStateInternal(S id, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			Collection<? extends Action<S, E>> stateActions, PseudoState<S, E> pseudoState, StateMachineModel<S, E> stateMachineModel);

	private Iterator<Node<StateData<S, E>>> buildStateDataIterator(StateMachineModel<S, E> stateMachineModel) {
		Tree<StateData<S, E>> tree = new Tree<StateData<S, E>>();
		treeAdd(tree, stateMachineModel.getStatesData().getStateData());
		return new TreeTraverser<Node<StateData<S, E>>>() {
			@Override
			public Iterable<Node<StateData<S, E>>> children(Node<StateData<S, E>> root) {
				return root.getChildren();
			}
		}.postOrderTraversal(tree.getRoot()).iterator();
	}

	private void treeAdd(Tree<StateData<S, E>> tree, Collection<StateData<S, E>> stateDatas) {
		// recursive call due to possible submachine data ref
		if (stateDatas == null) {
			return;
		}
		for (StateData<S, E> stateData : stateDatas) {
			tree.add(stateData, stateData.getState(), stateData.getParent());
			treeAdd(tree, stateData.getSubmachineStateData());
		}
	}

	protected abstract RegionState<S, E> buildRegionStateInternal(S id, Collection<Region<S, E>> regions, Collection<E> deferred,
			Collection<? extends Action<S, E>> entryActions, Collection<? extends Action<S, E>> exitActions,
			PseudoState<S, E> pseudoState, StateMachineModel<S, E> stateMachineModel);

	/**
	 * Simple utility listener waiting machine to get started if
	 * autostart was requestes. Needed for machine to be ready
	 * if async executor is used.
	 */
	private static class AutostartListener<S, E> extends StateMachineListenerAdapter<S, E> {
		final CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void stateMachineStarted(StateMachine<S, E> stateMachine) {
			latch.countDown();
		}
	}

	private static class HolderListItem<S, E> {
		S key;
		StateHolder<S, E> value;

		public HolderListItem(S key, StateHolder<S, E> value) {
			this.key = key;
			this.value = value;
		}
	}
}
