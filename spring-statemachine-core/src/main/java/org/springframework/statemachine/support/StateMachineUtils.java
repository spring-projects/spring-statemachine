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
package org.springframework.statemachine.support;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.statemachine.state.State;
import org.springframework.util.ObjectUtils;

/**
 * Various utility methods for state machine.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class StateMachineUtils {

	/**
	 * Checks if right hand side is a substate of a left hand side.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param left the super state
	 * @param right the sub state
	 * @return if sub is child of super
	 */
	public static <S, E> boolean isSubstate(State<S, E> left, State<S, E> right) {
		if (left == null) {
			return false;
		}
		Collection<State<S, E>> c = left.getStates();
		c.remove(left);
		return c.contains(right);
	}

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

	public static <S> Collection<String> toStringCollection(Collection<S> collection) {
		Collection<String> c = new ArrayList<String>();
		for (S item : collection) {
			c.add(item.toString());
		}
		return c;
	}

	public static Collection<String> toStringCollection(Object object) {
		Collection<String> c = new ArrayList<String>();
		if (ObjectUtils.isArray(object)) {
			for (Object o : ObjectUtils.toObjectArray(object)) {
				c.add(o.toString());
			}
		}
		return c;
	}

	public static boolean containsAtleastOneEqualString(Collection<String> left, String right) {
		Collection<String> r = new ArrayList<String>(1);
		r.add(right);
		return containsAtleastOneEqualString(left, r);
	}

	public static boolean containsAtleastOneEqualString(Collection<String> left, Collection<String> right) {
		if (left == null || right == null) {
			return false;
		}
		for (String id : left) {
			if (right.contains(id)) {
				return true;
			}
		}
		return false;
	}

}
