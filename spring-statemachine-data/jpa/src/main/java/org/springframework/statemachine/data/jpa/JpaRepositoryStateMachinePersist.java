/*
 * Copyright 2017-2026 the original author or authors.
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

import java.util.function.Consumer;

import com.esotericsoftware.kryo.Kryo;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.data.RepositoryStateMachinePersist;
import org.springframework.statemachine.data.StateMachineRepository;
import org.springframework.statemachine.service.StateMachineSerialisationService;

/**
 * {@code JPA} based implementation of a {@link RepositoryStateMachinePersist}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class JpaRepositoryStateMachinePersist<S, E> extends RepositoryStateMachinePersist<JpaRepositoryStateMachine, S, E> {

	private final JpaStateMachineRepository jpaStateMachineRepository;

	/**
	 * Instantiates a new jpa repository state machine persist.
	 *
	 * @param jpaStateMachineRepository the jpa state machine repository
	 */
	public JpaRepositoryStateMachinePersist(JpaStateMachineRepository jpaStateMachineRepository) {
		super();
		this.jpaStateMachineRepository = jpaStateMachineRepository;
	}

	/**
	 * Instantiates a new jpa repository state machine persist with a Kryo
	 * customizer for registering application-specific types.
	 *
	 * @param jpaStateMachineRepository the jpa state machine repository
	 * @param kryoCustomizer callback applied to each new Kryo instance after
	 *        the framework defaults; use it to register state/event enums
	 * @since 4.0.2
	 */
	public JpaRepositoryStateMachinePersist(JpaStateMachineRepository jpaStateMachineRepository,
			Consumer<Kryo> kryoCustomizer) {
		super(kryoCustomizer);
		this.jpaStateMachineRepository = jpaStateMachineRepository;
	}

	/**
	 * Instantiates a new jpa repository state machine persist.
	 *
	 * @param jpaStateMachineRepository the jpa state machine repository
	 * @param serialisationService the serialisation service
	 */
	public JpaRepositoryStateMachinePersist(JpaStateMachineRepository jpaStateMachineRepository,
			StateMachineSerialisationService<S, E> serialisationService) {
		super(serialisationService);
		this.jpaStateMachineRepository = jpaStateMachineRepository;
	}

	@Override
	protected StateMachineRepository<JpaRepositoryStateMachine> getRepository() {
		return jpaStateMachineRepository;
	}

	@Override
	protected JpaRepositoryStateMachine build(StateMachineContext<S, E> context, Object contextObj, byte[] serialisedContext) {
		JpaRepositoryStateMachine jpaRepositoryStateMachine = new JpaRepositoryStateMachine();
		jpaRepositoryStateMachine.setMachineId(context.getId());
		jpaRepositoryStateMachine.setState(context.getState() != null ? context.getState().toString() : null);
		jpaRepositoryStateMachine.setStateMachineContext(serialisedContext);
		return jpaRepositoryStateMachine;
	}
}
