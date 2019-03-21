/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.zookeeper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionFinal;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.data.Stat;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;
import org.springframework.util.Assert;

import com.esotericsoftware.kryo.Kryo;
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
	private final String logPath;
	private final int logSize;

	/**
	 * Instantiates a new zookeeper state machine persist.
	 *
	 * @param curatorClient the curator client
	 * @param path the path for persistent state
	 */
	public ZookeeperStateMachinePersist(CuratorFramework curatorClient, String path) {
		this(curatorClient, path, null, 0);
	}

	/**
	 * Instantiates a new zookeeper state machine persist.
	 *
	 * @param curatorClient the curator client
	 * @param path the path
	 * @param logPath the log path
	 * @param logSize the log size
	 */
	public ZookeeperStateMachinePersist(CuratorFramework curatorClient, String path, String logPath, int logSize) {
		if (logPath != null) {
			Assert.state(logSize > 0 && ((logSize & -logSize) == logSize), "Log size must be positive and power of two");
		}
		this.curatorClient = curatorClient;
		this.path = path;
		this.logPath = logPath;
		this.logSize = logSize;
	}

	@Override
	public void write(StateMachineContext<S,E> context, Stat stat) {
		byte[] data = serialize(context);
		CuratorTransaction tx = curatorClient.inTransaction();
		try {
			CuratorTransactionFinal tt = tx.setData().withVersion(stat.getVersion()).forPath(path, data).and();
			if (logPath != null) {
				tt = tt.setData().forPath(logPath + "/" + stat.getVersion() % logSize, data).and();
			}
			Collection<CuratorTransactionResult> results = tt.commit();
			int version = results.iterator().next().getResultStat().getVersion();
			stat.setVersion(version);
		} catch (Exception e) {
			throw new StateMachineException("Error persisting data", e);
		}

	}

	@Override
	public StateMachineContext<S, E> read(Stat stat) throws Exception {
		return deserialize(curatorClient.getData().storingStatIn(stat).forPath(path));
	}

	public StateMachineContext<S, E> readLog(int version, Stat stat) throws Exception {
		return deserialize(curatorClient.getData().storingStatIn(stat).forPath(logPath + "/" + version));
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
