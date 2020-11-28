/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.statemachine.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder.StateMachineTestPlanStep;

public class StateMachineTestPlanBuilderJUnit5Tests {

	@Test
	public void testBuilderNoSteps() throws Exception {
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.build();
		assertThat(plan).isNotNull();
		List<StateMachineTestPlanStep<?, ?>> steps = TestUtils.readField("steps", plan);
		assertThat(steps).isEmpty();
	}

	@Test
	public void testBuilderOneStep() throws Exception {
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.step().expectState("SI").and()
					.build();
		assertThat(plan).isNotNull();
		List<StateMachineTestPlanStep<?, ?>> steps = TestUtils.readField("steps", plan);
		assertThat(steps).hasSize(1);
	}

}
