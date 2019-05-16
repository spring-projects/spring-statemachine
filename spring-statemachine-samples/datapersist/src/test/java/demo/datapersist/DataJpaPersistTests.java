/*
 * Copyright 2017 the original author or authors.
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
package demo.datapersist;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import demo.datapersist.Application;
import demo.datapersist.StateMachineController;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class })
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DataJpaPersistTests {

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
	public void testSendEventE1() throws Exception {
		mvc.
			perform(get("/state").param("events", "E1")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("Exit S1")));
	}

	@Test
	public void testSendEventsE1E2() throws Exception {
		mvc.
			perform(get("/state").param("events", "E1").param("events", "E2")).
			andExpect(status().isOk()).
			andExpect(content().string(allOf(
						containsString("Exit S1"),
						containsString("Exit S2"))));
	}

	@Test
	public void testSendEventE4() throws Exception {
		mvc.
			perform(get("/state").param("events", "E1").param("machine", StateMachineController.MACHINE_ID_2)).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("Exit S1")));
	}

	@Test
	public void testChangeMachineRestores() throws Exception {
		mvc.
			perform(get("/state").param("events", "E1")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("Exit S1")));
		mvc.
			perform(get("/state").param("events", "E1").param("machine", StateMachineController.MACHINE_ID_2)).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("Exit S1")));
		mvc.
			perform(get("/state").param("events", "E2").param("machine", StateMachineController.MACHINE_ID_1)).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("Exit S2")));
		mvc.
			perform(get("/state").param("events", "E2").param("machine", StateMachineController.MACHINE_ID_2)).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("Exit S2")));
	}

	@Before
	public void setup() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}
}
