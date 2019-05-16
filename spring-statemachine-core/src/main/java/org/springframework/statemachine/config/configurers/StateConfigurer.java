/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.statemachine.config.configurers;

import java.util.Collection;
import java.util.Set;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineFactory;
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
	 * Specify a state {@code S} and its relation with a given
	 * machine as substate machine.
	 *
	 * @param state the state
	 * @param stateMachine the submachine
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, StateMachine<S, E> stateMachine);

	/**
	 * Specify a state {@code S} and its relation with a given
	 * machine as substate machine factory.
	 *
	 * @param state the state
	 * @param stateMachineFactory the submachine factory
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, StateMachineFactory<S, E> stateMachineFactory);

	/**
	 * Specify a state {@code S} with state {@link Action}s.
	 *
	 * @param state the state
	 * @param stateActions the state actions
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> stateActions);

	/**
	 * Specify a state {@code S} with state {@link Action}.
	 *
	 * @param state the state
	 * @param stateAction the state action
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, Action<S, E> stateAction);

	/**
	 * Specify a state {@code S} with state behaviour {@link Action}.
	 * Currently synonym for {@link #state(Object, Action)}.
	 *
	 * @param state the state
	 * @param action the state action
	 * @return configurer for chaining
	 * @see #state(Object, Action)
	 */
	StateConfigurer<S, E> stateDo(S state, Action<S, E> action);

	/**
	 * Specify a state {@code S} with state behaviour {@link Action} and
	 * error {@link Action} callback.
	 *
	 * @param state the state
	 * @param action the state action
	 * @param error action that will be called if any unexpected exception is thrown by the action.
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> stateDo(S state, Action<S, E> action, Action<S, E> error);

	/**
	 * Specify a state {@code S} with entry and exit {@link Action}s.
	 *
	 * @param state the state
	 * @param entryActions the state entry actions
	 * @param exitActions the state exit actions
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions);

	/**
	 * Specify a state {@code S} with entry and exit {@link Action}.
	 *
	 * @param state the state
	 * @param entryAction the state entry action
	 * @param exitAction the state exit action
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> state(S state, Action<S, E> entryAction, Action<S, E> exitAction);

	/**
	 * Specify a state {@code S} with state entry {@link Action}.
	 * Currently synonym for {@link #state(Object, Action, Action)}
	 * with no exit action.
	 *
	 * @param state the state
	 * @param action the state entry action
	 * @return configurer for chaining
	 * @see #state(Object, Action, Action)
	 */
	StateConfigurer<S, E> stateEntry(S state, Action<S, E> action);

	/**
	 * Specify a state {@code S} with state entry {@link Action} and
	 * error {@link Action} callback.
	 * Currently synonym for {@link #state(Object, Action, Action)}
	 * with no exit action.
	 *
	 * @param state the state
	 * @param action the state entry action
	 * @param error action that will be called if any unexpected exception is thrown by the action.
	 * @return configurer for chaining
	 * @see #state(Object, Action, Action)
	 */
	StateConfigurer<S, E> stateEntry(S state, Action<S, E> action, Action<S, E> error);

	/**
	 * Specify a state {@code S} with state exit {@link Action}.
	 * Currently synonym for {@link #state(Object, Action, Action)}
	 * with no entry action.
	 *
	 * @param state the state
	 * @param action the state exit action
	 * @return configurer for chaining
	 * @see #state(Object, Action, Action)
	 */
	StateConfigurer<S, E> stateExit(S state, Action<S, E> action);

	/**
	 * Specify a state {@code S} with state exit {@link Action} and
	 * error {@link Action} callback.
	 * Currently synonym for {@link #state(Object, Action, Action)}
	 * with no entry action.
	 *
	 * @param state the state
	 * @param action the state entry action
	 * @param error action that will be called if any unexpected exception is thrown by the action.
	 * @return configurer for chaining
	 * @see #state(Object, Action, Action)
	 */
	StateConfigurer<S, E> stateExit(S state, Action<S, E> action, Action<S, E> error);

	/**
	 * Specify a state {@code S} with a deferred events {@code E}.
	 *
	 * @param state the state
	 * @param deferred the deferred events
	 * @return configurer for chaining
	 */
	@SuppressWarnings("unchecked")
	StateConfigurer<S, E> state(S state, E... deferred);

	/**
	 * Specify a states {@code S}.
	 *
	 * @param states the states
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> states(Set<S> states);

	/**
	 * Specify a state {@code S} to be end state. This method
	 * can be called for each state to be marked as end state.
	 *
	 * @param end the end state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> end(S end);

	/**
	 * Specify a state {@code S} to be choice pseudo state.
	 *
	 * @param choice the choice pseudo state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> choice(S choice);

	/**
	 * Specify a state {@code S} to be junction pseudo state.
	 *
	 * @param junction the junction pseudo state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> junction(S junction);

	/**
	 * Specify a state {@code S} to be fork pseudo state.
	 *
	 * @param fork the fork pseudo state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> fork(S fork);

	/**
	 * Specify a state {@code S} to be join pseudo state.
	 *
	 * @param join the join pseudo state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> join(S join);

	/**
	 * Specify a state {@code S} to be history pseudo state.
	 *
	 * @param history the history pseudo state
	 * @param type the history pseudo state type
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> history(S history, History type);

	/**
	 * Specify a state {@code S} to be entrypoint pseudo state.
	 *
	 * @param entry the entrypoint pseudo state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> entry(S entry);

	/**
	 * Specify a state {@code S} to be exitpoint pseudo state.
	 *
	 * @param exit the exitpoint pseudo state
	 * @return configurer for chaining
	 */
	StateConfigurer<S, E> exit(S exit);

	/**
	 * Enumeration of a possible history pseudostate type.
	 */
	public enum History {

		/**
		 * Shallow history is a pseudo state representing the most
		 * recent substate of a submachine.
		 */
		SHALLOW,

		/**
		 * Deep history is a shallow history recursively reactivating
		 * the substates of the most recent substate.
		 */
		DEEP
	}
}
