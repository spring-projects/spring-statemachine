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
package org.springframework.statemachine;

import org.springframework.statemachine.StateMachineContext;

/**
 * Repository interface for saving and retrieving {@link StateMachineContext} objects.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @param <T> The type of state machine context
 */
public interface StateMachineContextRepository<S, E, T extends StateMachineContext<S, E>> {

	/**
	 * Save a context.
	 *
	 * @param context the context
	 * @param id the id
	 */
	void save(T context, String id);

	/**
	 * Gets the context.
	 *
	 * @param id the id
	 * @return the context
	 */
	T getContext(String id);

}
