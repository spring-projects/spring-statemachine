/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
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
		Class<?> annotationType = getAnnotation();
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
				annotationType.getName(), false));
		String[] names = attributes.getStringArray("name");

		BeanDefinition beanDefinition;
		try {
			beanDefinition = buildBeanDefinition();
		} catch (Exception e) {
			throw new RuntimeException("Error with onConfigurers", e);
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
	}

	protected abstract static class BeanDelegatingFactoryBean<B extends AnnotationBuilder<O>, O>
		implements FactoryBean<O>, InitializingBean {
		
		private final B builder;
		
		private O object;
		
		private List<AnnotationConfigurer<O, B>> configurers;
		
		public BeanDelegatingFactoryBean(B builder){
			this.builder = builder;
		}
		
		@Override
		public abstract Class<O> getObjectType();
		
		@Override
		public O getObject() throws Exception {
			return object;
		}
		
		@Override
		public boolean isSingleton() {
			return true;
		}
		
//		@Override
//		public void afterPropertiesSet() throws Exception {
//			for (AnnotationConfigurer<O, B> configurer : configurers) {
//				if (configurer.isAssignable(builder)) {
//					// we need builder.apply(configurer);
////					builder.
//				}
//			}
//			// should be getOrBuild???
//			object = builder.build();
//		}
		
		@Autowired(required = false)
		public void setConfigurers(List<AnnotationConfigurer<O, B>> configurers) {
			this.configurers = configurers;
		}
		
		public B getBuilder() {
			return builder;
		}
				
		public List<AnnotationConfigurer<O, B>> getConfigurers() {
			return configurers;
		}
		
		protected void setObject(O object) {
			this.object = object;
		}

	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory,
				"beanFactory be of type ListableBeanFactory but was " + beanFactory);
		this.beanFactory = beanFactory;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	/**
	 * Called to get a bean definition to register.
	 *
	 * @return the bean definition to register
	 * @throws Exception if error occurred
	 */
	protected abstract BeanDefinition buildBeanDefinition() throws Exception;

	/**
	 * Gets the annotation specific for this configurer.
	 *
	 * @return the annotation
	 */
	protected abstract Class<? extends Annotation> getAnnotation();

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

}
