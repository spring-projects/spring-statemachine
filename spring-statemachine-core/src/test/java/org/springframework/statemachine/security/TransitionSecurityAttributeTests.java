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
package org.springframework.statemachine.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.security.SecurityRule.ComparisonType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class TransitionSecurityAttributeTests extends AbstractSecurityTests {

	@Test
	@WithMockUser
	public void testAnonymousRole() throws Exception {
		TestListener listener = new TestListener();
		StateMachine<States,Events> machine = buildMachine(listener, "ROLE_ANONYMOUS", ComparisonType.ANY, null);
		assertTransitionDenied(machine, listener);
	}

	@Test
	@WithMockUser
	public void testTransitionSource() throws Exception {
		TestListener listener = new TestListener();
		StateMachine<States,Events> machine = buildMachine(listener, "TRANSITION_SOURCE_S0", ComparisonType.ANY, null);
		assertTransitionAllowed(machine, listener);
	}

	@Test
	@WithMockUser
	public void testTransitionTarget() throws Exception {
		TestListener listener = new TestListener();
		StateMachine<States,Events> machine = buildMachine(listener, "TRANSITION_TARGET_S1", ComparisonType.ANY, null);
		assertTransitionAllowed(machine, listener);
	}

	@Test
	@WithMockUser
	public void testTransitionSourceAndTargetAll() throws Exception {
		TestListener listener = new TestListener();
		StateMachine<States,Events> machine = buildMachine(listener, "TRANSITION_SOURCE_S0,TRANSITION_TARGET_S1", ComparisonType.ALL, null);
		assertTransitionAllowed(machine, listener);
	}

	@Test
	@WithMockUser
	public void testTransitionSourceAndTargetAny() throws Exception {
		TestListener listener = new TestListener();
		StateMachine<States,Events> machine = buildMachine(listener, "TRANSITION_SOURCE_S0,TRANSITION_TARGET_S1", ComparisonType.ANY, null);
		assertTransitionAllowed(machine, listener);
	}

	@Configuration
	public static class Config {
	}

}
