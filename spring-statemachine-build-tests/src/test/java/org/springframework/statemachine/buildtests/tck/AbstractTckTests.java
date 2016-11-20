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
package org.springframework.statemachine.buildtests.tck;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
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
}
