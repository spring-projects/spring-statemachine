/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.junit.Test;
import org.springframework.statemachine.support.ReactiveLifecycleManager.LifecycleState;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ReactiveLifecycleManagerTests {

	@Test
	public void testStartStop() {
		ReactiveLifecycleManager manager = new ReactiveLifecycleManager(() -> Mono.empty(), () -> Mono.empty(),
				() -> Mono.empty(), () -> Mono.empty());

		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));

		StepVerifier.create(manager.startReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(true));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STARTED));

		StepVerifier.create(manager.stopReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));

		StepVerifier.create(manager.startReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(true));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STARTED));

		StepVerifier.create(manager.stopReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));
	}

	@Test
	public void testStartRecursive() {
		RecursiveStartRequestSupplier startSupplier = new RecursiveStartRequestSupplier();
		ReactiveLifecycleManager manager = new ReactiveLifecycleManager(startSupplier, () -> Mono.empty(),
				() -> Mono.empty(), () -> Mono.empty());
		startSupplier.setManager(manager);
		StepVerifier.create(manager.startReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(true));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STARTED));
	}

	@Test
	public void testStartStops() {
		StartStopsRequestSupplier startSupplier = new StartStopsRequestSupplier();
		ReactiveLifecycleManager manager = new ReactiveLifecycleManager(() -> Mono.empty(), () -> Mono.empty(),
				startSupplier, () -> Mono.empty());
		startSupplier.setManager(manager);
		StepVerifier.create(manager.startReactively()).expectComplete().verify();
		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));
	}

	@Test
	public void testStartStops2() {
		StartStopsRequestSupplier startSupplier = new StartStopsRequestSupplier();
		ReactiveLifecycleManager manager = new ReactiveLifecycleManager(startSupplier, () -> Mono.empty(),
				() -> Mono.empty(), () -> Mono.empty());
		startSupplier.setManager(manager);
		StepVerifier.create(manager.startReactively()).expectComplete().verify();
//		assertThat(manager.isRunning(), is(false));
		assertThat(manager.getLifecycleState(), is(LifecycleState.STOPPED));
	}

	private static class RecursiveStartRequestSupplier implements Supplier<Mono<Void>> {

		private ReactiveLifecycleManager manager;
		private final AtomicBoolean recursive = new AtomicBoolean(true);

		@Override
		public Mono<Void> get() {
			if (recursive.compareAndSet(true, false)) {
				return manager.startReactively();
			} else {
				return Mono.empty();
			}
		}

		public void setManager(ReactiveLifecycleManager manager) {
			this.manager = manager;
		}
	}

	private static class StartStopsRequestSupplier implements Supplier<Mono<Void>> {

		private ReactiveLifecycleManager manager;
		private final AtomicBoolean stop = new AtomicBoolean(true);

		@Override
		public Mono<Void> get() {
			if (stop.compareAndSet(true, false)) {
				return manager.stopReactively();
			} else {
				return Mono.empty();
			}
		}

		public void setManager(ReactiveLifecycleManager manager) {
			this.manager = manager;
		}
	}
}
