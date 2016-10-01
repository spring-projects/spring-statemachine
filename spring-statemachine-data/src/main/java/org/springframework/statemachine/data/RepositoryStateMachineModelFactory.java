/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.data;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;

/**
 * A generic {@link StateMachineModelFactory} which is backed by a Spring Data
 * Repository abstraction.
 *
 * @author Janne Valkealahti
 *
 */
public class RepositoryStateMachineModelFactory implements StateMachineModelFactory<String, String> {

	private final StateRepository<? extends RepositoryState> stateRepository;
	private final TransitionRepository<? extends RepositoryTransition> transitionRepository;

	/**
	 * Instantiates a new repository state machine model factory.
	 *
	 * @param stateRepository the state repository
	 * @param transitionRepository the transition repository
	 */
	public RepositoryStateMachineModelFactory(StateRepository<? extends RepositoryState> stateRepository,
			TransitionRepository<? extends RepositoryTransition> transitionRepository) {
		this.stateRepository = stateRepository;
		this.transitionRepository = transitionRepository;
	}

	@Override
	public StateMachineModel<String, String> build() {
		return build(null);
	}

	@Override
	public StateMachineModel<String, String> build(String machineId) {
		ConfigurationData<String, String> configurationData = new ConfigurationData<>();

		Collection<StateData<String, String>> stateData = new ArrayList<>();
		for (RepositoryState s : stateRepository.findByMachineId(machineId)) {
			stateData.add(new StateData<String, String>(s.getParentState(), null, s.getState(), s.isInitial()));
		}
		StatesData<String, String> statesData = new StatesData<>(stateData);

		Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
		for (RepositoryTransition t : transitionRepository.findByMachineId(machineId)) {
			transitionData.add(new TransitionData<String, String>(t.getSource(), t.getTarget(), t.getEvent()));
		}
		TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

		StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);
		return stateMachineModel;
	}
}
