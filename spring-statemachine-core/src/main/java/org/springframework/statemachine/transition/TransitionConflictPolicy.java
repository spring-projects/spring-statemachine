/*
 * Copyright 2017-2018 the original author or authors.
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
 * Enumerations for possible transition conflict policies. In case multiple
 * transitions become possible, thus machine having a need to choose one, these
 * policies determine if transition is picked from originating child or parent
 * states.
 *
 * @author Janne Valkealahti
 *
 */
public enum TransitionConflictPolicy {

	/**
	 * Policy choosing transition from a child.
	 */
	CHILD,

	/**
	 * Policy choosing from a parent.
	 */
	PARENT;
}
