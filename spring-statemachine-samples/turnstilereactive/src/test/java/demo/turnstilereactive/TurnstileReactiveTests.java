/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TurnstileReactiveTests {

	@Autowired
	private WebTestClient webClient;

	@Test
	public void testState() {
		webClient.get().uri("/state").exchange()
			.expectBody(String.class).value(body -> {
				assertThat(body).contains("LOCKED");
			});
	}

	@Test
	public void testPushDenied() {
		webClient.post().uri("/events")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just("{\"event\":\"PUSH\"}"), String.class)
			.exchange()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].event").isEqualTo("PUSH")
			.jsonPath("$[0].resultType").isEqualTo("DENIED");
	}

	@Test
	public void testCoinAccepted() {
		webClient.post().uri("/events")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just("{\"event\":\"COIN\"}"), String.class)
			.exchange()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(1)
			.jsonPath("$[0].event").isEqualTo("COIN")
			.jsonPath("$[0].resultType").isEqualTo("ACCEPTED");
	}

	@Test
	public void testNullEvent() {
		webClient.post().uri("/events")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just("{\"event\":null}"), String.class)
			.exchange()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void testCoinPushAccepted() {
		webClient.post().uri("/events")
			.contentType(MediaType.APPLICATION_JSON)
			.body(Mono.just("[{\"event\":\"COIN\"},{\"event\":\"PUSH\"}]"), String.class)
			.exchange()
			.expectBody()
			.jsonPath("$.length()").isEqualTo(2)
			.jsonPath("$[0].event").isEqualTo("COIN")
			.jsonPath("$[0].resultType").isEqualTo("ACCEPTED")
			.jsonPath("$[1].event").isEqualTo("PUSH")
			.jsonPath("$[1].resultType").isEqualTo("ACCEPTED");
	}
}
