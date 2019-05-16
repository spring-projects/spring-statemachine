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
package org.springframework.statemachine.config.common.annotation;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.UUID;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.common.annotation.importing.EnableImportingTest;
import org.springframework.statemachine.config.common.annotation.importing.ImportingTestConfigBuilder;
import org.springframework.statemachine.config.common.annotation.importing.ImportingTestConfigurerAdapter;

public class ImportingBeanDefinitionTests {

	@Test
	public void testCorrentBeanUsed() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);
		Object beanBuilt = ctx.getBean("enableImportingTestBean");
		Object beanInjected = ctx.getBean("uuid");
		assertThat(beanBuilt, sameInstance(beanInjected));
		ctx.close();
	}


	@Configuration
	@EnableImportingTest
	static class Config extends ImportingTestConfigurerAdapter {

		@Override
		public void configure(ImportingTestConfigBuilder builder) throws Exception {
			builder.setUuid(uuid());
		}

		@Bean
		public UUID uuid() {
			return UUID.randomUUID();
		}

	}

}
