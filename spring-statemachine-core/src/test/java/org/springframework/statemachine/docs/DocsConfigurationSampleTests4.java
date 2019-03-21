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
package org.springframework.statemachine.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.EventHeaders;
import org.springframework.statemachine.annotation.OnEventNotAccepted;
import org.springframework.statemachine.annotation.OnExtendedStateChanged;
import org.springframework.statemachine.annotation.OnStateChanged;
import org.springframework.statemachine.annotation.OnStateEntry;
import org.springframework.statemachine.annotation.OnStateExit;
import org.springframework.statemachine.annotation.OnStateMachineError;
import org.springframework.statemachine.annotation.OnStateMachineStart;
import org.springframework.statemachine.annotation.OnStateMachineStop;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;

public class DocsConfigurationSampleTests4 extends AbstractStateMachineTests {

// tag::snippetA[]
	@WithStateMachine
	public class Bean1 {

		@OnTransition
		public void anyTransition() {
		}
	}
// end::snippetA[]

// tag::snippetAA[]
	@WithStateMachine(name = "myMachineBeanName")
	public class Bean2 {

		@OnTransition
		public void anyTransition() {
		}
	}
// end::snippetAA[]

// tag::snippetAAAA[]
	@WithStateMachine(id = "myMachineId")
	public class Bean16 {

		@OnTransition
		public void anyTransition() {
		}
	}
// end::snippetAAAA[]

// tag::snippetAAA[]
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@WithStateMachine(name = "myMachineBeanName")
	public @interface WithMyBean {
	}
// end::snippetAAA[]


// tag::snippetAAAAA[]
	public static StateMachine<String, String> buildMachine(BeanFactory beanFactory) throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration()
			.withConfiguration()
				.machineId("myMachineId")
				.beanFactory(beanFactory);

		builder.configureStates()
			.withStates()
				.initial("S1")
				.state("S2");

		builder.configureTransitions()
			.withExternal()
				.source("S1")
				.target("S2")
				.event("E1");

		return builder.build();
	}

	@WithStateMachine(id = "myMachineId")
	static class Bean17 {

		@OnStateChanged
		public void onStateChanged() {
		}
	}
// end::snippetAAAAA[]

// tag::snippetB[]
	@WithStateMachine
	public class Bean3 {

		@OnTransition
		public void anyTransition(StateContext<String, String> stateContext) {
		}
	}
// end::snippetB[]

// tag::snippetBB[]
	@WithStateMachine
	public class Bean4 {

		@OnTransition
		public void anyTransition(
				@EventHeaders Map<String, Object> headers,
				ExtendedState extendedState,
				StateMachine<String, String> stateMachine,
				Message<String> message,
				Exception e) {
		}
	}
// end::snippetBB[]

// tag::snippetC[]
	@WithStateMachine
	public class Bean5 {

		@OnTransition(source = "S1", target = "S2")
		public void fromS1ToS2() {
		}

		@OnTransition
		public void anyTransition() {
		}
	}
// end::snippetC[]

// tag::snippetD[]
	@WithStateMachine
	public class Bean6 {

		@StatesOnTransition(source = States.S1, target = States.S2)
		public void fromS1ToS2(@EventHeaders Map<String, Object> headers, ExtendedState extendedState) {
		}
	}
// end::snippetD[]

// tag::snippetE[]
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@OnTransition
	public @interface StatesOnTransition {

		States[] source() default {};

		States[] target() default {};
	}
// end::snippetE[]

// tag::snippetF[]
	@WithStateMachine
	public class Bean7 {

		@StatesOnTransition(source = States.S1, target = States.S2)
		public void fromS1ToS2() {
		}
	}
// end::snippetF[]

// tag::snippetG[]
	@WithStateMachine
	public class Bean8 {

		@OnStateChanged
		public void anyStateChange() {
		}
	}
// end::snippetG[]

// tag::snippetGG[]
	@WithStateMachine
	public class Bean9 {

		@OnStateChanged(source = "S1", target = "S2")
		public void stateChangeFromS1toS2() {
		}
	}
// end::snippetGG[]

// tag::snippetGGG[]
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@OnStateChanged
	public @interface StatesOnStates {

		States[] source() default {};

		States[] target() default {};
	}
// end::snippetGGG[]

// tag::snippetGGGG[]
	@WithStateMachine
	public class Bean10 {

		@StatesOnStates(source = States.S1, target = States.S2)
		public void fromS1ToS2() {
		}
	}
// end::snippetGGGG[]

// tag::snippetGGGGG[]
	@WithStateMachine
	public class Bean11 {

		@OnStateEntry
		public void anyStateEntry() {
		}

		@OnStateExit
		public void anyStateExit() {
		}
	}
// end::snippetGGGGG[]

// tag::snippetH[]
	@WithStateMachine
	public class Bean12 {

		@OnEventNotAccepted
		public void anyEventNotAccepted() {
		}

		@OnEventNotAccepted(event = "E1")
		public void e1EventNotAccepted() {
		}
	}
// end::snippetH[]

// tag::snippetI[]
	@WithStateMachine
	public class Bean13 {

		@OnStateMachineStart
		public void onStateMachineStart() {
		}

		@OnStateMachineStop
		public void onStateMachineStop() {
		}
	}
// end::snippetI[]

// tag::snippetII[]
	@WithStateMachine
	public class Bean14 {

		@OnStateMachineError
		public void onStateMachineError() {
		}
	}
// end::snippetII[]

// tag::snippetJ[]
	@WithStateMachine
	public class Bean15 {

		@OnExtendedStateChanged
		public void anyStateChange() {
		}

		@OnExtendedStateChanged(key = "key1")
		public void key1Changed() {
		}
	}
// end::snippetJ[]

}
