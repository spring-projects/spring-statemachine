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
 */
public class StateMachineHandler implements Ordered {

	private final StateMachineRuntimeProcessor<?> processor;

	private int order = Ordered.LOWEST_PRECEDENCE;

	/**
	 * Instantiates a new container handler.
	 *
	 * @param target the target bean
	 */
//	public StateMachineHandler(Object target) {
//		this(new MethodInvokingStateMachineRuntimeProcessor<Object>(target, OnTransition.class));
//	}

	/**
	 * Instantiates a new container handler.
	 *
	 * @param target the target bean
	 * @param method the method
	 */
	public StateMachineHandler(Object target, Method method) {
		this(new MethodInvokingStateMachineRuntimeProcessor<Object>(target, method));
	}

	/**
	 * Instantiates a new container handler.
	 *
	 * @param target the target bean
	 * @param methodName the method name
	 */
	public StateMachineHandler(Object target, String methodName) {
		this(new MethodInvokingStateMachineRuntimeProcessor<Object>(target, methodName));
	}

	/**
	 * Instantiates a new container handler.
	 *
	 * @param <T> the generic type
	 * @param processor the processor
	 */
	public <T> StateMachineHandler(MethodInvokingStateMachineRuntimeProcessor<T> processor) {
		this.processor = processor;
	}

	@Override
	public int getOrder() {
		return order;
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
	public Object handle(StateMachineRuntime stateMachineRuntime) {
		return processor.process(stateMachineRuntime);
	}

}
