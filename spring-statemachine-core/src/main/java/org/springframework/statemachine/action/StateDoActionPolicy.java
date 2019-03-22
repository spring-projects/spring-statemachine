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
package org.springframework.statemachine.action;

/**
 * Enumerations for possible state do action policies. {@code IMMEDIATE_CANCEL}
 * is a default setting.
 *
 * @author Janne Valkealahti
 *
 */
public enum StateDoActionPolicy {

	/**
	 * Policy interrupting action immediately when state is exited.
	 */
	IMMEDIATE_CANCEL,

	/**
	 * Policy interrupting action after a timeout before state is exited.
	 */
	TIMEOUT_CANCEL;
}
