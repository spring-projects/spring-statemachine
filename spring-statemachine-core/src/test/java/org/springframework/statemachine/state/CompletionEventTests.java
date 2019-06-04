/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.state;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.resolveMachine;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class CompletionEventTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testSimpleStateWithStateActionCompletes() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);
		TestCountAction testAction2 = context.getBean("testAction2", TestCountAction.class);
		doStartAndAssert(machine);

		doSendEventAndConsumeAll(machine, "E1");

		assertThat(testAction2.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction2.count, is(1));
		await().until(() -> machine.getState().getIds(), containsInAnyOrder("S3"));
		assertThat(machine.getState().getId(), is("S3"));
	}

	@Test
	public void testSimpleStateWithStateActionCompletesThreading() throws Exception {
		context.register(Config1.class, BaseConfig2.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);
		TestCountAction testAction2 = context.getBean("testAction2", TestCountAction.class);

		doStartAndAssert(machine);
		Thread.sleep(1000);

		doSendEventAndConsumeAll(machine, "E1");

		assertThat(testAction2.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(testAction2.count, is(1));
		Thread.sleep(1000);
		assertThat(machine.getState().getId(), is("S3"));
	}

	@Test
	public void testSimpleStateWithoutStateActionCompletes() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		assertThat(machine.getState().getId(), is("S1"));

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getId(), is("S3"));
	}

	public void testSubmachineWithStateActionCompletes() throws Exception {
	}

	@Test
	public void testSubmachineWithoutStateActionCompletes() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		assertThat(machine.getState().getId(), is("S1"));

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getId(), is("S3"));
	}

	@Test
	public void testSubmachineWithoutStateActionCompletes2() throws Exception {
		context.register(Config5.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		assertThat(machine.getState().getId(), is("S1"));

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getId(), is("S3"));
	}

	@Test
	public void testSubmachineWithoutStateActionCompletesThreading() throws Exception {
		context.register(Config3.class, BaseConfig2.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		Thread.sleep(200);
		assertThat(machine.getState().getId(), is("S1"));

		doSendEventAndConsumeAll(machine, "E1");
		Thread.sleep(200);
		assertThat(machine.getState().getId(), is("S3"));
	}

	public void testRegionWithStateActionCompletes() throws Exception {
	}

	@Test
	public void testRegionWithoutStateActionCompletes() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		assertThat(machine.getState().getId(), is("S1"));

		doSendEventAndConsumeAll(machine, "E1");
		assertThat(machine.getState().getId(), is("S3"));
	}

	@Test
	public void testRegionWithoutStateActionCompletesWithMultipleEnds1() throws Exception {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		assertThat(machine.getState().getId(), is("S1"));

		doSendEventAndConsumeAll(machine, "E1");
		doSendEventAndConsumeAll(machine, "E2");

		// TODO: REACTOR think this change is because we do subcribe
		//       with onComplete so things are not fully changed with sendEvent
		Awaitility.await().until(() -> machine.getState().getId(), is("S3"));
		// assertThat(machine.getState().getId(), is("S3"));
	}

	@Test
	public void testRegionWithoutStateActionCompletesWithMultipleEnds2() throws Exception {
		context.register(Config6.class);
		context.refresh();
		StateMachine<String,String> machine = resolveMachine(context);

		doStartAndAssert(machine);
		assertThat(machine.getState().getId(), is("S1"));

		doSendEventAndConsumeAll(machine, "E1");
		doSendEventAndConsumeAll(machine, "E3");

		// TODO: REACTOR think this change is because we do subcribe
		//       with onComplete so things are not fully changed with sendEvent
		Awaitility.await().until(() -> machine.getState().getId(), is("S3"));
		// assertThat(machine.getState().getId(), is("S3"));
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.stateDo("S2", testAction2())
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}

		@Bean
		public TestCountAction testAction2() {
			return new TestCountAction() {
				@Override
				public void execute(StateContext<String, String> context) {
					for (int i = 0; i < 10; i++) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
					super.execute(context);
				}
			};
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3")
					.and()
					.withStates()
						.parent("S2")
						.initial("S21")
						.state("S22")
						.end("S23");

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S21")
					.target("S22")
					.and()
				.withExternal()
					.source("S22")
					.target("S23")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3")
					.and()
					.withStates()
						.parent("S2")
						.initial("S201")
						.state("S202")
						.end("S203")
						.and()
					.withStates()
						.parent("S2")
						.initial("S211")
						.state("S212")
						.end("S213");

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S201")
					.target("S202")
					.and()
				.withExternal()
					.source("S202")
					.target("S203")
					.and()
				.withExternal()
					.source("S211")
					.target("S212")
					.and()
				.withExternal()
					.source("S212")
					.target("S213")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3")
					.and()
					.withStates()
						.parent("S2")
						.initial("S21")
						.state("S22")
						.end("S23")
						.and()
						.withStates()
							.parent("S22")
							.initial("S221")
							.state("S222")
							.end("S223");

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S21")
					.target("S22")
					.and()
				.withExternal()
					.source("S22")
					.target("S23")
					.and()
				.withExternal()
					.source("S221")
					.target("S222")
					.and()
				.withExternal()
					.source("S222")
					.target("S223")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config6 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3")
					.and()
					.withStates()
						.parent("S2")
						.initial("S201")
						.state("S202")
						.end("S203")
						.end("S204")
						.and()
					.withStates()
						.parent("S2")
						.initial("S211")
						.state("S212")
						.end("S213");

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withExternal()
					.source("S201")
					.target("S202")
					.and()
				.withExternal()
					.source("S202")
					.target("S203")
					.event("E2")
					.and()
				.withExternal()
					.source("S202")
					.target("S204")
					.event("E3")
					.and()
				.withExternal()
					.source("S211")
					.target("S212")
					.and()
				.withExternal()
					.source("S212")
					.target("S213")
					.and()
				.withExternal()
					.source("S2")
					.target("S3");
		}
	}

	private static class TestCountAction implements Action<String, String> {

		int count = 0;
		StateContext<String, String> context;
		CountDownLatch latch = new CountDownLatch(1);

		public TestCountAction() {
			count = 0;
		}

		@Override
		public void execute(StateContext<String, String> context) {
			this.context = context;
			count++;
			latch.countDown();
		}

	}

}
