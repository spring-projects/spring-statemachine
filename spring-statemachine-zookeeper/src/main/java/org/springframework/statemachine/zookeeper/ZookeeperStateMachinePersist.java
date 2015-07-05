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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.data.Stat;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.ensemble.StateMachinePersist;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * {@link StateMachinePersist} using zookeeper as a storage and
 *  kroy libraries as a backing serialization technique.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ZookeeperStateMachinePersist<S, E> implements StateMachinePersist<S, E, Stat> {

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

	private final CuratorFramework curatorClient;
	private final String path;

	/**
	 * Instantiates a new zookeeper state machine persist.
	 *
	 * @param curatorClient the curator client
	 * @param path the path for persistent state
	 */
	public ZookeeperStateMachinePersist(CuratorFramework curatorClient, String path) {
		this.curatorClient = curatorClient;
		this.path = path;
	}

	@Override
	public void write(org.springframework.statemachine.StateMachineContext<S,E> context, Stat stat) {
		byte[] data = serialize(context);
		CuratorTransaction tx = curatorClient.inTransaction();
		try {
			Collection<CuratorTransactionResult> results = tx.setData().forPath(path, data).and().commit();
			int version = results.iterator().next().getResultStat().getVersion();
			stat.setVersion(version);
		} catch (Exception e) {
			throw new StateMachineException("Error persisting data", e);
		}

	}

	@Override
	public StateMachineContext<S, E> read(Stat stat) throws Exception {
		byte[] data = curatorClient.getData().storingStatIn(stat).forPath(path);
		StateMachineContext<S, E> context = deserialize(data);
		return context;
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

	private static class StateMachineContextSerializer<S, E> extends Serializer<StateMachineContext<S, E>> {

		@Override
		public void write(Kryo kryo, Output output, StateMachineContext<S, E> context) {
			kryo.writeClassAndObject(output, context.getEvent());
			kryo.writeClassAndObject(output, context.getState());
			kryo.writeClassAndObject(output, context.getEventHeaders());
			kryo.writeClassAndObject(output, context.getExtendedState().getVariables());
			kryo.writeClassAndObject(output, context.getChilds());
		}

		@SuppressWarnings("unchecked")
		@Override
		public StateMachineContext<S, E> read(Kryo kryo, Input input, Class<StateMachineContext<S, E>> clazz) {
			E event = (E) kryo.readClassAndObject(input);
			S state = (S) kryo.readClassAndObject(input);
			Map<String, Object> eventHeaders = (Map<String, Object>) kryo.readClassAndObject(input);
			Map<Object, Object> variables = (Map<Object, Object>) kryo.readClassAndObject(input);
			List<StateMachineContext<S, E>> childs = (List<StateMachineContext<S, E>>) kryo.readClassAndObject(input);
			return new DefaultStateMachineContext<S, E>(childs, state, event, eventHeaders, new DefaultExtendedState(variables));
		}

	}

	private static class MessageHeadersSerializer extends Serializer<MessageHeaders> {

		@Override
		public void write(Kryo kryo, Output output, MessageHeaders object) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			for (Entry<String, Object> entry : object.entrySet()) {
				map.put(entry.getKey(), entry.getValue());
			}
			kryo.writeClassAndObject(output, map);
		}

		@SuppressWarnings("unchecked")
		@Override
		public MessageHeaders read(Kryo kryo, Input input, Class<MessageHeaders> type) {
			Map<String, Object> eventHeaders = (Map<String, Object>) kryo.readClassAndObject(input);
			return new MessageHeaders(eventHeaders);
		}

	}

	private static class UUIDSerializer extends Serializer<UUID> {

		public UUIDSerializer() {
			setImmutable(true);
		}

		@Override
		public void write(final Kryo kryo, final Output output, final UUID uuid) {
			output.writeLong(uuid.getMostSignificantBits());
			output.writeLong(uuid.getLeastSignificantBits());
		}

		@Override
		public UUID read(final Kryo kryo, final Input input, final Class<UUID> uuidClass) {
			return new UUID(input.readLong(), input.readLong());
		}
	}

}
