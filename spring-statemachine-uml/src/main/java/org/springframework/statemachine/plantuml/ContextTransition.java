/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.statemachine.plantuml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.StateContext;

@AllArgsConstructor
@Getter
public class ContextTransition<S, E> {
	final S source;
	final E event;
	final S target;

	public static <S, E> ContextTransition<S, E> of(@Nullable StateContext<S, E> stateContext) {
		if (stateContext != null) {
			return new ContextTransition<>(
					stateContext.getSource() != null
							? stateContext.getSource().getId()
							: null,
					stateContext.getEvent(),
					stateContext.getTarget() != null
							? stateContext.getTarget().getId()
							: null
			);
		}
		return null;
	}
}
