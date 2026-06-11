/*
 * Copyright 2017-2026 the original author or authors.
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

import java.util.function.Consumer;

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

	private final Consumer<Kryo> kryoCustomizer;

	/**
	 * Instantiates a new kryo state machine serialisation service with no
	 * extra class registrations beyond the framework defaults.
	 */
	public KryoStateMachineSerialisationService() {
		this.kryoCustomizer = null;
	}

	/**
	 * Instantiates a new kryo state machine serialisation service with an
	 * application-supplied Kryo customizer.
	 * <p>
	 * The customizer is invoked once per Kryo instance <em>after</em> the
	 * framework's safe-by-default registrations have been applied (via
	 * {@link KryoStateMachineSerialisationDefaults#registerDefaults(Kryo)}).
	 * Use it to register application-specific types — typically the {@code S}
	 * and {@code E} enums used by the state machine — so they are accepted by
	 * the registration-required allowlist:
	 * <pre>{@code
	 * new KryoStateMachineSerialisationService<>(kryo -> {
	 *     kryo.register(MyStates.class);
	 *     kryo.register(MyEvents.class);
	 * })
	 * }</pre>
	 *
	 * @param kryoCustomizer callback applied to each new Kryo instance;
	 *        may be {@code null}
	 * @since 4.0.2
	 */
	public KryoStateMachineSerialisationService(Consumer<Kryo> kryoCustomizer) {
		this.kryoCustomizer = kryoCustomizer;
	}

	@Override
	protected void doEncode(Kryo kryo, Object object, Output output) {
		kryo.writeObject(output, object);
	}

	@Override
	protected <T> T doDecode(Kryo kryo, Input input, Class<T> type) {
		return kryo.readObject(input, type);
	}

	/**
	 * Applies the optional {@link #kryoCustomizer} supplied at construction
	 * time. Subclasses may override this to register additional types on top
	 * of the framework defaults and the customizer.
	 */
	@Override
	protected void configureKryoInstance(Kryo kryo) {
		if (kryoCustomizer != null) {
			kryoCustomizer.accept(kryo);
		}
	}
}
