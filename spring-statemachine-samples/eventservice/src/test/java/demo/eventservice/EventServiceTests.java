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
package demo.eventservice;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import demo.eventservice.EventServiceTests.Config;
import demo.eventservice.StateMachineConfig.Events;
import demo.eventservice.StateMachineConfig.States;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class, Config.class })
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class EventServiceTests {

	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@Test
	public void testHome() throws Exception {
		mvc.
			perform(get("/state")).
			andExpect(status().isOk());
	}

	@Test
	public void testSendEvent() throws Exception {
		mvc.
			perform(get("/state").param("user", "joe").param("id", "VIEW_I")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("States: [ITEMS]")));
	}

	@Test
	public void testAdd() throws Exception {
		mvc.
			perform(get("/state").param("user", "joe").param("id", "VIEW_I")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("States: [ITEMS]")));
		mvc.
			perform(get("/state").param("user", "joe").param("id", "ADD")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("States: [ITEMS]"))).
			andExpect(content().string(containsString("Extended State: {COUNT=1, ITEMS=1}")));
	}

	@Test
	public void testFeed() throws Exception {
		mvc.
			perform(post("/feed").content("{\"user\":\"joe\",\"id\":\"VIEW_I\"}").contentType(MediaType.APPLICATION_JSON)).
			andExpect(status().isOk());
		mvc.
			perform(post("/feed").content("{\"user\":\"joe\",\"id\":\"ADD\"}").contentType(MediaType.APPLICATION_JSON)).
			andExpect(status().isOk());
		mvc.
			perform(get("/state").param("user", "joe")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("States: [ITEMS]"))).
			andExpect(content().string(containsString("Extended State: {COUNT=1, ITEMS=1}")));
	}

	@Before
	public void setup() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Configuration
	static class Config {

		@Bean
		public StateMachinePersist<States, Events, String> stateMachinePersist() {
			// use stateMachinePersist without redis to ease testing
			return new InMemoryStateMachinePersist();
		}
	}

	static class InMemoryStateMachinePersist implements StateMachinePersist<States, Events, String> {

		private final HashMap<String, StateMachineContext<States, Events>> contexts = new HashMap<>();

		@Override
		public void write(StateMachineContext<States, Events> context, String contextObj) throws Exception {
			contexts.put(contextObj, context);
		}

		@Override
		public StateMachineContext<States, Events> read(String contextObj) throws Exception {
			return contexts.get(contextObj);
		}
	}
}
