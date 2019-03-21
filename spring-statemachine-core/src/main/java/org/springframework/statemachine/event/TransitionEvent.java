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

import org.springframework.statemachine.transition.Transition;

/**
 * Generic base event representing something with a {@link Transition}.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("serial")
public abstract class TransitionEvent extends StateMachineEvent {

	private final Transition<?, ?> transition;

	/**
	 * Instantiates a new transition event.
	 *
	 * @param source the source
	 * @param transition the transition
	 */
	public TransitionEvent(Object source, Transition<?, ?> transition) {
		super(source);
		this.transition = transition;
	}

	/**
	 * Gets the transition.
	 *
	 * @return the transition
	 */
	public Transition<?, ?> getTransition() {
		return transition;
	}

	@Override
	public String toString() {
		return "OnTransitionStartEvent [transition=" + transition + "]";
	}

}
