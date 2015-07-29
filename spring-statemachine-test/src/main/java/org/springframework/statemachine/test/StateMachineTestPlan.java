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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hamcrest.Matcher;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder.StateMachineTestPlanStep;
import org.springframework.statemachine.test.support.LatchStateMachineListener;
import org.springframework.util.StringUtils;

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

	private final static Log log = LogFactory.getLog(StateMachineTestPlan.class);
	private final Map<Object, StateMachine<S, E>> stateMachines;
	private final List<StateMachineTestPlanStep<S, E>> steps;
	private Integer defaultAwaitTime = 10;

	/**
	 * Instantiates a new state machine test plan.
	 *
	 * @param stateMachines the state machines
	 * @param steps the steps
	 * @param defaultAwaitTime the default await time in seconds
	 */
	public StateMachineTestPlan(Map<Object, StateMachine<S, E>> stateMachines, List<StateMachineTestPlanStep<S, E>> steps,
			Integer defaultAwaitTime) {
		this.stateMachines = stateMachines;
		this.steps = steps;
		if (defaultAwaitTime != null) {
			this.defaultAwaitTime = defaultAwaitTime;
		}
	}

	/**
	 * Run test plan.
	 *
	 * @throws Exception the exception
	 */
	public void test() throws Exception {

		Map<StateMachine<S, E>, LatchStateMachineListener<S, E>> listeners =
				new HashMap<StateMachine<S,E>, LatchStateMachineListener<S,E>>();
		for (StateMachine<S, E> stateMachine : stateMachines.values()) {
			LatchStateMachineListener<S, E> listener = new LatchStateMachineListener<S, E>();
			listeners.put(stateMachine, listener);
			stateMachine.addStateListener(listener);
			stateMachine.start();
		}
		log.info("Running test plan for machines "
				+ StringUtils.collectionToCommaDelimitedString(stateMachines.values()));

		int stepCounter = 0;
		for (StateMachineTestPlanStep<S, E> step : steps) {
			log.info("Running test plan step " + stepCounter++);
			for (LatchStateMachineListener<S, E> listener : listeners.values()) {
				listener.reset(
						step.expectStateChanged != null ? step.expectStateChanged : 0,
						step.expectStateEntered != null ? step.expectStateEntered : 0,
						step.expectStateExited != null ? step.expectStateExited : 0,
						step.expectEventNotAccepted != null ? step.expectEventNotAccepted : 0,
						step.expectTransition != null ? step.expectTransition : 0,
						step.expectTransitionStarted != null ? step.expectTransitionStarted : 0,
						step.expectTransitionEnded != null ? step.expectTransitionEnded : 0,
						step.expectStateMachineStarted != null ? step.expectStateMachineStarted : 0,
						step.expectStateMachineStopped != null ? step.expectStateMachineStopped : 0);
			}

			if (step.expectStateMachineStarted != null) {
				for (Entry<StateMachine<S, E>, LatchStateMachineListener<S, E>> entry : listeners.entrySet()) {
					assertThat("StateMachineStarted Await not matched for machine " + entry.getKey(), entry.getValue()
							.getStateMachineStartedLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat("StateMachineStarted count not matched for machine " + entry.getKey(), entry.getValue()
							.getStateMachineStarted().size(), is(step.expectStateMachineStarted));
				}
			}

			if (step.sendEvent != null) {
				StateMachine<S, E> sendVia = null;
				if (step.sendEventMachineId != null) {
					sendVia = stateMachines.get(step.sendEventMachineId);
				} else {
					sendVia = stateMachines.values().iterator().next();
				}
				assertThat("Error finding machine to send via", sendVia, notNullValue());
				log.info("Sending test event " + step.sendEvent + " via machine " + sendVia);
				sendVia.sendEvent(step.sendEvent);
			}

			if (step.expectStateChanged != null) {
				for (Entry<StateMachine<S, E>, LatchStateMachineListener<S, E>> entry : listeners.entrySet()) {
					assertThat("StateChanged Await not matched for machine " + entry.getKey(), entry.getValue()
							.getStateChangedLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat("StateChanged count not matched for machine " + entry.getKey(), entry.getValue()
							.getStateChanged().size(), is(step.expectStateChanged));
				}
			}

			if (step.expectStateEntered != null) {
				for (LatchStateMachineListener<S, E> listener : listeners.values()) {
					assertThat(listener.getStateEnteredLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat(listener.getStateEntered().size(), is(step.expectStateEntered));
				}
			}

			if (step.expectStateExited != null) {
				for (LatchStateMachineListener<S, E> listener : listeners.values()) {
					assertThat(listener.getStateExitedLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat(listener.getStateExited().size(), is(step.expectStateExited));
				}
			}

			if (step.expectEventNotAccepted != null) {
				for (LatchStateMachineListener<S, E> listener : listeners.values()) {
					assertThat(listener.getEventNotAcceptedLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat(listener.getEventNotAccepted().size(), is(step.expectEventNotAccepted));
				}
			}

			if (step.expectTransition != null) {
				for (LatchStateMachineListener<S, E> listener : listeners.values()) {
					assertThat(listener.getTransitionLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat(listener.getTransition().size(), is(step.expectTransition));
				}
			}

			if (step.expectTransitionStarted != null) {
				for (LatchStateMachineListener<S, E> listener : listeners.values()) {
					assertThat(listener.getTransitionStartedLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat(listener.getTransitionStarted().size(), is(step.expectTransitionStarted));
				}
			}

			if (step.expectTransitionEnded != null) {
				for (LatchStateMachineListener<S, E> listener : listeners.values()) {
					assertThat(listener.getTransitionEndedLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat(listener.getTransitionEnded().size(), is(step.expectTransitionEnded));
				}
			}

			if (step.expectStateMachineStopped != null) {
				for (LatchStateMachineListener<S, E> listener : listeners.values()) {
					assertThat(listener.getStateMachineStoppedLatch().await(defaultAwaitTime, TimeUnit.SECONDS), is(true));
					assertThat(listener.getStateMachineStopped().size(), is(step.expectStateMachineStopped));
				}
			}

			if (!step.expectStates.isEmpty()) {
				for (StateMachine<S, E> stateMachine : stateMachines.values()) {
					assertThat(stateMachine.getState(), notNullValue());
					Collection<Matcher<? super S>> itemMatchers = new ArrayList<Matcher<? super S>>();
					for (S expectState : step.expectStates) {
						itemMatchers.add(is(expectState));
					}
					assertThat(stateMachine.getState().getIds(), containsInAnyOrder(itemMatchers));
				}
			}

			if (!step.expectVariableKeys.isEmpty()) {
				for (StateMachine<S, E> stateMachine : stateMachines.values()) {
					Map<Object, Object> variables = stateMachine.getExtendedState().getVariables();
					for (Object key : step.expectVariableKeys) {
						assertThat("Key " + key + " doesn't exist in extended state variables",
								variables.containsKey(key), is(true));
					}
				}
			}

			if (!step.expectVariables.isEmpty()) {
				for (StateMachine<S, E> stateMachine : stateMachines.values()) {
					Map<Object, Object> variables = stateMachine.getExtendedState().getVariables();
					for (Entry<Object, Object> entry : step.expectVariables.entrySet()) {
						assertThat("Key " + entry.getKey() + " doesn exist in extended state variables",
								variables.containsKey(entry.getKey()), is(true));
						assertThat("Variable " + entry.getKey() + " doesn't match in extended state variables",
								variables.get(entry.getKey()), is(entry.getValue()));
					}
				}
			}
		}
	}

}
