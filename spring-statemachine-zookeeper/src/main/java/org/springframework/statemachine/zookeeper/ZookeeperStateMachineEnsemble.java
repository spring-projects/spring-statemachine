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
package org.springframework.statemachine.zookeeper;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.ensemble.StateMachineEnsembleObjectSupport;
import org.springframework.statemachine.ensemble.StateMachinePersist;

/**
 * {@link StateMachineEnsemble} backed by a zookeeper.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ZookeeperStateMachineEnsemble<S, E> extends StateMachineEnsembleObjectSupport<S, E> {

	private final static Log log = LogFactory.getLog(ZookeeperStateMachineEnsemble.class);
	private final CuratorFramework curatorClient;
	private final String basePath;
	private final String statePath;
	private final String logPath;
	private final StateMachinePersist<S, E> persist = new KryoStateMachinePersist<S, E>();
	private final AtomicReference<StateWrapper> stateRef = new AtomicReference<StateWrapper>();
	private final CuratorWatcher watcher = new StateWatcher();

	/**
	 * Instantiates a new zookeeper state machine ensemble.
	 *
	 * @param curatorClient the curator client
	 * @param basePath the base zookeeper path
	 */
	public ZookeeperStateMachineEnsemble(CuratorFramework curatorClient, String basePath) {
		this.curatorClient = curatorClient;
		this.basePath = basePath;
		this.statePath = basePath + "/current";
		this.logPath = basePath + "/log";
	}

	@Override
	protected void onInit() throws Exception {
		initPaths();
	}

	@Override
	protected void doStart() {
	}

	@Override
	public void join(StateMachine<S, E> stateMachine) {
		StateWrapper stateWrapper = stateRef.get();
		if (stateWrapper == null) {
			try {
				StateWrapper currentStateWrapper = readCurrentContext();
				stateRef.set(new StateWrapper(currentStateWrapper.context, currentStateWrapper.version));
				stateWrapper = stateRef.get();
			} catch (Exception e) {
				log.error("Error reading current state during join", e);
			}
		}
		notifyJoined(stateWrapper != null ? stateWrapper.context : null);
	}

	@Override
	public void leave(StateMachine<S, E> stateMachine) {
	}

	@Override
	public void setState(StateMachineContext<S, E> context) {
		byte[] data = persist.serialize(context);
		CuratorTransaction tx = curatorClient.inTransaction();
		try {
			Collection<CuratorTransactionResult> results = tx.setData().forPath(statePath, data).and().commit();
			int version = results.iterator().next().getResultStat().getVersion();
			stateRef.set(new StateWrapper(context, version));
		} catch (Exception e) {
			throw new StateMachineException("Error persisting data", e);
		}
	}

	private StateWrapper readCurrentContext() {
		try {
			Stat stat = new Stat();
			byte[] data = curatorClient.getData().storingStatIn(stat).usingWatcher(watcher).forPath(statePath);
			StateMachineContext<S, E> context = persist.deserialize(data);
			return new StateWrapper(context, stat.getVersion());
		} catch (Exception e) {
			throw new StateMachineException("Error reading data", e);
		}
	}

	private void initPaths() {
		try {
			if (curatorClient.checkExists().forPath(statePath) == null) {
				curatorClient.inTransaction()
						.create().forPath(basePath)
						.and()
						.create().forPath(statePath)
						.and()
						.create().forPath(logPath)
						.and()
						.commit();
			}
		} catch (KeeperException.NodeExistsException e) {
			// ignore, already created
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class StateWatcher implements CuratorWatcher {

		@Override
		public void process(WatchedEvent event) throws Exception {
			if (log.isTraceEnabled()) {
				log.trace("Process WatchedEvent: " + event);
			}
			switch (event.getType()) {
			case NodeDataChanged:
				StateWrapper currentStateWrapper = stateRef.get();
				StateWrapper newStateWrapper = readCurrentContext();
				if (log.isTraceEnabled()) {
					log.trace("NodeDataChanged currentStateWrapper=" + currentStateWrapper + " newStateWrapper=" + newStateWrapper);
				}
				if (currentStateWrapper.version + 1 == newStateWrapper.version
						&& stateRef.compareAndSet(currentStateWrapper, newStateWrapper)) {
					if (log.isTraceEnabled()) {
						log.trace("Notify state change with new context");
					}
					notifyStateChanged(newStateWrapper.context);
				}
				break;
			default:
				curatorClient.checkExists().usingWatcher(this).forPath(statePath);
				break;
			}
		}

	}

	/**
	 * Wrapper object for a {@link StateMachineContext}.
	 */
	private class StateWrapper {
		private final StateMachineContext<S, E> context;
		private final int version;

		public StateWrapper(StateMachineContext<S, E> context, int version) {
			this.context = context;
			this.version = version;
		}

		@Override
		public String toString() {
			return "StateWrapper [context=" + context + ", version=" + version + "]";
		}
	}

}
