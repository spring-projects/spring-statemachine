/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.kryo.KryoStateMachineSerialisationService;
import org.springframework.statemachine.service.StateMachineSerialisationService;
import org.springframework.util.Assert;

/**
 * Base implementation of a {@link StateMachinePersist} using Spring Data Repositories.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 * @param <M> the type of entity
 */
public abstract class RepositoryStateMachinePersist<M extends RepositoryStateMachine, S, E> implements StateMachinePersist<S, E, Object> {

	private final Log log = LogFactory.getLog(RepositoryStateMachinePersist.class);
	private final StateMachineSerialisationService<S, E> serialisationService;

	/**
	 * Instantiates a new repository state machine persist.
	 */
	protected RepositoryStateMachinePersist() {
		this.serialisationService = new KryoStateMachineSerialisationService<S, E>();
	}

	/**
	 * Instantiates a new repository state machine persist.
	 *
	 * @param serialisationService the serialisation service
	 */
	protected RepositoryStateMachinePersist(StateMachineSerialisationService<S, E> serialisationService) {
		Assert.notNull(serialisationService, "'serialisationService' must be set");
		this.serialisationService = serialisationService;
	}

	@Override
	public void write(StateMachineContext<S, E> context, Object contextObj) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Persisting context " + context + " using contextObj " + contextObj);
		}
		M build = build(context, contextObj, serialisationService.serialiseStateMachineContext(context));
		getRepository().save(build);
	}

	@Override
	public StateMachineContext<S, E> read(Object contextObj) throws Exception {
		M repositoryStateMachine = getRepository().findOne(contextObj.toString());
		if (repositoryStateMachine != null) {
			return serialisationService.deserialiseStateMachineContext(repositoryStateMachine.getStateMachineContext());
		}
		return null;
	}

	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	protected abstract StateMachineRepository<M> getRepository();

	/**
	 * Builds the generic {@link RepositoryStateMachine} entity.
	 *
	 * @param context the context
	 * @param contextObj the context obj
	 * @param serialisedContext the serialised context
	 * @return the repository state machine entity
	 */
	protected abstract M build(StateMachineContext<S, E> context, Object contextObj, byte[] serialisedContext);
}
