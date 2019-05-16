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
package org.springframework.statemachine.trigger;

import java.util.concurrent.ScheduledFuture;

import org.springframework.statemachine.support.LifecycleObjectSupport;

public class TimerTrigger<S, E> extends LifecycleObjectSupport implements Trigger<S, E> {

	private final CompositeTriggerListener triggerListener = new CompositeTriggerListener();

	private final long period;

	private volatile ScheduledFuture<?> scheduled;

	public TimerTrigger(long period) {
		this.period = period;
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
		scheduled = getTaskScheduler().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				notifyTriggered();
			}
		}, period);
	}

	@Override
	protected void doStop() {
		if (scheduled != null) {
			scheduled.cancel(true);
		}
		scheduled = null;
	}

	private void notifyTriggered() {
		triggerListener.triggered();
	}

}
