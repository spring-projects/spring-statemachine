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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Tests for {@link StateMachineContextSerializer}.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineContextSerializerTests {

	private Kryo kryo;
	private Output output;
	private Input input;

	@Before
	public void setUp() throws Exception {
		kryo = new Kryo();
	}

	@Test
	public void testContextWithChilds() {
		StateMachineContextSerializer<String, String> serializer = new StateMachineContextSerializer<>();
		kryo.addDefaultSerializer(StateMachineContext.class, serializer);

		StateMachineContext<String, String> child = new DefaultStateMachineContext<String, String>(new ArrayList<>(), "child", "event1",
				new HashMap<String, Object>(), new DefaultExtendedState());
		List<StateMachineContext<String, String>> childs = new ArrayList<>();
		childs.add(child);
		StateMachineContext<String, String> root = new DefaultStateMachineContext<String, String>(childs, "root", "event2",
				new HashMap<String, Object>(), new DefaultExtendedState());

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		output = new Output(outStream);
		kryo.writeClassAndObject(output, root);
		output.flush();

		input = new Input(new ByteArrayInputStream(outStream.toByteArray()));
		kryo.readClassAndObject(input);
	}
}
