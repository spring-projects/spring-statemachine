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
package org.springframework.statemachine.monitor;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.transition.Transition;

public class StateMachineMonitorTests extends AbstractStateMachineTests {

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testSimpleMonitor() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, StateMachine.class);

		TestStateMachineMonitor monitor = context.getBean(TestStateMachineMonitor.class);
		Action<String, String> taction = context.getBean("taction", Action.class);
		Action<String, String> enaction = context.getBean("enaction", Action.class);
		Action<String, String> exaction = context.getBean("exaction", Action.class);
		LatchAction saction = context.getBean("saction", LatchAction.class);

		machine.start();
		assertThat(machine.getState().getIds(), contains("S1"));
		machine.sendEvent("E1");
		assertThat(machine.getState().getIds(), contains("S2"));
		// there's also initial transition, thus 2 instead 1
		assertThat(monitor.transitions.size(), is(2));
		assertThat(saction.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(monitor.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(monitor.actions.size(), is(4));
		assertThat(monitor.actions.keySet(), containsInAnyOrder(taction, enaction, exaction, saction));
		monitor.reset();
		machine.sendEvent("E2");
		assertThat(machine.getState().getIds(), contains("S1"));
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withMonitoring()
					.monitor(stateMachineMonitor());
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S1", null, exaction())
					.state("S2", saction())
					.state("S2", enaction(), null);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.action(taction())
					.event("E1")
					.and()
				.withExternal()
					.source("S2")
					.target("S1")
					.event("E2");
		}

		@Bean
		public Action<String, String> taction() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			};
		}

		@Bean
		public Action<String, String> enaction() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			};
		}

		@Bean
		public Action<String, String> exaction() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			};
		}

		@Bean
		public LatchAction saction() {
			return new LatchAction();
		}

		@Bean
		public StateMachineMonitor<String, String> stateMachineMonitor() {
			return new TestStateMachineMonitor();
		}

	}

	private static class LatchAction implements Action<String, String> {
		final CountDownLatch latch = new CountDownLatch(1);
		@Override
		public void execute(StateContext<String, String> context) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			} finally {
				latch.countDown();
			}
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	private static class TestStateMachineMonitor extends AbstractStateMachineMonitor<String, String> {

		Map<Transition<String, String>, Transitions> transitions = new HashMap<>();
		Map<Action<String, String>, Actions> actions = new HashMap<>();
		CountDownLatch latch = new CountDownLatch(4);

		@Override
		public void transition(StateMachine<String, String> stateMachine, Transition<String, String> transition, long duration) {
			transitions.put(transition, new Transitions(transition, duration));
		}

		@Override
		public void action(StateMachine<String, String> stateMachine, Action<String, String> action,
				long duration) {
			actions.put(action, new Actions(action, duration));
			latch.countDown();
		}

		void reset() {
			transitions.clear();
			actions.clear();
			latch = new CountDownLatch(4);
		}

		@SuppressWarnings("unused")
		static class Transitions {
			Transition<String, String> transition;
			Long duration;
			public Transitions(Transition<String, String> transition, Long duration) {
				super();
				this.transition = transition;
				this.duration = duration;
			}
		}
		@SuppressWarnings("unused")
		static class Actions {
			Action<String, String> action;
			Long duration;
			public Actions(Action<String, String> action, Long duration) {
				this.action = action;
				this.duration = duration;
			}
		}
	}
}
