/*
 * Copyright 2015-2017 the original author or authors.
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

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.stereotype.Component;

/**
 * Annotation which is marking a bean to be a candidate for participating with a
 * state machine events.
 *
 * @author Janne Valkealahti
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
@Configuration
public @interface WithStateMachine {

	/**
	 * The name of a state machine bean which annotated bean should be associated.
	 * Defaults to {@code stateMachine}
	 *
	 * @return the state machine bean name
	 */
	String name() default StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE;

	/**
	 * The id of a state machine which annotated bean should be associated.
	 *
	 * @return the state machine id
	 * @see StateMachine#getId()
	 */
	String id() default "";
}
