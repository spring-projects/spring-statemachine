/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.statemachine.test.assertj;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;

/**
 * Assertions applicable to a {@link StateMachineEventResult}.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineEventResultAssert
		extends AbstractAssert<StateMachineEventResultAssert, StateMachineEventResult<?, ?>> {

	/**
	 * Instantiates a new state machine event result assert.
	 *
	 * @param actual the actual state machine event result
	 */
	public StateMachineEventResultAssert(StateMachineEventResult<?, ?> actual) {
		super(actual, StateMachineEventResultAssert.class);
	}

	/**
	 * Verifies that the actual event result has the same {@link ResultType} as
	 * given {@link ResultType}.
	 *
	 * @param resultType the expected result type
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the result type of the actual event result is not equal to the given one.
	 */
	public StateMachineEventResultAssert hasResultType(ResultType resultType) {
		isNotNull();
		if (!Objects.deepEquals(actual.getResultType(), resultType)) {
			failWithMessage("Expected result's type to be <%s> but was <%s>", resultType, actual.getResultType());
		}
		return this;
	}
}
