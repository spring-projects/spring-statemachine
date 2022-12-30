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
package org.springframework.statemachine.data.redis;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.data.RepositoryStateMachinePersist;
import org.springframework.statemachine.data.StateMachineRepository;
import org.springframework.statemachine.service.StateMachineSerialisationService;

/**
 * {@code Redis} based implementation of a {@link RepositoryStateMachinePersist}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class RedisRepositoryStateMachinePersist<S, E> extends RepositoryStateMachinePersist<RedisRepositoryStateMachine, S, E> {

	private final RedisStateMachineRepository redisStateMachineRepository;

	/**
	 * Instantiates a new redis repository state machine persist.
	 *
	 * @param redisStateMachineRepository the redis state machine repository
	 */
	public RedisRepositoryStateMachinePersist(RedisStateMachineRepository redisStateMachineRepository) {
		super();
		this.redisStateMachineRepository = redisStateMachineRepository;
	}

	/**
	 * Instantiates a new redis repository state machine persist.
	 *
	 * @param redisStateMachineRepository the redis state machine repository
	 * @param serialisationService the serialisation service
	 */
	public RedisRepositoryStateMachinePersist(RedisStateMachineRepository redisStateMachineRepository,
			StateMachineSerialisationService<S, E> serialisationService) {
		super(serialisationService);
		this.redisStateMachineRepository = redisStateMachineRepository;
	}

	@Override
	protected StateMachineRepository<RedisRepositoryStateMachine> getRepository() {
		return redisStateMachineRepository;
	}

	@Override
	protected RedisRepositoryStateMachine build(StateMachineContext<S, E> context, Object contextObj, byte[] serialisedContext) {
		RedisRepositoryStateMachine redisRepositoryStateMachine = new RedisRepositoryStateMachine();
		redisRepositoryStateMachine.setId(contextObj.toString());
		redisRepositoryStateMachine.setMachineId(context.getId());
		redisRepositoryStateMachine.setState(context.getState().toString());
		redisRepositoryStateMachine.setStateMachineContext(serialisedContext);
		return redisRepositoryStateMachine;
	}
}
