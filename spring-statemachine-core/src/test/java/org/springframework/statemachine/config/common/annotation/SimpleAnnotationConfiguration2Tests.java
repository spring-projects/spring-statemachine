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
import org.springframework.statemachine.config.common.annotation.simple.EnableSimpleTest2;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfig;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBeanABuilder;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBeanBConfigurer;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBuilder;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigurerAdapter;
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
public class SimpleAnnotationConfiguration2Tests {

	@Autowired
	private ApplicationContext ctx;

	@Test
	public void testSimpleConfig() throws Exception {
		assertNotNull(ctx);
		assertTrue(ctx.containsBean("simpleConfig"));
		SimpleTestConfig config = ctx.getBean("simpleConfig", SimpleTestConfig.class);
		assertThat(config.simpleData, notNullValue());
		assertThat(config.simpleData, is("simpleData"));

		assertThat(config.simpleProperties, notNullValue());
		assertThat(config.simpleProperties.getProperty("simpleKey1"), notNullValue());
		assertThat(config.simpleProperties.getProperty("simpleKey1"), is("simpleValue1"));

		assertThat(config.simpleBeanA, notNullValue());
		assertThat(config.simpleBeanA.dataA, notNullValue());
		assertThat(config.simpleBeanA.resources, notNullValue());

		assertThat(config.simpleBeanA.dataA, is("simpleDataA"));
		assertThat(config.simpleBeanA.resources.size(), is(2));
		Iterator<Resource> iterator = config.simpleBeanA.resources.iterator();
		String fileName1 = iterator.next().getFilename();
		String fileName2 = iterator.next().getFilename();
		String[] fileNames = new String[2];
		fileNames[0] = fileName1.equals("simpleResourceA1") ? fileName1 : fileName2;
		fileNames[1] = fileName2.equals("simpleResourceA2") ? fileName2 : fileName1;
		assertThat(fileNames[0], is("simpleResourceA1"));
		assertThat(fileNames[1], is("simpleResourceA2"));

//		assertTrue(ctx.containsBean("simpleConfigData"));
//		assertTrue(ctx.containsBean("simpleConfigBeanB"));
//		SimpleTestConfigBeanB beanB = ctx.getBean("simpleConfigBeanB", SimpleTestConfigBeanB.class);
//		assertThat(beanB.dataB, is("simpleDataB"));
//		assertThat(beanB.dataBB, is("simpleDataBB"));
	}
	
	@Configuration
	@EnableSimpleTest2
	static class Config extends SimpleTestConfigurerAdapter {

		@Override
		public void configure(SimpleTestConfigBuilder config) throws Exception {
			config
				.withProperties()
					.property("simpleKey1", "simpleValue1");
		}

		@Override
		public void configure(SimpleTestConfigBeanABuilder beanA) throws Exception {
			beanA
				.withResources()
					.resource("simpleResourceA1")
					.resource("simpleResourceA2")
					.and()
				.setData("simpleDataA");
		}

		@Override
		public void configure(SimpleTestConfigBeanBConfigurer beanB) throws Exception {
			beanB
				.setData("simpleDataB")
				.setDataBB("simpleDataBB")
				.withResources().and();
		}

	}
	
}
