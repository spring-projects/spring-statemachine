/*
 * Copyright 2016-2023 the original author or authors.
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

import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.transition.TransitionKind;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA entity for transitions.
 *
 * @author Janne Valkealahti
 *
 */
@Entity
@Table(name = "transition")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public class JpaRepositoryTransition extends RepositoryTransition {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Long id;

	@Column(name = "machine_id")
	private String machineId;

    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "fk_transition_source"))
	private JpaRepositoryState source;

    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "fk_transition_target"))
	private JpaRepositoryState target;

	@Column(name = "event")
	private String event;

	@Column(name = "kind")
	private TransitionKind kind;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(foreignKey = @ForeignKey(name = "fk_transition_actions_t"), inverseForeignKey = @ForeignKey(name = "fk_transition_actions_a"))
	private Set<JpaRepositoryAction> actions;

    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "fk_transition_guard"))
	private JpaRepositoryGuard guard;

	/**
	 * Instantiates a new jpa repository transition.
	 */
	public JpaRepositoryTransition() {
		this(null, null, null);
	}

	/**
	 * Instantiates a new jpa repository transition.
	 *
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 */
	public JpaRepositoryTransition(JpaRepositoryState source, JpaRepositoryState target, String event) {
		this(null, source, target, event);
	}

	/**
	 * Instantiates a new jpa repository transition.
	 *
	 * @param machineId the machine id
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 */
	public JpaRepositoryTransition(String machineId, JpaRepositoryState source, JpaRepositoryState target, String event) {
		this(machineId, source, target, event, null);
	}

	/**
	 * Instantiates a new jpa repository transition.
	 *
	 * @param machineId the machine id
	 * @param source the source
	 * @param target the target
	 * @param event the event
	 * @param actions the actions
	 */
	public JpaRepositoryTransition(String machineId, JpaRepositoryState source, JpaRepositoryState target, String event, Set<JpaRepositoryAction> actions) {
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
	public JpaRepositoryState getSource() {
		return source;
	}

	public void setSource(JpaRepositoryState source) {
		this.source = source;
	}

	@Override
	public JpaRepositoryState getTarget() {
		return target;
	}

	public void setTarget(JpaRepositoryState target) {
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
	public Set<JpaRepositoryAction> getActions() {
		return actions;
	}

	public void setActions(Set<JpaRepositoryAction> actions) {
		this.actions = actions;
	}

	@Override
	public JpaRepositoryGuard getGuard() {
		return guard;
	}

	public void setGuard(JpaRepositoryGuard guard) {
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
		return "JpaRepositoryTransition [id=" + id + ", machineId=" + machineId + ", source=" + source + ", target=" + target + ", event="
				+ event + ", kind=" + kind + ", actions=" + actions + ", guard=" + guard + "]";
	}
}
