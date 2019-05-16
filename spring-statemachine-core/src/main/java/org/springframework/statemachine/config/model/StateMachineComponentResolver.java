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
package org.springframework.statemachine.config.model;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

/**
 * Strategy interface for resolving state machine components by their id's.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineComponentResolver<S, E> {

	/**
	 * Resolve action.
	 *
	 * @param id the id
	 * @return the action
	 */
	Action<S, E> resolveAction(String id);

	/**
	 * Resolve guard.
	 *
	 * @param id the id
	 * @return the guard
	 */
	Guard<S, E> resolveGuard(String id);
}
