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
package org.springframework.statemachine.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

public class DefaultStateMachineContextTests {

	@Test
	public void testEquals() {
		DefaultStateMachineContext<String, String> c1 = new DefaultStateMachineContext<String, String>(null, null, null,
				null);
		DefaultStateMachineContext<String, String> c2 = new DefaultStateMachineContext<String, String>(null, null, null,
				null);
		assertThat(c1.equals(c2), is(true));

		DefaultStateMachineContext<String, String> c0 = new DefaultStateMachineContext<String, String>(null, null, null,
				null);

		c1 = new DefaultStateMachineContext<String, String>(Arrays.asList("x", "y"), Arrays.asList(c0), "s1", "e1",
				new HashMap<>(), new DefaultExtendedState(), new HashMap<>(), "id");
		c2 = new DefaultStateMachineContext<String, String>(Arrays.asList("x", "y"), Arrays.asList(c0), "s1", "e1",
				new HashMap<>(), new DefaultExtendedState(), new HashMap<>(), "id");
		assertThat(c1.equals(c2), is(true));

		c2 = new DefaultStateMachineContext<String, String>(Arrays.asList("d", "y"), Arrays.asList(c0), "s1", "e1",
				new HashMap<>(), new DefaultExtendedState(), new HashMap<>(), "id");
		assertThat(c1.equals(c2), is(false));
	}
}
