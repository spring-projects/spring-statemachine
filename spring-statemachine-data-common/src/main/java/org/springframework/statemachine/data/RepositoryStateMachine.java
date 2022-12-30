/*
 * Copyright 2017 the original author or authors.
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

/**
 * Generic base class representing state machine entity.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class RepositoryStateMachine extends BaseRepositoryEntity {

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
	 * Gets the state machine context.
	 *
	 * @return the state machine context
	 */
	public abstract byte[] getStateMachineContext();
}
