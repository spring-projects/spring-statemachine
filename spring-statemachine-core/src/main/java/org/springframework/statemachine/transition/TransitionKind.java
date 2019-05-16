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
package org.springframework.statemachine.transition;

/**
 * Defines enumeration of a {@link Transition} kind. This is uses within a
 * transition to indicate whether its type is external, internal or local.
 *
 * @author Janne Valkealahti
 *
 */
public enum TransitionKind {

	/** Indicates an external transition kind. */
	EXTERNAL,

	/** Indicates an internal transition kind. */
	INTERNAL,

	/** Indicates a local transition kind. */
	LOCAL,

	/** Indicates an initial transition kind. */
	INITIAL

}
