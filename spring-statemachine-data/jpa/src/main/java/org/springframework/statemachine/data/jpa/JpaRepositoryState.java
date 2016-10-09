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

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.springframework.statemachine.data.RepositoryState;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * JPA entity for states.
 *
 * @author Janne Valkealahti
 *
 */
@Entity
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public class JpaRepositoryState extends RepositoryState {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String machineId;
	private String state;
	private boolean initial;

	@OneToOne(fetch = FetchType.EAGER)
	private JpaRepositoryState parentState;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<JpaRepositoryAction> stateActions;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<JpaRepositoryAction> entryActions;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<JpaRepositoryAction> exitActions;

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
	public JpaRepositoryState(String machineId, JpaRepositoryState parentState, String state, boolean initial) {
		this(machineId, parentState, state, initial, null, null, null);
	}

	/**
	 * Instantiates a new jpa repository state.
	 *
	 * @param machineId the machine id
	 * @param parentState the parent state
	 * @param state the state
	 * @param initial the initial
	 * @param stateActions the state actions
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public JpaRepositoryState(String machineId, JpaRepositoryState parentState, String state, boolean initial, Set<JpaRepositoryAction> stateActions,
			Set<JpaRepositoryAction> entryActions, Set<JpaRepositoryAction> exitActions) {
		this.machineId = machineId;
		this.parentState = parentState;
		this.state = state;
		this.initial = initial;
		this.stateActions = stateActions;
		this.entryActions = entryActions;
		this.exitActions = exitActions;
	}

	@Override
	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	@Override
	public JpaRepositoryState getParentState() {
		return parentState;
	}

	public void setParentState(JpaRepositoryState parentState) {
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

	@Override
	public Set<JpaRepositoryAction> getStateActions() {
		return stateActions;
	}

	public void setStateActions(Set<JpaRepositoryAction> stateActions) {
		this.stateActions = stateActions;
	}

	@Override
	public Set<JpaRepositoryAction> getEntryActions() {
		return entryActions;
	}

	public void setEntryActions(Set<JpaRepositoryAction> entryActions) {
		this.entryActions = entryActions;
	}

	@Override
	public Set<JpaRepositoryAction> getExitActions() {
		return exitActions;
	}

	public void setExitActions(Set<JpaRepositoryAction> exitActions) {
		this.exitActions = exitActions;
	}

	@Override
	public String toString() {
		return "JpaRepositoryState [id=" + id + ", machineId=" + machineId + ", parentState=" + parentState + ", state=" + state
				+ ", initial=" + initial + ", stateActions=" + stateActions + ", entryActions=" + entryActions + ", exitActions="
				+ exitActions + "]";
	}
}
