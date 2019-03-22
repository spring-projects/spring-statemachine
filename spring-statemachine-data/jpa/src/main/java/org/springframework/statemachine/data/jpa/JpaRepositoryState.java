/*
 * Copyright 2016-2018 the original author or authors.
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
package org.springframework.statemachine.data.jpa;

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.statemachine.data.RepositoryAction;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.state.PseudoStateKind;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * JPA entity for states.
 *
 * @author Janne Valkealahti
 *
 */
@Entity
@Table(name = "State")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public class JpaRepositoryState extends RepositoryState {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String machineId;
	private String state;
	private String region;

	@Column(name = "initialState")
	private boolean initial;
	private PseudoStateKind kind;
	private String submachineId;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "fk_state_initial_action"))
	private JpaRepositoryAction initialAction;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "fk_state_parent_state"))
	private JpaRepositoryState parentState;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(foreignKey = @ForeignKey(name = "fk_state_state_actions_s"), inverseForeignKey = @ForeignKey(name = "fk_state_state_actions_a"))
	private Set<JpaRepositoryAction> stateActions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(foreignKey = @ForeignKey(name = "fk_state_entry_actions_s"), inverseForeignKey = @ForeignKey(name = "fk_state_entry_actions_a"))
	private Set<JpaRepositoryAction> entryActions;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(foreignKey = @ForeignKey(name = "fk_state_exit_actions_s"), inverseForeignKey = @ForeignKey(name = "fk_state_exit_actions_a"))
	private Set<JpaRepositoryAction> exitActions;

	@ElementCollection(fetch = FetchType.EAGER, targetClass = String.class)
	@CollectionTable(name="DeferredEvents", foreignKey = @ForeignKey(name = "fk_state_deferred_events"))
	private Set<String> deferredEvents;

	/**
	 * Instantiates a new jpa repository state.
	 */
	public JpaRepositoryState() {
		this(null);
	}

	/**
	 * Instantiates a new jpa repository state.
	 *
	 * @param state the state
	 */
	public JpaRepositoryState(String state) {
		this(state, false);
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
		this.machineId = machineId == null ? "" : machineId;
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
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
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
	public PseudoStateKind getKind() {
		return kind;
	}

	public void setKind(PseudoStateKind kind) {
		this.kind = kind;
	}

	@Override
	public boolean isInitial() {
		return initial;
	}

	public void setInitial(boolean initial) {
		this.initial = initial;
	}

	@Override
	public RepositoryAction getInitialAction() {
		return initialAction;
	}

	public void setInitialAction(JpaRepositoryAction initialAction) {
		this.initialAction = initialAction;
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
	public Set<String> getDeferredEvents() {
		return deferredEvents;
	}

	public void setDeferredEvents(Set<String> deferredEvents) {
		this.deferredEvents = deferredEvents;
	}

	@Override
	public String getSubmachineId() {
		return submachineId;
	}

	public void setSubmachineId(String submachineId) {
		this.submachineId = submachineId;
	}

	@Override
	public String toString() {
		return "JpaRepositoryState [id=" + id + ", machineId=" + machineId + ", state=" + state + ", region=" + region
				+ ", initial=" + initial + ", kind=" + kind + ", submachineId=" + submachineId + ", parentState="
				+ parentState + ", stateActions=" + stateActions + ", entryActions=" + entryActions + ", exitActions="
				+ exitActions + ", deferredEvents=" + deferredEvents + "]";
	}
}
