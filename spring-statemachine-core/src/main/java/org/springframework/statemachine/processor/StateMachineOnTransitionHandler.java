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
package org.springframework.statemachine.processor;

import java.lang.reflect.Method;

import org.springframework.statemachine.annotation.OnTransition;

/**
 * Transition specific {@link StateMachineHandler}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineOnTransitionHandler<S, E> extends StateMachineHandler<S, E> {

	private final OnTransition annotation;

	/**
	 * Instantiates a new state machine on transition handler.
	 *
	 * @param target the target
	 * @param method the method
	 * @param annotation the annotation
	 */
	public StateMachineOnTransitionHandler(Object target, Method method, OnTransition annotation) {
		super(target, method);
		this.annotation = annotation;
	}

	/**
	 * Gets the annotation.
	 *
	 * @return the annotation
	 */
	public OnTransition getAnnotation() {
		return annotation;
	}

}
