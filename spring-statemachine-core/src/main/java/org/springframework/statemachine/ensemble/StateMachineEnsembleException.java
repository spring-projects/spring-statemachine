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
package org.springframework.statemachine.ensemble;

import java.io.IOException;

import org.springframework.statemachine.StateMachineException;

/**
 * General exception indicating a problem in ensemble.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineEnsembleException extends StateMachineException {

	private static final long serialVersionUID = 960498044587123343L;

	/**
	 * Instantiates a new state machine ensemble exception.
	 *
	 * @param e the e
	 */
	public StateMachineEnsembleException(IOException e) {
		super(e);
	}

	/**
	 * Instantiates a new state machine ensemble exception.
	 *
	 * @param message the message
	 * @param e the e
	 */
	public StateMachineEnsembleException(String message, Exception e) {
		super(message, e);
	}

	/**
	 * Instantiates a new state machine ensemble exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public StateMachineEnsembleException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new state machine ensemble exception.
	 *
	 * @param message the message
	 */
	public StateMachineEnsembleException(String message) {
		super(message);
	}

}
