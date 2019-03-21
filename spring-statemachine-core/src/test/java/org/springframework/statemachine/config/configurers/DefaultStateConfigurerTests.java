/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.config.configurers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.springframework.statemachine.AbstractStateMachineTests.TestEntryAction;
import org.springframework.statemachine.AbstractStateMachineTests.TestEvents;
import org.springframework.statemachine.AbstractStateMachineTests.TestExitAction;
import org.springframework.statemachine.AbstractStateMachineTests.TestStates;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.builders.StateMachineStateBuilder;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.state.PseudoStateKind;

public class DefaultStateConfigurerTests {

	@Test
	public void testInitialWithoutState() throws Exception {
		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.initial(TestStates.SI);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));
		assertThat(builder.data.iterator().next().getState(), is(TestStates.SI));
	}

	@Test
	public void testInitialWithState() throws Exception {
		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.initial(TestStates.SI);
		configurer.state(TestStates.SI);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));
		assertThat(builder.data.iterator().next().getState(), is(TestStates.SI));
	}

	@Test
	public void testSameStateShouldResultOneState() throws Exception {
		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.state(TestStates.SI);
		configurer.state(TestStates.SI);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));
		assertThat(builder.data.iterator().next().getState(), is(TestStates.SI));
	}

	@Test
	public void testParentSet() throws Exception {
		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.parent(TestStates.SI);
		configurer.state(TestStates.S1);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));
		assertThat(builder.data.iterator().next().getState(), is(TestStates.S1));
		assertThat((TestStates)builder.data.iterator().next().getParent(), is(TestStates.SI));
	}

	@Test
	public void testActionsInitialFirst() throws Exception {
		Collection<Action<TestStates, TestEvents>> exitActions = Arrays.asList(testExitAction());

		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.initial(TestStates.S1);
		configurer.state(TestStates.S1, null, exitActions);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));

		assertThat(builder.data.iterator().next().getState(), is(TestStates.S1));
		assertThat(builder.data.iterator().next().getEntryActions(), nullValue());
		assertThat(builder.data.iterator().next().getExitActions(), notNullValue());
	}

	@Test
	public void testActionsJustState() throws Exception {
		Collection<Action<TestStates, TestEvents>> entryActions = Arrays.asList(testEntryAction());

		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.state(TestStates.S2, entryActions, null);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));

		assertThat(builder.data.iterator().next().getState(), is(TestStates.S2));
		assertThat(builder.data.iterator().next().getExitActions(), nullValue());
		assertThat(builder.data.iterator().next().getEntryActions(), notNullValue());
	}

	@Test
	public void testEndStateNoState() throws Exception {
		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.end(TestStates.SF);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));
		assertThat(builder.data.iterator().next().getState(), is(TestStates.SF));
	}

	@Test
	public void testEndStateAsState() throws Exception {
		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.state(TestStates.SF);
		configurer.end(TestStates.SF);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));
		assertThat(builder.data.iterator().next().getState(), is(TestStates.SF));
	}

	@Test
	public void testChoiceStateNoState() throws Exception {
		DefaultStateConfigurer<TestStates, TestEvents> configurer = new DefaultStateConfigurer<TestStates, TestEvents>();
		TestStateMachineStateBuilder builder = new TestStateMachineStateBuilder();
		configurer.choice(TestStates.S1);
		configurer.configure(builder);
		assertThat(builder.data, notNullValue());
		assertThat(builder.data.size(), is(1));
		assertThat(builder.data.iterator().next().getState(), is(TestStates.S1));
		assertThat(builder.data.iterator().next().getPseudoStateKind(), is(PseudoStateKind.CHOICE));
	}

	private static class TestStateMachineStateBuilder extends StateMachineStateBuilder<TestStates, TestEvents> {

		Collection<StateData<TestStates, TestEvents>> data;

		@Override
		public void addStateData(Collection<StateData<TestStates, TestEvents>> stateDatas) {
			this.data = stateDatas;
		}
	}

	private Action<TestStates, TestEvents> testEntryAction() {
		return new TestEntryAction();
	}

	private Action<TestStates, TestEvents> testExitAction() {
		return new TestExitAction();
	}

}
