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

import java.util.ArrayList;
import java.util.List;

import org.springframework.statemachine.StateMachine;

/**
 * A builder for {@link StateMachineTestPlan}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineTestPlanBuilder<S, E> {

	private List<StateMachine<S, E>> stateMachines = new ArrayList<StateMachine<S, E>>();
	private final List<StateMachineTestPlanStep<S, E>> steps = new ArrayList<StateMachineTestPlanStep<S, E>>();

	/**
	 * Gets a new instance of this builder.
	 *
	 * @return the state machine test plan builder
	 */
	public static <S, E> StateMachineTestPlanBuilder<S, E> builder() {
		return new StateMachineTestPlanBuilder<S, E>();
	}

	/**
	 * Associate a state machine with this builder.
	 *
	 * @param stateMachine the state machine
	 * @return the state machine test plan builder
	 */
	public StateMachineTestPlanBuilder<S, E> stateMachine(StateMachine<S, E> stateMachine) {
		this.stateMachines.add(stateMachine);
		return this;
	}

	/**
	 * Gets a new step builder.
	 *
	 * @return the state machine test plan step builder
	 */
	public StateMachineTestPlanStepBuilder step() {
		return new StateMachineTestPlanStepBuilder();
	}

	/**
	 * Builds the state machine test plan.
	 *
	 * @return the state machine test plan
	 */
	public StateMachineTestPlan<S, E> build() {
		return new StateMachineTestPlan<S, E>(stateMachines, steps);
	}

	/**
	 * Builder for individual plan steps.
	 */
	public class StateMachineTestPlanStepBuilder {

		E sendEvent;
		S expectState;
		Integer expectStateChanged;

		/**
		 * Expect a state {@code S}.
		 *
		 * @param state the state
		 * @return the state machine test plan step builder
		 */
		public StateMachineTestPlanStepBuilder expectState(S state) {
			this.expectState = state;
			return this;
		}

		/**
		 * Send an event {@code E}.
		 *
		 * @param event the event
		 * @return the state machine test plan step builder
		 */
		public StateMachineTestPlanStepBuilder sendEvent(E event) {
			this.sendEvent = event;
			return this;
		}

		/**
		 * Expect state changed happening {@code count} times.
		 *
		 * @param count the count
		 * @return the state machine test plan step builder
		 */
		public StateMachineTestPlanStepBuilder expectStateChanged(int count) {
			if (count < 0) {
				throw new IllegalArgumentException("Expected count cannot be negative, was " + count);
			}
			this.expectStateChanged = count;
			return this;
		}

		/**
		 * Add a new step and return {@link StateMachineTestPlanBuilder}
		 * for chaining.
		 *
		 * @return the state machine test plan builder for chaining
		 */
		public StateMachineTestPlanBuilder<S, E> and() {
			steps.add(new StateMachineTestPlanStep<S, E>(sendEvent, expectState, expectStateChanged));
			return StateMachineTestPlanBuilder.this;
		}

	}

	static class StateMachineTestPlanStep<S, E> {
		E sendEvent;
		S expectState;
		Integer expectStateChanged;
		public StateMachineTestPlanStep(E sendEvent, S expectState, Integer expectStateChanged) {
			this.sendEvent = sendEvent;
			this.expectState = expectState;
			this.expectStateChanged = expectStateChanged;
		}

	}

}
