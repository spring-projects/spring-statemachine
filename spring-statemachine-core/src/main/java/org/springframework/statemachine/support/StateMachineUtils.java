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

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineMessageHeaders;
import org.springframework.statemachine.state.PseudoState;
import org.springframework.statemachine.state.PseudoStateKind;
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

	/**
	 * Checks if state is a normal pseudo state, meaning it is a pseudostate
	 * and its kind is not initial or end.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param state the state
	 * @return true, if is normal pseudo state
	 */
	public static <S, E> boolean isNormalPseudoState(State<S, E> state) {
		if (state == null) {
			return false;
		}
		PseudoState<S, E> pseudoState = state.getPseudoState();
		if (pseudoState == null) {
			return false;
		}
		PseudoStateKind kind = pseudoState.getKind();
		if (kind == PseudoStateKind.INITIAL || kind == PseudoStateKind.END) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks if state is a transient pseudo state, meaning it is a pseudostate
	 * and its kind indicates that machine will never stay on this state after
	 * run to completion has finished.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param state the state
	 * @return true, if is normal pseudo state
	 */
	public static <S, E> boolean isTransientPseudoState(State<S, E> state) {
		if (state == null) {
			return false;
		}
		PseudoState<S, E> pseudoState = state.getPseudoState();
		if (pseudoState == null) {
			return false;
		}
		PseudoStateKind kind = pseudoState.getKind();
		if (kind == PseudoStateKind.CHOICE || kind == PseudoStateKind.JUNCTION || kind == PseudoStateKind.ENTRY
				|| kind == PseudoStateKind.EXIT || kind == PseudoStateKind.HISTORY_DEEP
				|| kind == PseudoStateKind.HISTORY_SHALLOW || kind == PseudoStateKind.FORK
				|| kind == PseudoStateKind.JOIN) {
			return true;
		} else {
			return false;
		}
	}

	public static <S, E> boolean isPseudoState(State<S, E> state, PseudoStateKind kind) {
		if (state != null) {
			PseudoState<S, E> pseudoState = state.getPseudoState();
			return pseudoState != null && pseudoState.getKind() == kind;
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
		} else if (object != null) {
			c.add(object.toString());
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

	/**
	 * Gets a particular message header from a {@link StateContext} and parses
	 * it to {@code Long}. Any error parsing header results {@code NULL}.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param context the state context
	 * @return the header as {@code Long} or {@code NULL}
	 */
	public static <S, E> Long getMessageHeaderDoActionTimeout(StateContext<S, E> context) {
		try {
			Number number = context.getMessageHeaders().get(StateMachineMessageHeaders.HEADER_DO_ACTION_TIMEOUT, Number.class);
			if (number != null) {
				return number.longValue();
			}
		} catch (Exception e) {
		}
		return null;
	}
}
