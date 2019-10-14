/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.statemachine.cluster;

import java.util.HashMap;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.integration.leader.Context;
import org.springframework.integration.leader.DefaultCandidate;
import org.springframework.integration.zookeeper.leader.LeaderInitiator;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.zookeeper.ZookeeperStateMachineEnsemble;

/**
 * {@link StateMachineEnsemble} backed by a zookeeper and leader functionality
 * from a Spring Cloud Cluster.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class LeaderZookeeperStateMachineEnsemble<S, E> extends ZookeeperStateMachineEnsemble<S, E> {

	private final Map<StateMachine<S, E>, InitiatorHolder> holders = new HashMap<>();
	private final CuratorFramework curatorClient;
	private final String basePath;
	private StateMachine<S, E> leader;

	/**
	 * Instantiates a new leader zookeeper state machine ensemble.
	 *
	 * @param curatorClient the curator client
	 * @param basePath the base zookeeper path
	 */
	public LeaderZookeeperStateMachineEnsemble(CuratorFramework curatorClient, String basePath) {
		super(curatorClient, basePath);
		this.curatorClient = curatorClient;
		this.basePath = basePath;
	}

	/**
	 * Instantiates a new leader zookeeper state machine ensemble.
	 *
	 * @param curatorClient the curator client
	 * @param basePath the base zookeeper path
	 * @param cleanState if true clean existing state
	 * @param logSize the log size
	 */
	public LeaderZookeeperStateMachineEnsemble(CuratorFramework curatorClient, String basePath, boolean cleanState, int logSize) {
		super(curatorClient, basePath, cleanState, logSize);
		this.curatorClient = curatorClient;
		this.basePath = basePath;
	}

	@Override
	public void join(StateMachine<S, E> stateMachine) {
		super.join(stateMachine);
		StateMachineCandidate candidate = new StateMachineCandidate(stateMachine);
		LeaderInitiator initiator = new LeaderInitiator(curatorClient, candidate, basePath + "/leader");
		initiator.start();
		holders.put(stateMachine, new InitiatorHolder(candidate, initiator));
	}

	@Override
	public void leave(StateMachine<S, E> stateMachine) {
		super.leave(stateMachine);
		InitiatorHolder holder = holders.get(stateMachine);
		holder.candidate.yieldLeadership();
		holder.initiator.stop();
		holders.remove(stateMachine);
	}

	@Override
	public StateMachine<S, E> getLeader() {
		return leader;
	}

	private class InitiatorHolder {
		final StateMachineCandidate candidate;
		final LeaderInitiator initiator;

		public InitiatorHolder(StateMachineCandidate candidate, LeaderInitiator initiator) {
			this.candidate = candidate;
			this.initiator = initiator;
		}
	}

	private class StateMachineCandidate extends DefaultCandidate {
		final StateMachine<S, E> stateMachine;

		public StateMachineCandidate(StateMachine<S, E> stateMachine) {
			super();
			this.stateMachine = stateMachine;
		}

		@Override
		public void onGranted(Context ctx) {
			super.onGranted(ctx);
			leader = stateMachine;
			notifyGranted(stateMachine);
		}

		@Override
		public void onRevoked(Context ctx) {
			super.onRevoked(ctx);
			leader = null;
			notifyRevoked(stateMachine);
		}
	}
}
