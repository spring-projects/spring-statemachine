/*
 * Copyright 2016 the original author or authors.
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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.statemachine.support.CountTrigger;
import org.springframework.statemachine.support.LifecycleObjectSupport;

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
	private volatile ScheduledFuture<?> scheduled;

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

	@Override
	public boolean evaluate(TriggerContext<S, E> context) {
		return false;
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
	protected void doStart() {
		if (count > 0) {
			return;
		}
		schedule();
	}

	@Override
	protected void doStop() {
		cancel();
	}

	@Override
	public void arm() {
		if (scheduled != null) {
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
		scheduled = getTaskScheduler().schedule(new Runnable() {

			@Override
			public void run() {
				notifyTriggered();
			}
		}, new CountTrigger(count, period, initialDelay, TimeUnit.MILLISECONDS));
	}

	private void notifyTriggered() {
		triggerListener.triggered();
	}

	private void cancel() {
		if (scheduled != null) {
			scheduled.cancel(true);
		}
		scheduled = null;
	}
}
