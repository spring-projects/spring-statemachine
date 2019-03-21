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
package org.springframework.statemachine.buildtests;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

@SuppressWarnings("unchecked")
public class LinkedPseudoStatesTests extends AbstractBuildTests {

	@Test
	public void testTwoChoices() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step().sendEvent("E1").expectStates("S2").and()
					.build();
		plan.test();
	}

	@Test
	public void testForkViaChoice() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step().sendEvent("E2").expectStates("S3", "S31", "S32").and()
					.build();
		plan.test();
	}

	@Test
	public void testJunctionChoiceTwice() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step().sendEvent("E3").expectStates("S4").and()
					.build();
		plan.test();
	}

	@Test
	public void testEntryJunctionExitJunction() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step().sendEvent("E4").expectStates("S5", "S52").and()
					.step().sendEvent("E5").expectStates("S4").and()
					.build();
		plan.test();
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/linked-pseudostates.uml");
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
