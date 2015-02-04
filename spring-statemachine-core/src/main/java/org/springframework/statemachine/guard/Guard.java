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
package org.springframework.statemachine.guard;

import org.springframework.statemachine.StateContext;

/**
 * {@code Guard}s are typically considered as guard conditions which affect the
 * behaviour of a state machine by enabling actions or transitions only when they
 * evaluate to {@code TRUE} and disabling them when they evaluate to
 * {@code FALSE}.
 * 
 * @author Janne Valkealahti
 *
 */
public interface Guard {
	
	/**
	 * Evaluate a guard condition.
	 *
	 * @param context the state context
	 * @return true, if guard evaluation is successful, false otherwise.
	 */
	boolean evaluate(StateContext context);

}
