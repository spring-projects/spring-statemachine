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
package org.springframework.statemachine.data.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.statemachine.data.RepositoryStateMachine;

/**
 * A {@link RepositoryStateMachine} interface for Redis used for states machines.
 *
 * @author Janne Valkealahti
 *
 */
@Document(collection = "MongoDbRepositoryStateMachine")
public class MongoDbRepositoryStateMachine extends RepositoryStateMachine {

	@Id
	private String id;

	private String machineId;
	private String state;
	private byte[] stateMachineContext;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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
