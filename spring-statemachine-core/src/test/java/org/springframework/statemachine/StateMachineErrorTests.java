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
package org.springframework.statemachine;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.event.OnStateMachineError;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;

/**
 * Tests for various errors and error handling.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineErrorTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testEvents() throws Exception {
		context.register(Config.class, Config1.class);
		context.refresh();

		TestApplicationEventListener1 listener1 = context.getBean(TestApplicationEventListener1.class);
		TestApplicationEventListener2 listener3 = context.getBean(TestApplicationEventListener2.class);

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		assertThat(machine.hasStateMachineError(), is(false));

		TestStateMachineListener listener2 = new TestStateMachineListener();
		machine.addStateListener(listener2);

		machine.start();
		machine.setStateMachineError(new RuntimeException("myerror"));

		assertThat(listener1.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener1.count, is(1));
		assertThat(listener3.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener3.count, is(1));
		assertThat(listener2.latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener2.count, is(1));
		assertThat(machine.hasStateMachineError(), is(true));
	}

	@Test
	public void testInterceptHandlesError() throws Exception {
		context.register(Config.class, Config1.class);
		context.refresh();

		TestApplicationEventListener1 listener1 = context.getBean(TestApplicationEventListener1.class);

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		assertThat(machine.hasStateMachineError(), is(false));

		machine.getStateMachineAccessor().doWithRegion(new StateMachineFunction<StateMachineAccess<TestStates,TestEvents>>() {

			@Override
			public void apply(StateMachineAccess<TestStates, TestEvents> function) {
				function.addStateMachineInterceptor(new StateMachineInterceptorAdapter<TestStates,TestEvents>() {
					@Override
					public Exception stateMachineError(StateMachine<TestStates, TestEvents> stateMachine,
							Exception exception) {
						return null;
					}
				});
			}
		});

		TestStateMachineListener listener2 = new TestStateMachineListener();
		machine.addStateListener(listener2);

		machine.start();
		machine.setStateMachineError(new RuntimeException("myerror"));

		assertThat(listener1.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(listener1.count, is(0));
		assertThat(listener2.latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(listener2.count, is(0));
		assertThat(machine.hasStateMachineError(), is(false));
	}

	@Test
	public void testErrorActive() throws Exception {
		context.register(Config1.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);

		assertThat(machine.hasStateMachineError(), is(false));
		machine.start();
		machine.setStateMachineError(new RuntimeException("myerror"));
		assertThat(machine.hasStateMachineError(), is(true));
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));
		machine.sendEvent(TestEvents.E1);
		assertThat(machine.getState().getIds(), containsInAnyOrder(TestStates.S1));
	}


	@Configuration
	@EnableStateMachine
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S2)
					.state(TestStates.S3)
					.state(TestStates.S4);
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
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S3)
					.target(TestStates.S4)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S4)
					.target(TestStates.S3)
					.event(TestEvents.E4);
		}

	}

	@Configuration
	static class Config {

		@Bean
		public TestApplicationEventListener1 testApplicationEventListener1() {
			return new TestApplicationEventListener1();
		}

		@Bean
		public TestApplicationEventListener2 testApplicationEventListener2() {
			return new TestApplicationEventListener2();
		}
	}

	static class TestStateMachineListener extends StateMachineListenerAdapter<TestStates, TestEvents> {

		CountDownLatch latch = new CountDownLatch(1);
		int count = 0;

		@Override
		public void stateMachineError(StateMachine<TestStates, TestEvents> stateMachine, Exception exception) {
	    	count++;
	    	latch.countDown();
		}
	}

	static class TestApplicationEventListener1 implements ApplicationListener<StateMachineEvent> {

		CountDownLatch latch = new CountDownLatch(1);
		int count = 0;

	    @Override
	    public void onApplicationEvent(StateMachineEvent event) {
	    	if (event instanceof OnStateMachineError) {
		    	count++;
		    	latch.countDown();
	    	}
	    }
	}

	static class TestApplicationEventListener2 implements ApplicationListener<OnStateMachineError> {

		CountDownLatch latch = new CountDownLatch(1);
		int count = 0;

		@Override
		public void onApplicationEvent(OnStateMachineError event) {
	    	count++;
	    	latch.countDown();
		}

	}

}
