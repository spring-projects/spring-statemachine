package org.springframework.statemachine.config.configuration;

import java.lang.annotation.Annotation;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfig;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineStates;
import org.springframework.statemachine.config.builders.StateMachineTransitions;
import org.springframework.statemachine.config.common.annotation.AbstractImportingAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;

@Configuration
public class StateMachineFactoryConfiguration<S extends Enum<S>, E extends Enum<E>> extends
		AbstractImportingAnnotationConfiguration<StateMachineConfigBuilder<S, E>, StateMachineConfig<S, E>> {

	private final StateMachineConfigBuilder<S, E> builder = new StateMachineConfigBuilder<S, E>();

	@Override
	protected BeanDefinition buildBeanDefinition(AnnotationMetadata importingClassMetadata) throws Exception {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder
				.rootBeanDefinition(StateMachineFactoryDelegatingFactoryBean.class);
		beanDefinitionBuilder.addConstructorArgValue(builder);
		return beanDefinitionBuilder.getBeanDefinition();
	}

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableStateMachineFactory.class;
	}

	private static class StateMachineFactoryDelegatingFactoryBean<S extends Enum<S>, E extends Enum<E>> implements
			FactoryBean<StateMachineFactory<S, E>>, BeanFactoryAware, InitializingBean {

		private final StateMachineConfigBuilder<S, E> builder;

		private List<AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>>> configurers;

		private BeanFactory beanFactory;

		private StateMachineFactory<S, E> stateMachineFactory;

		@SuppressWarnings("unused")
		public StateMachineFactoryDelegatingFactoryBean(StateMachineConfigBuilder<S, E> builder) {
			this.builder = builder;
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
			for (AnnotationConfigurer<StateMachineConfig<S, E>, StateMachineConfigBuilder<S, E>> configurer : configurers) {
				builder.apply(configurer);
			}
			StateMachineConfig<S, E> stateMachineConfig = builder.getOrBuild();
			StateMachineTransitions<S, E> stateMachineTransitions = stateMachineConfig.getTransitions();
			StateMachineStates<S, E> stateMachineStates = stateMachineConfig.getStates();
			EnumStateMachineFactory<S,E> enumStateMachineFactory = new EnumStateMachineFactory<S, E>(stateMachineTransitions, stateMachineStates);
			enumStateMachineFactory.setBeanFactory(beanFactory);
			this.stateMachineFactory = enumStateMachineFactory;
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
