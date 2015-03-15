/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;

/**
 * Default implementation of a {@link ExtendedState}.
 *
 * @author Janne Valkealahti
 *
 */
public class DefaultExtendedState implements ExtendedState {

	private final Map<Object, Object> variables;

	/**
	 * Instantiates a new default extended state.
	 */
	public DefaultExtendedState() {
		this.variables = new HashMap<Object, Object>();
	}

	@Override
	public Map<Object, Object> getVariables() {
		return variables;
	}

}
