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
package org.springframework.statemachine.state;

import java.util.Collection;

import org.springframework.statemachine.action.Action;

public class EnumState<S extends Enum<S>, E extends Enum<E>> extends AbstractState<S, E> {

	public EnumState(S id) {
		super(id);
	}

	public EnumState(S id, Collection<E> deferred) {
		super(id, deferred);
	}

	public EnumState(S id, Collection<E> deferred, Collection<Action> entryActions, Collection<Action> exitActions) {
		super(id, deferred, entryActions, exitActions);
	}

	@Override
	public String toString() {
		return "EnumState [getId()=" + getId() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

}
