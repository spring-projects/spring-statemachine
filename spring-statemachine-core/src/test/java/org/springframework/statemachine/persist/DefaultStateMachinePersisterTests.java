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
package org.springframework.statemachine.persist;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Test;
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

		machine.start();
		InMemoryStateMachinePersist1 persist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(persist);
		persister.persist(machine, "xxx");
		StateMachineContext<String, String> context = persist.contexts.get("xxx");
		assertThat(context.getState(), is("SI"));
		assertThat(context.getId(), nullValue());
		assertThat(context.getChilds().isEmpty(), is(true));

		machine.sendEvent("E1");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState(), is("S1"));
	}

	@Test
	public void testDeepNested() throws Exception {
		StateMachine<String, String> machine = buildDeepNested();

		machine.start();
		InMemoryStateMachinePersist1 persist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(persist);
		persister.persist(machine, "xxx");
		StateMachineContext<String, String> context = persist.contexts.get("xxx");
		assertThat(context.getState(), is("SI"));
		assertThat(context.getId(), nullValue());
		assertThat(context.getChilds().isEmpty(), is(true));

		machine.sendEvent("E1");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState(), is("S1I"));
		assertThat(context.getChilds().isEmpty(), is(true));

		machine.sendEvent("E2");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState(), is("S11I"));
		assertThat(context.getChilds().isEmpty(), is(true));

		machine.sendEvent("E3");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getState(), is("S111"));
		assertThat(context.getChilds().isEmpty(), is(true));
	}

	@Test
	public void testDeepNestedRegions() throws Exception {
		StateMachine<String, String> machine = buildDeepNestedRegions();

		machine.start();
		InMemoryStateMachinePersist1 persist = new InMemoryStateMachinePersist1();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<>(persist);
		persister.persist(machine, "xxx");
		StateMachineContext<String, String> context = persist.contexts.get("xxx");
		assertThat(context.getState(), nullValue());
		assertThat(context.getId(), nullValue());
		assertThat(context.getChilds().size(), is(2));
		assertThat(context.getChilds().get(0).getState(), anyOf(is("S111"), is("S21")));
		assertThat(context.getChilds().get(1).getState(), anyOf(is("S111"), is("S21")));

		machine.sendEvent("E1");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getChilds().size(), is(2));
		assertThat(context.getChilds().get(0).getState(), anyOf(is("S12"), is("S21")));
		assertThat(context.getChilds().get(1).getState(), anyOf(is("S12"), is("S21")));

		machine.sendEvent("E2");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getChilds().size(), is(2));
		assertThat(context.getChilds().get(0).getState(), anyOf(is("S12"), is("S221")));
		assertThat(context.getChilds().get(1).getState(), anyOf(is("S12"), is("S221")));

		machine.sendEvent("E3");
		persister.persist(machine, "xxx");
		context = persist.contexts.get("xxx");
		assertThat(context.getChilds().size(), is(2));
		assertThat(context.getChilds().get(0).getState(), anyOf(is("S12"), is("S222")));
		assertThat(context.getChilds().get(1).getState(), anyOf(is("S12"), is("S222")));
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
