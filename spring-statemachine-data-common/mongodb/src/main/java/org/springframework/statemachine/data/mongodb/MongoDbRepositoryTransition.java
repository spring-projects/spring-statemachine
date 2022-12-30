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
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.transition.TransitionKind;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * MongoDb entity for transitions.
 *
 * @author Janne Valkealahti
 *
 */
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
@Document(collection = "MongoDbRepositoryTransition")
public class MongoDbRepositoryTransition extends RepositoryTransition {

	@Id
	private String id;

	private String machineId = "";

	@Reference
	private MongoDbRepositoryState source;

	@Reference
	private MongoDbRepositoryState target;

	private String event;
	private TransitionKind kind;

	@Reference
	private Set<MongoDbRepositoryAction> actions;

	@Reference
	private MongoDbRepositoryGuard guard;

	/**
	 * Instantiates a new MongoDb repository transition.
	 */
	public MongoDbRepositoryTransition() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new MongoDb repository transition.
	 *
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 */
	public MongoDbRepositoryTransition(MongoDbRepositoryState source, MongoDbRepositoryState target, String event) {
		this(null, source, target, event);
	}

	/**
	 * Instantiates a new MongoDb repository transition.
	 *
	 * @param machineId the machine id
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 */
	public MongoDbRepositoryTransition(String machineId, MongoDbRepositoryState source, MongoDbRepositoryState target, String event) {
		this(machineId, source, target, event, null);
	}

	/**
	 * Instantiates a new MongoDb repository transition.
	 *
	 * @param machineId the machine id
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 * @param actions the actions
	 */
	public MongoDbRepositoryTransition(String machineId, MongoDbRepositoryState source, MongoDbRepositoryState target, String event, Set<MongoDbRepositoryAction> actions) {
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
	public MongoDbRepositoryState getSource() {
		return source;
	}

	public void setSource(MongoDbRepositoryState source) {
		this.source = source;
	}

	@Override
	public MongoDbRepositoryState getTarget() {
		return target;
	}

	public void setTarget(MongoDbRepositoryState target) {
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
	public Set<MongoDbRepositoryAction> getActions() {
		return actions;
	}

	public void setActions(Set<MongoDbRepositoryAction> actions) {
		this.actions = actions;
	}

	@Override
	public MongoDbRepositoryGuard getGuard() {
		return guard;
	}

	public void setGuard(MongoDbRepositoryGuard guard) {
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
		return "MongoDbRepositoryTransition [id=" + id + ", machineId=" + machineId + ", source=" + source + ", target=" + target + ", event="
				+ event + ", kind=" + kind + ", actions=" + actions + ", guard=" + guard + "]";
	}
}
