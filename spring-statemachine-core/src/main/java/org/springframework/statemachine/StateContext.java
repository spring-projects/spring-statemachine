/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine;

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.transition.Transition;

/**
 * {@code StateContext} is representing a current context used in
 * {@link Transition}s, {@link Action}s and {@link Guard}s order to get access
 * to event headers and {@link ExtendedState}.
 * 
 * @author Janne Valkealahti
 *
 */
public interface StateContext {

	/**
	 * Gets the event message headers.
	 *
	 * @return the event message headers
	 */
	MessageHeaders getMessageHeaders();

	/**
	 * Gets the state machine extended state.
	 *
	 * @return the state machine extended state
	 */
	ExtendedState getExtendedState();

}
