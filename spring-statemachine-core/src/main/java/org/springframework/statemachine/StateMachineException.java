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

import java.io.IOException;

import org.springframework.dao.NonTransientDataAccessException;

/**
 * General exception indicating a problem in interacting with statemachine.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineException extends NonTransientDataAccessException {

	private static final long serialVersionUID = 4485522802268496000L;

	/**
	 * Constructs a generic StateMachineException.
	 *
	 * @param e the {@link IOException}
	 */
	public StateMachineException(IOException e) {
		super(e.getMessage(), e);
	}

	/**
	 * Constructs a generic StateMachineException.
	 *
	 * @param message the message
	 * @param e the exception
	 */
	public StateMachineException(String message, Exception e) {
		super(message, e);
	}

	/**
	 * Constructs a generic StateMachineException.
	 *
	 * @param message the message
	 * @param cause the throwable cause
	 */
	public StateMachineException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a generic StateMachineException.
	 *
	 * @param message the message
	 */
	public StateMachineException(String message) {
		super(message);
	}
	
}
