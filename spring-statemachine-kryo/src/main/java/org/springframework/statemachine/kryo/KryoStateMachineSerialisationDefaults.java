/*
 * Copyright 2026 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.ObservableMap;

import com.esotericsoftware.kryo.Kryo;

/**
 * Static helper that applies the safe-by-default configuration used by Spring
 * Statemachine's Kryo-based persistence: enables
 * {@link Kryo#setRegistrationRequired(boolean) registrationRequired=true} and
 * registers the concrete framework and JDK types known to be written through
 * {@link StateMachineContextSerializer}.
 * Applications that use custom state or event types (typically enums) need to
 * register those types as well. For Spring-Boot or annotation-config usage this
 * is done by subclassing {@link KryoStateMachineSerialisationService} and
 * overriding
 * {@link AbstractKryoStateMachineSerialisationService#configureKryoInstance(Kryo)}.
 * For the {@code spring-statemachine-data-redis} and
 * {@code spring-statemachine-zookeeper} backends, a {@code Consumer<Kryo>}
 * constructor parameter is provided.
 * @since 4.0.2
 * @author Spring Statemachine team
 */
public final class KryoStateMachineSerialisationDefaults {

	private KryoStateMachineSerialisationDefaults() {
	}

	/**
	 * Apply the safe-by-default configuration to the supplied Kryo instance.
	 *
	 * @param kryo the Kryo instance to configure
	 */
	public static void registerDefaults(Kryo kryo) {
		// Lock down class resolution: any class that has not been explicitly
		// registered below (or by application code afterwards) cannot be
		// resolved on read, regardless of what bytes the input stream contains.
		kryo.setRegistrationRequired(true);

		// Custom serializers for framework types must be installed before the
		// matching register() calls so that the registration picks them up
		// rather than falling back to FieldSerializer.
		kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer<>());
		kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
		kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());

		// Concrete framework types that travel through the wire.
		kryo.register(StateMachineContext.class);
		kryo.register(DefaultStateMachineContext.class);
		kryo.register(MessageHeaders.class);
		kryo.register(UUID.class);

		// Common JDK collection / map types that may legitimately appear in
		// event headers, extended-state variables, child collections and
		// history-state maps. Application code that uses other concrete map or
		// collection types must register them explicitly via the appropriate
		// extension hook.
		kryo.register(HashMap.class);
		kryo.register(LinkedHashMap.class);
		kryo.register(TreeMap.class);
		kryo.register(ConcurrentHashMap.class);
		kryo.register(ArrayList.class);
		kryo.register(LinkedList.class);
		kryo.register(HashSet.class);
		kryo.register(LinkedHashSet.class);
		kryo.register(TreeSet.class);

		// DefaultExtendedState wraps its variables in an ObservableMap; that
		// concrete wrapper class is therefore the type the serializer
		// observes for the variables slot.
		kryo.register(ObservableMap.class);
	}
}
