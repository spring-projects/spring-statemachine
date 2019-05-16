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

import org.springframework.util.ObjectUtils;

public class EventTrigger<S, E> implements Trigger<S, E> {

	private final E event;

	public EventTrigger(E event) {
		this.event = event;
	}

	@Override
	public boolean evaluate(TriggerContext<S, E> context) {
		return ObjectUtils.nullSafeEquals(event, context.getEvent());
	}

	@Override
	public void addTriggerListener(TriggerListener listener) {
		// no-opt
	}

	@Override
	public E getEvent() {
		return event;
	}

	@Override
	public void arm() {
		// no-opt
	}

	@Override
	public void disarm() {
		// no-opt
	}
}
