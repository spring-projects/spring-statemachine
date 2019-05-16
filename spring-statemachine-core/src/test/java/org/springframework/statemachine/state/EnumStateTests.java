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
package org.springframework.statemachine.state;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.statemachine.AbstractStateMachineTests.TestEvents;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;

public class EnumStateTests {

	@Test
	public void testSimpleEnumState() {
		State<TestStates,TestEvents> stateSI = new EnumState<TestStates,TestEvents>(TestStates.SI);
		assertThat(stateSI.isSimple(), is(true));
		assertThat(stateSI.isComposite(), is(false));
		assertThat(stateSI.isOrthogonal(), is(false));
		assertThat(stateSI.isSubmachineState(), is(false));
	}

}
