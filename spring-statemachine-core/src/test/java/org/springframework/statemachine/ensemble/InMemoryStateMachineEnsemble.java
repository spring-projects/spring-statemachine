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
package org.springframework.statemachine.ensemble;

import java.util.HashSet;
import java.util.Set;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;

public class InMemoryStateMachineEnsemble<S, E> extends StateMachineEnsembleObjectSupport<S, E> {

	private final Set<StateMachine<S, E>> joined = new HashSet<StateMachine<S,E>>();

	private StateMachineContext<S, E> current;

	@Override
	public void join(StateMachine<S, E> stateMachine) {
		if (!joined.contains(stateMachine)) {
			joined.add(stateMachine);
			notifyJoined(stateMachine, current);
		}
	}

	@Override
	public void leave(StateMachine<S, E> stateMachine) {
		if (joined.remove(stateMachine)) {
			notifyLeft(stateMachine, current);
		}
	}

	@Override
	public void setState(StateMachineContext<S, E> context) {
		current = context;
		notifyStateChanged(context);
	}

	@Override
	public StateMachineContext<S, E> getState() {
		return current;
	}

}
