/*
 * Copyright 2016-2019 the original author or authors.
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
package org.springframework.statemachine.trigger;

import java.time.Duration;

import org.springframework.statemachine.support.LifecycleObjectSupport;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of a {@link Trigger} capable of firing on a
 * static periods.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class TimerTrigger<S, E> extends LifecycleObjectSupport implements Trigger<S, E> {

	private final CompositeTriggerListener triggerListener = new CompositeTriggerListener();
	private final long period;
	private final int count;
	private Disposable disposable;

	/**
	 * Instantiates a new timer trigger.
	 *
	 * @param period the period in milliseconds
	 */
	public TimerTrigger(long period) {
		this(period, 0);
	}

	/**
	 * Instantiates a new timer trigger.
	 *
	 * @param period the period
	 * @param count the count
	 */
	public TimerTrigger(long period, int count) {
		this.period = period;
		this.count = count;
	}

	public long getPeriod() {
		return period;
	}

	public int getCount() {
		return count;
	}

	@Override
	public Mono<Boolean> evaluate(TriggerContext<S, E> context) {
		return Mono.just(false);
	}

	@Override
	public void addTriggerListener(TriggerListener listener) {
		triggerListener.register(listener);
	}

	@Override
	public E getEvent() {
		return null;
	}

	@Override
	protected Mono<Void> doPreStartReactively() {
		return Mono.defer(() -> {
			if (count > 0) {
				return Mono.empty();
			}
			return Mono.fromRunnable(() -> schedule());
		});
	}

	@Override
	protected Mono<Void> doPreStopReactively() {
		return Mono.defer(() -> {
			cancel();
			return Mono.empty();
		});
	}

	@Override
	public void arm() {
		if (disposable != null) {
			return;
		}
		schedule();
	}

	@Override
	public void disarm() {
		if (count > 0) {
			cancel();
		}
	}

	private void schedule() {
		long initialDelay = count > 0 ? period : 0;
		Flux<Long> interval = Flux.interval(Duration.ofMillis(initialDelay), Duration.ofMillis(period))
			.doOnNext(c -> {
				notifyTriggered();
			});
		if (count > 0) {
			interval = interval.take(count);
		}
		disposable = interval.subscribe();
	}

	private void notifyTriggered() {
		triggerListener.triggered();
	}

	private void cancel() {
		if (disposable != null) {
			disposable.dispose();
		}
		disposable = null;
	}
}
