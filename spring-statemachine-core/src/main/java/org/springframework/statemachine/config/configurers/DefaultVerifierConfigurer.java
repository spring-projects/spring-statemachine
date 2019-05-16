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

import org.springframework.statemachine.config.builders.StateMachineConfigurationBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.verifier.CompositeStateMachineModelVerifier;
import org.springframework.statemachine.config.model.verifier.StateMachineModelVerifier;

/**
 * Default implementation of a {@link VerifierConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultVerifierConfigurer <S, E>
		extends AnnotationConfigurerAdapter<ConfigurationData<S, E>, StateMachineConfigurationConfigurer<S, E>, StateMachineConfigurationBuilder<S, E>>
		implements VerifierConfigurer<S, E> {

	private boolean enabled = true;
	private StateMachineModelVerifier<S, E> verifier;

	@Override
	public void configure(StateMachineConfigurationBuilder<S, E> builder) throws Exception {
		builder.setVerifierEnabled(enabled);
		if (verifier != null) {
			builder.setVerifier(verifier);
		} else {
			builder.setVerifier(new CompositeStateMachineModelVerifier<S, E>());
		}
	}

	@Override
	public VerifierConfigurer<S, E> enabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public VerifierConfigurer<S, E> verifier(StateMachineModelVerifier<S, E> verifier) {
		this.verifier = verifier;
		return this;
	}
}
