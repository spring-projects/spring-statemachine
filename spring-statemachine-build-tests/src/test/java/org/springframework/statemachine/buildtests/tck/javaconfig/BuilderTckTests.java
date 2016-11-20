/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.buildtests.tck.javaconfig;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.buildtests.tck.AbstractTckTests;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;

/**
 * Tck tests for builder based javaconfig.
 *
 * @author Janne Valkealahti
 *
 */
public class BuilderTckTests extends AbstractTckTests {

	@Override
	protected StateMachine<String, String> getSimpleMachine() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial("S1")
				.state("S2")
				.state("S3");
		builder.configureTransitions()
			.withExternal()
				.source("S1").target("S2")
				.event("E1")
				.and()
			.withExternal()
				.source("S2").target("S3")
				.event("E2");
		return builder.build();
	}

	@Override
	protected StateMachine<String, String> getSimpleSubMachine() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial("S1")
				.state("S2")
				.state("S3")
				.and()
				.withStates()
					.parent("S2")
					.initial("S21")
					.state("S22");
		builder.configureTransitions()
			.withExternal()
				.source("S1").target("S2")
				.event("E1")
				.and()
			.withExternal()
				.source("S21").target("S22")
				.event("E2")
				.and()
			.withExternal()
				.source("S2").target("S3")
				.event("E3");
		return builder.build();
	}
}
