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
package org.springframework.statemachine.transition;

import java.util.Collection;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.trigger.Trigger;

public class DefaultInternalTransition<S, E> extends AbstractInternalTransition<S, E> {

	public DefaultInternalTransition(State<S, E> source, Collection<Action<S, E>> actions, E event, Guard<S, E> guard, Trigger<S, E> trigger) {
		super(source, actions, event, guard, trigger);
	}

}
