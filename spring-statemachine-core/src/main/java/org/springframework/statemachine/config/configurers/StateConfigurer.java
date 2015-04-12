/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.config.configurers;

import java.util.Collection;
import java.util.Set;

import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerBuilder;
import org.springframework.statemachine.state.State;

/**
 * Base {@code StateConfigurer} interface for configuring {@link State}s.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateConfigurer<S, E> extends
		AnnotationConfigurerBuilder<StateMachineStateConfigurer<S, E>> {

	/**
	 * Specify a initial state {@code S}.
	 *
	 * @param initial the initial state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> initial(S initial);

	/**
	 * Specify a initial state {@code S} with an {@link Action} to be executed
	 * with it. Action can be i.e. used to init extended variables.
	 *
	 * @param initial the initial state
	 * @param action the action
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> initial(S initial, Action<S, E> action);

	/**
	 * Specify a states configured by this configurer instance to be
	 * substates of state {@code S}.
	 *
	 * @param state the parent state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> parent(S state);

	/**
	 * Specify a state {@code S}.
	 *
	 * @param state the state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state);

	/**
	 * Specify a state {@code S} with entry and exit {@link Actions}s.
	 *
	 * @param state the state
	 * @param entryActions the state entry actions
	 * @param exitActions the state exit actions
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions);

	/**
	 * Specify a state {@code S} with entry and exit {@link Actions}.
	 *
	 * @param state the state
	 * @param entryAction the state entry action
	 * @param exitAction the state exit action
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, Action<S, E> entryAction, Action<S, E> exitAction);

	/**
	 * Specify a state {@code S} with a deferred events {@code E}.
	 *
	 * @param state the state
	 * @param deferred the deferred events
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, E... deferred);

	/**
	 * Specify a states {@code S}.
	 *
	 * @param states the states
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> states(Set<S> states);

	/**
	 * Specify a state {@code S} to be end state.
	 *
	 * @param end the end state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> end(S end);

}
