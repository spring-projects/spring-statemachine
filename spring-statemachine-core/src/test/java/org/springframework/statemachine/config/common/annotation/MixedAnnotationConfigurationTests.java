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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class MixedAnnotationConfigurationTests {

	@Autowired
	private ApplicationContext ctx;

	@Test
	public void testConfig() throws Exception {
		assertNotNull(ctx);
		assertTrue(ctx.containsBean("simpleConfig"));
		SimpleTestConfig simpleConfig = ctx.getBean("simpleConfig", SimpleTestConfig.class);
		assertThat(simpleConfig.simpleData, notNullValue());
		assertThat(simpleConfig.simpleData, is("simpleData"));

		assertThat(simpleConfig.simpleProperties, notNullValue());
		assertThat(simpleConfig.simpleProperties.getProperty("simpleKey1"), notNullValue());
		assertThat(simpleConfig.simpleProperties.getProperty("simpleKey1"), is("simpleValue1"));

		assertTrue(ctx.containsBean("complexConfig"));
		ComplexTestConfig complexConfig = ctx.getBean("complexConfig", ComplexTestConfig.class);
		assertThat(complexConfig.complexData, notNullValue());
		assertThat(complexConfig.complexData, is("complexData"));
		assertThat(complexConfig.complexProperties, notNullValue());
		assertThat(complexConfig.complexProperties.getProperty("complexKey1"), notNullValue());
		assertThat(complexConfig.complexProperties.getProperty("complexKey1"), is("complexValue1"));

		assertThat(complexConfig.simpleTestConfig, notNullValue());
		assertThat(complexConfig.simpleTestConfig.simpleData, notNullValue());
		assertThat(complexConfig.simpleTestConfig.simpleData, is("simpleData"));

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
