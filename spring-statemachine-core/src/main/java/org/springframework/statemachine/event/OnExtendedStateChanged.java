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
package org.springframework.statemachine.event;

/**
 * Generic event representing that extended state variable has been changed.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("serial")
public class OnExtendedStateChanged extends StateMachineEvent {

	private final Object key;
	private final Object value;

	/**
	 * Instantiates a new on extended state changed.
	 *
	 * @param source the source
	 * @param key the key
	 * @param value the value
	 */
	public OnExtendedStateChanged(Object source, Object key, Object value) {
		super(source);
		this.key = key;
		this.value = value;
	}

	/**
	 * Gets the modified extended state variable key.
	 *
	 * @return the key
	 */
	public Object getKey() {
		return key;
	}

	/**
	 * Gets the modified extended state variable value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "OnExtendedStateChanged [key=" + key + ", value=" + value + "]";
	}

}
