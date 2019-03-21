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

public class ForkJoinEntryExitTests extends AbstractBuildTests {

	@Test
	@SuppressWarnings("unchecked")
	public void testForkEntrys() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step()
						.sendEvent("E1")
						.expectStateEntered(3)
						.expectStates("S2", "S210", "S220").and()
					.build();
		plan.test();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testForkEntrysViaChoices() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step()
						.sendEvent("E1")
						.expectStateEntered(3)
						.expectStates("S2", "S210", "S220").and()
					.build();
		plan.test();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoinExits() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step()
						.sendEvent("E1")
						.expectStateEntered(3)
						.expectStateExited(1)
						.expectStates("S2", "S210", "S220").and()
					.step()
						.sendEvent("E2")
						.expectStateEntered(1)
						.expectStateExited(1)
						.expectStates("S2", "S211", "S220").and()
					.step()
						.sendEvent("E3")
						.expectStateEntered(2)
						.expectStateExited(4)
						.expectStates("S3").and()
					.build();
		plan.test();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoinExitsViaChoices() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("S1").and()
					.step()
						.sendEvent("E1")
						.expectStateEntered(3)
						.expectStateExited(1)
						.expectStates("S2", "S210", "S220").and()
					.step()
						.sendEvent("E2")
						.expectStateEntered(1)
						.expectStateExited(1)
						.expectStates("S2", "S211", "S220").and()
					.step()
						.sendEvent("E3")
						.expectStateEntered(2)
						.expectStateExited(4)
						.expectStates("S3").and()
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
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/forkjoin-entryexit.uml");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/forkjoin-entryexit2.uml");
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
