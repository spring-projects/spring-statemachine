/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.statemachine.action;

import org.springframework.statemachine.StateContext;

/**
 * Action Utilities.
 */
public final class Actions {

	private Actions() {
		// This helper class should not be instantiated.
	}

	/**
	 *
	 * @param <S> represents states Class
	 * @param <E> represents event Class
	 * @return an empty (Noop) Action.
	 */
	public static <S, E> Action<S, E> emptyAction() {
		return new Action<S, E>() {
			@Override
			public void execute(final StateContext<S, E> context) {
				// Nothing to do;
			}
		};
	}
}
