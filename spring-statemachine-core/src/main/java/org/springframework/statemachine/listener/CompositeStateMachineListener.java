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
package org.springframework.statemachine.listener;

import java.util.Iterator;

import org.springframework.statemachine.state.State;

public class CompositeStateMachineListener<S,E> extends AbstractCompositeListener<StateMachineListener<State<S,E>, E>> implements
		StateMachineListener<State<S,E>, E> {

	@Override
	public void stateChanged(State<S, E> from, State<S, E> to) {
		for (Iterator<StateMachineListener<State<S,E>, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<State<S,E>, E> listener = iterator.next();
			listener.stateChanged(from, to);
		}
	}

}
