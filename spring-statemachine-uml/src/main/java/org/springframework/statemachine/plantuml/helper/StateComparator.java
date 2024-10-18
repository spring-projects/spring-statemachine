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

package org.springframework.statemachine.plantuml.helper;

import org.springframework.statemachine.state.State;

import java.util.Comparator;

public class StateComparator<S, E> implements Comparator<State<S, E>> {
	@Override
	public int compare(State<S, E> state1, State<S, E> state2) {
		return state1.getId().toString().compareTo(state2.getId().toString());
	}
}
