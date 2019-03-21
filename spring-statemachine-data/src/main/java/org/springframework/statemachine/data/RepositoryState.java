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
package org.springframework.statemachine.data;

import java.util.Set;

import org.springframework.statemachine.state.PseudoStateKind;

/**
 * Generic base class representing state entity.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class RepositoryState extends BaseRepositoryEntity {

	/**
	 * Gets the parent state.
	 *
	 * @return the parent state
	 */
	public abstract RepositoryState getParentState();

	/**
	 * Gets the machine id.
	 *
	 * @return the machine id
	 */
	public abstract String getMachineId();

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public abstract String getState();

	/**
	 * Gets the region.
	 *
	 * @return the region
	 */
	public abstract String getRegion();

	/**
	 * Checks if is initial.
	 *
	 * @return true, if is initial
	 */
	public abstract boolean isInitial();

	/**
	 * Gets the initial action. This is any meaningful if
	 * state is initial state.
	 *
	 * @return the initial action
	 */
	public abstract RepositoryAction getInitialAction();

	/**
	 * Gets the state actions.
	 *
	 * @return the state actions
	 */
	public abstract Set<? extends RepositoryAction> getStateActions();

	/**
	 * Gets the entry actions.
	 *
	 * @return the entry actions
	 */
	public abstract Set<? extends RepositoryAction> getEntryActions();

	/**
	 * Gets the exit actions.
	 *
	 * @return the exit actions
	 */
	public abstract Set<? extends RepositoryAction> getExitActions();

	/**
	 * Gets the pseudo state kind.
	 *
	 * @return the pseudo state kind
	 */
	public abstract PseudoStateKind getKind();

	/**
	 * Gets the deferred events.
	 *
	 * @return the deferred events
	 */
	public abstract Set<String> getDeferredEvents();

	/**
	 * Gets the submachine id indicating that this is a submachine state
	 * and its structure is available from particular machine itself.
	 *
	 * @return the submachine id
	 */
	public abstract String getSubmachineId();
}
