/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine;

import java.util.Map;

/**
 * Extended states are used to supplement state machine with a variables. If
 * extended state is used a complete condition of a state machine is a
 * combination of its state an extended state variables.
 *
 * @author Janne Valkealahti
 *
 */
public interface ExtendedState {

	/**
	 * Gets the extended state variables.
	 *
	 * @return the extended state variables
	 */
	Map<Object, Object> getVariables();

	/**
	 * Gets a variable which is automatically casted into a type.
	 *
	 * @param <T> the return type
	 * @param key the variable key
	 * @param type the variable type
	 * @return the variable
	 */
	<T> T get(Object key, Class<T> type);

	/**
	 * Sets the extended state change listener.
	 *
	 * @param listener the new extended state change listener
	 */
	void setExtendedStateChangeListener(ExtendedStateChangeListener listener);

	/**
	 * The listener interface for receiving extended state change events.
	 */
	public interface ExtendedStateChangeListener {

		/**
		 * Called when extended state variable has been changed.
		 *
		 * @param key the key
		 * @param value the value
		 */
		void changed(Object key, Object value);

	}

}
