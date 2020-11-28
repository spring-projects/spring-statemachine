/*
 * Copyright 2015-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfig;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfigBuilder;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfigurerAdapter;
import org.springframework.statemachine.config.common.annotation.complex.EnableComplexTest;
import org.springframework.statemachine.config.common.annotation.simple.EnableSimpleTest;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfig;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBuilder;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigurerAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class MultipleAnnotationConfigurationTests {

	@Autowired
	private ApplicationContext ctx;

	@Test
	public void testConfig() throws Exception {
		assertThat(ctx.containsBean("simpleConfig")).isTrue();
		SimpleTestConfig simpleConfig = ctx.getBean("simpleConfig", SimpleTestConfig.class);
		assertThat(simpleConfig.simpleData).isNotNull();
		assertThat(simpleConfig.simpleData).isEqualTo("simpleData");

		assertThat(simpleConfig.simpleProperties).isNotNull();
		assertThat(simpleConfig.simpleProperties.getProperty("simpleKey1")).isNotNull();
		assertThat(simpleConfig.simpleProperties.getProperty("simpleKey1")).isEqualTo("simpleValue1");

		assertThat(ctx.containsBean("complexConfig")).isTrue();
		ComplexTestConfig complexConfig = ctx.getBean("complexConfig", ComplexTestConfig.class);
		assertThat(complexConfig.complexData).isNotNull();
		assertThat(complexConfig.complexData).isEqualTo("complexData");

		assertThat(complexConfig.complexProperties).isNotNull();
		assertThat(complexConfig.complexProperties.getProperty("complexKey1")).isNotNull();
		assertThat(complexConfig.complexProperties.getProperty("complexKey1")).isEqualTo("complexValue1");

	}

	@Configuration
	@EnableSimpleTest
	static class SimpleConfig extends SimpleTestConfigurerAdapter {
		@Override
		public void configure(SimpleTestConfigBuilder config) throws Exception {
			config
				.withProperties()
					.property("simpleKey1", "simpleValue1");
		}
	}


	@Configuration
	@EnableComplexTest
	static class ComplexConfig extends ComplexTestConfigurerAdapter {
		@Override
		public void configure(ComplexTestConfigBuilder config) throws Exception {
			config
				.withProperties()
					.property("complexKey1", "complexValue1");
		}
	}

}
