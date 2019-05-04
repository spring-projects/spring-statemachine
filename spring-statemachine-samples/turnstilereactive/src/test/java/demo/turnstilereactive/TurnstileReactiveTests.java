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
package demo.turnstilereactive;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TurnstileReactiveTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	public void testState() {
		webClient.get().uri("/state").exchange()
			.expectBody(String.class).value(containsString("LOCKED"));
	}

	@Test
	public void testEvent() {
		webClient.post().uri("/event").contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just("{\"event\":\"PUSH\"}"), String.class).exchange()
			.expectBody(String.class).value(containsString("DENIED"));
		webClient.post().uri("/event").contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just("{\"event\":\"COIN\"}"), String.class).exchange()
			.expectBody(String.class).value(containsString("ACCEPTED"));
		webClient.get().uri("/state").exchange()
			.expectBody(String.class).value(containsString("UNLOCKED"));
		webClient.post().uri("/event").contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just("{\"event\":null}"), String.class).exchange()
			.expectBody(String.class).value(containsString("[]"));
	}
}
