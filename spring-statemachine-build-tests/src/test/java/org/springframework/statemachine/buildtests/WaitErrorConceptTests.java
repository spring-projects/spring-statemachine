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
package org.springframework.statemachine.buildtests;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

@SuppressWarnings("unchecked")
public class WaitErrorConceptTests extends AbstractBuildTests {

	@Test
	public void testWaitInternalWaitLoop() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent("DO").expectStateChanged(11).expectStates("READY").and()
					.build();
		plan.test();
	}

	@Test
	public void testStep2SetsError() throws Exception {
		context.register(Config1.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		Message<String> message = MessageBuilder.withPayload("DO").setHeader("step2error", true).build();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent(message).expectStateChanged(5).expectStates("READY").and()
					.build();
		plan.test();
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:org/springframework/statemachine/buildtests/wait-error-1.uml");
		}

		@Bean
		public Action<String, String> waitSetKey() {
			return (context) -> {
				Long time = context.getExtendedState().get("time", Long.class);
				if ((time + 5000) < System.currentTimeMillis()) {
					context.getExtendedState().getVariables().put("key", "value");
				}
			};
		}

		@Bean
		public Action<String, String> step1Entry() {
			return (context) -> {
				context.getExtendedState().getVariables().put("time", System.currentTimeMillis());
			};
		}

		@Bean
		public Action<String, String> step2Entry() {
			return (context) -> {
				if (context.getMessageHeaders().containsKey("step2error")) {
					context.getExtendedState().getVariables().put("error", "step2error");
				}
			};
		}

		@Bean
		public Guard<String, String> hasError() {
			return (context) -> {
				return context.getExtendedState().getVariables().containsKey("error");
			};
		}

		@Bean
		public Guard<String, String> hasKey() {
			return (context) -> {
				return context.getExtendedState().getVariables().containsKey("key");
			};
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
