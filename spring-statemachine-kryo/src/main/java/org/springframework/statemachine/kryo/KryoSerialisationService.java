/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.kryo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Simple service and utility class helping with kryo serialisation.
 *
 * @author Janne Valkealahti
 *
 */
public class KryoSerialisationService {

	// kryo is not a thread safe so using thread local, also
	// adding custom serializer for state machine context.
	private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

		@SuppressWarnings("rawtypes")
		@Override
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
			kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
			kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
			return kryo;
		}
	};

	/**
	 * Serialize state machine context into byte array.
	 *
	 * @param <S> the generic type
	 * @param <E> the element type
	 * @param context the context
	 * @return the byte[]
	 */
	public <S, E> byte[] serializeStateMachineContext(StateMachineContext<S, E> context) {
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeObject(output, context);
		output.close();
		return out.toByteArray();
	}

	/**
	 * Deserialize state machine context from byte array.
	 *
	 * @param <S> the generic type
	 * @param <E> the element type
	 * @param data the data
	 * @return the state machine context
	 */
	@SuppressWarnings("unchecked")
	public <S, E> StateMachineContext<S, E> deserializeStateMachineContext(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Input input = new Input(in);
		return kryo.readObject(input, StateMachineContext.class);
	}
}
