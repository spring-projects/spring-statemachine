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
package org.springframework.statemachine.test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder.StateMachineTestPlanStep;
import org.springframework.statemachine.test.support.LatchStateMachineListener;

/**
 * {@code StateMachineTestPlan} is fully constructed plan how
 * a {@link StateMachine} should be tested.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineTestPlan<S, E> {

	private final List<StateMachine<S, E>> stateMachines;
	private final List<StateMachineTestPlanStep<S, E>> steps;

	/**
	 * Instantiates a new state machine test plan.
	 *
	 * @param stateMachines the state machines
	 * @param steps the steps
	 */
	public StateMachineTestPlan(List<StateMachine<S, E>> stateMachines, List<StateMachineTestPlanStep<S, E>> steps) {
		this.stateMachines = stateMachines;
		this.steps = steps;
	}

	/**
	 * Run test plan.
	 *
	 * @throws Exception the exception
	 */
	public void test() throws Exception {

		List<LatchStateMachineListener<S, E>> listeners = new ArrayList<LatchStateMachineListener<S, E>>();
		for (StateMachine<S, E> stateMachine : stateMachines) {
			LatchStateMachineListener<S, E> listener = new LatchStateMachineListener<S, E>();
			listeners.add(listener);
			stateMachine.addStateListener(listener);
			stateMachine.start();
		}


		for (StateMachineTestPlanStep<S, E> step : steps) {
			for (LatchStateMachineListener<S, E> listener : listeners) {
				listener.reset(step.expectStateChanged != null ? step.expectStateChanged : 0, 0, 0, 0, 0, 0, 0, 0, 0);
			}

			if (step.sendEvent != null) {
				stateMachines.get(0).sendEvent(step.sendEvent);
			}

			if (step.expectStateChanged != null) {
				for (LatchStateMachineListener<S, E> listener : listeners) {
					assertThat(listener.getStateChangedLatch().await(5, TimeUnit.SECONDS), is(true));
					assertThat(listener.getStateChanged().size(), is(step.expectStateChanged));
				}
			}

			if (step.expectState != null) {
				for (StateMachine<S, E> stateMachine : stateMachines) {
					assertThat(stateMachine.getState(), notNullValue());
					Collection<Matcher<? super S>> itemMatchers = new ArrayList<Matcher<? super S>>();
					itemMatchers.add(is(step.expectState));
					assertThat(stateMachine.getState().getIds(), containsInAnyOrder(itemMatchers));
				}
			}
		}
	}

}
