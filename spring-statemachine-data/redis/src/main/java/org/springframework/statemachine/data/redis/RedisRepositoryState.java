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
package org.springframework.statemachine.data.redis;

import java.util.Set;

//import javax.persistence.ElementCollection;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.OneToMany;
//import javax.persistence.OneToOne;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.statemachine.data.RepositoryAction;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.state.PseudoStateKind;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Redis entity for states.
 *
 * @author Janne Valkealahti
 *
 */
@RedisHash("RedisRepositoryState")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public class RedisRepositoryState extends RepositoryState {

	@Id
	private String id;

	@Indexed
	private String machineId = "";
	private String state;
	private String region;
	private boolean initial;
	private PseudoStateKind kind;
	private String submachineId;

	@Reference
	private RedisRepositoryState parentState;

	@Reference
	private RedisRepositoryAction initialAction;

	@Reference
	private Set<RedisRepositoryAction> stateActions;

	@Reference
	private Set<RedisRepositoryAction> entryActions;

	@Reference
	private Set<RedisRepositoryAction> exitActions;

	private Set<String> deferredEvents;

	/**
	 * Instantiates a new redis repository state.
	 */
	public RedisRepositoryState() {
	}

	/**
	 * Instantiates a new redis repository state.
	 *
	 * @param state the state
	 */
	public RedisRepositoryState(String state) {
		this.state = state;
	}

	/**
	 * Instantiates a new redis repository state.
	 *
	 * @param state the state
	 * @param initial the initial
	 */
	public RedisRepositoryState(String state, boolean initial) {
		this(null, state, initial);
	}

	/**
	 * Instantiates a new redis repository state.
	 *
	 * @param machineId the machine id
	 * @param state the state
	 * @param initial the initial
	 */
	public RedisRepositoryState(String machineId, String state, boolean initial) {
		this(machineId, null, state, initial);
	}

	/**
	 * Instantiates a new redis repository state.
	 *
	 * @param machineId the machine id
	 * @param parentState the parent state
	 * @param state the state
	 * @param initial the initial
	 */
	public RedisRepositoryState(String machineId, RedisRepositoryState parentState, String state, boolean initial) {
		this(machineId, parentState, state, initial, null, null, null);
	}

	/**
	 * Instantiates a new redis repository state.
	 *
	 * @param machineId the machine id
	 * @param parentState the parent state
	 * @param state the state
	 * @param initial the initial
	 * @param stateActions the state actions
	 * @param entryActions the entry actions
	 * @param exitActions the exit actions
	 */
	public RedisRepositoryState(String machineId, RedisRepositoryState parentState, String state, boolean initial, Set<RedisRepositoryAction> stateActions,
			Set<RedisRepositoryAction> entryActions, Set<RedisRepositoryAction> exitActions) {
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
	public RedisRepositoryState getParentState() {
		return parentState;
	}

	public void setParentState(RedisRepositoryState parentState) {
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

	public void setInitialAction(RedisRepositoryAction initialAction) {
		this.initialAction = initialAction;
	}

	@Override
	public Set<RedisRepositoryAction> getStateActions() {
		return stateActions;
	}

	public void setStateActions(Set<RedisRepositoryAction> stateActions) {
		this.stateActions = stateActions;
	}

	@Override
	public Set<RedisRepositoryAction> getEntryActions() {
		return entryActions;
	}

	public void setEntryActions(Set<RedisRepositoryAction> entryActions) {
		this.entryActions = entryActions;
	}

	@Override
	public Set<RedisRepositoryAction> getExitActions() {
		return exitActions;
	}

	public void setExitActions(Set<RedisRepositoryAction> exitActions) {
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
		return "RedisRepositoryState [id=" + id + ", machineId=" + machineId + ", state=" + state + ", region=" + region
				+ ", initial=" + initial + ", kind=" + kind + ", submachineId=" + submachineId + ", parentState="
				+ parentState + ", stateActions=" + stateActions + ", entryActions=" + entryActions + ", exitActions="
				+ exitActions + ", deferredEvents=" + deferredEvents + "]";
	}
}
