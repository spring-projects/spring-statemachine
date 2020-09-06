/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.statemachine.test.assertj;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.springframework.statemachine.StateMachine;

/**
 * Assertions applicable to a {@link StateMachine}.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineAssert extends AbstractAssert<StateMachineAssert, StateMachine<?, ?>> {

	/**
	 * Instantiates a new state machine assert.
	 *
	 * @param actual the actual
	 */
	public StateMachineAssert(StateMachine<?, ?> actual) {
		super(actual, StateMachineAssert.class);
	}

	/**
	 * Verifies that the actual machine has the same {@code state id} as given {@code id}.
	 *
	 * @param id the expected state id
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the target id of the actual context is not equal to the given one.
	 */
	public StateMachineAssert hasStateId(Object id) {
		isNotNull();
		if (actual.getState() == null) {
			failWithMessage("Expected machine's state to be not null");
		}
		if (!Objects.deepEquals(actual.getState().getId(), id)) {
			failWithMessage("Expected machine's state id to be <%s> but was <%s>", id, actual.getState().getId());
		}
		return this;
	}

	/**
	 * Verifies that the actual machine does not have a state.
	 *
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the machine has a state
	 */
	public StateMachineAssert doesNotHaveState() {
		isNotNull();
		if (actual.getState() != null) {
			failWithMessage("Expected machine's state to be null but was <%s>", actual.getState());
		}
		return this;
	}
}
