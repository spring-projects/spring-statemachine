/*
 * Copyright 2017 the original author or authors.
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;

public class TimerSmokeTests {

	private static ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	{
		taskExecutor.initialize();
	}

	private StateMachine<String, String> buildMachine() throws Exception {

		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.taskExecutor(taskExecutor);

		builder.configureStates()
			.withStates()
				.initial("initial")
				.end("end");

		builder.configureTransitions()
			.withExternal()
				.source("initial")
				.target("end")
				.timerOnce(30)
				.and()
			.withLocal()
				.source("initial")
				.event("repeate");

		return builder.build();
	}

	private StateMachine<String, String> buildMachine2() throws Exception {

		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.taskExecutor(taskExecutor);

		builder.configureStates()
			.withStates()
				.initial("initial").end("end").and()
				.withStates().parent("initial").initial("inner");

		builder.configureTransitions()
			.withExternal()
				.source("initial")
				.target("end")
				.timerOnce(30)
				.and()
			.withExternal()
				.source("inner")
				.target("end")
				.timerOnce(15)
				.and()
			.withLocal()
				.source("inner")
				.event("repeate");

		return builder.build();
	}

	@Test
	public void testNPE() throws Exception {
		StateMachine<String, String> stateMachine;
		for (int i = 0; i < 20; i++) {
			stateMachine = buildMachine();
			stateMachine.start();
			while (!stateMachine.isComplete()) {
				stateMachine.sendEvent("repeate");
			}
		}
	}

	@Test
	public void testNPE2() throws Exception {

		StateMachine<String, String> stateMachine;

		for (int i = 0; i < 20; i++) {
			stateMachine = buildMachine2();
			stateMachine.start();
			while(!stateMachine.isComplete()) {
				stateMachine.sendEvent("repeate");
			}
			stateMachine.stop();
		}
	}

	@Test
	public void testDeadlock() throws Exception {
		StateMachineTestPlan<String, String> plan;
		for (int i = 0; i < 20; i++) {
			plan = StateMachineTestPlanBuilder.<String, String> builder()
					.defaultAwaitTime(1)
					.stateMachine(buildMachine())
					.step()
						.expectStateMachineStarted(1)
						.expectStateEntered(1)
						.expectStateEntered("initial")
						.and()
					.step()
						.sendEvent("repeate")
						.expectStates("initial")
						.and()
					.step()
						.expectStateEntered(1)
						.expectStateEntered("end")
						.and()
					.step()
						.expectStateMachineStopped(1)
						.and()
					.build();
			plan.test();
		}
	}
}
