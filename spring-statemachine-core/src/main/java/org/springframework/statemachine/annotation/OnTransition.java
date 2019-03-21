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
package org.springframework.statemachine.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.transition.Transition;

/**
 * Indicates that a method is a candidate to be called with a {@link Transition}.
 * <p>
 * A method annotated with @OnTransition may accept a parameter of type
 * {@link ExtendedState}, {@link Map} if map argument itself is annotated
 * with {@link EventHeaders}, {@link StateMachine}, {@link Message} or {@link Exception}.
 * <p>
 * Return value can be anything and is effectively discarded.
 *
 * @author Janne Valkealahti
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OnTransition {

	/**
	 * The source states.
	 *
	 * @return the source states.
	 */
	String[] source() default {};

	/**
	 * The target states.
	 *
	 * @return the target states.
	 */
	String[] target() default {};

}
