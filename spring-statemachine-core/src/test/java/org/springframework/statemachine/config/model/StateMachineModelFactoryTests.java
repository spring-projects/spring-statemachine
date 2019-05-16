/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.statemachine.config.model;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.TestUtils;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.ObjectStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

public class StateMachineModelFactoryTests extends AbstractStateMachineTests {

	@Test
	public void testResolvingFromContext() {
		context.register(Config1.class);
		context.refresh();

		TestStateMachineModelFactory modelBuilder = new TestStateMachineModelFactory();
		modelBuilder.setBeanFactory(context);
		ObjectStateMachineFactory<String, String> factory = new ObjectStateMachineFactory<>(modelBuilder.build());

		StateMachine<String,String> stateMachine = factory.getStateMachine();
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

	@Test
	public void testFromAnnotationConfig() {
		context.register(Config2.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

	@Test
	public void testModelRecreates() {
		context.register(Config3.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String,String> stateMachine = stateMachineFactory.getStateMachine();
		TestStateMachineModelFactory modelFactory = context.getBean(TestStateMachineModelFactory.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
		stateMachine.stop();

		modelFactory.state1 = "SS1";
		modelFactory.state2 = "SS2";
		modelFactory.event1 = "EE1";

		stateMachine = stateMachineFactory.getStateMachine();
		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("SS1"));
		stateMachine.sendEvent("EE1");
		assertThat(stateMachine.getState().getIds(), contains("SS2"));
		stateMachine.stop();
	}

	@Test
	public void testConfigAdapterConfigFromModel() throws Exception {
		context.register(Config4.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		Object o1 = TestUtils.readField("stateListener", stateMachine);
		Object o2 = TestUtils.readField("listeners", o1);
		Object o3 = TestUtils.readField("list", o2);
		assertThat(((List<?>)o3).size(), is(0));
	}

	@Test
	public void testConfigAdapterConfigFromAdapter() throws Exception {
		context.register(Config5.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		Object o1 = TestUtils.readField("stateListener", stateMachine);
		Object o2 = TestUtils.readField("listeners", o1);
		Object o3 = TestUtils.readField("list", o2);
		assertThat(((List<?>)o3).size(), is(1));
	}

	@Configuration
	static class Config1 {
		@Bean
		public Action<String, String> action1() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
				}
			};
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new TestStateMachineModelFactory();
		}

		@Bean
		public Action<String, String> action1() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
				}
			};
		}
	}

	@Configuration
	@EnableStateMachineFactory
	public static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new TestStateMachineModelFactory();
		}

		@Bean
		public Action<String, String> action1() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
				}
			};
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.listener(stateMachineListener());
		}

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new TestStateMachineModelFactory();
		}

		@Bean
		public Action<String, String> action1() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
				}
			};
		}

		@Bean
		public StateMachineListener<String, String> stateMachineListener() {
			return new StateMachineListenerAdapter<String, String>(){};
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config5 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.listener(stateMachineListener());
		}

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new TestStateMachineModelFactory2();
		}

		@Bean
		public Action<String, String> action1() {
			return new Action<String, String>() {
				@Override
				public void execute(StateContext<String, String> context) {
				}
			};
		}

		@Bean
		public StateMachineListener<String, String> stateMachineListener() {
			return new StateMachineListenerAdapter<String, String>(){};
		}
	}

	@SuppressWarnings("unchecked")
	private static class TestStateMachineModelFactory implements StateMachineModelFactory<String, String>, BeanFactoryAware {
		private BeanFactory beanFactory;
		String state1 = "S1";
		String state2 = "S2";
		String event1 = "E1";

		@Override
		public StateMachineModel<String, String> build() {

			Action<String, String> action1 = beanFactory.getBean("action1", Action.class);
			Collection<Action<String, String>> s2Actions = new ArrayList<>();
			s2Actions.add(action1);

			ConfigurationData<String, String> configurationData = new ConfigurationData<>();

			Collection<StateData<String, String>> stateData = new ArrayList<>();
			stateData.add(new StateData<String, String>(state1, true));
			stateData.add(new StateData<String, String>(null, null, state2, null, s2Actions, null));
			StatesData<String, String> statesData = new StatesData<>(stateData);

			Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
			transitionData.add(new TransitionData<String, String>(state1, state2, event1));
			TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

			StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(configurationData, statesData, transitionsData);
			return stateMachineModel;
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public StateMachineModel<String, String> build(String machineId) {
			return build();
		}
	}

	@SuppressWarnings("unchecked")
	private static class TestStateMachineModelFactory2 implements StateMachineModelFactory<String, String>, BeanFactoryAware {
		private BeanFactory beanFactory;
		String state1 = "S1";
		String state2 = "S2";
		String event1 = "E1";

		@Override
		public StateMachineModel<String, String> build() {

			Action<String, String> action1 = beanFactory.getBean("action1", Action.class);
			Collection<Action<String, String>> s2Actions = new ArrayList<>();
			s2Actions.add(action1);

			Collection<StateData<String, String>> stateData = new ArrayList<>();
			stateData.add(new StateData<String, String>(state1, true));
			stateData.add(new StateData<String, String>(null, null, state2, null, s2Actions, null));
			StatesData<String, String> statesData = new StatesData<>(stateData);

			Collection<TransitionData<String, String>> transitionData = new ArrayList<>();
			transitionData.add(new TransitionData<String, String>(state1, state2, event1));
			TransitionsData<String, String> transitionsData = new TransitionsData<>(transitionData);

			StateMachineModel<String, String> stateMachineModel = new DefaultStateMachineModel<>(null, statesData, transitionsData);
			return stateMachineModel;
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public StateMachineModel<String, String> build(String machineId) {
			return build();
		}
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}
}
