/*
 * Copyright 2015-2018 the original author or authors.
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
package org.springframework.statemachine.config.configuration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.ObjectStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfig;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.common.annotation.AbstractImportingAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.util.ClassUtils;

/**
 * {@link Configuration} which gets imported from {@link EnableStateMachineFactory} and registers
 * a {@link StateMachineFactory} build from a {@link StateMachineConfigurerAdapter} via
 * a {@link BeanDefinition}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
@Configuration
public class StateMachineFactoryConfiguration<S, E> extends
		AbstractImportingAnnotationConfiguration<StateMachineConfigBuilder<S, E>, StateMachineConfig<S, E>> {

	private final StateMachineConfigBuilder<S, E> builder = new StateMachineConfigBuilder<S, E>();

	@Override
	protected BeanDefinition buildBeanDefinition(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> namedAnnotation) throws Exception {

		String enableStateMachineEnclosingClassName = importingClassMetadata.getClassName();
		// for below classloader, see gh122
		Class<?> enableStateMachineEnclosingClass = ClassUtils.forName(enableStateMachineEnclosingClassName,
				ClassUtils.getDefaultClassLoader());

		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(StateMachineFactoryDelegatingFactoryBean.class);
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
				EnableStateMachineFactory.class.getName(), false));
		Boolean contextEvents = attributes.getBoolean("contextEvents");
		beanDefinitionBuilder.addConstructorArgValue(builder);
		beanDefinitionBuilder.addConstructorArgValue(importingClassMetadata.getClassName());
		beanDefinitionBuilder.addConstructorArgValue(contextEvents);

		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();

		// try to add more info about generics
		ResolvableType type = resolveFactoryObjectType(enableStateMachineEnclosingClass);
		if (type != null && beanDefinition instanceof RootBeanDefinition) {
			((RootBeanDefinition)beanDefinition).setTargetType(type);
		}

		return beanDefinition;
	}

	private ResolvableType resolveFactoryObjectType(Class<?> enableStateMachineEnclosingClass) {
		ResolvableType type = null;
		try {
			Class<?>[] generics = ResolvableType.forClass(enableStateMachineEnclosingClass).as(StateMachineConfigurerAdapter.class).resolveGenerics();
			if (generics != null && generics.length == 2) {
				type = ResolvableType.forClassWithGenerics(StateMachineFactory.class, generics);
			}
		} catch (Exception e) {
		}
		return type;
	}

	@Override
	protected List<Class<? extends Annotation>> getAnnotations() {
		List<Class<? extends Annotation>> types = new ArrayList<Class<? extends Annotation>>();
		types.add(EnableStateMachineFactory.class);
		return types;
	}

	private static class StateMachineFactoryDelegatingFactoryBean<S, E> implements
			FactoryBean<StateMachineFactory<S, E>>, BeanFactoryAware, InitializingBean, BeanClassLoaderAware {

		private final StateMachineConfigBuilder<S, E> builder;
		private BeanFactory beanFactory;
		private StateMachineFactory<S, E> stateMachineFactory;
		private String clazzName;
		private Boolean contextEvents;
		private ClassLoader classLoader;

		@SuppressWarnings("unused")
		public StateMachineFactoryDelegatingFactoryBean(StateMachineConfigBuilder<S, E> builder, String clazzName, Boolean contextEvents) {
			this.builder = builder;
			this.clazzName = clazzName;
			this.contextEvents = contextEvents;
		}

		@Override
		public StateMachineFactory<S, E> getObject() throws Exception {
			return stateMachineFactory;
		}

		@Override
		public Class<?> getObjectType() {
			return StateMachineFactory.class;
		}

		@Override
		public boolean isSingleton() {
			return true;
		}

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void afterPropertiesSet() throws Exception {
			AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>> configurer =
			        (AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>>) beanFactory
						.getBean(ClassUtils.forName(clazzName, classLoader));
		    builder.apply(configurer);

			StateMachineConfig<S, E> stateMachineConfig = builder.getOrBuild();
			TransitionsData<S, E> stateMachineTransitions = stateMachineConfig.getTransitions();
			StatesData<S, E> stateMachineStates = stateMachineConfig.getStates();
			ConfigurationData<S, E> stateMachineConfigurationConfig = stateMachineConfig
					.getStateMachineConfigurationConfig();

			ObjectStateMachineFactory<S, E> objectStateMachineFactory = null;
			if (stateMachineConfig.getModel() != null && stateMachineConfig.getModel().getFactory() != null) {
				objectStateMachineFactory = new ObjectStateMachineFactory<S, E>(
						new DefaultStateMachineModel<S, E>(stateMachineConfigurationConfig, null, null),
						stateMachineConfig.getModel().getFactory());
			} else {
				objectStateMachineFactory = new ObjectStateMachineFactory<S, E>(new DefaultStateMachineModel<S, E>(
						stateMachineConfigurationConfig, stateMachineStates, stateMachineTransitions), null);
			}

			objectStateMachineFactory.setBeanFactory(beanFactory);
			objectStateMachineFactory.setContextEventsEnabled(contextEvents);
			// explicitly tell factory to handle auto-start because
			// machine is not created as a bean so factory need to
			// call lifecycle methods manually
			objectStateMachineFactory.setHandleAutostartup(true);
			this.stateMachineFactory = objectStateMachineFactory;
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

	}

}
