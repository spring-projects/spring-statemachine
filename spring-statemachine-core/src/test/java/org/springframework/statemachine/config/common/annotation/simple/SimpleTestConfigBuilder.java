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
package org.springframework.statemachine.config.common.annotation.simple;

import java.util.Properties;

import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.configurers.DefaultPropertiesConfigurer;
import org.springframework.statemachine.config.common.annotation.configurers.PropertiesConfigurerAware;


/**
 * {@link AnnotationBuilder} for {@link SimpleTestConfig}.
 *
 * @author Janne Valkealahti
 *
 */
public class SimpleTestConfigBuilder extends AbstractConfiguredAnnotationBuilder<SimpleTestConfig, SimpleTestConfigBuilder,SimpleTestConfigBuilder>
		implements PropertiesConfigurerAware {

	private final Properties properties = new Properties();

	@Override
	protected SimpleTestConfig performBuild() throws Exception {
		SimpleTestConfig bean = new SimpleTestConfig("simpleData", properties);
		bean.simpleBeanA = getSharedObject(SimpleTestConfigBeanABuilder.class).build();
		bean.simpleBeanB = getSharedObject(SimpleTestConfigBeanBBuilder.class).build();
		return bean;
	}

	@Override
	public void configureProperties(Properties properties) {
		getProperties().putAll(properties);
	}

	public Properties getProperties() {
		return properties;
	}

	public DefaultPropertiesConfigurer<SimpleTestConfig, SimpleTestConfigBuilder,SimpleTestConfigBuilder> withProperties() throws Exception {
		return getOrApply(new DefaultPropertiesConfigurer<SimpleTestConfig, SimpleTestConfigBuilder,SimpleTestConfigBuilder>());
	}

}
