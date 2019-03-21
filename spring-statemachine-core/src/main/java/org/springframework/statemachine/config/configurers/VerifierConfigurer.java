/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.config.configurers;

import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerBuilder;
import org.springframework.statemachine.config.model.verifier.StateMachineModelVerifier;

/**
 * Base {@code ConfigConfigurer} interface for configuring state machine model verifier.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface VerifierConfigurer <S, E> extends
		AnnotationConfigurerBuilder<StateMachineConfigurationConfigurer<S, E>> {

	/**
	 * Specify if verifier is enabled. On default verifier is enabled.
	 *
	 * @param enabled the enable flag
	 * @return configurer for chaining
	 */
	VerifierConfigurer<S, E> enabled(boolean enabled);

	/**
	 * Specify a custom model verifier.
	 *
	 * @param verifier the state machine model verifier
	 * @return configurer for chaining
	 */
	VerifierConfigurer<S, E> verifier(StateMachineModelVerifier<S, E> verifier);
}
