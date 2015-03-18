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

public interface StateConfigurer<S, E> extends
		AnnotationConfigurerBuilder<StateMachineStateConfigurer<S, E>> {

	StateConfigurer<S, E> initial(S initial);

	StateConfigurer<S, E> parent(S state);

	StateConfigurer<S, E> state(S state);

	StateConfigurer<S, E> state(S state, Collection<? extends Action<S, E>> entryActions,
			Collection<? extends Action<S, E>> exitActions);

	StateConfigurer<S, E> state(S state, Action<S, E> entryAction, Action<S, E> exitAction);

	StateConfigurer<S, E> state(S state, E... deferred);

	StateConfigurer<S, E> states(Set<S> states);

	StateConfigurer<S, E> end(S end);

}
