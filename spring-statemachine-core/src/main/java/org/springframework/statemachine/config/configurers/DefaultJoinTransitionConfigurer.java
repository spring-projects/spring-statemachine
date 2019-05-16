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
import java.util.Collection;
import java.util.List;

import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;

/**
 * Default implementation of a {@link JoinTransitionConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultJoinTransitionConfigurer<S, E>
		extends	AnnotationConfigurerAdapter<StateMachineTransitions<S, E>, StateMachineTransitionConfigurer<S, E>, StateMachineTransitionBuilder<S, E>>
		implements JoinTransitionConfigurer<S, E> {

	private S target;

	private final List<S> sources = new ArrayList<S>();

	@Override
	public void configure(StateMachineTransitionBuilder<S, E> builder) throws Exception {
		builder.addJoin(target, sources);
	}

	@Override
	public JoinTransitionConfigurer<S, E> source(S source) {
		this.sources.add(source);
		return this;
	}

	@Override
	public JoinTransitionConfigurer<S, E> sources(Collection<S> sources) {
		this.sources.addAll(sources);
		return this;
	}

	@Override
	public JoinTransitionConfigurer<S, E> target(S target) {
		this.target = target;
		return this;
	}

}
