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

import java.util.Date;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * A value object representing a statemachine trace event: at a particular time
 * with a simple (map) information.
 *
 * @author Janne Valkealahti
 *
 */
public final class StateMachineTrace {

	private final Date timestamp;
	private final Map<String, Object> info;

	/**
	 * Instantiate a new {@code StateMachineTrace}.
	 *
	 * @param timestamp the timestamp
	 * @param info the trace info
	 */
	public StateMachineTrace(Date timestamp, Map<String, Object> info) {
		super();
		Assert.notNull(timestamp, "Timestamp must not be null");
		Assert.notNull(info, "Info must not be null");
		this.timestamp = timestamp;
		this.info = info;
	}

	/**
	 * Gets a timestamp
	 *
	 * @return a trace timestamp
	 */
	public Date getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Gets a trace info.
	 *
	 * @return a trace info
	 */
	public Map<String, Object> getInfo() {
		return this.info;
	}
}
