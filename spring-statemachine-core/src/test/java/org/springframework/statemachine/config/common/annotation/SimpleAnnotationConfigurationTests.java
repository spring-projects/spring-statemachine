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

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.common.annotation.simple.EnableSimpleTest;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfig;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBeanABuilder;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBeanB;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBeanBConfigurer;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigBuilder;
import org.springframework.statemachine.config.common.annotation.simple.SimpleTestConfigurerAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Tests for generic javaconfig builder/configurer concepts.
 *
 * @author Janne Valkealahti
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class SimpleAnnotationConfigurationTests {

	@Autowired
	private ApplicationContext ctx;

	@Test
	public void testSimpleConfig() throws Exception {
		assertThat(ctx.containsBean("simpleConfig")).isTrue();
		SimpleTestConfig config = ctx.getBean("simpleConfig", SimpleTestConfig.class);
		assertThat(config.simpleData).isNotNull();
		assertThat(config.simpleData).isEqualTo("simpleData");

		assertThat(config.simpleProperties).isNotNull();
		assertThat(config.simpleProperties.getProperty("simpleKey1")).isNotNull();
		assertThat(config.simpleProperties.getProperty("simpleKey1")).isEqualTo("simpleValue1");

		assertThat(config.simpleBeanA).isNotNull();
		assertThat(config.simpleBeanA.dataA).isNotNull();
		assertThat(config.simpleBeanA.resources).isNotNull();

		assertThat(config.simpleBeanA.dataA).isEqualTo("simpleDataA");
		assertThat(config.simpleBeanA.resources).hasSize(2);
		Iterator<Resource> iterator = config.simpleBeanA.resources.iterator();
		String fileName1 = iterator.next().getFilename();
		String fileName2 = iterator.next().getFilename();
		String[] fileNames = new String[2];
		fileNames[0] = fileName1.equals("simpleResourceA1") ? fileName1 : fileName2;
		fileNames[1] = fileName2.equals("simpleResourceA2") ? fileName2 : fileName1;
		assertThat(fileNames[0]).isEqualTo("simpleResourceA1");
		assertThat(fileNames[1]).isEqualTo("simpleResourceA2");

		assertThat(ctx.containsBean("simpleConfigData")).isTrue();
		assertThat(ctx.containsBean("simpleConfigBeanB")).isTrue();
		SimpleTestConfigBeanB beanB = ctx.getBean("simpleConfigBeanB", SimpleTestConfigBeanB.class);
		assertThat(beanB.dataB).isEqualTo("simpleDataB");
		assertThat(beanB.dataBB).isEqualTo("simpleDataBB");
	}

	@Configuration
	@EnableSimpleTest
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
