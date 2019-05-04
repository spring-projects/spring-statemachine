/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.assertj;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.statemachine.StateMachineEventResult;
import org.springframework.statemachine.StateMachineEventResult.ResultType;

public class StateMachineEventResultAssertTests {

	@Test
	public void test() {
		StateMachineEventResult<?, ?> mock = mock(StateMachineEventResult.class);
		Mockito.when(mock.getResultType()).thenReturn(ResultType.ACCEPTED);
		StateMachineEventResultAssert assertions = new StateMachineEventResultAssert(mock);

		assertThat(assertions.hasResultType(ResultType.ACCEPTED));

		assertThatExceptionOfType(AssertionError.class)
			.isThrownBy(() -> assertThat(assertions.hasResultType(ResultType.DENIED)))
			.withMessageContaining("Expected result's type to be <DENIED> but was <ACCEPTED>");
	}
}

