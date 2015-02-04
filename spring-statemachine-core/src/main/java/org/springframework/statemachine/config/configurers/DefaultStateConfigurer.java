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
package org.springframework.statemachine.config.configurers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineStateBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineStates.StateData;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;

public class DefaultStateConfigurer<S, E>
		extends AnnotationConfigurerAdapter<StateMachineStates<S, E>, StateMachineStateConfigurer<S, E>, StateMachineStateBuilder<S, E>>
		implements StateConfigurer<S, E> {

	private final Collection<StateData<S, E>> states = new ArrayList<StateData<S, E>>();

	private S initial;

	@Override
	public void configure(StateMachineStateBuilder<S, E> builder) throws Exception {
		builder.add(states);
		builder.setInitialState(initial);
	}

	@Override
	public StateConfigurer<S, E> initial(S initial) {
		this.initial = initial;
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state) {
		return state(state, (E[])null);
	}
	
	@Override
	public StateConfigurer<S, E> state(S state, Collection<Action> entryActions, Collection<Action> exitActions) {
		states.add(new StateData<S, E>(state, null, entryActions, exitActions));
		return this;
	}

	@Override
	public StateConfigurer<S, E> state(S state, E... deferred) {
		states.add(new StateData<S, E>(state, deferred));
		return this;
	}

	@Override
	public StateConfigurer<S, E> states(Set<S> states) {
		for (S s : states) {
			state(s);
		}
		return this;
	}

}
