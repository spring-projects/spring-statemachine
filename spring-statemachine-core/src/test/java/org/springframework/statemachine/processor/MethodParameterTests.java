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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeaders;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateContext;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.ReflectionUtils;

public class MethodParameterTests {

	@SuppressWarnings("unchecked")
	@Test
	public void testOnTransition() {
		Bean1 bean1 = new Bean1();
		Method method = ReflectionUtils.findMethod(Bean1.class, "onTransition", Map.class, ExtendedState.class, StateMachine.class,
				Message.class, Exception.class, StateContext.class);
		OnTransition annotation = AnnotationUtils.findAnnotation(method, OnTransition.class);
		StateMachineHandler<OnTransition, String, String> handler = new StateMachineHandler<OnTransition, String, String>(Bean1.class,
				bean1, method, annotation, annotation);

		Message<String> message = MessageBuilder.withPayload("S").build();
		MessageHeaders messageHeaders = message.getHeaders();
		ExtendedState extendedState = new DefaultExtendedState();
		Transition<String, String> transition = mock(Transition.class);
		StateMachine<String, String> stateMachine = mock(StateMachine.class);
		State<String, String> source = mock(State.class);
		State<String, String> target = mock(State.class);
		Exception exception = new RuntimeException();

		StateMachineRuntime<String, String> runtime = new StateMachineRuntime<String, String>() {
			@Override
			public StateContext<String, String> getStateContext() {
				return new DefaultStateContext<String, String>(Stage.TRANSITION, message, messageHeaders, extendedState, transition, stateMachine, source,
						target, exception);
			}
		};

		handler.handle(runtime);
	}

	public static class Bean1 {

		@OnTransition
		public void onTransition(@EventHeaders Map<String, Object> headers, ExtendedState extendedState, StateMachine<?, ?> stateMachine,
				Message<?> message, Exception e, StateContext<?, ?> stateContext) {
			assertThat(headers, notNullValue());
			assertThat(extendedState, notNullValue());
			assertThat(stateMachine, notNullValue());
			assertThat(message, notNullValue());
			assertThat(e, notNullValue());
			assertThat(stateContext, notNullValue());
		}

	}

}
