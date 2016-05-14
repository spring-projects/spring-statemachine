/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.ordershipping;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { StateMachineConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class StateMachineTests {

	@Autowired
	private StateMachineFactory<String, String> stateMachineFactory;

	private StateMachine<String, String> stateMachine;

	@Before
	public void setup() {
		stateMachine = stateMachineFactory.getStateMachine();
	}

	@Test
	public void testInitial() throws Exception {
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step()
						.expectState("WAIT_NEW_ORDER")
						.and()
					.build();
		plan.test();
	}

	@Test
	public void testNoCustomerOrOrder() throws Exception {
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step()
						.expectState("WAIT_NEW_ORDER")
						.and()
					.step()
						.sendEvent("PLACE_ORDER")
						.expectStates("CUSTOMER_ERROR")
						.and()
					.build();
		plan.test();
	}

	@Test
	public void testPlaceOrder() throws Exception {
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step()
						.expectState("WAIT_NEW_ORDER")
						.and()
					.step()
						.sendEvent(MessageBuilder.withPayload("PLACE_ORDER")
								.setHeader("customer", "customer1")
								.setHeader("order", "order1").build())
						.expectStates("HANDLE_ORDER", "WAIT_PAYMENT", "WAIT_PRODUCT")
						.expectStateChanged(8)
						.and()
					.step()
						.sendEvent(MessageBuilder.withPayload("RECEIVE_PAYMENT")
								.setHeader("payment", "1000").build())
						.expectStates("ORDER_SHIPPED")
						.and()
					.build();
		plan.test();
	}

	@Test
	public void testNoPayment() throws Exception {
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step()
						.expectState("WAIT_NEW_ORDER")
						.and()
					.step()
						.sendEvent(MessageBuilder.withPayload("PLACE_ORDER")
								.setHeader("customer", "customer1")
								.setHeader("order", "order1").build())
						.expectStates("HANDLE_ORDER", "WAIT_PAYMENT", "WAIT_PRODUCT")
						.expectStateChanged(8)
						.and()
					.step()
						.sendEvent(MessageBuilder.withPayload("RECEIVE_PAYMENT").build())
						.expectStates("HANDLE_ORDER", "WAIT_PAYMENT", "WAIT_PRODUCT")
						.and()
					.build();
		plan.test();
	}

	@Test
	public void testReminder() throws Exception {
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step()
						.expectState("WAIT_NEW_ORDER")
						.and()
					.step()
						.sendEvent(MessageBuilder.withPayload("PLACE_ORDER")
								.setHeader("customer", "customer1")
								.setHeader("order", "order1").build())
						.expectStates("HANDLE_ORDER", "WAIT_PAYMENT", "WAIT_PRODUCT")
						.expectStateChanged(8)
						.and()
					.step()
						.expectStateEntered("SEND_REMINDER")
						.expectStateChanged(1)
						.and()
					.build();
		plan.test();
	}

}
