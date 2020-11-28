/*
 * Copyright 2018-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Tests for {@link StateMachineContextSerializer}.
 * <p>
 * When StateMachineContextSerializer structure is changes, copy previous
 * version here and test raw bytes from old serializer against new one and other
 * combinations as needed.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineContextSerializerTests {

	@Test
	public void testContextWithChilds() {
		Kryo kryo = new Kryo();
		StateMachineContextSerializer<String, String> serializer = new StateMachineContextSerializer<>();
		kryo.addDefaultSerializer(StateMachineContext.class, serializer);

		StateMachineContext<String, String> child = new DefaultStateMachineContext<String, String>(new ArrayList<>(), "child", "event1",
				new HashMap<String, Object>(), new DefaultExtendedState());
		List<StateMachineContext<String, String>> childs = new ArrayList<>();
		childs.add(child);
		StateMachineContext<String, String> root = new DefaultStateMachineContext<String, String>(childs, "root", "event2",
				new HashMap<String, Object>(), new DefaultExtendedState());

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		Output output = new Output(outStream);
		kryo.writeClassAndObject(output, root);
		output.flush();

		Input input = new Input(new ByteArrayInputStream(outStream.toByteArray()));
		kryo.readClassAndObject(input);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testContextFromInitialVersionToCurrent() {
		// test added for PR #722
		// as a time writing this test, we had two version, initial(V1)
		// and current(V2). raw bytes from V1 to V2.
		Kryo kryoFrom = new Kryo();
		Kryo kryoTo = new Kryo();

		StateMachineContextSerializerV1<String, String> serializerV1 = new StateMachineContextSerializerV1<>();
		kryoFrom.addDefaultSerializer(StateMachineContext.class, serializerV1);

		StateMachineContext<String, String> childFrom = new DefaultStateMachineContext<String, String>("child", "event1",
				new HashMap<String, Object>(), new DefaultExtendedState());
		List<StateMachineContext<String, String>> childsFrom = new ArrayList<>();
		childsFrom.add(childFrom);
		StateMachineContext<String, String> rootFrom = new DefaultStateMachineContext<String, String>("root", "event2",
				new HashMap<String, Object>(), new DefaultExtendedState());

		ByteArrayOutputStream outStreamFrom = new ByteArrayOutputStream();
		Output outputFrom = new Output(outStreamFrom);
		kryoFrom.writeClassAndObject(outputFrom, rootFrom);
		outputFrom.flush();

		StateMachineContextSerializer<String, String> serializerCurrent = new StateMachineContextSerializer<>();
		kryoTo.addDefaultSerializer(StateMachineContext.class, serializerCurrent);

		Input inputTo = new Input(new ByteArrayInputStream(outStreamFrom.toByteArray()));
		StateMachineContext<String, String> rootTo = (StateMachineContext<String, String>) kryoTo.readClassAndObject(inputTo);
		assertThat(rootFrom).isEqualTo(rootTo);
	}

	/**
	 * Initial implementation of a StateMachineContextSerializer which is used to
	 * test read to current version.
	 */
	private static class StateMachineContextSerializerV1<S, E> extends Serializer<StateMachineContext<S, E>> {

		@Override
		public void write(Kryo kryo, Output output, StateMachineContext<S, E> context) {
			kryo.writeClassAndObject(output, context.getEvent());
			kryo.writeClassAndObject(output, context.getState());
			kryo.writeClassAndObject(output, context.getEventHeaders());
			kryo.writeClassAndObject(output, context.getExtendedState() != null ? context.getExtendedState().getVariables() : null);
			kryo.writeClassAndObject(output, context.getChilds());
			kryo.writeClassAndObject(output, context.getHistoryStates());
			kryo.writeClassAndObject(output, context.getId());
		}

		@SuppressWarnings("unchecked")
		@Override
		public StateMachineContext<S, E> read(Kryo kryo, Input input, Class<StateMachineContext<S, E>> clazz) {
			E event = (E) kryo.readClassAndObject(input);
			S state = (S) kryo.readClassAndObject(input);
			Map<String, Object> eventHeaders = (Map<String, Object>) kryo.readClassAndObject(input);
			Map<Object, Object> variables = (Map<Object, Object>) kryo.readClassAndObject(input);
			List<StateMachineContext<S, E>> childs = (List<StateMachineContext<S, E>>) kryo.readClassAndObject(input);
			Map<S, S> historyStates = (Map<S, S>) kryo.readClassAndObject(input);
			String id = (String) kryo.readClassAndObject(input);
			return new DefaultStateMachineContext<S, E>(childs, state, event, eventHeaders,
					new DefaultExtendedState(variables), historyStates, id);
		}
	}
}
