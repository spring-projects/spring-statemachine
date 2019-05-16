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

/**
 * Various constants used in state machine lib.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class StateMachineSystemConstants {

	/** Default bean id for state machine. */
	public static final String DEFAULT_ID_STATEMACHINE = "stateMachine";

	/** Default bean id for state machine factory. */
	public static final String DEFAULT_ID_STATEMACHINEFACTORY = "stateMachineFactory";

	/** Default bean id for state machine event publisher. */
	public static final String DEFAULT_ID_EVENT_PUBLISHER = "stateMachineEventPublisher";

	/** State machine id key for headers and variables */
	public static final String STATEMACHINE_IDENTIFIER = "_sm_id_";

	/** Bean name for task executor */
	public static final String TASK_EXECUTOR_BEAN_NAME = "stateMachineTaskExecutor";

}
