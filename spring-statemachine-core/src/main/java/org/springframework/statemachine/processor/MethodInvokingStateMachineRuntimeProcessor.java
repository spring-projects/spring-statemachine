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

/**
 * A simple {@link StateMachineRuntimeProcessor} implementation using
 * methods from a state machine protected bean.
 *
 * @author Janne Valkealahti
 *
 * @param <T> the return type
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class MethodInvokingStateMachineRuntimeProcessor<T, S, E> implements StateMachineRuntimeProcessor<T, S, E> {

	private final StateMachineMethodInvokerHelper<T, S, E> delegate;

	public MethodInvokingStateMachineRuntimeProcessor(Object targetObject, Method method) {
		delegate = new StateMachineMethodInvokerHelper<T, S, E>(targetObject, method);
	}

	public MethodInvokingStateMachineRuntimeProcessor(Object targetObject, String methodName) {
		delegate = new StateMachineMethodInvokerHelper<T, S, E>(targetObject, methodName);
	}

	public MethodInvokingStateMachineRuntimeProcessor(Object targetObject, Class<? extends Annotation> annotationType) {
		delegate = new StateMachineMethodInvokerHelper<T, S, E>(targetObject, annotationType);
	}

	@Override
	public T process(StateMachineRuntime<S, E> stateMachineRuntime) {
		try {
			return delegate.process(stateMachineRuntime);
		} catch (Exception e) {
			throw new RuntimeException("Error processing bean", e);
		}
	}

}
