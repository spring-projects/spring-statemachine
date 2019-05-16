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
package org.springframework.statemachine.guard;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.support.DefaultStateContext;

/**
 * Tests for using spel expressions in guards.
 *
 * @author Janne Valkealahti
 *
 */
public class SpelExpressionGuardTests extends AbstractStateMachineTests {

	@Test
	public void testSimpleSpel() {
		SpelExpressionParser parser = new SpelExpressionParser(
				new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
		Expression expression = parser.parseExpression("messageHeaders.get('foo')=='bar'");
		SpelExpressionGuard<TestStates, TestEvents> guard = new SpelExpressionGuard<TestStates, TestEvents>(expression);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("foo", "bar");
		MessageHeaders headers = new MessageHeaders(map);
		DefaultStateContext<TestStates, TestEvents> stateContext = new DefaultStateContext<TestStates, TestEvents>(null, null, headers,
				null, null, null, null, null, null);
		assertThat(guard.evaluate(stateContext), is(true));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void testGuardDenyStateChange() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BaseConfig.class, Config1.class);
		assertTrue(ctx.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
		ObjectStateMachine<TestStates,TestEvents> machine =
				ctx.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		machine.start();

		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		machine.sendEvent(MessageBuilder.withPayload(TestEvents.E1).build());
		assertThat(machine.getState().getIds(), contains(TestStates.S1));
		ctx.close();
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

		@Override
		public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
			states
				.withStates()
					.initial(TestStates.S1)
					.states(EnumSet.allOf(TestStates.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
			transitions
				.withExternal()
					.source(TestStates.S1)
					.target(TestStates.S2)
					.event(TestEvents.E1)
					.guardExpression("false");
		}

	}

}
