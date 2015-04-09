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
package org.springframework.statemachine.config.configuration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfig;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.common.annotation.AbstractImportingAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;
import org.springframework.util.ClassUtils;

@Configuration
public class StateMachineConfiguration<S extends Enum<S>, E extends Enum<E>> extends
		AbstractImportingAnnotationConfiguration<StateMachineConfigBuilder<S, E>, StateMachineConfig<S, E>> {

	private final StateMachineConfigBuilder<S, E> builder = new StateMachineConfigBuilder<S, E>();

	@Override
	protected BeanDefinition buildBeanDefinition(AnnotationMetadata importingClassMetadata,
			Class<? extends Annotation> namedAnnotation) throws Exception {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(StateMachineDelegatingFactoryBean2.class);
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
				EnableStateMachine.class.getName(), false));
		Boolean contextEvents = attributes.getBoolean("contextEvents");

		beanDefinitionBuilder.addConstructorArgValue(builder);
		beanDefinitionBuilder.addConstructorArgValue(StateMachine.class);
		beanDefinitionBuilder.addConstructorArgValue(importingClassMetadata.getClassName());
		beanDefinitionBuilder.addConstructorArgValue(contextEvents);
		return beanDefinitionBuilder.getBeanDefinition();
	}

	@Override
	protected List<Class<? extends Annotation>> getAnnotations() {
		List<Class<? extends Annotation>> types = new ArrayList<Class<? extends Annotation>>();
		types.add(EnableStateMachine.class);
		return types;
	}

	private static class StateMachineDelegatingFactoryBean2<S extends Enum<S>, E extends Enum<E>>
		extends BeanDelegatingFactoryBean<StateMachine<S, E>,StateMachineConfigBuilder<S, E>,StateMachineConfig<S, E>> {

		private String clazzName;
		private Boolean contextEvents;

		public StateMachineDelegatingFactoryBean2(StateMachineConfigBuilder<S, E> builder, Class<StateMachine<S, E>> clazz,
				String clazzName, Boolean contextEvents) {
			super(builder, clazz);
			this.clazzName = clazzName;
			this.contextEvents = contextEvents;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			for (AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>> configurer : getConfigurers()) {
				Class<?> clazz = configurer.getClass();
				if (ClassUtils.getUserClass(clazz).getName().equals(clazzName)) {
					getBuilder().apply(configurer);
				}
			}
			StateMachineConfig<S, E> stateMachineConfig = getBuilder().getOrBuild();
			StateMachineTransitions<S, E> stateMachineTransitions = stateMachineConfig.getTransitions();
			StateMachineStates<S, E> stateMachineStates = stateMachineConfig.getStates();
			EnumStateMachineFactory<S, E> stateMachineFactory = new EnumStateMachineFactory<S, E>(
					stateMachineTransitions, stateMachineStates);
			stateMachineFactory.setBeanFactory(getBeanFactory());
			stateMachineFactory.setContextEventsEnabled(contextEvents);
			setObject(stateMachineFactory.getStateMachine());
		}

	}

}
