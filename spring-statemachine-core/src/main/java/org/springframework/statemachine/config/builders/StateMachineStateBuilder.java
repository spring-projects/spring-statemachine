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
package org.springframework.statemachine.config.builders;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.statemachine.config.builders.StateMachineStates.StateData;
import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.statemachine.config.configurers.DefaultStateConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;

public class StateMachineStateBuilder<S, E>
		extends AbstractConfiguredAnnotationBuilder<StateMachineStates<S, E>, StateMachineStateConfigurer<S, E>, StateMachineStateBuilder<S, E>>
		implements StateMachineStateConfigurer<S, E> {

	private Collection<StateData<S, E>> states = new ArrayList<StateData<S, E>>();
	private S initialState;
	private S endState;

	public StateMachineStateBuilder() {
		super();
	}

	public StateMachineStateBuilder(ObjectPostProcessor<Object> objectPostProcessor,
			boolean allowConfigurersOfSameType) {
		super(objectPostProcessor, allowConfigurersOfSameType);
	}

	public StateMachineStateBuilder(ObjectPostProcessor<Object> objectPostProcessor) {
		super(objectPostProcessor);
	}

	@Override
	protected StateMachineStates<S, E> performBuild() throws Exception {
		StateMachineStates<S, E> bean = new StateMachineStates<S, E>(initialState, endState, states);
		return bean;
	}

	@Override
	public StateConfigurer<S, E> withStates() throws Exception {
		return apply(new DefaultStateConfigurer<S, E>());
	}

	public void add(Collection<StateData<S, E>> states) {
		this.states.addAll(states);
	}

	public void setInitialState(S state) {
		this.initialState = state;
	}
	
	public void setEndState(S endState) {
		this.endState = endState;
	}

}
