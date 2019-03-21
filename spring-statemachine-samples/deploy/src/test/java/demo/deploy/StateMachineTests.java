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
package demo.deploy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
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
	private StateMachine<String, String> stateMachine;

	@Test
	public void testDeployInstalledInstallOkInstallOk() throws Exception {
		Message<String> deploy = MessageBuilder.withPayload("DEPLOY")
				.setHeader("isInstalled", true)
				.setHeader("installedOk", true)
				.setHeader("hasError", true).build();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent(deploy)
						.expectStateChanged(6)
						.expectStateEntered("DEPLOY", "PREPAREDEPLOY", "INSTALL", "START", "ERROR", "READY")
						.expectStates("READY").and()
					.build();
		plan.test();
	}

	@Test
	public void testDeployInstalledInstallOkInstallError() throws Exception {
		Message<String> deploy = MessageBuilder.withPayload("DEPLOY")
				.setHeader("isInstalled", true)
				.setHeader("hasError", true).build();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent(deploy)
						.expectStateChanged(5)
						.expectStateEntered("DEPLOY", "PREPAREDEPLOY", "INSTALL", "ERROR", "READY")
						.expectStates("READY").and()
					.build();
		plan.test();
	}

	@Test
	public void testDeployInstalledInstallFail() throws Exception {
		Message<String> deploy = MessageBuilder.withPayload("DEPLOY")
				.setHeader("isInstalled", true).build();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent(deploy)
						.expectStateChanged(4)
						.expectStateEntered("DEPLOY", "PREPAREDEPLOY", "INSTALL", "READY")
						.expectStates("READY").and()
					.build();
		plan.test();
	}

	@Test
	public void testUndeploy() throws Exception {
		Message<String> deploy = MessageBuilder.withPayload("UNDEPLOY").build();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent(deploy)
						.expectStateChanged(3)
						.expectStateEntered("UNDEPLOY", "PREPAREUNDEPLOY", "READY")
						.expectStateExited("READY", "PREPAREUNDEPLOY", "UNDEPLOY")
						.expectStates("READY").and()
					.build();
		plan.test();
	}

	@Test
	public void testUndeployRunningOk() throws Exception {
		Message<String> deploy = MessageBuilder.withPayload("UNDEPLOY")
				.setHeader("isRunning", true)
				.setHeader("hasError", true).build();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent(deploy)
						.expectStateChanged(5)
						.expectStateEntered("UNDEPLOY", "PREPAREUNDEPLOY", "STOP", "ERROR", "READY")
						.expectStates("READY").and()
					.build();
		plan.test();
	}

	@Test
	public void testUndeployNotRunningOk() throws Exception {
		Message<String> undeploy = MessageBuilder.withPayload("UNDEPLOY").build();
		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectState("READY").and()
					.step().sendEvent(undeploy)
						.expectStateChanged(3)
						.expectStateEntered("UNDEPLOY", "PREPAREUNDEPLOY", "READY")
						.expectStates("READY").and()
					.build();
		plan.test();
	}
}
