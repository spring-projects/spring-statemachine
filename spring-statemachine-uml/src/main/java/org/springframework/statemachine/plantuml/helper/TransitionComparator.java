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

import org.springframework.statemachine.transition.Transition;

import java.util.Comparator;

public class TransitionComparator<S, E> implements Comparator<Transition<S, E>> {

	@Override
	public int compare(Transition<S, E> transition1, Transition<S, E> transition2) {
		// First compare by source
		int compareBySource = transition1.getSource().toString().compareTo(transition2.getSource().toString());
		if (compareBySource != 0) {
			return compareBySource;
		}
		// If sources are equal, then compare by target
		return transition1.getTarget().toString().compareTo(transition2.getTarget().toString());
	}
}
