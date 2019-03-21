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
package org.springframework.statemachine.config.configurers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;

/**
 * Default implementation of a {@link ForkTransitionConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultForkTransitionConfigurer<S, E>
		extends	AnnotationConfigurerAdapter<StateMachineTransitions<S, E>, StateMachineTransitionConfigurer<S, E>, StateMachineTransitionBuilder<S, E>>
		implements ForkTransitionConfigurer<S, E> {

	private S source;

	private final List<S> targets = new ArrayList<S>();

	@Override
	public void configure(StateMachineTransitionBuilder<S, E> builder) throws Exception {
		builder.addFork(source, targets);
	}

	@Override
	public ForkTransitionConfigurer<S, E> source(S source) {
		this.source = source;
		return this;
	}

	@Override
	public ForkTransitionConfigurer<S, E> target(S target) {
		targets.add(target);
		return this;
	}

}
