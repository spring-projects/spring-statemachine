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
package org.springframework.statemachine.config.common.annotation.complex;

import java.util.Properties;

import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.configurers.DefaultPropertiesConfigurer;
import org.springframework.statemachine.config.common.annotation.configurers.PropertiesConfigurerAware;


/**
 * {@link AnnotationBuilder} for {@link ComplexTestConfig}.
 *
 * @author Janne Valkealahti
 *
 */
public class ComplexTestConfigBuilder extends AbstractConfiguredAnnotationBuilder<ComplexTestConfig, ComplexTestConfigBuilder,ComplexTestConfigBuilder>
		implements PropertiesConfigurerAware {

	private final Properties properties = new Properties();

	@Override
	protected ComplexTestConfig performBuild() throws Exception {
		ComplexTestConfig bean = new ComplexTestConfig("complexData", properties);
		bean.complexBeanA = getSharedObject(ComplexTestConfigBeanABuilder.class).build();
		bean.complexBeanB = getSharedObject(ComplexTestConfigBeanBBuilder.class).build();
		return bean;
	}

	@Override
	public void configureProperties(Properties properties) {
		getProperties().putAll(properties);
	}

	public Properties getProperties() {
		return properties;
	}

	public DefaultPropertiesConfigurer<ComplexTestConfig, ComplexTestConfigBuilder,ComplexTestConfigBuilder> withProperties() throws Exception {
		return getOrApply(new DefaultPropertiesConfigurer<ComplexTestConfig, ComplexTestConfigBuilder,ComplexTestConfigBuilder>());
	}

}
