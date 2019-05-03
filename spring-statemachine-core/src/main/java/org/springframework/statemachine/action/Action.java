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
package org.springframework.statemachine.action;

import org.springframework.statemachine.StateContext;

/**
 * Generic strategy interface used by a state machine to respond
 * events by executing an {@code Action} with a {@link StateContext}.
 * 
 * @author Janne Valkealahti
 * 
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface Action<S, E> {

	/**
	 * Execute action with a {@link StateContext}.
	 *
	 * @param context the state context
	 */
	void execute(StateContext<S, E> context);
}
