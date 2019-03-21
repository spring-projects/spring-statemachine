/*
 * Copyright 2015-2016 the original author or authors.
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
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.ConfigurationData;
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
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(StateMachineFactoryDelegatingFactoryBean.class);
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(
				EnableStateMachineFactory.class.getName(), false));
		Boolean contextEvents = attributes.getBoolean("contextEvents");
		beanDefinitionBuilder.addConstructorArgValue(builder);
		beanDefinitionBuilder.addConstructorArgValue(importingClassMetadata.getClassName());
		beanDefinitionBuilder.addConstructorArgValue(contextEvents);
		return beanDefinitionBuilder.getBeanDefinition();
	}


	@Override
	protected List<Class<? extends Annotation>> getAnnotations() {
		List<Class<? extends Annotation>> types = new ArrayList<Class<? extends Annotation>>();
		types.add(EnableStateMachineFactory.class);
		return types;
	}

	private static class StateMachineFactoryDelegatingFactoryBean<S, E> implements
			FactoryBean<StateMachineFactory<S, E>>, BeanFactoryAware, InitializingBean {

		private final StateMachineConfigBuilder<S, E> builder;
		private List<AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>>> configurers;
		private BeanFactory beanFactory;
		private StateMachineFactory<S, E> stateMachineFactory;
		private String clazzName;
		private Boolean contextEvents;

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
		public void afterPropertiesSet() throws Exception {
			// do not continue without configurers, it would not work
			if (configurers == null || configurers.size() == 0) {
				throw new BeanDefinitionStoreException(
						"Cannot configure state machine due to missing configurers. Did you remember to use " +
						"@EnableStateMachineFactory with a StateMachineConfigurerAdapter.");
			}
			for (AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>> configurer : configurers) {
				Class<?> clazz = configurer.getClass();
				if (ClassUtils.getUserClass(clazz).getName().equals(clazzName)) {
					builder.apply(configurer);
				}
			}
			StateMachineConfig<S, E> stateMachineConfig = builder.getOrBuild();
			TransitionsData<S, E> stateMachineTransitions = stateMachineConfig.getTransitions();
			StatesData<S, E> stateMachineStates = stateMachineConfig.getStates();
			ConfigurationData<S, E> stateMachineConfigurationConfig = stateMachineConfig
					.getStateMachineConfigurationConfig();
			ObjectStateMachineFactory<S, E> objectStateMachineFactory = new ObjectStateMachineFactory<S, E>(
					new DefaultStateMachineModel<S, E>(stateMachineConfigurationConfig, stateMachineStates, stateMachineTransitions));
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

		@Autowired(required=false)
		protected void onConfigurers(
				List<AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>>> configurers)
				throws Exception {
			this.configurers = configurers;
		}

	}

}
