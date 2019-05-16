/*
 * Copyright 2016 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;

public class StateMachinePersistTests2 extends AbstractStateMachineTests {

	// States
	public static final String RECORD_AWAITING_CONTENT = "RECORD_AWAITING_CONTENT";
	public static final String RECORD_LOGGING_ACTIVE = "RECORD_LOGGING_ACTIVE";
	public static final String RECORD_LOGGED = "RECORD_LOGGED";
	public static final String RECORD_DISCARDED = "RECORD_DISCARDED";
	public static final String RECORD_LOGGING_ON_HOLD_WITH_ERROR = "RECORD_LOGGING_ON_HOLD_WITH_ERROR";
	public static final String RECORD_LOGGING_IN_PROGRESS = "RECORD_LOGGING_IN_PROGRESS";
	public static final String RECORD_AWAITING_LOGGING = "RECORD_AWAITING_LOGGING";
	public static final String RECORD_LOGGING_ON_HOLD = "RECORD_LOGGING_ON_HOLD";
	public static final String HISTORY = "HISTORY";

	// Events
	public static final String UPLOAD_RECORD = "UPLOAD_RECORD";
	public static final String SUSPEND_RECORD_LOGGING_WITH_ERROR = "SUSPEND_RECORD_LOGGING_WITH_ERROR";
	public static final String DISCARD_RECORD = "DISCARD_RECORD";
	public static final String CANCEL_RECORD_LOGGING = "CANCEL_RECORD_LOGGING";
	public static final String LOG_RECORD = "LOG_RECORD";
	public static final String SUSPEND_RECORD_LOGGING = "SUSPEND_RECORD_LOGGING";
	public static final String START_LOGGING_RECORD = "START_LOGGING_RECORD";
	public static final String RESUME_RECORD_LOGGING = "RESUME_RECORD_LOGGING";

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testVariousPersistOperations() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachinePersist<String, String, String> stateMachinePersist = new InMemoryStateMachinePersist();
		StateMachinePersister<String, String, String> persister = new DefaultStateMachinePersister<String, String, String>(
				stateMachinePersist);

		StateMachineFactory<String, String> factory = context.getBean("LOG_RECORD", StateMachineFactory.class);
		StateMachine<String,String> m = factory.getStateMachine();

		assertThat(m.getState().getId(), equalTo(RECORD_AWAITING_CONTENT));

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(UPLOAD_RECORD), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[] { RECORD_LOGGING_ACTIVE, RECORD_AWAITING_LOGGING }));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ACTIVE, RECORD_AWAITING_LOGGING}));
		persister.persist(m, "xxx");

		assertThat(m.sendEvent(SUSPEND_RECORD_LOGGING), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ACTIVE, RECORD_LOGGING_ON_HOLD}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ACTIVE, RECORD_LOGGING_ON_HOLD}));
		persister.persist(m, "xxx");

		assertThat(m.sendEvent(START_LOGGING_RECORD), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_IN_PROGRESS}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(CANCEL_RECORD_LOGGING), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ACTIVE, RECORD_LOGGING_ON_HOLD}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(RESUME_RECORD_LOGGING), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ACTIVE, RECORD_AWAITING_LOGGING}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(START_LOGGING_RECORD), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_IN_PROGRESS}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(CANCEL_RECORD_LOGGING), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ACTIVE, RECORD_AWAITING_LOGGING}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(SUSPEND_RECORD_LOGGING_WITH_ERROR), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ON_HOLD_WITH_ERROR}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(RESUME_RECORD_LOGGING), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_ACTIVE, RECORD_AWAITING_LOGGING}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(START_LOGGING_RECORD), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGING_IN_PROGRESS}));
		persister.persist(m, "xxx");

		m = loadStateMachine(factory, persister, "xxx");
		assertThat(m.sendEvent(LOG_RECORD), is(true));
		assertThat(m.getState().getIds(), containsInAnyOrder(new String[]{RECORD_LOGGED}));
		persister.persist(m, "xxx");
	}

	private static StateMachine<String, String> loadStateMachine(StateMachineFactory<String, String> factory,
			StateMachinePersister<String, String, String> persister, final String id) throws Exception {
		StateMachine<String, String> stateMachine = factory.getStateMachine();
		persister.restore(stateMachine, id);
		return stateMachine;
	}

	@Configuration
	@EnableStateMachineFactory(name = "LOG_RECORD", contextEvents = false)
	static class Config1 extends StateMachineConfigurerAdapter<String,String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String,String> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String,String> states) throws Exception {
			states.withStates()
				.initial(RECORD_AWAITING_CONTENT)
				.state(RECORD_LOGGING_ON_HOLD_WITH_ERROR)
				.state(RECORD_LOGGING_ACTIVE)
				.state(RECORD_LOGGING_IN_PROGRESS)
				.end(RECORD_LOGGED)
				.end(RECORD_DISCARDED)
				.and()
				.withStates()
					.parent(RECORD_LOGGING_ACTIVE)
					.initial(RECORD_AWAITING_LOGGING)
					.state(RECORD_LOGGING_ON_HOLD)
					.history(HISTORY, StateConfigurer.History.DEEP);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String,String> transitions) throws Exception {
			transitions
				/* ****************************************************************************** */
				/*                          FROM RECORD_AWAITING_CONTENT                          */
				/* ****************************************************************************** */
				.withExternal()
					.source(RECORD_AWAITING_CONTENT)
					.event(UPLOAD_RECORD)
					.target(RECORD_LOGGING_ACTIVE)
					.and()
				.withExternal()
					.source(RECORD_AWAITING_CONTENT)
					.event(SUSPEND_RECORD_LOGGING_WITH_ERROR)
					.target(RECORD_LOGGING_ON_HOLD_WITH_ERROR)
					.and()
				.withExternal()
					.source(RECORD_AWAITING_CONTENT)
					.event(DISCARD_RECORD)
					.target(RECORD_DISCARDED)
					.and()
				/* ****************************************************************************** */
				/*                          FROM RECORD_LOGGING_ACTIVE                            */
				/* ****************************************************************************** */
				.withExternal()
					.source(RECORD_LOGGING_ACTIVE)
					.event(SUSPEND_RECORD_LOGGING_WITH_ERROR)
					.target(RECORD_LOGGING_ON_HOLD_WITH_ERROR)
					.and()
				.withExternal()
					.source(RECORD_LOGGING_ACTIVE)
					.event(DISCARD_RECORD)
					.target(RECORD_DISCARDED)
					.and()

				/* ****************************************************************************** */
				/*                       FROM RECORD_LOGGING_IN_PROGRESS                          */
				/* ****************************************************************************** */
				.withExternal()
					.source(RECORD_LOGGING_IN_PROGRESS)
					.event(CANCEL_RECORD_LOGGING)
					.target(HISTORY)
					.and()
				.withExternal()
					.source(RECORD_LOGGING_IN_PROGRESS)
					.event(DISCARD_RECORD)
					.target(RECORD_DISCARDED)
					.and()
				.withExternal()
					.source(RECORD_LOGGING_IN_PROGRESS)
					.event(LOG_RECORD)
					.target(RECORD_LOGGED)
					.and()


				/* ****************************************************************************** */
				/*                       FROM RECORD_AWAITING_LOGGING                          */
				/* ****************************************************************************** */
				.withExternal()
					.source(RECORD_AWAITING_LOGGING)
					.event(SUSPEND_RECORD_LOGGING)
					.target(RECORD_LOGGING_ON_HOLD)
					.and()
				.withExternal()
					.source(RECORD_AWAITING_LOGGING)
					.event(START_LOGGING_RECORD)
					.target(RECORD_LOGGING_IN_PROGRESS)
					.and()


				/* ****************************************************************************** */
				/*                       FROM RECORD_LOGGING_ON_HOLD                              */
				/* ****************************************************************************** */
				.withExternal()
					.source(RECORD_LOGGING_ON_HOLD)
					.event(START_LOGGING_RECORD)
					.target(RECORD_LOGGING_IN_PROGRESS)
					.and()
				.withExternal()
					.source(RECORD_LOGGING_ON_HOLD)
					.event(RESUME_RECORD_LOGGING)
					.target(RECORD_AWAITING_LOGGING)
					.and()


				/* ****************************************************************************** */
				/*                       FROM RECORD_LOGGING_ON_HOLD_WITH_ERROR                   */
				/* ****************************************************************************** */
				.withExternal()
					.source(RECORD_LOGGING_ON_HOLD_WITH_ERROR)
					.event(RESUME_RECORD_LOGGING)
					.target(HISTORY)
					.and()
				.withExternal()
					.source(RECORD_LOGGING_ON_HOLD_WITH_ERROR)
					.event(DISCARD_RECORD)
					.target(RECORD_DISCARDED)
					.and();
		}
	}

	static class InMemoryStateMachinePersist implements StateMachinePersist<String, String, String> {
		private final Map<Object,StateMachineContext<String, String>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<String, String> stateMachineContext, String contextOjb) throws Exception {
			contexts.put(contextOjb,stateMachineContext);
		}

		@Override
		public StateMachineContext<String, String> read(String contextOjb) throws Exception {
			return contexts.get(contextOjb);
		}
	}
}
