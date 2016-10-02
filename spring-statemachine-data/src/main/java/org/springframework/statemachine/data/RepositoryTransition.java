/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.data;

import java.util.Set;

import org.springframework.statemachine.transition.TransitionKind;

/**
 * Generic interface representing transition entity.
 *
 * @author Janne Valkealahti
 *
 */
public interface RepositoryTransition {

	/**
	 * Gets the machine id.
	 *
	 * @return the machine id
	 */
	String getMachineId();

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	String getSource();

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	String getTarget();

	/**
	 * Gets the event.
	 *
	 * @return the event
	 */
	String getEvent();

	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	Set<? extends RepositoryAction> getActions();

	/**
	 * Gets the guard.
	 *
	 * @return the guard
	 */
	RepositoryGuard getGuard();

	/**
	 * Gets the transition kind.
	 *
	 * @return the transition kind
	 */
	TransitionKind getKind();
}
