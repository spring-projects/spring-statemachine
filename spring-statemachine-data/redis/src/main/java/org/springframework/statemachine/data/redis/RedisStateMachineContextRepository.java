/*
 * Copyright 2015-2026 the original author or authors.
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
package org.springframework.statemachine.data.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineContextRepository;
import org.springframework.statemachine.kryo.KryoStateMachineSerialisationDefaults;
import org.springframework.util.Assert;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A {@link StateMachineContextRepository} backed by a redis and kryo serialization.
 * <p>
 * Persisted bytes are deserialised through Kryo with the
 * {@linkplain KryoStateMachineSerialisationDefaults safe-by-default class
 * allowlist} applied. Applications that use custom state or event types
 * (typically enums) must register those types via the
 * {@link #RedisStateMachineContextRepository(RedisConnectionFactory, String, Consumer)}
 * constructor's {@code kryoCustomizer} parameter; otherwise Kryo will reject
 * them with {@code IllegalArgumentException} ("Class is not registered").
 * <p>
 * Keys are written with a fixed namespace prefix (default
 * {@value #DEFAULT_KEY_NAMESPACE}) so that user-supplied {@code machineId}
 * values cannot collide with unrelated keys in the same Redis logical database.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class RedisStateMachineContextRepository<S, E> implements StateMachineContextRepository<S, E, StateMachineContext<S, E>> {

	/**
	 * Default key namespace prefix used when none is supplied.
	 */
	public static final String DEFAULT_KEY_NAMESPACE = "ssm:context:";

	private final ThreadLocal<Kryo> kryoThreadLocal;

	private final RedisOperations<String,byte[]> redisOperations;

	private final String keyNamespace;

	/**
	 * Instantiates a new redis state machine context repository using the
	 * default key namespace and no extra Kryo registrations.
	 *
	 * @param redisConnectionFactory the redis connection factory
	 */
	public RedisStateMachineContextRepository(RedisConnectionFactory redisConnectionFactory) {
		this(redisConnectionFactory, DEFAULT_KEY_NAMESPACE, null);
	}

	/**
	 * Instantiates a new redis state machine context repository with a custom
	 * key namespace prefix and no extra Kryo registrations.
	 *
	 * @param redisConnectionFactory the redis connection factory
	 * @param keyNamespace the prefix prepended to every Redis key
	 * @since 4.0.2
	 */
	public RedisStateMachineContextRepository(RedisConnectionFactory redisConnectionFactory, String keyNamespace) {
		this(redisConnectionFactory, keyNamespace, null);
	}

	/**
	 * Instantiates a new redis state machine context repository.
	 *
	 * @param redisConnectionFactory the redis connection factory
	 * @param keyNamespace the prefix prepended to every Redis key. Must not be {@code null};
	 *        pass an empty string to disable prefixing (not recommended).
	 * @param kryoCustomizer optional callback invoked once per Kryo instance after the
	 *        framework's default registrations are applied. Use this to register
	 *        application-specific state and event types (typically enums) so that they
	 *        are accepted by the allowlist. May be {@code null}.
	 * @since 4.0.2
	 */
	public RedisStateMachineContextRepository(RedisConnectionFactory redisConnectionFactory,
			String keyNamespace, Consumer<Kryo> kryoCustomizer) {
		Assert.notNull(redisConnectionFactory, "Redis connection factory must not be null");
		Assert.notNull(keyNamespace, "Key namespace must not be null");
		this.redisOperations = createDefaultTemplate(redisConnectionFactory);
		this.keyNamespace = keyNamespace;
		this.kryoThreadLocal = ThreadLocal.withInitial(() -> {
			Kryo kryo = new Kryo();
			KryoStateMachineSerialisationDefaults.registerDefaults(kryo);
			if (kryoCustomizer != null) {
				kryoCustomizer.accept(kryo);
			}
			return kryo;
		});
	}

	@Override
	public void save(StateMachineContext<S, E> context, String id) {
		redisOperations.opsForValue().set(prefixedKey(id), serialize(context));
	}

	@Override
	public StateMachineContext<S, E> getContext(String id) {
		return deserialize(redisOperations.opsForValue().get(prefixedKey(id)));
	}

	private String prefixedKey(String id) {
		Assert.notNull(id, "Machine id must not be null");
		return keyNamespace + id;
	}

	private static RedisTemplate<String,byte[]> createDefaultTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String,byte[]> template = new RedisTemplate<String,byte[]>();
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setConnectionFactory(connectionFactory);
		template.afterPropertiesSet();
		return template;
	}

	private byte[] serialize(StateMachineContext<S, E> context) {
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeObject(output, context);
		output.close();
		return out.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private StateMachineContext<S, E> deserialize(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Input input = new Input(in);
		return kryo.readObject(input, StateMachineContext.class);
	}

}
