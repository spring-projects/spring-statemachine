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

/**
 * Generic interface representing state entity.
 *
 * @author Janne Valkealahti
 *
 */
public interface RepositoryState {

	/**
	 * Gets the parent state.
	 *
	 * @return the parent state
	 */
	String getParentState();

	/**
	 * Gets the machine id.
	 *
	 * @return the machine id
	 */
	String getMachineId();

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	String getState();

	/**
	 * Checks if is initial.
	 *
	 * @return true, if is initial
	 */
	boolean isInitial();

	/**
	 * Gets the state actions.
	 *
	 * @return the state actions
	 */
	Set<? extends RepositoryAction> getStateActions();

	/**
	 * Gets the entry actions.
	 *
	 * @return the entry actions
	 */
	Set<? extends RepositoryAction> getEntryActions();

	/**
	 * Gets the exit actions.
	 *
	 * @return the exit actions
	 */
	Set<? extends RepositoryAction> getExitActions();
}
