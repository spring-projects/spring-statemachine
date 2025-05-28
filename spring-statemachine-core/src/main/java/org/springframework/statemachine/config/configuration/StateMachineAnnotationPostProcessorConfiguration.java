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
package org.springframework.statemachine.config.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.processor.StateMachineAnnotationPostProcessor;

/**
 * Configuration for annotation post processor which is needed i.e. when
 * {@link WithStateMachine} is used.
 *
 * @author Janne Valkealahti
 *
 */
@Configuration
public class StateMachineAnnotationPostProcessorConfiguration {

	private final static String POST_PROCESSOR_BEAN_ID = "org.springframework.statemachine.processor.stateMachineAnnotationPostProcessor";

	@Bean(name = POST_PROCESSOR_BEAN_ID)
	public static StateMachineAnnotationPostProcessor springStateMachineAnnotationPostProcessor() {
		return new StateMachineAnnotationPostProcessor();
	}

}
