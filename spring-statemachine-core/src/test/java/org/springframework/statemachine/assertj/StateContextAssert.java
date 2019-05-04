/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.assertj;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.Objects;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;

/**
 * Assertions applicable to a {@link StateContext}.
 *
 * @author Janne Valkealahti
 *
 */
public class StateContextAssert extends AbstractAssert<StateContextAssert, StateContext<?, ?>> {

	/**
	 * Instantiates a new state context assert.
	 *
	 * @param actual the actual state context
	 */
	public StateContextAssert(StateContext<?, ?> actual) {
		super(actual, StateContextAssert.class);
	}

	/**
	 * Verifies that the actual context has the same {@link Stage} as given {@link Stage}.
	 *
	 * @param stage the expected stage
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the stage of the actual context is not equal to the given one.
	 */
	public StateContextAssert hasStage(Stage stage) {
		isNotNull();
		if (!Objects.areEqual(actual.getStage(), stage)) {
			failWithMessage("Expected context's stage to be <%s> but was <%s>", stage, actual.getStage());
		}
		return this;
	}

	/**
	 * Verifies that the actual context has the same {@code event} as given {@code event}.
	 *
	 * @param stage the expected stage
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the stage of the actual context is not equal to the given one.
	 */
	public StateContextAssert hasEvent(Object event) {
		isNotNull();
		if (!Objects.areEqual(actual.getEvent(), event)) {
			failWithMessage("Expected context's event to be <%s> but was <%s>", event, actual.getEvent());
		}
		return this;
	}

	/**
	 * Verifies that the actual context has the same {@code source id} as given {@code id}.
	 *
	 * @param id the expected source id
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the source id of the actual context is not equal to the given one.
	 */
	public StateContextAssert hasSourceId(Object id) {
		isNotNull();
		if (actual.getSource() == null) {
			failWithMessage("Expected context's source to be not null");
		}
		if (!Objects.areEqual(actual.getSource().getId(), id)) {
			failWithMessage("Expected context's source id to be <%s> but was <%s>", id, actual.getSource().getId());
		}
		return this;
	}

	/**
	 * Verifies that the actual context does not have a source.
	 *
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the machine has a source
	 */
	public StateContextAssert doesNotHaveSource() {
		isNotNull();
		if (actual.getSource() != null) {
			failWithMessage("Expected context's source to be null but was <%s>", actual.getSource());
		}
		return this;
	}

	/**
	 * Verifies that the actual context has the same {@code target id} as given {@code id}.
	 *
	 * @param id the expected target id
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the target id of the actual context is not equal to the given one.
	 */
	public StateContextAssert hasTargetId(Object id) {
		isNotNull();
		if (actual.getTarget() == null) {
			failWithMessage("Expected context's target to be not null");
		}
		if (!Objects.areEqual(actual.getTarget().getId(), id)) {
			failWithMessage("Expected context's target id to be <%s> but was <%s>", id, actual.getTarget().getId());
		}
		return this;
	}

	/**
	 * Verifies that the actual context does not have a target.
	 *
	 * @return {@code this} assertion object.
	 * @throws AssertionError if the machine has a target
	 */
	public StateContextAssert doesNotHaveTarget() {
		isNotNull();
		if (actual.getTarget() != null) {
			failWithMessage("Expected context's target to be null but was <%s>", actual.getTarget());
		}
		return this;
	}
}
