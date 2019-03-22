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
package org.springframework.statemachine.support;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionConflictPolicy;

/**
 * {@link Comparator} for {@link Transition}s. This comparator tries to compare
 * transitions simply checking
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
class TransitionComparator<S, E> implements Comparator<Transition<S, E>> {

	private final static Log log = LogFactory.getLog(TransitionComparator.class);
	private final TransitionConflictPolicy transitionConflictPolicy;

	/**
	 * Instantiates a new transition comparator.
	 *
	 * @param transitionConflictPolicy the transition conflict policy
	 */
	public TransitionComparator(TransitionConflictPolicy transitionConflictPolicy) {
		this.transitionConflictPolicy = transitionConflictPolicy == null ? TransitionConflictPolicy.CHILD : transitionConflictPolicy;
	}

	@Override
	public int compare(Transition<S, E> left, Transition<S, E> right) {
		if (log.isTraceEnabled()) {
			log.trace("Compare left='" + left + "' right='" + right +"'");
		}
		if (left == right) {
			return 0;
		} else {
			boolean substate = StateMachineUtils.isSubstate(left.getSource(), right.getSource());
			if (transitionConflictPolicy == TransitionConflictPolicy.CHILD) {
				return substate ? 1 : -1;
			} else {
				return substate ? -1 : 1;
			}
		}
	}

	@Override
	public String toString() {
		return "TransitionComparator [transitionConflightPolicy=" + transitionConflictPolicy + "]";
	}
}
