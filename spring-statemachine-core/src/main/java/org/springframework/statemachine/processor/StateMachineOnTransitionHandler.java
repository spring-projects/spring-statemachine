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

import java.lang.annotation.Annotation;
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

	private final OnTransition metaAnnotation;
	private final Annotation annotation;

	/**
	 * Instantiates a new state machine on transition handler.
	 *
	 * @param beanClass the bean class
	 * @param target the target
	 * @param method the method
	 * @param metaAnnotation the meta annotation
	 * @param annotation the annotation
	 */
	public StateMachineOnTransitionHandler(Class<?> beanClass, Object target, Method method, OnTransition metaAnnotation, Annotation annotation) {
		super(beanClass, target, method);
		this.metaAnnotation = metaAnnotation;
		this.annotation = annotation;
	}

	/**
	 * Gets the meta annotation.
	 *
	 * @return the meta annotation
	 */
	public OnTransition getMetaAnnotation() {
		return metaAnnotation;
	}

	/**
	 * Gets the annotation.
	 *
	 * @return the annotation
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

}
