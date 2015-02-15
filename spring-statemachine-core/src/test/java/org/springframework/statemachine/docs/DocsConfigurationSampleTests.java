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
package org.springframework.statemachine.docs;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;

/**
 * Tests for state machine configuration.
 *
 * @author Janne Valkealahti
 *
 */
public class DocsConfigurationSampleTests extends AbstractStateMachineTests {

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<MyStates, MyEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<MyStates, MyEvents> states) throws Exception {
			states
				.withStates()
					.initial(MyStates.S1)
					.end(MyStates.SF)
					.states(EnumSet.allOf(MyStates.class));
		}

	}
// end::snippetA[]

}
