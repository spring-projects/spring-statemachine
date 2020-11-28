/*
 * Copyright 2015-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

public class Gh737Tests extends AbstractBuildTests {

	@Test
	public void test() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<Status, Event> machine = resolveMachine(context);
		doStartAndAssert(machine);

		assertThat(machine.getState().getIds()).containsOnly(Status.ROOT, Status.S0);
		doSendEventAndConsumeAll(machine, Event.NEW);

		assertThat(machine.getState().getIds()).containsOnly(Status.ROOT, Status.S2, Status.S21I, Status.S22I,
				Status.S23_IN_PROGRESS, Status.S24E);
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<Status, Event> {

		@Override
		public void configure(StateMachineStateConfigurer<Status, Event> states) throws Exception {
			states
				.withStates()
					.initial(Status.ROOT)
					.state(Status.ROOT)
					.and()
					.withStates()
						.parent(Status.ROOT)
						.initial(Status.S0)
						.state(Status.S0)
						.state(Status.S1)
						.fork(Status.FORK_S2)
						.state(Status.S2)
						.join(Status.JOIN_S2)
						.state(Status.S3)
						.state(Status.S4)
						.state(Status.S5)
						.and()
							.withStates()
							.parent(Status.S2)
							.initial(Status.S21I)
							.state(Status.S21_IN_PROGRESS)
							.state(Status.S21_NOT_REQUIRED)
							.end(Status.S21E)
							.and()
						.withStates()
							.parent(Status.S2)
							.initial(Status.S22I)
							.state(Status.S22_IN_PROGRESS)
							.state(Status.S22_PASSED)
							.end(Status.S22E)
							.and()
						.withStates()
							.parent(Status.S2)
							.initial(Status.S23I)
							.state(Status.S23_IN_PROGRESS)
							.state(Status.S23_PASSED)
							.end(Status.S23E)
							.and()
						.withStates()
							.parent(Status.S2)
							.initial(Status.S24I)
							.choice(Status.CHOICE_S24)
							.state(Status.S24_IN_PROGRESS)
							.state(Status.S24_PASSED)
							.state(Status.S24_NOT_REQUIRED)
							.end(Status.S24E);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<Status, Event> transitions) throws Exception {
			transitions
				.withExternal().source(Status.S0).target(Status.S1).event(Event.NEW).and()

				.withExternal().source(Status.S1).target(Status.FORK_S2).and()

				.withFork().source(Status.FORK_S2).target(Status.S2).and()

				.withExternal().source(Status.S21I).target(Status.S21_NOT_REQUIRED).event(Event.S21_NOT_REQUIRED)
				.and()

				.withExternal().source(Status.S21_NOT_REQUIRED).target(Status.S21E).and()

				.withExternal().source(Status.S22I).target(Status.S22_IN_PROGRESS).event(Event.S22_IN_PROGRESS)
				.and()

				.withExternal().source(Status.S22_IN_PROGRESS).target(Status.S22_PASSED).event(Event.S22_PASSED)
				.and()

				.withExternal().source(Status.S22_PASSED).target(Status.S22E).and()

				.withExternal().source(Status.S23I).target(Status.S23_IN_PROGRESS).and()

				.withExternal().source(Status.S23_IN_PROGRESS).target(Status.S23_PASSED).event(Event.S23_PASSED)
				.and()

				.withExternal().source(Status.S23_PASSED).target(Status.S23E).and()

				.withExternal().source(Status.S24I).target(Status.CHOICE_S24).and()

				.withChoice().source(Status.CHOICE_S24).first(Status.S24_IN_PROGRESS, ctx -> false)
				.last(Status.S24_NOT_REQUIRED).and()

				.withExternal().source(Status.S24_NOT_REQUIRED).target(Status.S24E).and()

				.withExternal().source(Status.S24_IN_PROGRESS).target(Status.S24_PASSED).event(Event.S24_PASSED)
				.and()

				.withExternal().source(Status.S24_PASSED).target(Status.S24E).and()

				.withJoin().source(Status.S2).target(Status.JOIN_S2).and()

				.withExternal().source(Status.JOIN_S2).target(Status.S3).guard(ctx -> true).and()

				.withExternal().source(Status.S3).target(Status.S4).guard(toggledVarTrue()).and()

				.withInternal().source(Status.ROOT).event(Event.TOGGLE_STATUS_0)
				.action(ctx -> ctx.getExtendedState().getVariables().put("toggle_status", "0")).and()

				.withInternal().source(Status.ROOT).event(Event.TOGGLE_STATUS_1)
				.action(ctx -> ctx.getExtendedState().getVariables().put("toggle_status", "1"));
		}

		private Guard<Status, Event> toggledVarTrue() {
			return context -> {
				boolean guard = String.valueOf(context.getExtendedState().get("toggle_status", String.class))
						.equalsIgnoreCase("1");
				return guard;
			};
		}

	}

	public enum Status {
		ROOT,
		S0,
		S1,
		FORK_S2, S2, JOIN_S2,
		S21I, S21_IN_PROGRESS, S21_NOT_REQUIRED, S21E,
		S22I, S22_IN_PROGRESS, S22_PASSED, S22E,
		S23I, S23_IN_PROGRESS, S23_PASSED, S23E,
		S24I, S24_PASSED, S24_IN_PROGRESS, S24_NOT_REQUIRED, S24E, CHOICE_S24,
		S4, S5, S3;
	}

	public enum Event {
		NEW,
		S21_NOT_REQUIRED,
		S22_IN_PROGRESS, S22_PASSED,
		S23_PASSED,
		TOGGLE_STATUS_0, TOGGLE_STATUS_1, S24_PASSED,
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
