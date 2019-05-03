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
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.boot.autoconfigure.StateMachineJpaRepositoriesAutoConfiguration;
import org.springframework.statemachine.data.jpa.JpaRepositoryState;

public class StateMachineJpaRepositoriesAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	@Test
	public void testJpaEnabled() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(TestConfiguration.class);
		context.register(
				EmbeddedDataSourceConfiguration.class,
				HibernateJpaAutoConfiguration.class,
				JpaRepositoriesAutoConfiguration.class,
				StateMachineJpaRepositoriesAutoConfiguration.class);

		context.refresh();
		assertThat(context.containsBean("jpaStateRepository"), is(true));
	}

	@Test
	public void testJpaDisabled() throws Exception {
		context = new AnnotationConfigApplicationContext();
		TestPropertyValues.of("spring.statemachine.data.jpa.repositories.enabled=false").applyTo(context);
		context.register(StateMachineJpaRepositoriesAutoConfiguration.class);
		context.refresh();
		assertThat(context.containsBean("jpaStateRepository"), is(false));
	}

	@Configuration
	@TestAutoConfigurationPackage(JpaRepositoryState.class)
	protected static class TestConfiguration {
	}
}
