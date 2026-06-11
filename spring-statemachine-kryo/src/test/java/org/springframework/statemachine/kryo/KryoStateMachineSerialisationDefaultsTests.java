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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Tests for @{link KryoStateMachineSerialisationDefaults}.
 */
public class KryoStateMachineSerialisationDefaultsTests {

	public static class SomeClass {
		public String payload;
	}

	public enum MyStates {
		LOCKED, UNLOCKED
	}

	public enum MyEvents {
		COIN, PUSH
	}

	@Test
	public void testKryoDefaults() {
		Kryo myKryo = new Kryo();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		SomeClass someClass = new SomeClass();
		someClass.payload = "foo";
		myKryo.writeClassAndObject(output, someClass);
		output.close();

		Kryo kryo = new Kryo();
		KryoStateMachineSerialisationDefaults.registerDefaults(kryo);
		Input input = new Input(new ByteArrayInputStream(out.toByteArray()));

		assertThatThrownBy(() -> kryo.readClassAndObject(input))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Class is not registered");
	}

	@Test
	public void testKryoDefaultsRoundTrip() {
		HashMap<String, Object> headers = new HashMap<>();
		headers.put("foo", "bar");
		StateMachineContext<String, String> original =
				new DefaultStateMachineContext<>("S1", "E1", headers, new DefaultExtendedState());

		KryoStateMachineSerialisationService<String, String> service =
				new KryoStateMachineSerialisationService<>();
		byte[] bytes;
		StateMachineContext<String, String> roundTrip;
		try {
			bytes = service.serialiseStateMachineContext(original);
			roundTrip = service.deserialiseStateMachineContext(bytes);
		} catch (Exception ex) {
			throw new AssertionError("Round-trip of framework-default payload must not fail", ex);
		}

		assertThat(roundTrip.getState()).isEqualTo("S1");
		assertThat(roundTrip.getEvent()).isEqualTo("E1");
		assertThat(roundTrip.getEventHeaders()).containsEntry("foo", "bar");
	}

	@Test
	public void applicationEnumsCanBeAddedViaSubclass() {
		KryoStateMachineSerialisationService<MyStates, MyEvents> service =
				new KryoStateMachineSerialisationService<MyStates, MyEvents>() {
					@Override
					protected void configureKryoInstance(Kryo kryo) {
						kryo.register(MyStates.class);
						kryo.register(MyEvents.class);
					}
				};

		StateMachineContext<MyStates, MyEvents> original = new DefaultStateMachineContext<>(
				MyStates.LOCKED, MyEvents.COIN, new HashMap<>(), new DefaultExtendedState());

		byte[] bytes;
		StateMachineContext<MyStates, MyEvents> roundTrip;
		try {
			bytes = service.serialiseStateMachineContext(original);
			roundTrip = service.deserialiseStateMachineContext(bytes);
		} catch (Exception ex) {
			throw new AssertionError("Round-trip with user enums must not fail", ex);
		}

		assertThat(roundTrip.getState()).isEqualTo(MyStates.LOCKED);
		assertThat(roundTrip.getEvent()).isEqualTo(MyEvents.COIN);
	}

	@Test
	public void applicationEnumsShouldBeRegistered() {
		KryoStateMachineSerialisationService<MyStates, MyEvents> service =
				new KryoStateMachineSerialisationService<>();

		StateMachineContext<MyStates, MyEvents> original = new DefaultStateMachineContext<>(
				MyStates.LOCKED, MyEvents.COIN, new HashMap<>(), new DefaultExtendedState());

		assertThatThrownBy(() -> service.serialiseStateMachineContext(original))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Class is not registered");
	}
}
