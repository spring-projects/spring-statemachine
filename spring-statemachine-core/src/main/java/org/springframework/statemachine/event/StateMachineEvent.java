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
package org.springframework.statemachine.event;

import org.springframework.context.ApplicationEvent;

/**
 * Base {@link ApplicationEvent} class for leader based events. All custom event
 * classes should be derived from this class.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("serial")
public abstract class StateMachineEvent extends ApplicationEvent {

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the component that published the event (never {@code null})
	 */
	public StateMachineEvent(Object source) {
		super(source);
	}

	@Override
	public String toString() {
		return "StateMachineEvent [source=" + source + "]";
	}

}
