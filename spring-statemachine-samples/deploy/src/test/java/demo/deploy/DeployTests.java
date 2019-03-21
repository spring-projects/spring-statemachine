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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class})
@WebAppConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DeployTests {

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
	public void testDeploy() throws Exception {
		mvc.
			perform(get("/state").param("event", "DEPLOY")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("States: [READY]")));
	}

	@Test
	public void testUndeploy() throws Exception {
		mvc.
			perform(get("/state").param("event", "UNDEPLOY")).
			andExpect(status().isOk()).
			andExpect(content().string(containsString("States: [READY]")));
	}

	@Before
	public void setup() throws Exception {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}
}
