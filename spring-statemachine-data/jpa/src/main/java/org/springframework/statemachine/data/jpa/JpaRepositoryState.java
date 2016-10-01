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
package org.springframework.statemachine.data.jpa;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.statemachine.data.RepositoryState;

/**
 * JPA entity for states.
 *
 * @author Janne Valkealahti
 *
 */
@Entity
public class JpaRepositoryState implements RepositoryState {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String machineId;
	private String parentState;
	private String state;
	private boolean initial;

	/**
	 * Instantiates a new jpa repository state.
	 */
	public JpaRepositoryState() {
	}

	/**
	 * Instantiates a new jpa repository state.
	 *
	 * @param state the state
	 */
	public JpaRepositoryState(String state) {
		this.state = state;
	}

	/**
	 * Instantiates a new jpa repository state.
	 *
	 * @param state the state
	 * @param initial the initial
	 */
	public JpaRepositoryState(String state, boolean initial) {
		this(null, state, initial);
	}

	/**
	 * Instantiates a new jpa repository state.
	 *
	 * @param machineId the machine id
	 * @param state the state
	 * @param initial the initial
	 */
	public JpaRepositoryState(String machineId, String state, boolean initial) {
		this(machineId, null, state, initial);
	}

	/**
	 * Instantiates a new jpa repository state.
	 *
	 * @param machineId the machine id
	 * @param parentState the parent state
	 * @param state the state
	 * @param initial the initial
	 */
	public JpaRepositoryState(String machineId, String parentState, String state, boolean initial) {
		this.machineId = machineId;
		this.parentState = parentState;
		this.state = state;
		this.initial = initial;
	}

	@Override
	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	@Override
	public String getParentState() {
		return parentState;
	}

	public void setParentState(String parentState) {
		this.parentState = parentState;
	}

	@Override
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
	}
}
