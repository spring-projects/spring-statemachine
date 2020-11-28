/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.statemachine.listener;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.TestUtils;

import java.util.List;

public class CompositeStateMachineListenerTests {

	@Test
	public void testRegister() throws Exception {
		CompositeStateMachineListener<String, String> listener = new CompositeStateMachineListener<String, String>();
		StateMachineListenerAdapter<String, String> adapter1 = new StateMachineListenerAdapter<String, String>();
		listener.register(adapter1);
		OrderedComposite<StateMachineListener<String, String>> listeners = listener.getListeners();
		List<Object> list = TestUtils.readField("list", listeners);
		assertThat(list).hasSize(1);
	}

	@Test
	public void testUnregister() throws Exception {
		CompositeStateMachineListener<String, String> listener = new CompositeStateMachineListener<String, String>();
		StateMachineListenerAdapter<String, String> adapter1 = new StateMachineListenerAdapter<String, String>();
		listener.register(adapter1);
		listener.unregister(adapter1);
		OrderedComposite<StateMachineListener<String, String>> listeners = listener.getListeners();
		List<Object> list = TestUtils.readField("list", listeners);
		assertThat(list).isEmpty();
	}

}
