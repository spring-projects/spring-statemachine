/*
 * Copyright 2015-2021 the original author or authors.
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
package org.springframework.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE;
import static org.springframework.statemachine.StateMachineSystemConstants.DEFAULT_ID_STATEMACHINEFACTORY;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.util.ReflectionUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Utils for tests.
 *
 * @author Janne Valkealahti
 *
 */
public class TestUtils {

	private static Log log = LogFactory.getLog(TestUtils.class);

	@SuppressWarnings("unchecked")
	public static <S, E> StateMachine<S, E> resolveMachine(BeanFactory beanFactory) {
		assertThat(beanFactory.containsBean(DEFAULT_ID_STATEMACHINE)).isTrue();
		return (StateMachine<S, E>) beanFactory.getBean(DEFAULT_ID_STATEMACHINE);
	}

	@SuppressWarnings("unchecked")
	public static <S, E> StateMachine<S, E> resolveMachine(String id, BeanFactory beanFactory) {
		return (StateMachine<S, E>) beanFactory.getBean(id, StateMachine.class);
	}

	@SuppressWarnings("unchecked")
	public static <S, E> StateMachineFactory<S, E> resolveFactory(BeanFactory beanFactory) {
		assertThat(beanFactory.containsBean(DEFAULT_ID_STATEMACHINEFACTORY)).isTrue();
		return (StateMachineFactory<S, E>) beanFactory.getBean(DEFAULT_ID_STATEMACHINEFACTORY);
	}

	@SuppressWarnings("unchecked")
	public static <S, E> StateMachineFactory<S, E> resolveFactory(String id, BeanFactory beanFactory) {
		return (StateMachineFactory<S, E>) beanFactory.getBean(id, StateMachineFactory.class);
	}

	@SuppressWarnings("unchecked")
	public static <S, E, T> StateMachinePersister<S, E, T> resolvePersister(BeanFactory beanFactory) {
		return (StateMachinePersister<S, E, T>) beanFactory.getBean(StateMachinePersister.class);
	}

	@SuppressWarnings("unchecked")
	public static <S, E> Action<S, E> resolveAction(String id, BeanFactory beanFactory) {
		return (Action<S, E>) beanFactory.getBean(id, Action.class);
	}

	public static <S, E> void doStartAndAssert(StateMachine<S, E> stateMachine) {
		StepVerifier.create(stateMachine.startReactively()).expectComplete().verify();
	}

	public static <S, E> void doStopAndAssert(StateMachine<S, E> stateMachine) {
		StepVerifier.create(stateMachine.stopReactively()).expectComplete().verify();
	}

	public static <T> Mono<Message<T>> eventAsMono(T event) {
		return Mono.just(MessageBuilder.withPayload(event).build());
	}

	public static <T> Mono<Message<T>> eventAsMono(Message<T> event) {
		return Mono.just(event);
	}

	@SafeVarargs
	public static <T> Flux<Message<T>> eventsAsFlux(T... events) {
		return Flux.fromArray(events).map(e -> MessageBuilder.withPayload(e).build());
	}

	public static <S, E> void doSendEventAndConsumeAll(StateMachine<S, E> stateMachine, E event) {
		StepVerifier.create(stateMachine.sendEvent(eventAsMono(event)))
			.thenConsumeWhile(eventResult -> {
				log.debug("Consume eventResult " + eventResult);
				return true;
			})
			.expectComplete()
			.verify(Duration.ofSeconds(5));
	}

	public static <S, E> void doSendEventAndConsumeAll(StateMachine<S, E> stateMachine, Message<E> event) {
		StepVerifier.create(stateMachine.sendEvent(eventAsMono(event)))
			.thenConsumeWhile(eventResult -> true)
			.verifyComplete();
	}

	public static <S, E> void doSendEventAndConsumeResultAsDenied(StateMachine<S, E> stateMachine, E event) {
		StepVerifier.create(stateMachine.sendEvent(eventAsMono(event)))
			.consumeNextWith(result -> {
				assertThat(result.getResultType()).isEqualTo(ResultType.DENIED);
			})
			.verifyComplete();
	}

	public static <S, E> void doSendEventAndConsumeResultAsDenied(StateMachine<S, E> stateMachine, Message<E> event) {
		StepVerifier.create(stateMachine.sendEvent(eventAsMono(event)))
			.consumeNextWith(result -> {
				assertThat(result.getResultType()).isEqualTo(ResultType.DENIED);
			})
			.verifyComplete();
	}

	@SafeVarargs
	public static <S, E> void doSendEventsAndConsumeAll(StateMachine<S, E> stateMachine, E... events) {
		StepVerifier.create(stateMachine.sendEvents(eventsAsFlux(events)))
			.thenConsumeWhile(eventResult -> {
				log.debug("Consume eventResult " + eventResult);
				return true;
			})
			.expectComplete()
			.verify(Duration.ofSeconds(5));
	}

	@SafeVarargs
	public static <S, E> void doSendEventsAndConsumeAllWithComplete(StateMachine<S, E> stateMachine, E... events) {
		Flux<Void> completions = stateMachine.sendEvents(eventsAsFlux(events))
			.doOnNext(result -> {
				log.debug("Consume eventResult " + result);
			})
			.flatMap(result -> result.complete());
		StepVerifier.create(completions)
			.thenConsumeWhile(complete -> true)
			.expectComplete()
			.verify(Duration.ofSeconds(10));
	}

	@SuppressWarnings("unchecked")
	public static <T> T readField(String name, Object target) throws Exception {
		Field field = null;
		Class<?> clazz = target.getClass();
		do {
			try {
				field = clazz.getDeclaredField(name);
			} catch (Exception ex) {
			}

			clazz = clazz.getSuperclass();
		} while (field == null && !clazz.equals(Object.class));

		if (field == null)
			throw new IllegalArgumentException("Cannot find field '" + name + "' in the class hierarchy of "
					+ target.getClass());
		field.setAccessible(true);
		return (T) field.get(target);
	}

	@SuppressWarnings("unchecked")
	public static <T> T callMethod(String name, Object target) throws Exception {
		Class<?> clazz = target.getClass();
		Method method = ReflectionUtils.findMethod(clazz, name);

		if (method == null)
			throw new IllegalArgumentException("Cannot find method '" + method + "' in the class hierarchy of "
					+ target.getClass());
		method.setAccessible(true);
		return (T) ReflectionUtils.invokeMethod(method, target);
	}

	public static void setField(String name, Object target, Object value) throws Exception {
		Field field = null;
		Class<?> clazz = target.getClass();
		do {
			try {
				field = clazz.getDeclaredField(name);
			} catch (Exception ex) {
			}

			clazz = clazz.getSuperclass();
		} while (field == null && !clazz.equals(Object.class));

		if (field == null)
			throw new IllegalArgumentException("Cannot find field '" + name + "' in the class hierarchy of "
					+ target.getClass());
		field.setAccessible(true);
		field.set(target, value);
	}

	@SuppressWarnings("unchecked")
	public static <T> T callMethod(String name, Object target, Object[] args, Class<?>[] argsTypes) throws Exception {
		Class<?> clazz = target.getClass();
		Method method = ReflectionUtils.findMethod(clazz, name, argsTypes);

		if (method == null)
			throw new IllegalArgumentException("Cannot find method '" + method + "' in the class hierarchy of "
					+ target.getClass());
		method.setAccessible(true);
		return (T) ReflectionUtils.invokeMethod(method, target, args);
	}

}
