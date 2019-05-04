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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveLifecycleManager implements StateMachineReactiveLifecycle {

	private static final Log log = LogFactory.getLog(ReactiveLifecycleManager.class);
	private final AtomicEnum state = new AtomicEnum(LifecycleState.STOPPED);
	private EmitterProcessor<Mono<Void>> startRequestsProcessor;
	private EmitterProcessor<Mono<Void>> stopRequestsProcessor;
	private Flux<Mono<Void>> startRequests;
	private Flux<Mono<Void>> stopRequests;
	private Supplier<Mono<Void>> preStartRequest;
	private Supplier<Mono<Void>> preStopRequest;
	private Supplier<Mono<Void>> postStartRequest;
	private Supplier<Mono<Void>> postStopRequest;
	private AtomicBoolean stopRequested = new AtomicBoolean();
	private Object owner;

	public enum LifecycleState {
		STOPPED,
		STARTING,
		STARTED,
		STOPPING;
	}

	public ReactiveLifecycleManager(Supplier<Mono<Void>> preStartRequest, Supplier<Mono<Void>> preStopRequest,
			Supplier<Mono<Void>> postStartRequest, Supplier<Mono<Void>> postStopRequest) {
		this.preStartRequest = preStartRequest;
		this.preStopRequest = preStopRequest;
		this.postStartRequest = postStartRequest;
		this.postStopRequest = postStopRequest;
		this.startRequestsProcessor = EmitterProcessor.<Mono<Void>>create(false);
		this.stopRequestsProcessor = EmitterProcessor.<Mono<Void>>create(false);
		this.startRequests = this.startRequestsProcessor.cache(1);
		this.stopRequests = this.stopRequestsProcessor.cache(1);
	}

	@Override
	public Mono<Void> startReactively() {
		log.debug("Request startReactively " + this);
		return Mono.defer(() -> {
			return Mono.just(state.compareAndSet(LifecycleState.STOPPED, LifecycleState.STARTING))
				.filter(owns -> owns)
				.flatMap(owns -> this.startRequests.next().flatMap(Function.identity()).doOnSuccess(aVoid -> {
					state.set(LifecycleState.STARTED);
				}))
				;
		})
		.then(Mono.defer(postStartRequest))
		.then(Mono.defer(() -> {
			if (stopRequested.compareAndSet(true, false)) {
				log.debug("Stopping as stopRequested is true");
				return stopReactively();
			}
			return Mono.empty();
		}))
		;
	}

	@Override
	public Mono<Void> stopReactively() {
		log.debug("Request stopReactively " + this);
		return Mono.defer(() -> {
			return Mono.just(state.compareAndSet(LifecycleState.STARTED, LifecycleState.STOPPING))
				.doOnNext(owns -> {
					// TODO: REACTOR needs better use of atomic
					if (!owns && state.get() != LifecycleState.STOPPED) {
						log.debug("Don't own, requesting to postpone stop" + this);
						stopRequested.compareAndSet(false, true);
					}
				})
				.filter(owns -> owns)
				.flatMap(owns -> this.stopRequests.next().flatMap(Function.identity()).doOnSuccess(aVoid -> {
					state.set(LifecycleState.STOPPED);
				}))
				;
		})
		.then(Mono.defer(postStopRequest))
		;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public LifecycleState getLifecycleState() {
		return state.get();
	}

	public boolean isRunning() {
		return state.get() == LifecycleState.STARTED;
	}

	@Override
	public String toString() {
		return "[lifecyclestate=" + state.get() + ", owner=" + owner + "]";
	}

	private class AtomicEnum {

		private final AtomicReference<LifecycleState> ref;

		public AtomicEnum(final LifecycleState initialValue) {
			this.ref = new AtomicReference<LifecycleState>(initialValue);
		}

		public void set(final LifecycleState newValue) {
			log.debug("Lifecycle to " + newValue + " in " + ReactiveLifecycleManager.this);
			this.ref.set(newValue);
		}

		public LifecycleState get() {
			return this.ref.get();
		}

		public boolean compareAndSet(final LifecycleState expect, final LifecycleState update) {
			boolean set = this.ref.compareAndSet(expect, update);
			if (set) {
				log.debug("Lifecycle from " + expect + " to " + update + " in " + ReactiveLifecycleManager.this);
				if (update == LifecycleState.STARTING) {
					log.debug("Next start request with doStartReactively in " + ReactiveLifecycleManager.this);
					startRequestsProcessor.onNext(preStartRequest.get());
				} else if (update == LifecycleState.STOPPING) {
					log.debug("Next stop request with doStopReactively in " + ReactiveLifecycleManager.this);
					stopRequestsProcessor.onNext(preStopRequest.get());
				}
			}
			return set;
		}
	}
}
