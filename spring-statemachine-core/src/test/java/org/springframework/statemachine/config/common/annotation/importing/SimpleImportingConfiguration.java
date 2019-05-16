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
package org.springframework.statemachine.config.common.annotation.importing;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.statemachine.config.common.annotation.AbstractImportingAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;

@Configuration
public class SimpleImportingConfiguration extends
		AbstractImportingAnnotationConfiguration<ImportingTestConfigBuilder, ImportingTestConfig> {

	private final ImportingTestConfigBuilder builder = new ImportingTestConfigBuilder();

	@Override
	protected BeanDefinition buildBeanDefinition(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> namedAnnotation) throws Exception {
		List<Class<? extends Annotation>> annotationTypes = getAnnotations();
		String[] names = null;
		if (annotationTypes != null) {
			for (Class<?> annotationType : annotationTypes) {
				AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
						annotationType.getName(), false));
				if (attributes.containsKey("name")) {
					names = attributes.getStringArray("name");
					break;
				}
			}
		}

		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(SimpleUUIDDelegatingFactoryBean.class);
		beanDefinitionBuilder.addConstructorArgValue(builder);
		beanDefinitionBuilder.addConstructorArgValue(UUID.class);
		beanDefinitionBuilder.addConstructorArgValue(names);
		return beanDefinitionBuilder.getBeanDefinition();
	}


	@Override
	protected List<Class<? extends Annotation>> getAnnotations() {
		List<Class<? extends Annotation>> types = new ArrayList<Class<? extends Annotation>>();
		types.add(EnableImportingTest.class);
		return types;
	}

	private static class SimpleUUIDDelegatingFactoryBean extends
			BeanDelegatingFactoryBean<UUID, ImportingTestConfigBuilder, ImportingTestConfig> {

		private final String[] beanNames;

		public SimpleUUIDDelegatingFactoryBean(ImportingTestConfigBuilder builder, Class<UUID> clazz, String[] beanNames) {
			super(builder, clazz);
			this.beanNames = beanNames;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			for (AnnotationConfigurer<ImportingTestConfig, ImportingTestConfigBuilder> configurer : getConfigurers()) {
				Class<?> clazz = configurer.getClass();
				EnableImportingTest findAnnotation = AnnotationUtils.findAnnotation(clazz, EnableImportingTest.class);
				String[] annonames = findAnnotation.name();
				if (beanNames[0].equals(annonames[0])) {
					getBuilder().apply(configurer);
				}
			}
			setObject(getBuilder().getOrBuild().importingData);
		}

	}

}
