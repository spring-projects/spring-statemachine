/*
 * Copyright 2015-2016 the original author or authors.
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

import org.springframework.core.Ordered;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

/**
 * Handler for a common object representing something to be run.
 * This is usually used when a plain pojo is configured with {@link WithStateMachine}
 * and {@link OnTransition} annotations.
 *
 * @author Janne Valkealahti
 *
 * @param <T> the type of annotation
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineHandler<T extends Annotation, S, E> implements Ordered {

	private final Class<?> beanClass;
	private final StateMachineRuntimeProcessor<?, S, E> processor;
	private final T metaAnnotation;
	private final Annotation annotation;
	private int order = Ordered.LOWEST_PRECEDENCE;

	/**
	 * Instantiates a new container handler.
	 *
	 * @param beanClass the bean class
	 * @param target the target bean
	 * @param metaAnnotation the meta annotation
	 * @param annotation the annotation
	 * @param method the method
	 */
	public StateMachineHandler(Class<?> beanClass, Object target, Method method, T metaAnnotation, Annotation annotation) {
		this(beanClass, metaAnnotation, annotation, new MethodInvokingStateMachineRuntimeProcessor<T, S, E>(target, method));
	}

	/**
	 * Instantiates a new container handler.
	 *
	 * @param beanClass the bean class
	 * @param target the target bean
	 * @param methodName the method name
	 * @param metaAnnotation the meta annotation
	 * @param annotation the annotation
	 */
	public StateMachineHandler(Class<?> beanClass, Object target, String methodName, T metaAnnotation, Annotation annotation) {
		this(beanClass, metaAnnotation, annotation, new MethodInvokingStateMachineRuntimeProcessor<T, S, E>(target, methodName));
	}

	/**
	 * Instantiates a new container handler.
	 *
	 * @param beanClass the bean class
	 * @param metaAnnotation the meta annotation
	 * @param annotation the annotation
	 * @param processor the processor
	 */
	public StateMachineHandler(Class<?> beanClass, T metaAnnotation, Annotation annotation,
			MethodInvokingStateMachineRuntimeProcessor<T, S, E> processor) {
		this.beanClass = beanClass;
		this.processor = processor;
		this.metaAnnotation = metaAnnotation;
		this.annotation = annotation;
	}

	@Override
	public int getOrder() {
		return order;
	}

	/**
	 * Gets the meta annotation.
	 *
	 * @return the meta annotation
	 */
	public T getMetaAnnotation() {
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

	/**
	 * Gets the bean class.
	 *
	 * @return the bean class
	 */
	public Class<?> getBeanClass() {
		return beanClass;
	}

	/**
	 * Sets the order used get value from {@link #getOrder()}.
	 * Default value is {@link Ordered#LOWEST_PRECEDENCE}.
	 *
	 * @param order the new order
	 */
	public void setOrder(int order) {
		this.order = order;
	}

	/**
	 * Handle container using a {@link StateMachineRuntimeProcessor}.
	 *
	 * @param stateMachineRuntime the state machine runtime
	 * @return the result value
	 */
	public Object handle(StateMachineRuntime<S, E> stateMachineRuntime) {
		return processor.process(stateMachineRuntime);
	}

	@Override
	public String toString() {
		return "StateMachineHandler [beanClass=" + beanClass + "]";
	}
}
