/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.statemachine.persist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doSendEventAndConsumeAll;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;

/**
 * Tests for {@link DefaultStateMachinePersister}.
 *
 * @author Janne Valkealahti
 *
 */
public class DefaultStateMachinePersisterTests {

	@Test
	public void testSimpleFlat() throws Exception {
		StateMachine<String, String> machine = buildSimpleFlat();

		doStartAndAssert(machine);
		doStartAndAssert(machine);
		InMemoryStateMachinePersist1 persist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(persist);
		persister.persist(machine, "xxx");
		StateMachineContext<String, String> context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("SI");
		assertThat(context.getId()).isNull();
		assertThat(context.getChilds().isEmpty()).isTrue();

		doSendEventAndConsumeAll(machine, "E1");
		doSendEventAndConsumeAll(machine, "E1");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S1");
	}

	@Test
	public void testDeepNested() throws Exception {
		StateMachine<String, String> machine = buildDeepNested();

		doStartAndAssert(machine);
		InMemoryStateMachinePersist1 persist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(persist);
		persister.persist(machine, "xxx");
		StateMachineContext<String, String> context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("SI");
		assertThat(context.getId()).isNull();
		assertThat(context.getChilds().isEmpty()).isTrue();

		doSendEventAndConsumeAll(machine, "E1");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S1I");
		assertThat(context.getChilds()).hasSize(1);

		doSendEventAndConsumeAll(machine, "E2");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S11");
		assertThat(context.getChilds()).hasSize(1);

		doSendEventAndConsumeAll(machine, "E3");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S11");
		assertThat(context.getChilds()).hasSize(1);
	}

	@Test
	public void testDeepNestedRegions() throws Exception {
		StateMachine<String, String> machine = buildDeepNestedRegions();

		doStartAndAssert(machine);
		InMemoryStateMachinePersist1 persist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(persist);
		persister.persist(machine, "xxx");
		StateMachineContext<String, String> context = persist.contexts.get("xxx");
		assertThat(context.getState()).isNull();
		assertThat(context.getId()).isNull();
		assertThat(context.getChilds()).hasSize(2);
		assertThat(context.getChilds().get(0).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S111"),
			state -> assertThat(state).isEqualTo("S21")
		);
		assertThat(context.getChilds().get(1).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S111"),
			state -> assertThat(state).isEqualTo("S21")
		);

		doSendEventAndConsumeAll(machine, "E1");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getChilds()).hasSize(2);
		assertThat(context.getChilds().get(0).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S12"),
			state -> assertThat(state).isEqualTo("S21")
		);
		assertThat(context.getChilds().get(1).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S12"),
			state -> assertThat(state).isEqualTo("S21")
		);

		doSendEventAndConsumeAll(machine, "E2");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getChilds()).hasSize(2);
		assertThat(context.getChilds().get(0).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S12"),
			state -> assertThat(state).isEqualTo("S221")
		);
		assertThat(context.getChilds().get(1).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S12"),
			state -> assertThat(state).isEqualTo("S221")
		);

		doSendEventAndConsumeAll(machine, "E3");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getChilds()).hasSize(2);
		assertThat(context.getChilds().get(0).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S12"),
			state -> assertThat(state).isEqualTo("S222")
		);
		assertThat(context.getChilds().get(1).getState()).satisfiesAnyOf(
			state -> assertThat(state).isEqualTo("S12"),
			state -> assertThat(state).isEqualTo("S222")
		);
	}

	@Test
	public void testDeepNestedRegionsAndFork() throws Exception {
		StateMachine<String, String> machine = buildDeepNestedRegionsAndFork();

		doStartAndAssert(machine);
		InMemoryStateMachinePersist1 persist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(persist);
		persister.persist(machine, "xxx");
		StateMachineContext<String, String> context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S2");

		doSendEventAndConsumeAll(machine, "E1");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S3");
		assertThat(context.getChilds()).hasSize(1);
		assertThat(context.getChilds().get(0).getChilds()).hasSize(2);

		doSendEventAndConsumeAll(machine, "E2");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S3");
		assertThat(context.getChilds()).hasSize(1);
		assertThat(context.getChilds().get(0).getChilds()).hasSize(2);

		doSendEventAndConsumeAll(machine, "E3");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S3");
		assertThat(context.getChilds()).hasSize(1);
		assertThat(context.getChilds().get(0).getChilds()).hasSize(2);

		doSendEventAndConsumeAll(machine, "E4");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("S3");
		assertThat(context.getChilds()).hasSize(1);
		assertThat(context.getChilds().get(0).getChilds()).hasSize(2);

		doSendEventAndConsumeAll(machine, "E5");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState()).isEqualTo("END");
		assertThat(context.getChilds()).hasSize(1);
		assertThat(context.getChilds().get(0).getChilds().isEmpty()).isTrue();
	}

	private StateMachine<String, String> buildSimpleFlat() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial("SI")
				.state("S1");
		builder.configureTransitions()
			.withExternal()
				.source("SI")
				.target("S1")
				.event("E1");
		return builder.build();
	}

	private StateMachine<String, String> buildDeepNested() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial("SI")
				.state("S1")
				.and()
					.withStates()
						.parent("S1")
						.initial("S1I")
						.state("S11")
						.and()
							.withStates()
								.parent("S11")
								.initial("S11I")
								.state("S111");
		builder.configureTransitions()
			.withExternal()
				.source("SI")
				.target("S1")
				.event("E1")
				.and()
			.withExternal()
				.source("S1I")
				.target("S11")
				.event("E2")
				.and()
			.withExternal()
				.source("S11I")
				.target("S111")
				.event("E3");
		return builder.build();
	}

	private StateMachine<String, String> buildDeepNestedRegions() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
			.withStates()
				.initial("S11")
				.state("S12")
				.state("S13")
				.and()
					.withStates()
						.parent("S11")
						.initial("S111")
						.state("S111")
						.state("S112")
						.and()
			.withStates()
				.initial("S21")
				.state("S22")
				.state("S23")
				.and()
					.withStates()
						.parent("S22")
						.initial("S221")
						.state("S221")
						.state("S222");

		builder.configureTransitions()
			.withExternal()
				.source("S11")
				.target("S12")
				.event("E1")
				.and()
			.withExternal()
				.source("S21")
				.target("S22")
				.event("E2")
				.and()
			.withExternal()
				.source("S221")
				.target("S222")
				.event("E3");

		return builder.build();
	}

	private StateMachine<String, String> buildDeepNestedRegionsAndFork() throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates()
		   	.withStates()
			   	.initial("S1")
			   	.and()
				   	.withStates()
					   	.parent("S1")
					   	.initial("S2")
					   	.state("S22")
					   	.fork("F1")
					   	.state("S3")
						.join("J1")
						.end("END")
					   	.and()
						   	.withStates()
							   	.parent("S3")
									.initial("S4")
									.state("S41")
									.end("S4E")
									.and()
							.withStates()
								.parent("S3")
									.initial("S5")
									.state("S51")
									.end("S5E");

		builder.configureTransitions()
		   	.withExternal()
			   	.source("S2")
			   	.target("S22")
			   	.event("E1")
			   	.and()
		   	.withExternal()
			   	.source("S22")
			   	.target("F1")
		   	.and()
		   	.withFork()
			   	.source("F1")
			   	.target("S3")
			   	.and()
		   	.withExternal()
				.source("S4")
				.target("S41")
				.event("E2")
				.and()
		   	.withExternal()
				.source("S41")
				.target("S4E")
				.event("E3")
				.and()
		   	.withExternal()
				.source("S5")
				.target("S51")
				.event("E4")
				.and()
		   	.withExternal()
				.source("S51")
				.target("S5E")
				.event("E5")
				.and()
			.withJoin()
				.source("S3")
				.target("J1")
				.and()
			.withExternal()
				.source("J1")
				.target("END");

		return builder.build();
	}

	static class InMemoryStateMachinePersist1 implements StateMachinePersist<String, String, String> {

		public final HashMap<String, StateMachineContext<String, String>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<String, String> context, String contextObj) throws Exception {
			contexts.put(contextObj, context);
		}

		@Override
		public StateMachineContext<String, String> read(String contextObj) throws Exception {
			return contexts.get(contextObj);
		}
	}

}
