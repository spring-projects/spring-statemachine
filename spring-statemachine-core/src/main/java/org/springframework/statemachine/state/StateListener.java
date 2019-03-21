/*
 * Copyright 2016-2018 the original author or authors.
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
package org.springframework.statemachine.state;

import org.springframework.statemachine.StateContext;

/**
 * {@code StateListener} for various state events.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateListener<S, E> {

	/**
	 * Called when {@link State} want to notify of its entry.
	 *
	 * @param context the state context
	 */
	void onEntry(StateContext<S, E> context);

	/**
	 * Called when {@link State} want to notify of its exit.
	 *
	 * @param context the state context
	 */
	void onExit(StateContext<S, E> context);

	/**
	 * Called when {@link State} want to notify of its completion.
	 *
	 * @param context the state context
	 */
	void onComplete(StateContext<S, E> context);
}
