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

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.data.RepositoryStateMachinePersist;
import org.springframework.statemachine.data.StateMachineRepository;
import org.springframework.statemachine.service.StateMachineSerialisationService;

/**
 * {@code MongoDb} based implementation of a {@link RepositoryStateMachinePersist}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class MongoDbRepositoryStateMachinePersist<S, E> extends RepositoryStateMachinePersist<MongoDbRepositoryStateMachine, S, E> {

	private final MongoDbStateMachineRepository mongodbStateMachineRepository;

	/**
	 * Instantiates a new mongodb repository state machine persist.
	 *
	 * @param mongodbStateMachineRepository the mongodb state machine repository
	 */
	public MongoDbRepositoryStateMachinePersist(MongoDbStateMachineRepository mongodbStateMachineRepository) {
		super();
		this.mongodbStateMachineRepository = mongodbStateMachineRepository;
	}

	/**
	 * Instantiates a new mongodb repository state machine persist.
	 *
	 * @param mongodbStateMachineRepository the mongodb state machine repository
	 * @param serialisationService the serialisation service
	 */
	public MongoDbRepositoryStateMachinePersist(MongoDbStateMachineRepository mongodbStateMachineRepository,
			StateMachineSerialisationService<S, E> serialisationService) {
		super(serialisationService);
		this.mongodbStateMachineRepository = mongodbStateMachineRepository;
	}

	@Override
	protected StateMachineRepository<MongoDbRepositoryStateMachine> getRepository() {
		return mongodbStateMachineRepository;
	}

	@Override
	protected MongoDbRepositoryStateMachine build(StateMachineContext<S, E> context, Object contextObj, byte[] serialisedContext) {
		MongoDbRepositoryStateMachine mongodbRepositoryStateMachine = new MongoDbRepositoryStateMachine();
		mongodbRepositoryStateMachine.setId(contextObj.toString());
		mongodbRepositoryStateMachine.setMachineId(context.getId());
		mongodbRepositoryStateMachine.setState(context.getState().toString());
		mongodbRepositoryStateMachine.setStateMachineContext(serialisedContext);
		return mongodbRepositoryStateMachine;
	}
}
