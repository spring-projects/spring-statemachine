/*
 * Copyright 2019 the original author or authors.
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
package demo.datajpamultipersist;

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

import demo.datajpamultipersist.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { Application.class })
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DataJpaMultiPersistTests {

	private MockMvc mvc;

	@Autowired
	private WebApplicationContext context;

	@Test
	public void testHome() throws Exception {
		mvc.
			perform(get("/state")).
			andExpect(status().isOk()).
			andExpect(content().string(allOf(
				containsString("Enter S1"),
				containsString("Machine started"))));
	}

	@Test
	public void testSendEventE1WithMachine1() throws Exception {
		mvc.
			perform(get("/state")
				.param("events", "E1")
				.param("machine", StateMachineController.MACHINE_ID_1)).
			andExpect(status().isOk()).
			andExpect(content().string(allOf(
				containsString("Enter S1"),
				containsString("Exit S1"),
				containsString("Enter S2"))));
	}

	@Test
	public void testSendEventsE1E2WithMachine1() throws Exception {
		mvc.
			perform(get("/state")
				.param("events", "E1")
				.param("events", "E2")
				.param("machine", StateMachineController.MACHINE_ID_1)).
			andExpect(status().isOk()).
			andExpect(content().string(allOf(
				containsString("Enter S1"),
				containsString("Exit S1"),
				containsString("Enter S2"),
				containsString("Exit S2"),
				containsString("Enter S3"))));
	}

	@Test
	public void testWithMachine2() throws Exception {
		mvc.
			perform(get("/state")
				.param("machine", StateMachineController.MACHINE_ID_2)).
			andExpect(status().isOk()).
			andExpect(content().string(allOf(
				containsString("Enter S10"),
				containsString("Enter S20"),
				containsString("Enter null"))));
	}

	@Test
	public void testSendEventsE10E20WithMachine2() throws Exception {
		mvc.
			perform(get("/state")
				.param("events", "E10")
				.param("events", "E20")
				.param("machine", StateMachineController.MACHINE_ID_2)).
			andExpect(status().isOk()).
			andExpect(content().string(allOf(
				containsString("Enter S10"),
				containsString("Enter S20"),
				containsString("Enter S11"),
				containsString("Enter S21"),
				containsString("Enter null"))));
	}

	@Before
	public void setup() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}
}
