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
package org.springframework.statemachine.state;

/**
 * Defines enumeration of a {@link PseudoState} kind. This is used within a
 * transitive states indicating its kind.
 *
 * @author Janne Valkealahti
 *
 */
public enum PseudoStateKind {

	/** Indicates an initial kind. */
	INITIAL,

	/** End or terminate kind */
	END,

	/** Choice kind */
	CHOICE,

	/** History deep kind */
	HISTORY_DEEP,

	/** History shallow kind */
	HISTORY_SHALLOW,

	/** Fork kind */
	FORK,

	/** Join kind */
	JOIN

}
