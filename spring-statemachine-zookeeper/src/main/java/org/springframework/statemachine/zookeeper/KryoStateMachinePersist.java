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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.ensemble.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * {@link StateMachinePersist} using kroy libraries as a backing
 * serialization technique.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class KryoStateMachinePersist<S, E> implements StateMachinePersist<S, E> {

	private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

		@SuppressWarnings("rawtypes")
		@Override
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
			return kryo;
		}
	};

	@Override
	public byte[] serialize(StateMachineContext<S, E> context) {
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeObject(output, context);
		output.close();
		return out.toByteArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public StateMachineContext<S, E> deserialize(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Input input = new Input(in);
		return kryo.readObject(input, StateMachineContext.class);
	}

	private static class StateMachineContextSerializer<S, E> extends Serializer<StateMachineContext<S, E>> {

		@Override
		public void write(Kryo kryo, Output output, StateMachineContext<S, E> context) {
			kryo.writeClassAndObject(output, context.getEvent());
			kryo.writeClassAndObject(output, context.getState());
		}

		@SuppressWarnings("unchecked")
		@Override
		public StateMachineContext<S, E> read(Kryo kryo, Input input, Class<StateMachineContext<S, E>> clazz) {
			E event = (E) kryo.readClassAndObject(input);
			S state = (S) kryo.readClassAndObject(input);
			return new DefaultStateMachineContext<S, E>(null, state, event, null, null);
		}

	}

}
