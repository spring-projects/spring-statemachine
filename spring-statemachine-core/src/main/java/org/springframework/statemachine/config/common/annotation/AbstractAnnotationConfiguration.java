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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Base implementation of @{@link Configuration} class.
 *
 * @author Janne Valkealahti
 *
 * @param <O> The object that used builder returns
 * @param <B> The type of the builder
 */
public abstract class AbstractAnnotationConfiguration<B extends AnnotationBuilder<O>, O>
		implements ImportAware, BeanClassLoaderAware {

	private List<AnnotationConfigurer<O,B>> configurers;

	private ClassLoader beanClassLoader;
	
	private AnnotationAttributes annotationAttributes;

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		beanClassLoader = classLoader;
	}

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		Map<String, Object> enableConfigurationAttrMap =
				importMetadata.getAnnotationAttributes(EnableAnnotationConfiguration.class.getName());
		AnnotationAttributes enableConfigurationAttrs = AnnotationAttributes.fromMap(enableConfigurationAttrMap);
		if(enableConfigurationAttrs == null) {
			// search parent classes
			Class<?> currentClass = ClassUtils.resolveClassName(importMetadata.getClassName(), beanClassLoader);
			for(Class<?> classToInspect = currentClass ;classToInspect != null; classToInspect = classToInspect.getSuperclass()) {
				EnableAnnotationConfiguration enableConfigurationAnnotation =
						AnnotationUtils.findAnnotation(classToInspect, EnableAnnotationConfiguration.class);
				if(enableConfigurationAnnotation == null) {
					continue;
				}
				enableConfigurationAttrMap = AnnotationUtils
						.getAnnotationAttributes(enableConfigurationAnnotation);
				enableConfigurationAttrs = AnnotationAttributes.fromMap(enableConfigurationAttrMap);
			}
		}
		annotationAttributes = enableConfigurationAttrs;
	}

	/**
	 * Sets the configurers.
	 *
	 * @param configurers the configurers
	 * @throws Exception the exception
	 */
	@Autowired(required=false)
	public void setConfigurers(List<AnnotationConfigurer<O, B>> configurers) throws Exception {
		this.configurers = configurers;
		onConfigurers(configurers);
	}

	public AnnotationAttributes getAnnotationAttributes() {
		return annotationAttributes;
	}

	public List<AnnotationConfigurer<O, B>> getConfigurers() {
		return configurers;
	}

	protected abstract void onConfigurers(List<AnnotationConfigurer<O, B>> configurers) throws Exception;

}
