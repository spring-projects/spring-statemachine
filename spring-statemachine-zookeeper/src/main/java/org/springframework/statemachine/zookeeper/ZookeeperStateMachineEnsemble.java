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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode.Mode;
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
	private final String uuid = UUID.randomUUID().toString();
	private final static int DEFAULT_LOGSIZE = 32;
	private final static String PATH_CURRENT = "current";
	private final static String PATH_LOG = "log";
	private final static String PATH_MEMBERS = "members";
	private final static String PATH_MUTEX = "mutex";
	private final CuratorFramework curatorClient;
	private final String baseDataPath;
	private final String statePath;
	private final String logPath;
	private final int logSize;
	private final String memberPath;
	private final String mutexPath;
	private final boolean cleanState;
	private final StateMachinePersist<S, E, Stat> persist;
	private final AtomicReference<StateWrapper> stateRef = new AtomicReference<StateWrapper>();
	private final CuratorWatcher watcher = new StateWatcher();
	private PersistentEphemeralNode node;

	/**
	 * Instantiates a new zookeeper state machine ensemble.
	 *
	 * @param curatorClient the curator client
	 * @param basePath the base zookeeper path
	 */
	public ZookeeperStateMachineEnsemble(CuratorFramework curatorClient, String basePath) {
		this(curatorClient, basePath, true, DEFAULT_LOGSIZE);
	}

	/**
	 * Instantiates a new zookeeper state machine ensemble.
	 *
	 * @param curatorClient the curator client
	 * @param basePath the base zookeeper path
	 * @param cleanState if true clean existing state
	 * @param logSize the log size
	 */
	public ZookeeperStateMachineEnsemble(CuratorFramework curatorClient, String basePath, boolean cleanState, int logSize) {
		this.curatorClient = curatorClient;
		this.cleanState = cleanState;
		this.logSize = logSize;
		this.baseDataPath = basePath + "/data";
		this.statePath = baseDataPath + "/" + PATH_CURRENT;
		this.logPath = baseDataPath + "/" + PATH_LOG;
		this.memberPath = basePath + "/" + PATH_MEMBERS;
		this.mutexPath = basePath + "/" + PATH_MUTEX;
		this.persist = new ZookeeperStateMachinePersist<S, E>(curatorClient, statePath, logPath, logSize);
	}

	@Override
	protected void onInit() throws Exception {
		initPaths();
	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
		if (node != null && curatorClient.getState() != CuratorFrameworkState.STOPPED) {
			try {
				node.close();
			} catch (IOException e) {
			} finally {
				node = null;
			}
		}
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
		if (node != null) {
			try {
				node.close();
			} catch (IOException e) {
			}
		}
		StateWrapper stateWrapper = stateRef.get();
		notifyLeft(stateWrapper != null ? stateWrapper.context : null);
	}

	@Override
	public void setState(StateMachineContext<S, E> context) {
		try {
			Stat stat = new Stat();
			StateWrapper stateWrapper = stateRef.get();
			if (stateWrapper != null) {
				stat.setVersion(stateWrapper.version);
			}
			persist.write(context, stat);
			stateRef.set(new StateWrapper(context, stat.getVersion()));
		} catch (Exception e) {
			throw new StateMachineException("Error persisting data", e);
		}
	}

	private StateWrapper readCurrentContext() {
		try {
			Stat stat = new Stat();

			// TODO: not nice that we need to set watcher here when persister is reading data
			curatorClient.getData().usingWatcher(watcher).forPath(statePath);
			StateMachineContext<S, E> context = persist.read(stat);
			return new StateWrapper(context, stat.getVersion());
		} catch (Exception e) {
			throw new StateMachineException("Error reading data", e);
		}
	}

	private void initPaths() {

		InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(curatorClient, mutexPath);
		try {
			if (log.isTraceEnabled()) {
				log.trace("About to acquire mutex");
			}
			mutex.acquire();
			if (log.isTraceEnabled()) {
				log.trace("Mutex acquired");
			}

			if (cleanState) {
				if (curatorClient.checkExists().forPath(memberPath) != null) {
					if (curatorClient.getChildren().forPath(memberPath).size() == 0) {
						log.info("Deleting from " + baseDataPath);
						curatorClient.delete().deletingChildrenIfNeeded().forPath(baseDataPath);
					}
				}
			}

			node = new PersistentEphemeralNode(curatorClient, Mode.EPHEMERAL, memberPath + "/" + uuid, new byte[0]);
			node.start();
			node.waitForInitialCreate(60, TimeUnit.SECONDS);

			if (curatorClient.checkExists().forPath(baseDataPath) == null) {
				CuratorTransaction tx = curatorClient.inTransaction();
				CuratorTransactionFinal tt = tx.create().forPath(baseDataPath).and();
				tt = tt.create().forPath(statePath).and();
				tt = tt.create().forPath(logPath).and();
				for (int i = 0; i<logSize; i++) {
					tt = tt.create().forPath(logPath + "/" + i).and();
				}
				tt.commit();
			}

		} catch (Exception e) {
			log.warn("Error in initPaths", e);
		} finally {
			try {
				mutex.release();
				if (log.isTraceEnabled()) {
					log.trace("Mutex released");
				}
			} catch (Exception e) {
			}
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

				// if we don't have a previous version, we've missed an update.
				// we're going to replay those from log paths.
				if (currentStateWrapper.version + 1 == newStateWrapper.version
						&& stateRef.compareAndSet(currentStateWrapper, newStateWrapper)) {
					if (log.isTraceEnabled()) {
						log.trace("Notify state change with new context");
					}
					notifyStateChanged(newStateWrapper.context);
				} else {

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
