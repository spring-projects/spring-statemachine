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
package org.springframework.statemachine.processor;

/**
 * Defines a strategy of processing a state machine and returning
 * some Object (or null).
 *
 * @author Janne Valkealahti
 *
 * @param <T> the return type
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineRuntimeProcessor<T, S, E> {

	/**
	 * Process the container based on information available
	 * from {@link StateMachineRuntime}.
	 *
	 * @param stateMachineRuntime the yarn container runtime
	 * @return the result
	 */
	T process(StateMachineRuntime<S, E> stateMachineRuntime);

}
