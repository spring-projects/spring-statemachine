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

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.ObjectStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfig;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurer;
import org.springframework.statemachine.config.common.annotation.AbstractImportingAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;
import org.springframework.statemachine.config.model.ConfigurationData;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link Configuration} which gets imported from {@link EnableStateMachine} and registers
 * a {@link StateMachine} build from a {@link StateMachineConfigurerAdapter} via
 * a {@link BeanDefinition}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
@Configuration
public class StateMachineConfiguration<S, E> extends
		AbstractImportingAnnotationConfiguration<StateMachineConfigBuilder<S, E>, StateMachineConfig<S, E>> {

	private final StateMachineConfigBuilder<S, E> builder = new StateMachineConfigBuilder<S, E>();

	@Override
	protected BeanDefinition buildBeanDefinition(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> namedAnnotation) throws Exception {

		String enableStateMachineEnclosingClassName = importingClassMetadata.getClassName();
		// for below classloader, see gh122
		Class<?> enableStateMachineEnclosingClass = ClassUtils.forName(enableStateMachineEnclosingClassName,
				ClassUtils.getDefaultClassLoader());
		// return null if it looks like @EnableStateMachine was annotated with class
		// not extending StateMachineConfigurer.
		if (!ClassUtils.isAssignable(StateMachineConfigurer.class, enableStateMachineEnclosingClass)) {
			return null;
		}

		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(StateMachineDelegatingFactoryBean.class);
		AnnotationAttributes esmAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
				EnableStateMachine.class.getName(), false));
		Boolean contextEvents = esmAttributes.getBoolean("contextEvents");

		// check if Scope annotation is defined and set scope from it
		AnnotationAttributes scopeAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
				Scope.class.getName(), false));
		if (scopeAttributes != null) {
			String scope = scopeAttributes.getString("value");
			if (StringUtils.hasText(scope)) {
				beanDefinitionBuilder.setScope(scope);
			}
		}

		beanDefinitionBuilder.addConstructorArgValue(builder);
		beanDefinitionBuilder.addConstructorArgValue(StateMachine.class);
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

	@Override
	protected List<Class<? extends Annotation>> getAnnotations() {
		List<Class<? extends Annotation>> types = new ArrayList<Class<? extends Annotation>>();
		types.add(EnableStateMachine.class);
		return types;
	}

	private ResolvableType resolveFactoryObjectType(Class<?> enableStateMachineEnclosingClass) {
		ResolvableType type = null;
		try {
			Class<?>[] generics = ResolvableType.forClass(enableStateMachineEnclosingClass).as(StateMachineConfigurerAdapter.class).resolveGenerics();
			if (generics != null && generics.length == 2) {
				type = ResolvableType.forClassWithGenerics(StateMachine.class, generics);
			}
		} catch (Exception e) {
		}
		return type;
	}

	private static class StateMachineDelegatingFactoryBean<S, E>
		extends BeanDelegatingFactoryBean<StateMachine<S, E>,StateMachineConfigBuilder<S, E>,StateMachineConfig<S, E>>
		implements SmartLifecycle, BeanNameAware, BeanClassLoaderAware {

		private String clazzName;
		private Boolean contextEvents;
		private SmartLifecycle lifecycle;
		private DisposableBean disposableBean;
		private String beanName;
		private StateMachineMonitor<S, E> stateMachineMonitor;
		private ClassLoader classLoader;

		public StateMachineDelegatingFactoryBean(StateMachineConfigBuilder<S, E> builder, Class<StateMachine<S, E>> clazz,
				String clazzName, Boolean contextEvents) {
			super(builder, clazz);
			this.clazzName = clazzName;
			this.contextEvents = contextEvents;
		}

		@Override
		public void setBeanName(String name) {
			this.beanName = name;
		}

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void afterPropertiesSet() throws Exception {
			AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>> configurer =
					(AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>>) getBeanFactory()
						.getBean(ClassUtils.forName(clazzName, classLoader));
			getBuilder().apply(configurer);

			StateMachineConfig<S, E> stateMachineConfig = getBuilder().getOrBuild();
			TransitionsData<S, E> stateMachineTransitions = stateMachineConfig.getTransitions();
			StatesData<S, E> stateMachineStates = stateMachineConfig.getStates();
			ConfigurationData<S, E> stateMachineConfigurationConfig = stateMachineConfig.getStateMachineConfigurationConfig();

			ObjectStateMachineFactory<S, E> stateMachineFactory = null;
			if (stateMachineConfig.getModel() != null && stateMachineConfig.getModel().getFactory() != null) {
				stateMachineFactory = new ObjectStateMachineFactory<S, E>(
						new DefaultStateMachineModel<S, E>(stateMachineConfigurationConfig, null, null),
						stateMachineConfig.getModel().getFactory());
			} else {
				stateMachineFactory = new ObjectStateMachineFactory<S, E>(new DefaultStateMachineModel<S, E>(
						stateMachineConfigurationConfig, stateMachineStates, stateMachineTransitions), null);
			}

			stateMachineFactory.setBeanFactory(getBeanFactory());
			stateMachineFactory.setContextEventsEnabled(contextEvents);
			stateMachineFactory.setBeanName(beanName);
			stateMachineFactory.setHandleAutostartup(stateMachineConfigurationConfig.isAutoStart());
			if (stateMachineMonitor != null) {
				stateMachineFactory.setStateMachineMonitor(stateMachineMonitor);
			}
			StateMachine<S, E> stateMachine = stateMachineFactory.getStateMachine();
			this.lifecycle = (SmartLifecycle) stateMachine;
			this.disposableBean = (DisposableBean) stateMachine;
			setObject(stateMachine);
		}

		@Override
		public void destroy() throws Exception {
			disposableBean.destroy();
		}

		@Override
		public void start() {
			lifecycle.start();
		}

		@Override
		public void stop() {
			lifecycle.stop();
		}

		@Override
		public boolean isRunning() {
			return lifecycle.isRunning();
		}

		@Override
		public int getPhase() {
			return 0;
		}

		@Override
		public boolean isAutoStartup() {
			return lifecycle.isAutoStartup();
		}

		@Override
		public void stop(Runnable callback) {
			lifecycle.stop(callback);
		}

		@Autowired(required = false)
		public void setStateMachineMonitor(StateMachineMonitor<S, E> stateMachineMonitor) {
			this.stateMachineMonitor = stateMachineMonitor;
		}
	}

}
