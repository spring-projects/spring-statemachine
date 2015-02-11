/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.support;

import java.util.Collection;

/**
 * Various utility methods for state machine.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class StateMachineUtils {

	/**
	 * Checks if right hand collection has atleast one same item as left hand
	 * collection.
	 *
	 * @param <S> the generic type
	 * @param left the left collection
	 * @param right the right collection
	 * @return true, right contains at least one item from left, false otherwise.
	 */
	public static <S> boolean containsAtleastOne(Collection<S> left, Collection<S> right) {
		if (left == null || right == null) {
			return false;
		}
		for (S id : left) {
			if (right.contains(id)) {
				return true;
			}
		}
		return false;
	}

}
