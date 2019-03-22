/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * State machine context events related tests.
 *
 * @author Janne Valkealahti
 *
 */
public class ContextEventTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void contextEventsEnabled() throws Exception {
		context.register(BaseConfig.class, Config.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		machine.sendEvent(TestEvents.E1);
		StateMachineApplicationEventListener listener = context.getBean(StateMachineApplicationEventListener.class);
		listener.latch.await(1, TimeUnit.SECONDS);
		assertThat(listener.count, greaterThan(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void contextEventsDisabled() throws Exception {
		context.register(BaseConfig.class, Config.class, Config2.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		machine.sendEvent(TestEvents.E1);
		StateMachineApplicationEventListener listener = context.getBean(StateMachineApplicationEventListener.class);
		listener.latch.await(1, TimeUnit.SECONDS);
		assertThat(listener.count, is(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void contextEventsWithManualBuilder() throws Exception {
		context.register(BaseConfig.class, Config.class, Config3.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();
		machine.sendEvent(TestEvents.E1);
		StateMachineApplicationEventListener listener = context.getBean(StateMachineApplicationEventListener.class);
		listener.latch.await(1, TimeUnit.SECONDS);
		assertThat(listener.count, greaterThan(1));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void contextEventsWithManualBuilderExternalConfigClass() throws Exception {
		context.register(BaseConfig.class, Config.class, ExternalConfig.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		StateMachineApplicationEventListener listener = context.getBean(StateMachineApplicationEventListener.class);
		machine.start();
		listener.latch.await(1, TimeUnit.SECONDS);
		listener.reset();
		machine.sendEvent(TestEvents.E1);
		listener.latch.await(1, TimeUnit.SECONDS);
		assertThat(listener.count, greaterThan(1));
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
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1);
		}

	}

	@Configuration
	@EnableStateMachine(contextEvents = false)
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1);
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config3 {

		@Bean
		public StateMachine<TestStates, TestEvents> stateMachine() throws Exception {

			Builder<TestStates, TestEvents> builder = StateMachineBuilder.builder();
			builder.configureStates()
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2);
			builder.configureTransitions()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1);
			return builder.build();
		}

	}

	@Configuration
	static class Config {

		@Bean
		public StateMachineApplicationEventListener stateMachineApplicationEventListener() {
			return new StateMachineApplicationEventListener();
		}
	}

	static class StateMachineApplicationEventListener implements ApplicationListener<StateMachineEvent> {

		volatile CountDownLatch latch = new CountDownLatch(1);
		volatile int count = 0;

	    @Override
	    public void onApplicationEvent(StateMachineEvent event) {
	    	count++;
	    	latch.countDown();
	    }

		public void reset() {
			count = 0;
			latch = new CountDownLatch(1);
		}
	}

}
