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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.statemachine.config.builders.StateMachineStates.StateData;
import org.springframework.statemachine.config.builders.StateMachineStates.StateMachineStatesData;
import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.statemachine.config.configurers.DefaultStateConfigurer;
import org.springframework.statemachine.config.configurers.DefaultSubStateConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.statemachine.config.configurers.SubStateConfigurer;

/**
 * {@link AnnotationBuilder} for {@link StateMachineStates}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineStateBuilder<S, E>
		extends AbstractConfiguredAnnotationBuilder<StateMachineStates<S, E>, StateMachineStateConfigurer<S, E>, StateMachineStateBuilder<S, E>>
		implements StateMachineStateConfigurer<S, E> {

	private final Map<Object, StateMachineStatesData<S, E>> data = new HashMap<Object, StateMachineStatesData<S,E>>();

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
		return new StateMachineStates<S, E>(data);
	}

	@Override
	public StateConfigurer<S, E> withStates() throws Exception {
		return apply(new DefaultStateConfigurer<S, E>(null, null));
	}

	@Override
	public StateConfigurer<S, E> withStates(Object parent) throws Exception {
		return apply(new DefaultStateConfigurer<S, E>(null, parent));
	}

	@Override
	public SubStateConfigurer<S, E> withSubStates(S state, Object parent) throws Exception {
		return apply(new DefaultSubStateConfigurer<S, E>(state, state, parent));
	}

	public void add(Object id, Object parent, Collection<StateData<S, E>> states, S initialState, S endState) {
		data.put(id, new StateMachineStatesData<S, E>(states, initialState, endState, parent));
	}

}
