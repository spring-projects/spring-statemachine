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
package org.springframework.statemachine.config;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.stereotype.Controller;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration
@DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
public class SessionScopedManualTests {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setUp() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void testScopedMachines() throws Exception {
		MockHttpSession session1 = new MockHttpSession();
		MockHttpSession session2 = new MockHttpSession();

		mvc.
			perform(get("/state").session(session1)).
			andExpect(status().isOk()).
			andExpect(content().string(is("S1")));
		mvc.
			perform(get("/state").session(session2)).
			andExpect(status().isOk()).
			andExpect(content().string(is("S1")));

		mvc.
			perform(post("/state").session(session1).param("event", "E1")).
			andExpect(status().isAccepted());
		mvc.
			perform(post("/state").session(session2).param("event", "E1")).
			andExpect(status().isAccepted());

		mvc.
			perform(get("/state").session(session1)).
			andExpect(status().isOk()).
			andExpect(content().string(is("S2")));
		mvc.
			perform(get("/state").session(session2)).
			andExpect(status().isOk()).
			andExpect(content().string(is("S2")));
	}

	@Test
	public void testDestruction() throws Exception  {
		MockHttpSession session1 = new MockHttpSession();
		mvc.
			perform(get("/state").session(session1)).
			andExpect(status().isOk()).
			andExpect(content().string(is("S1")));
		Object machine = session1.getAttribute("scopedTarget.stateMachine");
		assertThat(machine, notNullValue());
		assertThat(TestUtils.readField("running", machine), is(true));
		session1.invalidate();
		assertThat(TestUtils.readField("running", machine), is(false));
	}

	@Test
	public void testAutoStart() throws Exception {
		MockHttpSession session1 = new MockHttpSession();
		mvc.
			perform(get("/ping").session(session1)).
			andExpect(status().isOk());
		Object machine = session1.getAttribute("scopedTarget.stateMachine");
		assertThat(machine, notNullValue());
		assertThat(TestUtils.callMethod("isRunning", machine), is(true));
	}

	@Configuration
	static class Config {

		@Bean
		@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(true)
					.taskExecutor(new SyncTaskExecutor());
			builder.configureStates()
				.withStates()
					.initial("S1").state("S2");
			builder.configureTransitions()
				.withExternal()
					.source("S1").target("S2").event("E1")
					.and()
				.withExternal()
					.source("S2").target("S1").event("E2");
			StateMachine<String, String> stateMachine = builder.build();
			return stateMachine;
		}

		@Bean
		TestController testController() {
			return new TestController();
		}
	}

	@Controller
	static class TestController {

		@Autowired
		StateMachine<String, String> stateMachine;

		@RequestMapping(path="/ping", method=RequestMethod.GET)
		public HttpEntity<Void> dummyPing() {
			// dummy ping to instantiate session and then create machine
			stateMachine.getState();
			return new ResponseEntity<Void>(HttpStatus.OK);
		}

		@RequestMapping(path="/state", method=RequestMethod.POST)
		public HttpEntity<Void> setState(@RequestParam("event") String event) {
			stateMachine.sendEvent(event);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		}

		@RequestMapping(path="/state", method=RequestMethod.GET)
		@ResponseBody
		public String getState() {
			return stateMachine.getState().getId();
		}
	}
}
