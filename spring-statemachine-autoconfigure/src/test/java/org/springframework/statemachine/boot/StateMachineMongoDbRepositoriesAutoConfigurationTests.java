/*
 * Copyright 2016-2017 the original author or authors.
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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.boot.autoconfigure.StateMachineMongoDbRepositoriesAutoConfiguration;
import org.springframework.statemachine.boot.autoconfigure.StateMachineRedisRepositoriesAutoConfiguration;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryState;

public class StateMachineMongoDbRepositoriesAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	@Test
	public void testMongoEnabled() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(TestConfiguration.class);
		context.register(
				MongoAutoConfiguration.class,
				MongoDataAutoConfiguration.class,
				MongoRepositoriesAutoConfiguration.class,
				StateMachineMongoDbRepositoriesAutoConfiguration.class);

		context.refresh();
		assertThat(context.containsBean("mongoDbStateRepository"), is(true));
	}

	@Test
	public void testMongoDisabled() throws Exception {
		context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.statemachine.data.redis.repositories.enabled=false").applyTo(context);
		context.register(StateMachineRedisRepositoriesAutoConfiguration.class);
		context.refresh();
		assertThat(context.containsBean("mongoDbStateRepository"), is(false));
	}

	@Configuration
	@TestAutoConfigurationPackage(MongoDbRepositoryState.class)
	protected static class TestConfiguration {
	}
}
