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

import java.util.Map;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

@SuppressWarnings("unchecked")
public class ShowcaseUmlTests extends AbstractBuildTests {

	@Test
	public void testInitialState() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testA() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("A")
						.expectStateChanged(0)
						.expectStateEntered(0)
						.expectStateExited(0)
						.expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testB() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("B")
						.expectStateChanged(2)
						.expectStateEntered(2)
						.expectStateExited(2)
						.expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testCHKA() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("C")
						.expectStateEntered(3)
						.expectStates("S0", "S2", "S21", "S211").and()
					.step()
						.sendEvent("H")
						.expectVariable("foo", 1)
						.expectTransition(1)
						.expectStates("S0", "S2", "S21", "S211").and()
					.step()
						.sendEvent("K")
						.expectStateEntered(2)
						.expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("A")
						.expectStateChanged(2)
						.expectStateEntered(2)
						.expectStateExited(2)
						.expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testC() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("C")
						.expectStateEntered(3)
						.expectStates("S0", "S2", "S21", "S211").and()
					.build();
		plan.test();
	}

	@Test
	public void testCK() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("C")
						.expectStateEntered(3)
						.expectStates("S0", "S2", "S21", "S211").and()
					.step()
						.sendEvent("K")
						.expectStateEntered(2)
						.expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testD() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("D")
						.expectStateEntered(3)
						.expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testCD() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("C")
						.expectStateEntered(3)
						.expectStates("S0", "S2", "S21", "S211").and()
					.step()
						.sendEvent("D")
						.expectStateEntered(2)
						.expectStates("S0", "S2", "S21", "S211").and()
					.build();
		plan.test();
	}

	@Test
	public void testI() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("I")
						.expectStateEntered(1)
						.expectStates("S0", "S1", "S12").and()
					.build();
		plan.test();
	}

	@Test
	public void testII() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("I")
						.expectStateEntered(1)
						.expectStateEntered(1)
						.expectStateExited(1)
						.expectStates("S0", "S1", "S12").and()
					.step()
						.sendEvent("I")
						.expectStateChanged(2)
						.expectStateEntered(3)
						.expectStateExited(2)
						.expectStates("S0", "S2", "S21", "S212").and()
					.build();
		plan.test();
	}

	@Test
	public void testH() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step()
						.expectStates("S0", "S1", "S11")
						.expectVariable("foo", 0).and()
					.step()
						.sendEvent("H")
						.expectTransition(1)
						.expectVariable("foo", 0)
						.expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testCH() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step()
						.expectStates("S0", "S1", "S11")
						.expectVariable("foo", 0).and()
					.step()
						.sendEvent("C")
						.expectStateEntered(3)
						.expectStates("S0", "S2", "S21", "S211").and()
					.step()
						.sendEvent("H")
						.expectVariable("foo", 1)
						.expectTransition(1)
						.expectStates("S0", "S2", "S21", "S211").and()
					.build();
		plan.test();
	}

	@Test
	public void testACH() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("A")
						.expectStateChanged(0)
						.expectStateEntered(0)
						.expectStateExited(0)
						.expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("C")
						.expectStateEntered(3)
						.expectStates("S0", "S2", "S21", "S211").and()
					.step()
						.sendEvent("H")
						.expectVariable("foo", 1)
						.expectTransition(1)
						.expectStates("S0", "S2", "S21", "S211").and()
					.build();
		plan.test();
	}

	@Test
	public void testE() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("E")
						.expectStateChanged(3)
						.expectStateEntered(4)
						.expectStateExited(3)
						.expectStates("S0", "S2", "S21", "S211").and()
					.build();
		plan.test();
	}

	@Test
	public void testF() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("F")
						.expectStateChanged(2)
						.expectStateEntered(3)
						.expectStateExited(2)
						.expectStates("S0", "S2", "S21", "S211").and()
					.build();
		plan.test();
	}

	@Test
	public void testG() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.step()
						.sendEvent("G")
						.expectStateChanged(2)
						.expectStateEntered(3)
						.expectStateExited(2)
						.expectStates("S0", "S2", "S21", "S211").and()
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
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/showcase.uml");
		}

		@Bean
		public Guard<String, String> exitGuard() {
			return (context) -> {
				return true;
			};
		}

		@Bean
		public FooGuard foo0Guard() {
			return new FooGuard(0);
		}

		@Bean
		public FooGuard foo1Guard() {
			return new FooGuard(1);
		}

		@Bean
		public FooAction fooAction() {
			return new FooAction();
		}
	}

	private static class FooAction implements Action<String, String> {

		@Override
		public void execute(StateContext<String, String> context) {
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			Integer foo = context.getExtendedState().get("foo", Integer.class);
			if (foo == null) {
				variables.put("foo", 0);
			} else if (foo == 0) {
				variables.put("foo", 1);
			} else if (foo == 1) {
				variables.put("foo", 0);
			}
		}
	}

	private static class FooGuard implements Guard<String, String> {

		private final int match;

		public FooGuard(int match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			Object foo = context.getExtendedState().getVariables().get("foo");
			return !(foo == null || !foo.equals(match));
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
