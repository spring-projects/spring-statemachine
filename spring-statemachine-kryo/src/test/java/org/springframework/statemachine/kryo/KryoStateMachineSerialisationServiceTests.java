/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.statemachine.kryo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

/**
 * Tests for {@link KryoStateMachineSerialisationService}.
 *
 * @author Janne Valkealahti
 *
 */
public class KryoStateMachineSerialisationServiceTests {

	@Test
	public void testContextWithChilds() throws Exception {
		StateMachineContext<String, String> child1 = new DefaultStateMachineContext<String, String>("child1", null, null,
				new DefaultExtendedState());
		StateMachineContext<String, String> child2 = new DefaultStateMachineContext<String, String>("child2", null, null,
				new DefaultExtendedState());
		List<StateMachineContext<String, String>> childs = new ArrayList<>();
		childs.add(child1);
		childs.add(child2);
		StateMachineContext<String, String> root = new DefaultStateMachineContext<String, String>(childs, "root", null,
				null, new DefaultExtendedState());

		KryoStateMachineSerialisationService<String, String> service = new KryoStateMachineSerialisationService<>();
		byte[] bytes = service.serialiseStateMachineContext(root);

		StateMachineContext<String, String> context = service.deserialiseStateMachineContext(bytes);
		assertThat(context.getChilds().size(), is(2));
	}
}
