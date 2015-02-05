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
package org.springframework.statemachine.state;

/**
 * Base implementation of a {@link PseudoState}.
 * 
 * @author Janne Valkealahti
 *
 */
public abstract class AbstractPseudoState implements PseudoState {

	private final PseudoStateKind kind;

	/**
	 * Instantiates a new abstract pseudo state.
	 *
	 * @param kind the kind
	 */
	public AbstractPseudoState(PseudoStateKind kind) {
		this.kind = kind;
	}

	@Override
	public PseudoStateKind getKind() {
		return kind;
	}

}
