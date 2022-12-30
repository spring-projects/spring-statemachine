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

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.OneToMany;
//import javax.persistence.OneToOne;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.transition.TransitionKind;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Redis entity for transitions.
 *
 * @author Janne Valkealahti
 *
 */
@RedisHash("RedisRepositoryTransition")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public class RedisRepositoryTransition extends RepositoryTransition {

	@Id
	private String id;

	@Indexed
	private String machineId = "";

	@Reference
	private RedisRepositoryState source;

	@Reference
	private RedisRepositoryState target;

	private String event;
	private TransitionKind kind;

	@Reference
	private Set<RedisRepositoryAction> actions;

	@Reference
	private RedisRepositoryGuard guard;

	/**
	 * Instantiates a new redis repository transition.
	 */
	public RedisRepositoryTransition() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new redis repository transition.
	 *
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 */
	public RedisRepositoryTransition(RedisRepositoryState source, RedisRepositoryState target, String event) {
		this(null, source, target, event);
	}

	/**
	 * Instantiates a new redis repository transition.
	 *
	 * @param machineId the machine id
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 */
	public RedisRepositoryTransition(String machineId, RedisRepositoryState source, RedisRepositoryState target, String event) {
		this(machineId, source, target, event, null);
	}

	/**
	 * Instantiates a new redis repository transition.
	 *
	 * @param machineId the machine id
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 * @param actions the actions
	 */
	public RedisRepositoryTransition(String machineId, RedisRepositoryState source, RedisRepositoryState target, String event, Set<RedisRepositoryAction> actions) {
		this.machineId = machineId == null ? "" : machineId;
		this.source = source;
		this.target = target;
		this.event = event;
		this.actions = actions;
	}

	@Override
	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	@Override
	public RedisRepositoryState getSource() {
		return source;
	}

	public void setSource(RedisRepositoryState source) {
		this.source = source;
	}

	@Override
	public RedisRepositoryState getTarget() {
		return target;
	}

	public void setTarget(RedisRepositoryState target) {
		this.target = target;
	}

	@Override
	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	@Override
	public Set<RedisRepositoryAction> getActions() {
		return actions;
	}

	public void setActions(Set<RedisRepositoryAction> actions) {
		this.actions = actions;
	}

	@Override
	public RedisRepositoryGuard getGuard() {
		return guard;
	}

	public void setGuard(RedisRepositoryGuard guard) {
		this.guard = guard;
	}

	@Override
	public TransitionKind getKind() {
		return kind;
	}

	public void setKind(TransitionKind kind) {
		this.kind = kind;
	}

	@Override
	public String toString() {
		return "RedisRepositoryTransition [id=" + id + ", machineId=" + machineId + ", source=" + source + ", target=" + target + ", event="
				+ event + ", kind=" + kind + ", actions=" + actions + ", guard=" + guard + "]";
	}
}
