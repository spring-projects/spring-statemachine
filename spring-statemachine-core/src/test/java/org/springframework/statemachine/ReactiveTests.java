/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.statemachine.assertj.StateMachineAsserts.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachineEventResult.ResultType;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ReactiveTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private static <T> Mono<Message<T>> asMono(T event) {
		return Mono.just(MessageBuilder.withPayload(event).build());
	}

	@SafeVarargs
	private static <T> Flux<Message<T>> asFlux(T... events) {
		return Flux.fromArray(events).map(e -> MessageBuilder.withPayload(e).build());
	}

	private static <S, E> void verifyStart(StateMachine<S, E> machine) {
		StepVerifier.create(machine.startReactively()).expectComplete().verify();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMonosAllAccepted() {
		context.register(Config1.class);
		context.refresh();
		assertThat(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)).isTrue();
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine).isNotNull();
		verifyStart(machine);
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S1);

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E1)))
			.assertNext(r -> {
				assertThat(r).hasResultType(ResultType.ACCEPTED);
				assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S2);
			})
			.expectComplete()
			.verify();

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E2)))
			.assertNext(r -> {
				assertThat(r).hasResultType(ResultType.ACCEPTED);
				assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S3);
			})
			.expectComplete()
			.verify();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFluxAllAccepted() {
		context.register(Config1.class);
		context.refresh();
		assertThat(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)).isTrue();
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine).isNotNull();
		verifyStart(machine);
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S1);

		StepVerifier.create(machine.sendEvents(asFlux(TestEvents.E1, TestEvents.E2)))
			.expectNextMatches(r -> r.getResultType() == ResultType.ACCEPTED)
			.expectNextMatches(r -> r.getResultType() == ResultType.ACCEPTED)
			.expectComplete()
			.verify();
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMonosSomeDenied() {
		context.register(Config1.class);
		context.refresh();
		assertThat(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)).isTrue();
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine).isNotNull();
		verifyStart(machine);
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S1);

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E1)))
			.expectNextMatches(r -> r.getResultType() == ResultType.ACCEPTED)
			.expectComplete()
			.verify();
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S2);

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E2)))
			.expectNextMatches(r -> r.getResultType() == ResultType.ACCEPTED)
			.expectComplete()
			.verify();
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S3);

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E3)))
			.expectNextMatches(r -> r.getResultType() == ResultType.DENIED)
			.expectComplete()
			.verify();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJoin() throws Exception {
		context.register(BaseConfig.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(machine).isNotNull();
		verifyStart(machine);
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.SI);

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E1)))
			.expectNextMatches(r -> r.getResultType() == ResultType.ACCEPTED)
			.expectComplete()
			.verify();
		await().until(() -> machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S20, TestStates.S30));

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E2)))
			.expectNextCount(3)
			.expectComplete()
			.verify();
		await().until(() -> machine.getState().getIds(), containsInAnyOrder(TestStates.S2, TestStates.S21, TestStates.S30));

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E3)))
			.expectNextCount(3)
			.expectComplete()
			.verify();
		await().until(() -> machine.getState().getIds(), containsInAnyOrder(TestStates.S4));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMonosSomeDefer() {
		context.register(Config3.class);
		context.refresh();
		assertThat(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)).isTrue();
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine).isNotNull();
		verifyStart(machine);
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder("READY");

		StepVerifier.create(machine.sendEvent(asMono("E1")))
			.assertNext(r -> {
				assertThat(r).hasResultType(ResultType.ACCEPTED);
				assertThat(machine.getState().getIds()).containsExactlyInAnyOrder("S1");
			})
			.expectComplete()
			.verify();

		StepVerifier.create(machine.sendEvent(asMono("E3")))
			.assertNext(r -> {
				assertThat(r).hasResultType(ResultType.DEFERRED);
				assertThat(machine.getState().getIds()).containsExactlyInAnyOrder("S1");
			})
			.expectComplete()
			.verify();

		StepVerifier.create(machine.sendEvent(asMono("E2")))
			.assertNext(r -> {
				assertThat(r).hasResultType(ResultType.ACCEPTED);
				assertThat(machine.getState().getIds()).containsExactlyInAnyOrder("S3");
			})
			.expectComplete()
			.verify();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRegions() {
		context.register(Config4.class);
		context.refresh();
		assertThat(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE)).isTrue();
		StateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);
		assertThat(machine).isNotNull();
		verifyStart(machine);
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S10, TestStates.S20);

		List<StateMachineEventResult<TestStates, TestEvents>> ers = new ArrayList<>();

		StepVerifier.create(machine.sendEvent(asMono(TestEvents.E1)))
			.thenConsumeWhile(er -> true, er -> {
				ers.add(er);
			})
			.expectComplete()
			.verify();

		assertThat(ers).filteredOnAssertions(er -> assertThat(er).hasResultType(ResultType.ACCEPTED)).hasSize(1);
		assertThat(ers).filteredOnAssertions(er -> assertThat(er).hasResultType(ResultType.DENIED)).hasSize(1);
		assertThat(machine.getState().getIds()).containsExactlyInAnyOrder(TestStates.S11, TestStates.S20);
	}

	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S3)
					.event(TestEvents.E2);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.SI)
					.state(TestStates.S2)
					.join(TestStates.S3)
					.state(TestStates.S4)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S30)
						.state(TestStates.S30)
						.state(TestStates.S31);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.SI)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S30)
					.target(TestStates.S31)
					.event(TestEvents.E3)
					.and()
				.withJoin()
					.source(TestStates.S21)
					.source(TestStates.S31)
					.target(TestStates.S3)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.SI)
					.event(TestEvents.E4);
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("READY")
					.state("S1", "E3")
					.state("S2")
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("READY").target("S1")
					.event("E1")
					.and()
				.withExternal()
					.source("S1").target("S2")
					.event("E2")
					.and()
				.withExternal()
					.source("S2").target("S3")
					.event("E3");
		}
	}

	@Configuration
	@EnableStateMachine
	static class Config4 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S10)
					.state(TestStates.S11)
					.and()
				.withStates()
					.initial(TestStates.S20)
					.state(TestStates.S21);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S10)
					.target(TestStates.S11)
					.event(TestEvents.E1)
					.and()
				.withExternal()
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2);
		}
	}
}
