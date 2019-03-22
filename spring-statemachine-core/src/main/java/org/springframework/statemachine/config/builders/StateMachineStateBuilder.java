/*
 * Copyright 2015-2016 the original author or authors.
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
package org.springframework.statemachine.config.builders;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.statemachine.config.configurers.DefaultStateConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StatesData;

/**
 * {@link AnnotationBuilder} for {@link StatesData}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineStateBuilder<S, E>
		extends AbstractConfiguredAnnotationBuilder<StatesData<S, E>, StateMachineStateConfigurer<S, E>, StateMachineStateBuilder<S, E>>
		implements StateMachineStateConfigurer<S, E> {

	private final Collection<StateData<S, E>> stateDatas = new ArrayList<StateData<S,E>>();

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
	protected StatesData<S, E> performBuild() throws Exception {
		return new StatesData<S, E>(stateDatas);
	}

	@Override
	public StateConfigurer<S, E> withStates() throws Exception {
		return apply(new DefaultStateConfigurer<S, E>());
	}

	public void addStateData(Collection<StateData<S, E>> stateDatas) {
		this.stateDatas.addAll(stateDatas);
	}

}
