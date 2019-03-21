/*
 * Copyright 2017 the original author or authors.
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

import java.util.UUID;

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.service.StateMachineSerialisationService;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Implementation for {@link StateMachineSerialisationService} using kryo.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class KryoStateMachineSerialisationService<S, E> extends AbstractKryoStateMachineSerialisationService<S, E> {

	@Override
	protected void doEncode(Kryo kryo, Object object, Output output) {
		kryo.writeObject(output, object);
	}

	@Override
	protected <T> T doDecode(Kryo kryo, Input input, Class<T> type) {
		return kryo.readObject(input, type);
	}

	@Override
	protected void configureKryoInstance(Kryo kryo) {
		kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer<S, E>());
		kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
		kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
	}
}
