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
package org.springframework.statemachine.config.model;

import java.io.IOException;

import org.springframework.statemachine.StateMachineException;

/**
 * Generic exception indicating ill-formed state machine configuration.
 *
 * @author Janne Valkealahti
 *
 */
public class MalformedConfigurationException extends StateMachineException {

	private static final long serialVersionUID = -1658322647044177891L;

	private StringBuilder trace;

	/**
	 * Instantiates a new malformed configuration exception.
	 *
	 * @param e the e
	 */
	public MalformedConfigurationException(IOException e) {
		super(e);
	}

	/**
	 * Instantiates a new malformed configuration exception.
	 *
	 * @param message the message
	 * @param e the e
	 */
	public MalformedConfigurationException(String message, Exception e) {
		super(message, e);
	}

	/**
	 * Instantiates a new malformed configuration exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public MalformedConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new malformed configuration exception.
	 *
	 * @param message the message
	 */
	public MalformedConfigurationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new malformed configuration exception.
	 *
	 * @param message the message
	 * @param infos the infos
	 */
	public MalformedConfigurationException(String message, String... infos) {
		super(message);
		for (String info : infos) {
			addTrace(info);
		}
	}

	@Override
	public String getMessage() {
		if (trace == null) return super.getMessage();
		StringBuilder buffer = new StringBuilder(512);
		buffer.append(super.getMessage());
		if (buffer.length() > 0) {
			buffer.append('\n');
		}
		buffer.append("Statemachine trace:");
		buffer.append(trace);
		return buffer.toString();
	}

	/**
	 * Adds a trace info into this exception.
	 *
	 * @param info the info
	 */
	public void addTrace(String info) {
		if (info == null) {
			throw new IllegalArgumentException("info cannot be null.");
		}
		if (trace == null) {
			trace = new StringBuilder(256);
		}
		trace.append('\n');
		trace.append(info);
	}
}
