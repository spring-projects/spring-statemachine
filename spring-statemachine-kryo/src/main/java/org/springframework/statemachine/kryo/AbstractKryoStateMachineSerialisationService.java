/*
 * Copyright 2017-2018 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.service.StateMachineSerialisationService;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoCallback;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

/**
 * Abstract base implementation for {@link StateMachineSerialisationService} using kryo.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public abstract class AbstractKryoStateMachineSerialisationService<S, E> implements StateMachineSerialisationService<S, E> {

	protected final KryoPool pool;

	protected AbstractKryoStateMachineSerialisationService() {
		KryoFactory factory = new KryoFactory() {

			@Override
			public Kryo create() {
				Kryo kryo = new Kryo();
				// kryo is really getting trouble checking things if class loaders
				// doesn't match. for now just use below trick before we try
				// to go fully on beans and get a bean class loader.
				kryo.setClassLoader(ClassUtils.getDefaultClassLoader());
				configureKryoInstance(kryo);
				return kryo;
			}
		};
		this.pool = new KryoPool.Builder(factory).softReferences().build();
	}

	@Override
	public byte[] serialiseStateMachineContext(StateMachineContext<S, E> context) throws Exception {
		return encode(context);
	}

	@SuppressWarnings("unchecked")
	@Override
	public StateMachineContext<S, E> deserialiseStateMachineContext(byte[] data) throws Exception {
		return decode(data, StateMachineContext.class);
	}

	/**
	 * Subclasses implement this method to encode with Kryo.
	 *
	 * @param kryo the Kryo instance
	 * @param object the object to encode
	 * @param output the Kryo Output instance
	 */
	protected abstract void doEncode(Kryo kryo, Object object, Output output);

	/**
	 * Subclasses implement this method to decode with Kryo.
	 *
	 * @param kryo the Kryo instance
	 * @param input the Kryo Input instance
	 * @param type the class of the decoded object
	 * @param <T> the type for decoded object
	 * @return the decoded object
	 */
	protected abstract <T> T doDecode(Kryo kryo, Input input, Class<T> type);

	/**
	 * Subclasses implement this to configure the kryo instance.
	 * This is invoked on each new Kryo instance when it is created.
	 *
	 * @param kryo the kryo instance
	 */
	protected abstract void configureKryoInstance(Kryo kryo);

	private byte[] encode(Object object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		encode(object, bos);
		byte[] bytes = bos.toByteArray();
		bos.close();
		return bytes;
	}

	private void encode(final Object object, OutputStream outputStream) throws IOException {
		Assert.notNull(object, "cannot encode a null object");
		Assert.notNull(outputStream, "'outputSteam' cannot be null");
		final Output output = (outputStream instanceof Output ? (Output) outputStream : new Output(outputStream));
		this.pool.run(new KryoCallback<Void>() {

			@Override
			public Void execute(Kryo kryo) {
				doEncode(kryo, object, output);
				return null;
			}
		});
		output.close();
	}

	private <T> T decode(byte[] bytes, Class<T> type) throws IOException {
		Assert.notNull(bytes, "'bytes' cannot be null");
		final Input input = new Input(bytes);
		try {
			return decode(input, type);
		}
		finally {
			input.close();
		}
	}

	private <T> T decode(InputStream inputStream, final Class<T> type) throws IOException {
		Assert.notNull(inputStream, "'inputStream' cannot be null");
		Assert.notNull(type, "'type' cannot be null");
		final Input input = (inputStream instanceof Input ? (Input) inputStream : new Input(inputStream));
		T result = null;
		try {
			result = this.pool.run(new KryoCallback<T>(){

				@Override
				public T execute(Kryo kryo) {
					return doDecode(kryo, input, type);
				}
			});
		}
		finally {
			input.close();
		}
		return result;
	}
}
