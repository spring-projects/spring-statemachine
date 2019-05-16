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

import java.lang.annotation.Annotation;
import java.util.List;

import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Base class for {@link Configuration} which works on a bean definition level
 * relying on {@link ImportBeanDefinitionRegistrar} phase to register beans.
 *
 * @author Janne Valkealahti
 *
 * @param <O> The object that used builder returns
 * @param <B> The type of the builder
 */
public abstract class AbstractImportingAnnotationConfiguration<B extends AnnotationBuilder<O>, O> implements
		ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware {

	private BeanFactory beanFactory;

	private Environment environment;

	private final BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		List<Class<? extends Annotation>> annotationTypes = getAnnotations();
		Class<? extends Annotation> namedAnnotation = null;
		String[] names = null;
		ScopedProxyMode proxyMode = null;
		if (annotationTypes != null) {
			for (Class<? extends Annotation> annotationType : annotationTypes) {
				AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
						annotationType.getName(), false));
				if (attributes != null && attributes.containsKey("name")) {
					names = attributes.getStringArray("name");
					namedAnnotation = annotationType;
					break;
				}
			}
		}

		// check if Scope annotation is defined and get proxyMode from it
		AnnotationAttributes scopeAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
				Scope.class.getName(), false));
		if (scopeAttributes != null) {
			proxyMode = scopeAttributes.getEnum("proxyMode");
		}

		BeanDefinition beanDefinition;
		try {
			beanDefinition = buildBeanDefinition(importingClassMetadata, namedAnnotation);
		} catch (Exception e) {
			throw new RuntimeException("Error with onConfigurers", e);
		}

		// implementation didn't return definition so don't continue registration
		if (beanDefinition == null) {
			return;
		}

		if (ObjectUtils.isEmpty(names)) {
			// ok, name(s) not given, generate one
			names = new String[] { beanNameGenerator.generateBeanName(beanDefinition, registry) };
		}

		registry.registerBeanDefinition(names[0], beanDefinition);
		if (names.length > 1) {
			for (int i = 1; i < names.length; i++) {
				registry.registerAlias(names[0], names[i]);
			}
		}

		// wrap in scoped proxy if needed
		if (proxyMode != null && proxyMode != ScopedProxyMode.DEFAULT && proxyMode != ScopedProxyMode.NO) {
			BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, names[0]);
			BeanDefinitionHolder scopedProxy = null;
			if (proxyMode == ScopedProxyMode.TARGET_CLASS) {
				scopedProxy = ScopedProxyUtils.createScopedProxy(definitionHolder, registry, true);
			} else if (proxyMode == ScopedProxyMode.INTERFACES) {
				scopedProxy = ScopedProxyUtils.createScopedProxy(definitionHolder, registry, false);
			} else {
				throw new IllegalArgumentException("Unknown proxyMode " + proxyMode);
			}
			BeanDefinitionReaderUtils.registerBeanDefinition(scopedProxy, registry);
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory);
		this.beanFactory = beanFactory;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Called to get a bean definition to register.
	 *
	 * @param importingClassMetadata annotation metadata of the importing class
	 * @param namedAnnotation found annotations for bean names
	 * @return the bean definition to register
	 * @throws Exception if error occurred
	 */
	protected abstract BeanDefinition buildBeanDefinition(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> namedAnnotation) throws Exception;

	/**
	 * Gets the annotations specific for this configurer.
	 *
	 * @return the annotations
	 */
	protected abstract List<Class<? extends Annotation>> getAnnotations();

	/**
	 * Gets the bean factory.
	 *
	 * @return the bean factory
	 */
	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Gets the environment.
	 *
	 * @return the environment
	 */
	protected Environment getEnvironment() {
		return environment;
	}

	protected abstract static class BeanDelegatingFactoryBean<T, B extends AnnotationBuilder<O>, O> implements
			FactoryBean<T>, BeanFactoryAware, InitializingBean, DisposableBean {

		private final B builder;

		private T object;

		private List<AnnotationConfigurer<O, B>> configurers;

		private BeanFactory beanFactory;

		private Class<T> clazz;

		public BeanDelegatingFactoryBean(B builder, Class<T> clazz) {
			this.builder = builder;
			this.clazz = clazz;
		}

		@Override
		public Class<?> getObjectType() {
			return clazz;
		}

		@Override
		public T getObject() throws Exception {
			return object;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

		@Autowired(required = false)
		public void setConfigurers(List<AnnotationConfigurer<O, B>> configurers) {
			this.configurers = configurers;
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void destroy() throws Exception {
		}

		public B getBuilder() {
			return builder;
		}

		public List<AnnotationConfigurer<O, B>> getConfigurers() {
			return configurers;
		}

		protected void setObject(T object) {
			this.object = object;
		}

		protected BeanFactory getBeanFactory() {
			return beanFactory;
		}

	}

}
