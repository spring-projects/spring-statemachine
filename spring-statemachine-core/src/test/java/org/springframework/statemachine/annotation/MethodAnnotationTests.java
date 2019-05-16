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
package org.springframework.statemachine.annotation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer.History;

public class MethodAnnotationTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOnTransition() throws Exception {
		context.register(BaseConfig.class, BeanConfig1.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		Bean1 bean1 = context.getBean(Bean1.class);

		bean1.reset(1, 1, 1, 1, 1, 1, 1, 1);
		machine.start();

		assertThat(bean1.onTransitionFromS1ToS2Latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(bean1.onTransitionLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onTransitionFromS1ToS2Count, is(0));
		assertThat(bean1.onTransitionCount, is(1));

		bean1.reset(1, 1, 1, 1, 1, 1, 1, 1);
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(bean1.onTransitionFromS1ToS2Latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onTransitionLatch.await(1, TimeUnit.SECONDS), is(true));

		assertThat(bean1.onTransitionFromS1ToS2Count, is(1));
		assertThat(bean1.onTransitionCount, is(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOnStateChanged() throws Exception {
		context.register(BaseConfig.class, BeanConfig1.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		Bean1 bean1 = context.getBean(Bean1.class);

		bean1.reset(1, 1, 1, 1, 1, 1, 1, 1);
		machine.start();

		assertThat(bean1.onStateChangedFromS1ToS2Latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(bean1.onStateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onStateChangedFromS1ToS2Count, is(0));
		assertThat(bean1.onStateChangedCount, is(1));

		bean1.reset(1, 1, 1, 1, 1, 1, 1, 1);

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(bean1.onStateChangedFromS1ToS2Latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onStateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onStateChangedFromS1ToS2Count, is(1));
		assertThat(bean1.onStateChangedCount, is(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOnStateMachineStartStop() throws Exception {
		context.register(BaseConfig.class, BeanConfig1.class, Config1.class);
		context.refresh();
		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		Bean1 bean1 = context.getBean(Bean1.class);

		bean1.reset(1, 1, 1, 1, 1, 1, 1, 1);

		machine.start();
		assertThat(bean1.onStateMachineStartLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onStateMachineStartCount, is(1));
		assertThat(bean1.onStateMachineStopLatch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(bean1.onStateMachineStopCount, is(0));

		bean1.reset(1, 1, 1, 1, 1, 1, 1, 1);
		machine.stop();
		assertThat(bean1.onStateMachineStartLatch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(bean1.onStateMachineStartCount, is(0));
		assertThat(bean1.onStateMachineStopLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean1.onStateMachineStopCount, is(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOnExtendedStateChanged() throws Exception {
		context.register(BaseConfig.class, BeanConfig5.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		Bean5 bean5 = context.getBean(Bean5.class);
		machine.start();
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("V1", "V1val").build());

		assertThat(bean5.onExtendedStateChanged1Latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean5.onExtendedStateChanged1Count, is(1));

		assertThat(bean5.onExtendedStateChanged2Latch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(bean5.onExtendedStateChanged2Count, is(1));
		assertThat(bean5.onExtendedStateChanged2Value, is("V1val"));

		assertThat(bean5.onExtendedStateChangedKeyV2Latch.await(1, TimeUnit.SECONDS), is(false));
		assertThat(bean5.onExtendedStateChangedKeyV2Count, is(0));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations2() throws Exception {
		context.register(BaseConfig.class, BeanConfig2.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		machine.start();

		Bean2 bean2 = context.getBean(Bean2.class);

		// this event should cause 'method1' to get called
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).setHeader("foo", "jee").build());
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E2).build());

		assertThat(bean2.onMethod1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean2.headers, notNullValue());
		assertThat((String)bean2.headers.get("foo"), is("jee"));
		assertThat(bean2.extendedState, notNullValue());

		assertThat(bean2.onMethod2Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean2.variable, notNullValue());
		assertThat((String)bean2.variable, is("jee"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations3() throws Exception {
		context.register(BaseConfig.class, BeanConfig3.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		machine.start();

		Bean3 bean3 = context.getBean(Bean3.class);

		// this event should cause 'method1' to get called
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());

		assertThat(bean3.onStateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations4() throws Exception {
		context.register(BaseConfig.class, BeanConfig4.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		assertThat(context.containsBean("fooMachine"), is(true));
		Bean4 bean4 = context.getBean(Bean4.class);
		machine.start();

		assertThat(bean4.onStateEntryLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean4.onStateExitLatch.await(2, TimeUnit.SECONDS), is(false));
		assertThat(bean4.onStateEntryCount, is(1));
		assertThat(bean4.onStateExitCount, is(0));

		bean4.reset(1, 1);

		// this event should cause 'method1' to get called
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(bean4.onStateEntryLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean4.onStateExitLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean4.onStateEntryCount, is(1));
		assertThat(bean4.onStateExitCount, is(1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations5() throws Exception {
		context.register(BaseConfig.class, BeanConfig6.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		Bean6 bean6 = context.getBean(Bean6.class);
		machine.start();

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E4).build());
		assertThat(bean6.onEventNotAccepted1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean6.onEventNotAccepted2Latch.await(2, TimeUnit.SECONDS), is(false));
		assertThat(bean6.onEventNotAccepted3Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean6.onEventNotAccepted4Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean6.onEventNotAccepted4Message, notNullValue());
		assertThat(bean6.onEventNotAccepted4Message.getPayload(), is(TestEvents.E4));
		assertThat(bean6.onEventNotAccepted5Latch.await(2, TimeUnit.SECONDS), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations6() throws Exception {
		context.register(BaseConfig.class, BeanConfig7.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		Bean7 bean7 = context.getBean(Bean7.class);
		machine.start();

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E4).build());

		machine.setStateMachineError(new RuntimeException());

		assertThat(bean7.OnStateMachineError1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean7.OnStateMachineError2Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean7.OnStateMachineError2Exception, notNullValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations7() throws Exception {
		context.register(BaseConfig.class, BeanConfig8.class, Config1.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		Bean8 bean8 = context.getBean(Bean8.class);
		machine.start();

		machine.sendEvent(MessageBuilder.withPayload(TestEvents.EF).build());

		assertThat(bean8.OnTransition1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean8.OnTransition2Latch.await(2, TimeUnit.SECONDS), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations8() throws Exception {
		context.register(BaseConfig.class, BeanConfig9.class, Config2.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		Bean9 bean9 = context.getBean(Bean9.class);
		machine.start();

		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21));
		assertThat(bean9.OnStateEntry1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean9.OnTransition1Latch.await(2, TimeUnit.SECONDS), is(true));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMethodAnnotations9() throws Exception {
		context.register(BaseConfig.class, BeanConfig10.class, Config3.class);
		context.refresh();

		ObjectStateMachine<TestStates,TestEvents> machine =
				context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		Bean10 bean10 = context.getBean(Bean10.class);
		machine.start();

		machine.sendEvent(TestEvents.E1);
		machine.sendEvent(TestEvents.E2);
		machine.sendEvent(TestEvents.E3);
		machine.sendEvent(TestEvents.E4);

		assertThat(machine.getState().getIds(), contains(TestStates.S2, TestStates.S21));
		assertThat(bean10.OnStateEntry1Latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(bean10.OnTransition1Latch.await(2, TimeUnit.SECONDS), is(true));
	}

	@WithStateMachine
	static class Bean1 {
		CountDownLatch onTransitionFromS1ToS2Latch = new CountDownLatch(1);
		CountDownLatch onTransitionLatch = new CountDownLatch(1);
		CountDownLatch onStateChangedFromS1ToS2Latch = new CountDownLatch(1);
		CountDownLatch onStateChangedLatch = new CountDownLatch(1);
		CountDownLatch onTransitionEndLatch = new CountDownLatch(1);
		CountDownLatch onTransitionStartLatch = new CountDownLatch(1);
		CountDownLatch onStateMachineStartLatch = new CountDownLatch(1);
		CountDownLatch onStateMachineStopLatch = new CountDownLatch(1);
		int onTransitionFromS1ToS2Count = 0;
		int onTransitionCount = 0;
		int onStateChangedFromS1ToS2Count = 0;
		int onStateChangedCount = 0;
		int onTransitionEndCount = 0;
		int onTransitionStartCount = 0;
		int onStateMachineStartCount = 0;
		int onStateMachineStopCount = 0;

		@OnTransition(source = "S1", target = "S2")
		public void onTransitionFromS1ToS2() {
			onTransitionFromS1ToS2Count++;
			onTransitionFromS1ToS2Latch.countDown();
		}

		@OnTransition
		public void onTransition() {
			onTransitionCount++;
			onTransitionLatch.countDown();
		}

		@OnTransitionEnd
		public void onTransitionEnd() {
			onTransitionEndCount++;
			onTransitionEndLatch.countDown();
		}

		@OnTransitionStart
		public void onTransitionStart() {
			onTransitionStartCount++;
			onTransitionStartLatch.countDown();
		}

		@OnStateChanged(source = "S1", target = "S2")
		public void onStateChangedFromS1ToS2() {
			onStateChangedFromS1ToS2Count++;
			onStateChangedFromS1ToS2Latch.countDown();
		}

		@OnStateChanged
		public void onStateChanged() {
			onStateChangedCount++;
			onStateChangedLatch.countDown();
		}

		@OnStateMachineStart
		public void onStateMachineStart() {
			onStateMachineStartLatch.countDown();
			onStateMachineStartCount++;
		}

		@OnStateMachineStart
		public void onStateMachineStartWithParam(StateMachine<TestStates,TestEvents> machine) {
		}

		@OnStateMachineStop
		public void onStateMachineStop() {
			onStateMachineStopLatch.countDown();
			onStateMachineStopCount++;
		}

		@OnStateMachineStop
		public void onStateMachineStopWithParam(StateMachine<TestStates,TestEvents> machine) {
		}

		public void reset(int a1, int a2, int a3, int a4, int a5, int a6, int a7, int a8) {
			onTransitionFromS1ToS2Latch = new CountDownLatch(a1);
			onTransitionLatch = new CountDownLatch(a2);
			onStateChangedFromS1ToS2Latch = new CountDownLatch(a3);
			onStateChangedLatch = new CountDownLatch(a4);
			onTransitionEndLatch = new CountDownLatch(a5);
			onTransitionStartLatch = new CountDownLatch(a6);
			onStateMachineStartLatch = new CountDownLatch(a7);
			onStateMachineStopLatch = new CountDownLatch(a8);
			onTransitionFromS1ToS2Count = 0;
			onTransitionCount = 0;
			onStateChangedFromS1ToS2Count = 0;
			onStateChangedCount = 0;
			onTransitionEndCount = 0;
			onTransitionStartCount = 0;
			onStateMachineStartCount = 0;
			onStateMachineStopCount = 0;
		}

	}

	@WithStateMachine
	static class Bean2 {

		CountDownLatch onMethod1Latch = new CountDownLatch(1);
		CountDownLatch onMethod2Latch = new CountDownLatch(1);
		Map<String, Object> headers;
		ExtendedState extendedState;
		Object variable;

		@OnTransition(source = "S1", target = "S2")
		public void method1(@EventHeaders Map<String, Object> headers, ExtendedState extendedState) {
			this.headers = headers;
			extendedState.getVariables().put("foo", "jee");
			this.extendedState = extendedState;
			onMethod1Latch.countDown();
		}

		@OnTransition(source = "S2", target = "S3")
		public void method2(@EventHeaders Map<String, Object> headers, ExtendedState extendedState) {
			variable = extendedState.getVariables().get("foo");
			onMethod2Latch.countDown();
		}

	}

	@WithStateMachine
	static class Bean3 {

		CountDownLatch onStateChangedLatch = new CountDownLatch(1);

		@OnStateChanged
		public void onStateChanged() {
			onStateChangedLatch.countDown();
		}

	}

	@WithStateMachine
	static class Bean4 {

		CountDownLatch onStateEntryLatch = new CountDownLatch(1);
		CountDownLatch onStateExitLatch = new CountDownLatch(1);
		int onStateEntryCount = 0;
		int onStateExitCount = 0;

		@OnStateEntry
		public void onStateEntry() {
			onStateEntryCount++;
			onStateEntryLatch.countDown();
		}

		@OnStateExit
		public void onStateExit() {
			onStateExitCount++;
			onStateExitLatch.countDown();
		}

		public void reset(int a1, int a2) {
			onStateEntryCount = 0;
			onStateExitCount = 0;
			onStateEntryLatch = new CountDownLatch(a1);
			onStateExitLatch = new CountDownLatch(a2);
		}

	}

	@WithStateMachine
	static class Bean5 {

		CountDownLatch onExtendedStateChanged1Latch = new CountDownLatch(1);
		CountDownLatch onExtendedStateChanged2Latch = new CountDownLatch(1);
		CountDownLatch onExtendedStateChangedKeyV2Latch = new CountDownLatch(1);
		int onExtendedStateChanged1Count = 0;
		int onExtendedStateChanged2Count = 0;
		Object onExtendedStateChanged2Value = null;
		int onExtendedStateChangedKeyV2Count = 0;

		@OnExtendedStateChanged
		public void onExtendedStateChanged1() {
			onExtendedStateChanged1Count++;
			onExtendedStateChanged1Latch.countDown();
		}

		@OnExtendedStateChanged
		public void onExtendedStateChanged2(@ExtendedStateVariable("V1") Object value) {
			onExtendedStateChanged2Value = value;
			onExtendedStateChanged2Count++;
			onExtendedStateChanged2Latch.countDown();
		}

		@OnExtendedStateChanged(key = "V2")
		public void onExtendedStateChangedKeyV2() {
			onExtendedStateChangedKeyV2Count++;
			onExtendedStateChangedKeyV2Latch.countDown();
		}

		public void reset(int a1, int a2, int a3) {
			onExtendedStateChanged1Latch = new CountDownLatch(a1);
			onExtendedStateChanged2Latch = new CountDownLatch(a2);
			onExtendedStateChangedKeyV2Latch = new CountDownLatch(a3);
			onExtendedStateChanged1Count = 0;
			onExtendedStateChanged2Count = 0;
			onExtendedStateChanged2Value = null;
			onExtendedStateChangedKeyV2Count = 0;
		}

	}

	@WithStateMachine
	static class Bean6 {
		CountDownLatch onEventNotAccepted1Latch = new CountDownLatch(1);
		CountDownLatch onEventNotAccepted2Latch = new CountDownLatch(1);
		CountDownLatch onEventNotAccepted3Latch = new CountDownLatch(1);
		CountDownLatch onEventNotAccepted4Latch = new CountDownLatch(1);
		CountDownLatch onEventNotAccepted5Latch = new CountDownLatch(1);
		Message<TestEvents> onEventNotAccepted4Message;

		@OnEventNotAccepted
		public void onEventNotAccepted1() {
			onEventNotAccepted1Latch.countDown();
		}

		@OnEventNotAccepted(event = "E1")
		public void onEventNotAccepted2() {
			onEventNotAccepted2Latch.countDown();
		}

		@OnEventNotAccepted(event = "E4")
		public void onEventNotAccepted3() {
			onEventNotAccepted3Latch.countDown();
		}

		@OnEventNotAccepted()
		public void onEventNotAccepted4(Message<TestEvents> message) {
			onEventNotAccepted4Message = message;
			onEventNotAccepted4Latch.countDown();
		}

		@OnEventNotAccepted(event = {"E1", "E4"})
		public void onEventNotAccepted5() {
			onEventNotAccepted5Latch.countDown();
		}

		void reset() {
			onEventNotAccepted1Latch = new CountDownLatch(1);
			onEventNotAccepted2Latch = new CountDownLatch(1);
			onEventNotAccepted3Latch = new CountDownLatch(1);
			onEventNotAccepted4Latch = new CountDownLatch(1);
			onEventNotAccepted4Message = null;
		}
	}

	@WithStateMachine
	static class Bean7 {
		CountDownLatch OnStateMachineError1Latch = new CountDownLatch(1);
		CountDownLatch OnStateMachineError2Latch = new CountDownLatch(1);
		Exception OnStateMachineError2Exception;

		@OnStateMachineError
		public void OnStateMachineError1() {
			OnStateMachineError1Latch.countDown();
		}

		@OnStateMachineError
		public void OnStateMachineError2(Exception e) {
			OnStateMachineError2Exception = e;
			OnStateMachineError2Latch.countDown();
		}

	}

	@WithStateMachine
	static class Bean8 {
		CountDownLatch OnTransition1Latch = new CountDownLatch(2);
		CountDownLatch OnTransition2Latch = new CountDownLatch(1);

		@OnTransition
		public void OnTransition1() {
			OnTransition1Latch.countDown();
		}

		@OnTransition(target = "SF")
		public void OnTransition2() {
			OnTransition2Latch.countDown();
		}
	}

	@WithStateMachine
	static class Bean9 {
		CountDownLatch OnTransition1Latch = new CountDownLatch(1);
		CountDownLatch OnStateEntry1Latch = new CountDownLatch(2);

		@OnTransition(target = "S21")
		public void OnTransition1() {
			OnTransition1Latch.countDown();
		}

		@OnStateEntry(target = "S21")
		public void OnStateEntry1() {
			OnStateEntry1Latch.countDown();
		}
	}

	@WithStateMachine
	static class Bean10 {
		CountDownLatch OnTransition1Latch = new CountDownLatch(1);
		CountDownLatch OnStateEntry1Latch = new CountDownLatch(2);

		@OnTransition(target = "S21")
		public void OnTransition1() {
			OnTransition1Latch.countDown();
		}

		@OnStateEntry(target = "S21")
		public void OnStateEntry1() {
			OnStateEntry1Latch.countDown();
		}
	}

	@Configuration
	static class BeanConfig1 {

		@Bean
		public Bean1 bean1() {
			return new Bean1();
		}

	}

	@Configuration
	static class BeanConfig2 {

		@Bean
		public Bean2 bean2() {
			return new Bean2();
		}

	}

	@Configuration
	static class BeanConfig3 {

		@Bean
		public Bean3 bean3() {
			return new Bean3();
		}

	}

	@Configuration
	static class BeanConfig4 {

		@Bean
		public Bean4 bean4() {
			return new Bean4();
		}

	}

	@Configuration
	static class BeanConfig5 {

		@Bean
		public Bean5 bean5() {
			return new Bean5();
		}

	}

	@Configuration
	static class BeanConfig6 {

		@Bean
		public Bean6 bean6() {
			return new Bean6();
		}

	}

	@Configuration
	static class BeanConfig7 {

		@Bean
		public Bean7 bean7() {
			return new Bean7();
		}

	}

	@Configuration
	static class BeanConfig8 {

		@Bean
		public Bean8 bean8() {
			return new Bean8();
		}

	}

	@Configuration
	static class BeanConfig9 {

		@Bean
		public Bean9 bean9() {
			return new Bean9();
		}

	}

	@Configuration
	static class BeanConfig10 {

		@Bean
		public Bean10 bean10() {
			return new Bean10();
		}

	}

	@Configuration
	@EnableStateMachine(name = {StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, "fooMachine"})
	static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.end(TestStates.SF)
					.states(EnumSet.allOf(TestStates.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.guard(testGuard())
					.action(testAction())
					.action(extendedStateAction())
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
					.source(TestStates.S1)
					.target(TestStates.SF)
					.event(TestEvents.EF);
		}

		@Bean
		public TestGuard testGuard() {
			return new TestGuard();
		}

		@Bean
		public TestAction testAction() {
			return new TestAction();
		}

		@Bean
		public Action<TestStates, TestEvents> extendedStateAction() {
			return new Action<TestStates, TestEvents>() {

				@Override
				public void execute(StateContext<TestStates, TestEvents> context) {
					String e1 = context.getMessageHeaders().get("V1", String.class);
					if (e1 != null) {
						context.getExtendedState().getVariables().put("V1", e1);
					}
				}
			};
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config2 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.state(TestStates.S21)
						.history(TestStates.SH, History.SHALLOW);

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
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.E4);
		}

	}

	@Configuration
	@EnableStateMachine
	static class Config3 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.state(TestStates.S1)
					.state(TestStates.S2)
					.and()
					.withStates()
						.parent(TestStates.S2)
						.initial(TestStates.S20)
						.state(TestStates.S20)
						.end(TestStates.S21)
						.history(TestStates.SH, History.SHALLOW);

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
					.source(TestStates.S20)
					.target(TestStates.S21)
					.event(TestEvents.E2)
					.and()
				.withExternal()
					.source(TestStates.S2)
					.target(TestStates.S1)
					.event(TestEvents.E3)
					.and()
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.SH)
					.event(TestEvents.E4);
		}

	}
}
