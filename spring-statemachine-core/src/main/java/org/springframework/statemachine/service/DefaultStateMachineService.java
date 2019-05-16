/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.util.Assert;

/**
 * Default implementation of a {@link StateMachineService}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateMachineService<S, E> implements StateMachineService<S, E>, DisposableBean {

	private final static Log log = LogFactory.getLog(DefaultStateMachineService.class);
	private final StateMachineFactory<S, E> stateMachineFactory;
	private final Map<String, StateMachine<S, E>> machines = new HashMap<String, StateMachine<S, E>>();
	private StateMachinePersist<S, E, String> stateMachinePersist;

	/**
	 * Instantiates a new default state machine service.
	 *
	 * @param stateMachineFactory the state machine factory
	 */
	public DefaultStateMachineService(StateMachineFactory<S, E> stateMachineFactory) {
		this(stateMachineFactory, null);
	}

	/**
	 * Instantiates a new default state machine service.
	 *
	 * @param stateMachineFactory the state machine factory
	 * @param stateMachinePersist the state machine persist
	 */
	public DefaultStateMachineService(StateMachineFactory<S, E> stateMachineFactory,
			StateMachinePersist<S, E, String> stateMachinePersist) {
		Assert.notNull(stateMachineFactory, "'stateMachineFactory' must be set");
		this.stateMachineFactory = stateMachineFactory;
		this.stateMachinePersist = stateMachinePersist;
	}

	@Override
	public final void destroy() throws Exception {
		doStop();
	}

	@Override
	public StateMachine<S, E> acquireStateMachine(String machineId) {
		return acquireStateMachine(machineId, true);
	}

	@Override
	public StateMachine<S, E> acquireStateMachine(String machineId, boolean start) {
		log.info("Acquiring machine with id " + machineId);
		synchronized (machines) {
			StateMachine<S,E> stateMachine = machines.get(machineId);
			if (stateMachine == null) {
				log.info("Getting new machine from factory with id " + machineId);
				stateMachine = stateMachineFactory.getStateMachine(machineId);
				if (stateMachinePersist != null) {
					try {
						StateMachineContext<S, E> stateMachineContext = stateMachinePersist.read(machineId);
						stateMachine = restoreStateMachine(stateMachine, stateMachineContext);
					} catch (Exception e) {
						log.error("Error handling context", e);
						throw new StateMachineException("Unable to read context from store", e);
					}
				}
				machines.put(machineId, stateMachine);
			}
			return handleStart(stateMachine, start);
		}
	}

	@Override
	public void releaseStateMachine(String machineId) {
		log.info("Releasing machine with id " + machineId);
		synchronized (machines) {
			StateMachine<S, E> stateMachine = machines.remove(machineId);
			if (stateMachine != null) {
				log.info("Found machine with id " + machineId);
				stateMachine.stop();
			}
		}
	}

	@Override
	public void releaseStateMachine(String machineId, boolean stop) {
		log.info("Releasing machine with id " + machineId);
		synchronized (machines) {
			StateMachine<S, E> stateMachine = machines.remove(machineId);
			if (stateMachine != null) {
				log.info("Found machine with id " + machineId);
				handleStop(stateMachine, stop);
			}
		}
	}

	/**
	 * Sets the state machine persist.
	 *
	 * @param stateMachinePersist the state machine persist
	 */
	public void setStateMachinePersist(StateMachinePersist<S, E, String> stateMachinePersist) {
		this.stateMachinePersist = stateMachinePersist;
	}

	protected void doStop() {
		log.info("Entering stop sequence, stopping all managed machines");
		synchronized (machines) {
			ArrayList<String> machineIds = new ArrayList<>(machines.keySet());
			for (String machineId : machineIds) {
				releaseStateMachine(machineId, true);
			}
		}
	}

	protected StateMachine<S, E> restoreStateMachine(StateMachine<S, E> stateMachine, final StateMachineContext<S, E> stateMachineContext) {
		if (stateMachineContext == null) {
			return stateMachine;
		}
		stateMachine.stop();
		stateMachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<S, E>>() {

			@Override
			public void apply(StateMachineAccess<S, E> function) {
				function.resetStateMachine(stateMachineContext);
			}
		});
		return stateMachine;
	}

	protected StateMachine<S, E> handleStart(StateMachine<S, E> stateMachine, boolean start) {
		if (start) {
			if (!((Lifecycle) stateMachine).isRunning()) {
				StartListener<S, E> listener = new StartListener<>(stateMachine);
				stateMachine.addStateListener(listener);
				stateMachine.start();
				try {
					listener.latch.await();
				} catch (InterruptedException e) {
				}
			}
		}
		return stateMachine;
	}

	protected StateMachine<S, E> handleStop(StateMachine<S, E> stateMachine, boolean stop) {
		if (stop) {
			if (((Lifecycle) stateMachine).isRunning()) {
				StopListener<S, E> listener = new StopListener<>(stateMachine);
				stateMachine.addStateListener(listener);
				stateMachine.stop();
				try {
					listener.latch.await();
				} catch (InterruptedException e) {
				}
			}
		}
		return stateMachine;
	}

	private static class StartListener<S, E> extends StateMachineListenerAdapter<S, E> {

		final CountDownLatch latch = new CountDownLatch(1);
		final StateMachine<S, E> stateMachine;

		public StartListener(StateMachine<S, E> stateMachine) {
			this.stateMachine = stateMachine;
		}

		@Override
		public void stateMachineStarted(StateMachine<S, E> stateMachine) {
			this.stateMachine.removeStateListener(this);
			latch.countDown();
		}
	}

	private static class StopListener<S, E> extends StateMachineListenerAdapter<S, E> {

		final CountDownLatch latch = new CountDownLatch(1);
		final StateMachine<S, E> stateMachine;

		public StopListener(StateMachine<S, E> stateMachine) {
			this.stateMachine = stateMachine;
		}

		@Override
		public void stateMachineStopped(StateMachine<S, E> stateMachine) {
			this.stateMachine.removeStateListener(this);
			latch.countDown();
		}
	}
}
