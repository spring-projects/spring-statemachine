/*
 * Copyright 2017-2018 the original author or authors.
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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.springframework.statemachine.data.RepositoryStateMachine;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * A {@link RepositoryStateMachine} interface for JPA used for states machines.
 *
 * @author Janne Valkealahti
 *
 */
@Entity
@Table(name = "StateMachine")
@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
public class JpaRepositoryStateMachine extends RepositoryStateMachine {

	@Id
	private String machineId;

	private String state;

	@Lob
	private byte[] stateMachineContext;

	@Override
	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	@Override
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public byte[] getStateMachineContext() {
		return stateMachineContext;
	}

	public void setStateMachineContext(byte[] stateMachineContext) {
		this.stateMachineContext = stateMachineContext;
	}

}
