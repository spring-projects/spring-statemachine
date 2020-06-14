/*
 * Copyright 2015-2019 the original author or authors.
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
package org.springframework.statemachine.config;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.config.builders.StateMachineConfigBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationBuilder;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineConfigurer;
import org.springframework.statemachine.config.builders.StateMachineModelBuilder;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionBuilder;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.statemachine.config.model.ConfigurationData;

/**
 * {@code StateMachineBuilder} provides a builder pattern for
 * {@link StateMachine} using a similar concepts found from a
 * normal annotation based configuration.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineBuilder {

	/**
	 * Gets a builder for a {@link StateMachine}.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @return the builder
	 */
	public static <S, E> Builder<S, E> builder() {
		return new Builder<S, E>();
	}

	/**
	 * {@code Builder} implementation handling logic of building
	 * a {@link StateMachine} manually.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 */
	public static class Builder<S, E> {

		private StateMachineConfigBuilder<S, E> builder;
		private BuilderStateMachineConfigurerAdapter<S, E> adapter;

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			adapter = new BuilderStateMachineConfigurerAdapter<S, E>();
			builder = new StateMachineConfigBuilder<S, E>();
		}

		/**
		 * Configure model.
		 *
		 * @return the state machine model configurer
		 */
		public StateMachineModelConfigurer<S, E> configureModel() {
			return adapter.modelBuilder;
		}

		/**
		 * Configure configuration.
		 *
		 * @return the state machine configuration configurer
		 */
		public StateMachineConfigurationConfigurer<S, E> configureConfiguration() {
			return adapter.configurationBuilder;
		}

		/**
		 * Configure states.
		 *
		 * @return the state machine state configurer
		 */
		public StateMachineStateConfigurer<S, E> configureStates() {
			return adapter.stateBuilder;
		}

		/**
		 * Configure transitions.
		 *
		 * @return the state machine transition configurer
		 */
		public StateMachineTransitionConfigurer<S, E> configureTransitions() {
			return adapter.transitionBuilder;
		}

		/**
		 * Creates a {@link StateMachineFactory} from builder
		 *
		 * @return the factory to create a state machine
		 */
		public StateMachineFactory<S, E> createFactory() {
			try {
				builder.apply(adapter);

				ObjectStateMachineFactory<S, E> stateMachineFactory = StateMachineFactory.create(builder);
				ConfigurationData<S, E> stateMachineConfigurationConfig = builder.getOrBuild().stateMachineConfigurationConfig;

				stateMachineFactory.setHandleAutostartup(stateMachineConfigurationConfig.isAutoStart());

				if (stateMachineConfigurationConfig.getBeanFactory() != null) {
					stateMachineFactory.setBeanFactory(stateMachineConfigurationConfig.getBeanFactory());
				}
				return stateMachineFactory;
			} catch (Exception e) {
				throw new StateMachineException("Error building state machine", e);
			}
		}

		/**
		 * Builds a {@link StateMachine}.
		 *
		 * @return the state machine
		 */
		public StateMachine<S, E> build() {
			return createFactory().getStateMachine();
		}

	}

	private static class BuilderStateMachineConfigurerAdapter<S extends Object, E extends Object>
			implements StateMachineConfigurer<S, E> {

		private StateMachineModelBuilder<S, E> modelBuilder;
		private StateMachineTransitionBuilder<S, E> transitionBuilder;
		private StateMachineStateBuilder<S, E> stateBuilder;
		private StateMachineConfigurationBuilder<S, E> configurationBuilder;

		BuilderStateMachineConfigurerAdapter() {
			try {
				getStateMachineModelBuilder();
				getStateMachineTransitionBuilder();
				getStateMachineStateBuilder();
				getStateMachineConfigurationBuilder();
			} catch (Exception e) {
				throw new StateMachineException("Error instantiating builder adapter", e);
			}
		}

		@Override
		public void init(StateMachineConfigBuilder<S, E> config) throws Exception {
			config.setSharedObject(StateMachineModelBuilder.class, getStateMachineModelBuilder());
			config.setSharedObject(StateMachineTransitionBuilder.class, getStateMachineTransitionBuilder());
			config.setSharedObject(StateMachineStateBuilder.class, getStateMachineStateBuilder());
			config.setSharedObject(StateMachineConfigurationBuilder.class, getStateMachineConfigurationBuilder());
		}

		@Override
		public void configure(StateMachineConfigBuilder<S, E> builder) throws Exception {
		}

		@Override
		public boolean isAssignable(AnnotationBuilder<StateMachineConfig<S, E>> builder) {
			return false;
		}

		@Override
		public void configure(StateMachineModelConfigurer<S, E> model) throws Exception {
		}

		@Override
		public void configure(StateMachineConfigurationConfigurer<S, E> config) throws Exception {
		}

		@Override
		public void configure(StateMachineStateConfigurer<S, E> states) throws Exception {
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<S, E> transitions) throws Exception {
		}

		protected final StateMachineModelBuilder<S, E> getStateMachineModelBuilder() throws Exception {
			if (modelBuilder != null) {
				return modelBuilder;
			}
			modelBuilder = new StateMachineModelBuilder<S, E>(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR, true);
			configure(modelBuilder);
			return modelBuilder;
		}

		protected final StateMachineTransitionBuilder<S, E> getStateMachineTransitionBuilder() throws Exception {
			if (transitionBuilder != null) {
				return transitionBuilder;
			}
			transitionBuilder = new StateMachineTransitionBuilder<S, E>(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR, true);
			return transitionBuilder;
		}

		protected final StateMachineStateBuilder<S, E> getStateMachineStateBuilder() throws Exception {
			if (stateBuilder != null) {
				return stateBuilder;
			}
			stateBuilder = new StateMachineStateBuilder<S, E>(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR, true);
			return stateBuilder;
		}

		protected final StateMachineConfigurationBuilder<S, E> getStateMachineConfigurationBuilder() throws Exception {
			if (configurationBuilder != null) {
				return configurationBuilder;
			}
			configurationBuilder = new StateMachineConfigurationBuilder<S, E>(ObjectPostProcessor.QUIESCENT_POSTPROCESSOR, true);
			return configurationBuilder;
		}
	}
}
