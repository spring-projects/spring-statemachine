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
package org.springframework.statemachine.access;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.statemachine.StateMachine;

/**
 * Functional interface for {@link StateMachine} to allow more programmatic
 * access to underlying functionality. Functions prefixed "doWith" will expose
 * {@link StateMachineAccess} via {@link Consumer} for better functional
 * access with jdk8. Functions prefixed "with" is better suitable for lambdas.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineAccessor<S, E> {

	/**
	 * Execute given function with all recursive regions.
	 *
	 * @param stateMachineAccess the state machine access
	 */
	void doWithAllRegions(Consumer<StateMachineAccess<S, E>> stateMachineAccess);

	/**
	 * Gets all regions.
	 *
	 * @return the all regions
	 */
	List<StateMachineAccess<S, E>> withAllRegions();

	/**
	 * Execute given function with a region.
	 *
	 * @param stateMachineAccess the state machine access
	 */
	void doWithRegion(Consumer<StateMachineAccess<S, E>> stateMachineAccess);

	/**
	 * Get a region.
	 *
	 * @return the state machine access
	 */
	StateMachineAccess<S, E> withRegion();

}
