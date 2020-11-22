/*
 * Copyright 2016-2020 the original author or authors.
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
package org.springframework.statemachine.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.boot.autoconfigure.StateMachineRedisRepositoriesAutoConfiguration;
import org.springframework.statemachine.data.redis.RedisRepositoryState;

public class StateMachineRedisRepositoriesAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@AfterEach
	public void close() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	@Test
	public void testRedisEnabled() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(TestConfiguration.class);
		context.register(
				RedisAutoConfiguration.class,
				RedisRepositoriesAutoConfiguration.class,
				StateMachineRedisRepositoriesAutoConfiguration.class);

		context.refresh();
		assertThat(context.containsBean("redisStateRepository")).isTrue();
	}

	@Test
	public void testRedisDisabled() throws Exception {
		context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.statemachine.data.redis.repositories.enabled=false").applyTo(context);
		context.register(StateMachineRedisRepositoriesAutoConfiguration.class);
		context.refresh();
		assertThat(context.containsBean("redisStateRepository")).isFalse();
	}

	@Configuration
	@TestAutoConfigurationPackage(RedisRepositoryState.class)
	protected static class TestConfiguration {
	}
}
