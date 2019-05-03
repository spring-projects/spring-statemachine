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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.statemachine.config.common.annotation.simple.EnableSimpleTest;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBeanABuilder;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBeanBConfigurer;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBuilder;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigurerAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Tests for using java config and importing xml config
 * with dependency for beans created from a javaconfig.
 *
 * @author Janne Valkealahti
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class XmlImportDependenciesTests {

	@Autowired
	private ApplicationContext ctx;

	@Test
	public void testDependencyBeanFromXml() throws Exception {
		assertNotNull(ctx);
		assertTrue(ctx.containsBean("simpleConfig"));
		assertTrue(ctx.containsBean("simpleConfigBeanB"));
		assertTrue(ctx.containsBean("dependencyBean"));

		DependencyBean dependencyBean = ctx.getBean(DependencyBean.class);
		assertThat(dependencyBean, notNullValue());
		assertThat(dependencyBean.getBeanB(), notNullValue());
	}

	@Configuration
	@EnableSimpleTest
	@ImportResource("org/springframework/statemachine/config/common/annotation/XmlImportDependencies.xml")
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
