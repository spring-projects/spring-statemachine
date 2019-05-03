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
package org.springframework.statemachine.buildtests.tck;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

/**
 * Base tck test class for defining various machine tests
 * which different config implementations can implement to
 * test that same machine behaviour.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class AbstractTckTests {

	protected AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		cleanInternal();
		context = buildContext();
	}

	@After
	public void clean() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	@SuppressWarnings("unchecked")
	protected StateMachine<String, String> getStateMachineFromContext() {
		return context.getBean(StateMachine.class);
	}

	@SuppressWarnings("unchecked")
	protected StateMachineFactory<String, String> getStateMachineFactoryFromContext() {
		return context.getBean(StateMachineFactory.class);
	}

	protected void cleanInternal() {
	}

	protected AnnotationConfigApplicationContext buildContext() {
		return null;
	}

	@Test
	public void testSimpleMachine() throws Exception {
		StateMachine<String,String> stateMachine = getSimpleMachine();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2").and()
					.step().sendEvent("E2").expectStates("S3").and()
					.build();
		plan.test();
	}

	/**
	 * Return state machine for {@link #testSimpleMachine()}.
	 *
	 * @return StateMachine for SimpleMachine
	 */
	protected abstract StateMachine<String, String> getSimpleMachine() throws Exception;

	@Test
	public void testSimpleSubMachine() throws Exception {
		StateMachine<String,String> stateMachine = getSimpleSubMachine();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S21").and()
					.step().sendEvent("E2").expectStates("S2", "S22").and()
					.step().sendEvent("E3").expectStates("S3").and()
					.build();
		plan.test();
	}

	/**
	 * Return state machine for {@link #testSimpleSubMachine()}.
	 *
	 * @return StateMachine for SimpleSubMachine
	 */
	protected abstract StateMachine<String, String> getSimpleSubMachine() throws Exception;

	@Test
	public void testShowcaseMachineInitialState() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S0", "S1", "S11").and()
					.build();
		plan.test();
	}

	@Test
	public void testShowcaseMachineA() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineB() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineCHKA() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineC() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineCK() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineD() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineCD() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineI() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineII() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineH() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineCH() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineACH() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineE() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineF() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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
	public void testShowcaseMachineG() throws Exception {
		StateMachine<String,String> stateMachine = getShowcaseMachine();
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

	/**
	 * Return state machine for showcase tests.
	 *
	 * @return StateMachine for ShowcaseMachine
	 */
	protected abstract StateMachine<String, String> getShowcaseMachine() throws Exception;

	@Configuration
	protected static class ShowcaseMachineBeansConfig {

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

	protected static class FooAction implements Action<String, String> {

		public FooAction() {
		}

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

	protected static class FooGuard implements Guard<String, String> {

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
}
