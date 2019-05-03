/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.statemachine.boot.actuate;

import java.util.List;
import java.util.Map;

/**
 * A repository for {@link StateMachineTrace}s.
 *
 * @author Janne Valkealahti
 *
 */
public interface StateMachineTraceRepository {

	/**
	 * Find all {@link StateMachineTrace} objects contained in the repository.
	 *
	 * @return the results
	 */
	List<StateMachineTrace> findAll();

	/**
	 * Add a new {@link StateMachineTrace} object at the current time.
	 *
	 * @param traceInfo trace information
	 */
	void add(Map<String, Object> traceInfo);
}
