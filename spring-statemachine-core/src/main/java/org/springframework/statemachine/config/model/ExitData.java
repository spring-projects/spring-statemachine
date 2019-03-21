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

/**
 * A simple data object keeping exitpoint related configs in a same place.
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class ExitData<S, E> {
	private final S source;
	private final S target;

	/**
	 * Instantiates a new exit data.
	 *
	 * @param source the source
	 * @param target the target
	 */
	public ExitData(S source, S target) {
		this.source = source;
		this.target = target;
	}

	/**
	 * Gets the source.
	 *
	 * @return the source
	 */
	public S getSource() {
		return source;
	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public S getTarget() {
		return target;
	}
}