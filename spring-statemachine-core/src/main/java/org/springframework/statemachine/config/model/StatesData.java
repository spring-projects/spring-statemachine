/*
 * Copyright 2015-2016 the original author or authors.
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
package org.springframework.statemachine.config.model;

import java.util.Collection;

import org.springframework.statemachine.config.configurers.StateConfigurer;

/**
 * Data object used return and build data from a {@link StateConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StatesData<S, E> {

	private final Collection<StateData<S, E>> stateData;

	/**
	 * Instantiates a new states data.
	 *
	 * @param stateData the state data
	 */
	public StatesData(Collection<StateData<S, E>> stateData) {
		this.stateData = stateData;
	}

	/**
	 * Gets the state data.
	 *
	 * @return the state data
	 */
	public Collection<StateData<S, E>> getStateData() {
		return stateData;
	}

}
