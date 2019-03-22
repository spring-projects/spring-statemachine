/*
 * Copyright 2015-2017 the original author or authors.
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

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.config.model.ChoiceData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.guard.Guard;

/**
 * Default implementation of a {@link ChoiceTransitionConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultChoiceTransitionConfigurer<S, E>
		extends	AnnotationConfigurerAdapter<TransitionsData<S, E>, StateMachineTransitionConfigurer<S, E>, StateMachineTransitionBuilder<S, E>>
		implements ChoiceTransitionConfigurer<S, E> {

	private S source;
	private ChoiceData<S, E> first;
	private final List<ChoiceData<S, E>> thens = new ArrayList<ChoiceData<S, E>>();
	private ChoiceData<S, E> last;

	@Override
	public void configure(StateMachineTransitionBuilder<S, E> builder) throws Exception {
		List<ChoiceData<S, E>> choices = new ArrayList<ChoiceData<S, E>>();
		if (first != null) {
			choices.add(first);
		}
		choices.addAll(thens);
		if (last != null) {
			choices.add(last);
		}
		builder.addChoice(source, choices);
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> source(S source) {
		this.source = source;
		return this;
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> first(S target, Guard<S, E> guard) {
		return first(target, guard, null);
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> first(S target, Guard<S, E> guard, Action<S, E> action) {
		return first(target, guard, action, null);
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> first(S target, Guard<S, E> guard, Action<S, E> action, Action<S, E> error) {
		Collection<Action<S, E>> actions = new ArrayList<>();
		if (action != null) {
			actions.add(error != null ? Actions.errorCallingAction(action, error) : action);
		}
		this.first = new ChoiceData<S, E>(source, target, guard, actions);
		return this;
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> then(S target, Guard<S, E> guard) {
		return then(target, guard, null);
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> then(S target, Guard<S, E> guard, Action<S, E> action) {
		return then(target, guard, action, null);
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> then(S target, Guard<S, E> guard, Action<S, E> action, Action<S, E> error) {
		Collection<Action<S, E>> actions = new ArrayList<>();
		if (action != null) {
			actions.add(error != null ? Actions.errorCallingAction(action, error) : action);
		}
		thens.add(new ChoiceData<S, E>(source, target, guard, actions));
		return this;
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> last(S target) {
		return last(target, null);
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> last(S target, Action<S, E> action) {
		return last(target, action, null);
	}

	@Override
	public ChoiceTransitionConfigurer<S, E> last(S target, Action<S, E> action, Action<S, E> error) {
		Collection<Action<S, E>> actions = new ArrayList<>();
		if (action != null) {
			actions.add(error != null ? Actions.errorCallingAction(action, error) : action);
		}
		this.last = new ChoiceData<S, E>(source, target, null, actions);
		return this;
	}
}
