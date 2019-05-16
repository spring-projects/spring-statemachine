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

import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfig;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfigBeanABuilder;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfigBeanB;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfigBeanBConfigurer;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfigBuilder;
import org.springframework.statemachine.config.common.annotation.complex.ComplexTestConfigurerAdapter;
import org.springframework.statemachine.config.common.annotation.complex.EnableComplexTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Tests for generic javaconfig builder/configurer concepts.
 *
 * @author Janne Valkealahti
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class ComplexAnnotationConfigurationTests {

	@Autowired
	private ApplicationContext ctx;

	@Test
	public void testSimpleConfig() throws Exception {
		assertNotNull(ctx);
		assertTrue(ctx.containsBean("complexConfig"));
		ComplexTestConfig config = ctx.getBean("complexConfig", ComplexTestConfig.class);
		assertThat(config.complexData, notNullValue());
		assertThat(config.complexData, is("complexData"));

		assertThat(config.complexProperties, notNullValue());
		assertThat(config.complexProperties.getProperty("complexKey1"), notNullValue());
		assertThat(config.complexProperties.getProperty("complexKey1"), is("complexValue1"));

		assertThat(config.complexBeanA, notNullValue());
		assertThat(config.complexBeanA.dataA, notNullValue());
		assertThat(config.complexBeanA.resources, notNullValue());

		assertThat(config.complexBeanA.dataA, is("complexDataA"));
		assertThat(config.complexBeanA.resources.size(), is(2));
		Iterator<Resource> iterator = config.complexBeanA.resources.iterator();
		String fileName1 = iterator.next().getFilename();
		String fileName2 = iterator.next().getFilename();
		String[] fileNames = new String[2];
		fileNames[0] = fileName1.equals("complexResourceA1") ? fileName1 : fileName2;
		fileNames[1] = fileName2.equals("complexResourceA2") ? fileName2 : fileName1;
		assertThat(fileNames[0], is("complexResourceA1"));
		assertThat(fileNames[1], is("complexResourceA2"));

		assertTrue(ctx.containsBean("complexConfigData"));
		assertTrue(ctx.containsBean("complexConfigBeanB"));
		ComplexTestConfigBeanB beanB = ctx.getBean("complexConfigBeanB", ComplexTestConfigBeanB.class);
		assertThat(beanB.dataB, is("complexDataB"));
		assertThat(beanB.dataBB, is("complexDataBB"));
	}

	@Configuration
	@EnableComplexTest
	static class Config extends ComplexTestConfigurerAdapter {

		@Override
		public void configure(ComplexTestConfigBuilder config) throws Exception {
			config
				.withProperties()
					.property("complexKey1", "complexValue1");
		}

		@Override
		public void configure(ComplexTestConfigBeanABuilder beanA) throws Exception {
			beanA
				.withResources()
					.resource("complexResourceA1")
					.resource("complexResourceA2")
					.and()
				.setData("complexDataA");
		}

		@Override
		public void configure(ComplexTestConfigBeanBConfigurer beanB) throws Exception {
			beanB
				.setData("complexDataB")
				.setDataBB("complexDataBB")
				.withResources().and();
		}

	}

}
