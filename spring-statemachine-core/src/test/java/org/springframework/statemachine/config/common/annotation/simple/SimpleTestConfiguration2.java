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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.statemachine.config.common.annotation.AbstractImportingAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;

@Configuration
public class SimpleTestConfiguration2 extends AbstractImportingAnnotationConfiguration<SimpleTestConfigBuilder, SimpleTestConfig> {

	private final SimpleTestConfigBuilder builder = new SimpleTestConfigBuilder();

	@Override
	protected BeanDefinition buildBeanDefinition(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> namedAnnotation) throws Exception {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(SimpleTestConfigDelegatingFactoryBean.class);
		beanDefinitionBuilder.addConstructorArgValue(builder);
		return beanDefinitionBuilder.getBeanDefinition();
	}

	@Override
	protected List<Class<? extends Annotation>> getAnnotations() {
		List<Class<? extends Annotation>> types = new ArrayList<Class<? extends Annotation>>();
		types.add(EnableSimpleTest2.class);
		return types;
	}

	private static class SimpleTestConfigDelegatingFactoryBean extends BeanDelegatingFactoryBean<SimpleTestConfig, SimpleTestConfigBuilder, SimpleTestConfig> {

		public SimpleTestConfigDelegatingFactoryBean(SimpleTestConfigBuilder builder) {
			super(builder, SimpleTestConfig.class);
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			for (AnnotationConfigurer<SimpleTestConfig, SimpleTestConfigBuilder> configurer : getConfigurers()) {
				if (configurer.isAssignable(getBuilder())) {
					getBuilder().apply(configurer);
				}
			}
			setObject(getBuilder().getOrBuild());
		}

	}

}
