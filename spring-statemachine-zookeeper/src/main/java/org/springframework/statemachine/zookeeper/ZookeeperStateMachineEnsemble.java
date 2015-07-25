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
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import org.springframework.statemachine.ensemble.StateMachineEnsembleException;
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
	private final AtomicReference<StateWrapper> notifyRef = new AtomicReference<StateWrapper>();
	private final CuratorWatcher watcher = new StateWatcher();
	private PersistentEphemeralNode node;
	private final Queue<StateMachine<S, E>> joinQueue = new ConcurrentLinkedQueue<StateMachine<S, E>>();

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
		setAutoStartup(true);
	}

	@Override
	protected void onInit() throws Exception {
		initPaths();
	}

	@Override
	protected void doStart() {
		// initially setting a watcher here, further watchers
		// will be set when events are received.
		registerWatcherForStatePath();

		StateWrapper stateWrapper = stateRef.get();
		if (stateWrapper == null) {
			try {
				StateWrapper currentStateWrapper = readCurrentContext();
				stateRef.set(currentStateWrapper);
				notifyRef.set(currentStateWrapper);
			} catch (Exception e) {
				log.error("Error reading current state during start", e);
			}
		}
		joinQueued();
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
		if (!isRunning()) {
			joinQueue.add(stateMachine);
		} else {
			StateWrapper stateWrapper = stateRef.get();
			notifyJoined(stateMachine, stateWrapper != null ? stateWrapper.context : null);
		}
	}

	private void joinQueued() {
		StateWrapper stateWrapper = stateRef.get();
		StateMachine<S, E> stateMachine = null;
		while ((stateMachine = joinQueue.poll()) != null) {
			notifyJoined(stateMachine, stateWrapper != null ? stateWrapper.context : null);
		}
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
		notifyLeft(stateMachine, stateWrapper != null ? stateWrapper.context : null);
	}

	@Override
	public synchronized void setState(StateMachineContext<S, E> context) {
		if (log.isDebugEnabled()) {
			log.debug("Setting state context=" + context);
		}
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

	@Override
	public StateMachineContext<S, E> getState() {
		return readCurrentContext().context;
	}

	private StateWrapper readCurrentContext() {
		try {
			Stat stat = new Stat();
			registerWatcherForStatePath();
			StateMachineContext<S, E> context = persist.read(stat);
			return new StateWrapper(context, stat.getVersion());
		} catch (Exception e) {
			throw new StateMachineException("Error reading data", e);
		}
	}

	/**
	 * Create all needed paths including what ZookeeperStateMachinePersist
	 * is going to need because it doesn't handle any path creation. We also
	 * use a mutex lock to make a decision if cleanState is enabled to wipe
	 * out existing data.
	 */
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

	/**
	 * Register existing {@link CuratorWatcher} for a state path.
	 */
	protected void registerWatcherForStatePath() {
		try {
			if (curatorClient.getState() != CuratorFrameworkState.STOPPED) {
				curatorClient.checkExists().usingWatcher(watcher).forPath(statePath);
			}
		} catch (Exception e) {
			log.warn("Registering wacher for path " + statePath + " threw error", e);
		}
	}

	private void mayNotifyStateChanged(StateWrapper wrapper) {
		StateWrapper notifyWrapper = notifyRef.get();
		if (notifyWrapper == null) {
			notifyRef.set(wrapper);
			notifyStateChanged(wrapper.context);
		} else if (wrapper.version > notifyWrapper.version) {
			notifyRef.set(wrapper);
			notifyStateChanged(wrapper.context);
		}
	}

	private void traceLogWrappers(StateWrapper currentWrapper, StateWrapper notifyWrapper, StateWrapper newWrapper) {
		if (log.isTraceEnabled()) {
			log.trace("Wrappers \ncurrentWrapper=[" + currentWrapper + "] \nnotifyWrapper=[" + notifyWrapper
					+ "] \nnewWrapper=[" + newWrapper + "]");
		}
	}

	private class StateWatcher implements CuratorWatcher {

		// zk is not really reliable for watching events because
		// you need to re-register watcher when it fires. most likely
		// we will miss events so need to do little tricks here via
		// event logs.

		// NOTE: because paths are pre-created, version always start
		//       from 1 when real data is set. initial path contains
		//       empty data with version 0.

		@Override
		public void process(WatchedEvent event) throws Exception {
			if (log.isTraceEnabled()) {
				log.trace("Process WatchedEvent: " + event);
			}
			switch (event.getType()) {
			case NodeDataChanged:
				try {
					StateWrapper currentWrapper = stateRef.get();
					StateWrapper notifyWrapper = notifyRef.get();
					StateWrapper newWrapper = readCurrentContext();
					traceLogWrappers(currentWrapper, notifyWrapper, newWrapper);

					if (currentWrapper.version + 1 == newWrapper.version
							&& stateRef.compareAndSet(currentWrapper, newWrapper)) {
						mayNotifyStateChanged(newWrapper);
					} else {
						final int start = (notifyWrapper != null ? (notifyWrapper.version) : 0) % logSize;
						int count = newWrapper.version - (notifyWrapper != null ? (notifyWrapper.version) : 0);
						if (log.isDebugEnabled()) {
							log.debug("Events missed, trying to replay start " + start + " count " + count);
						}
						for (int i = start; i < (start + count); i++) {
							try {
								Stat stat = new Stat();
								StateMachineContext<S, E> context = ((ZookeeperStateMachinePersist<S, E>) persist)
										.readLog(i, stat);
								int ver = (stat.getVersion() - 1) * logSize + (i + 1);

								// check if we're behind more than a log size meaning we can't
								// replay full history, notify and break out from a loop
								if (i + logSize < ver) {
									notifyError(new StateMachineEnsembleException("Current version behind more that log size"));
									break;
								}
								if (log.isDebugEnabled()) {
									log.debug("Replay position " + i + " with version " + ver);
									log.debug("Context in position " + i + " " + context);
								}
								StateWrapper wrapper = new StateWrapper(context, ver);
								mayNotifyStateChanged(wrapper);
							} catch (Exception e) {
								log.error("error reading log", e);
							}
						}
					}
				} catch (Exception e) {
					log.error("Error handling event", e);
				}
				registerWatcherForStatePath();
				break;
			default:
				registerWatcherForStatePath();
				break;
			}
		}

	}

	/**
	 * Wrapper object for a {@link StateMachineContext} and its
	 * current version.
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
