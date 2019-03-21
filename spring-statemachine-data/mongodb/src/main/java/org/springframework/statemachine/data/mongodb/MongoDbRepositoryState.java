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
package org.springframework.statemachine.data.mongodb;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.statemachine.data.RepositoryAction;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.state.PseudoStateKind;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * MongoDb entity for states.
 *
 * @author Janne Valkealahti
 *
 */
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
@Document(collection = "MongoDbRepositoryState")
public class MongoDbRepositoryState extends RepositoryState {

	@Id
	private String id;

	private String machineId = "";
	private String state;
	private String region;
	private boolean initial;
	private PseudoStateKind kind;
	private String submachineId;

	@Reference
	private MongoDbRepositoryState parentState;

	@Reference
	private MongoDbRepositoryAction initialAction;

	@Reference
	private Set<MongoDbRepositoryAction> stateActions;

	@Reference
	private Set<MongoDbRepositoryAction> entryActions;

	@Reference
	private Set<MongoDbRepositoryAction> exitActions;

	private Set<String> deferredEvents;

	/**
	 * Instantiates a new MongoDb repository state.
	 */
	public MongoDbRepositoryState() {
	}

	/**
	 * Instantiates a new MongoDb repository state.
	 *
	 * @param state the state
	 */
	public MongoDbRepositoryState(String state) {
		this.state = state;
	}

	/**
	 * Instantiates a new MongoDb repository state.
	 *
	 * @param state the state
	 * @param initial the initial
	 */
	public MongoDbRepositoryState(String state, boolean initial) {
		this(null, state, initial);
	}

	/**
	 * Instantiates a new MongoDb repository state.
	 *
	 * @param machineId the machine id
	 * @param state the state
	 * @param initial the initial
	 */
	public MongoDbRepositoryState(String machineId, String state, boolean initial) {
		this(machineId, null, state, initial);
	}

	/**
	 * Instantiates a new MongoDb repository state.
	 *
	 * @param machineId the machine id
	 * @param parentState the parent state
	 * @param state the state
	 * @param initial the initial
	 */
	public MongoDbRepositoryState(String machineId, MongoDbRepositoryState parentState, String state, boolean initial) {
		this(machineId, parentState, state, initial, null, null, null);
	}

	/**
	 * Instantiates a new MongoDb repository state.
	 *
	 * @param machineId the machine id
	 * @param parentState the parent state
	 * @param state the state
	 * @param initial the initial
	 * @param stateActions the state actions
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public MongoDbRepositoryState(String machineId, MongoDbRepositoryState parentState, String state, boolean initial, Set<MongoDbRepositoryAction> stateActions,
			Set<MongoDbRepositoryAction> entryActions, Set<MongoDbRepositoryAction> exitActions) {
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
	public MongoDbRepositoryState getParentState() {
		return parentState;
	}

	public void setParentState(MongoDbRepositoryState parentState) {
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

	public void setInitialAction(MongoDbRepositoryAction initialAction) {
		this.initialAction = initialAction;
	}

	@Override
	public Set<MongoDbRepositoryAction> getStateActions() {
		return stateActions;
	}

	public void setStateActions(Set<MongoDbRepositoryAction> stateActions) {
		this.stateActions = stateActions;
	}

	@Override
	public Set<MongoDbRepositoryAction> getEntryActions() {
		return entryActions;
	}

	public void setEntryActions(Set<MongoDbRepositoryAction> entryActions) {
		this.entryActions = entryActions;
	}

	@Override
	public Set<MongoDbRepositoryAction> getExitActions() {
		return exitActions;
	}

	public void setExitActions(Set<MongoDbRepositoryAction> exitActions) {
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
		return "MongoDbRepositoryState [id=" + id + ", machineId=" + machineId + ", state=" + state + ", region=" + region
				+ ", initial=" + initial + ", kind=" + kind + ", submachineId=" + submachineId + ", parentState="
				+ parentState + ", stateActions=" + stateActions + ", entryActions=" + entryActions + ", exitActions="
				+ exitActions + ", deferredEvents=" + deferredEvents + "]";
	}
}
