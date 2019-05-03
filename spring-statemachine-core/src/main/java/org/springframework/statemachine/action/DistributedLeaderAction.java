/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link Action} which is used to wrap execution of an {@link Action}
 * so that only a {@link StateMachine} considered to be a leader in an
 * {@link StateMachineEnsemble} will do the execution.
 *
 * Executing action via {@code DistributedLeaderAction} is bound to if
 * current machine is a leader or not. Effectively this means that if
 * leader doesn't exist, execution is discarded.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DistributedLeaderAction<S, E> implements Action<S, E> {

	private final static Log log = LogFactory.getLog(DistributedLeaderAction.class);
	private final Action<S, E> action;
	private final StateMachineEnsemble<S, E> ensemble;

	/**
	 * Instantiates a new distributed leader action.
	 *
	 * @param action the action
	 * @param ensemble the ensemble
	 */
	public DistributedLeaderAction(Action<S, E> action, StateMachineEnsemble<S, E> ensemble) {
		Assert.notNull(action, "Action must be set");
		Assert.notNull(ensemble, "Ensemble must be set");
		this.action = action;
		this.ensemble = ensemble;
	}

	@Override
	public void execute(StateContext<S, E> context) {
		StateMachine<S, E> left = context.getStateMachine();
		StateMachine<S, E> right = ensemble.getLeader();
		boolean leader = (left != null & right != null) && ObjectUtils.nullSafeEquals(left.getUuid(), right.getUuid());
		if (log.isDebugEnabled()) {
			log.debug("Distibuted action leader status is " + leader);
		}
		if (leader) {
			action.execute(context);
		}
	}
}
