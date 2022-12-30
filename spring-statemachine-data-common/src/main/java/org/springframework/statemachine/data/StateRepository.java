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
package org.springframework.statemachine.data;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * Generic {@link Repository} interface for states.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the state entity type
 */
@NoRepositoryBean
public interface StateRepository<S extends RepositoryState> extends CrudRepository<S, Long> {

	/**
	 * Find states by machine id.
	 *
	 * @param machineId the machine id
	 * @return the list of transitions
	 */
	List<S> findByMachineId(String machineId);
}
