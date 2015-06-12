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
package org.springframework.statemachine.zookeeper;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.ensemble.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;

public class KryoStateMachinePersistTests {

	@Test
	public void testStateEvent() {
		StateMachinePersist<String, String> persist = new KryoStateMachinePersist<String, String>();
		StateMachineContext<String, String> contextOut =
				new DefaultStateMachineContext<String, String>(null, "S1", "E1", null, null);
		byte[] data = persist.serialize(contextOut);
		StateMachineContext<String, String> contextIn = persist.deserialize(data);

		assertThat(contextOut.getState(), is(contextIn.getState()));
		assertThat(contextOut.getEvent(), is(contextIn.getEvent()));
	}

}
