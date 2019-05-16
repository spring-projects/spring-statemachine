/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.state;

/**
 * Default implementation of a {@link PseudoStateContext}.
 * 
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultPseudoStateContext<S, E> implements PseudoStateContext<S, E> {

	private final PseudoState<S, E> pseudoState;
	
	private final PseudoAction pseudoAction;
	
	/**
	 * Instantiates a new default pseudo state context.
	 *
	 * @param pseudoState the pseudo state
	 * @param pseudoAction the pseudo action
	 */
	public DefaultPseudoStateContext(PseudoState<S, E> pseudoState, PseudoAction pseudoAction) {
		this.pseudoState = pseudoState;
		this.pseudoAction = pseudoAction;
	}

	@Override
	public PseudoState<S, E> getPseudoState() {
		return pseudoState;
	}
	
	@Override
	public PseudoAction getPseudoAction() {
		return pseudoAction;
	}

}
