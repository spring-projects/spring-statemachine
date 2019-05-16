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
package org.springframework.statemachine.config.builders;

import org.springframework.statemachine.config.configurers.ChoiceTransitionConfigurer;
import org.springframework.statemachine.config.configurers.EntryTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ExitTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ExternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.ForkTransitionConfigurer;
import org.springframework.statemachine.config.configurers.HistoryTransitionConfigurer;
import org.springframework.statemachine.config.configurers.InternalTransitionConfigurer;
import org.springframework.statemachine.config.configurers.JoinTransitionConfigurer;
import org.springframework.statemachine.config.configurers.JunctionTransitionConfigurer;
import org.springframework.statemachine.config.configurers.LocalTransitionConfigurer;

/**
 * Configurer interface exposing different type of transitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineTransitionConfigurer<S, E> {

	/**
	 * Gets a configurer for external transition.
	 *
	 * @return {@link ExternalTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 * @see #withLocal()
	 */
	ExternalTransitionConfigurer<S, E> withExternal() throws Exception;

	/**
	 * Gets a configurer for internal transition. Internal transition is used
	 * when action needs to be executed without causing a state transition. With
	 * internal transition source and target state is always a same and it is
	 * identical with self-transition in the absence of state entry and exit
	 * actions.
	 *
	 * @return {@link InternalTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	InternalTransitionConfigurer<S, E> withInternal() throws Exception;

	/**
	 * Gets a configurer for local transition. Local transition doesn’t cause
	 * exit and entry to source state if target state is a substate of a source
	 * state. Other way around, local transition doesn’t cause exit and entry to
	 * target state if target is a superstate of a source state.
	 *
	 * @return {@link LocalTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	LocalTransitionConfigurer<S, E> withLocal() throws Exception;

	/**
	 * Gets a configurer for transition from a choice pseudostate.
	 *
	 * @return {@link ChoiceTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	ChoiceTransitionConfigurer<S, E> withChoice() throws Exception;

	/**
	 * Gets a configurer for transition from a junction pseudostate.
	 *
	 * @return {@link JunctionTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	JunctionTransitionConfigurer<S, E> withJunction() throws Exception;

	/**
	 * Gets a configurer for transition from a fork pseudostate.
	 *
	 * @return {@link ForkTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	ForkTransitionConfigurer<S, E> withFork() throws Exception;

	/**
	 * Gets a configurer for transition from a join pseudostate.
	 *
	 * @return {@link JoinTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	JoinTransitionConfigurer<S, E> withJoin() throws Exception;

	/**
	 * Gets a configurer for transition from an entrypoint pseudostate.
	 *
	 * @return {@link EntryTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	EntryTransitionConfigurer<S, E> withEntry() throws Exception;

	/**
	 * Gets a configurer for transition from an exitpoint pseudostate.
	 *
	 * @return {@link ExitTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	ExitTransitionConfigurer<S, E> withExit() throws Exception;

	/**
	 * Gets a configurer for default history transition.
	 *
	 * @return {@link HistoryTransitionConfigurer} for chaining
	 * @throws Exception if configuration error happens
	 */
	HistoryTransitionConfigurer<S, E> withHistory() throws Exception;
}
