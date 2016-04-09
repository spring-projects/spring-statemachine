/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.uml;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;

public class UmlStateMachineModelFactoryTests extends AbstractUmlTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testSimpleFlat() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.registerAction("action1", new LatchAction());
		builder.setBeanFactory(context);
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(2));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleSubmachine() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-submachine.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(4));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getParent(), nullValue());
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getParent(), nullValue());
			} else if (stateData.getState().equals("S11")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getParent(), is("S1"));
			} else if (stateData.getState().equals("S12")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getParent(), is("S1"));
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	public void testSimpleRootRegions() {
		context.refresh();
		Resource model1 = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
		UmlStateMachineModelFactory builder = new UmlStateMachineModelFactory(model1);
		builder.setBeanFactory(context);
		assertThat(model1.exists(), is(true));
		StateMachineModel<String, String> stateMachineModel = builder.build();
		assertThat(stateMachineModel, notNullValue());
		Collection<StateData<String, String>> stateDatas = stateMachineModel.getStatesData().getStateData();
		assertThat(stateDatas.size(), is(4));
		for (StateData<String, String> stateData : stateDatas) {
			if (stateData.getState().equals("S1")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getRegion(), notNullValue());
			} else if (stateData.getState().equals("S2")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getRegion(), notNullValue());
			} else if (stateData.getState().equals("S3")) {
				assertThat(stateData.isInitial(), is(true));
				assertThat(stateData.getRegion(), notNullValue());
			} else if (stateData.getState().equals("S4")) {
				assertThat(stateData.isInitial(), is(false));
				assertThat(stateData.getRegion(), notNullValue());
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleFlatMachine() throws Exception {
		context.register(Config2.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);
		LatchAction action1 = context.getBean("action1", LatchAction.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1"));
		stateMachine.sendEvent("E1");
		assertThat(action1.latch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleSubmachineMachine() throws Exception {
		context.register(Config3.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), contains("S1", "S11"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), contains("S1", "S12"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), contains("S2"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testSimpleRootRegionsMachine() throws Exception {
		context.register(Config4.class);
		context.refresh();
		StateMachine<String, String> stateMachine = context.getBean(StateMachine.class);

		stateMachine.start();
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S1", "S3"));
		stateMachine.sendEvent("E1");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S3"));
		stateMachine.sendEvent("E2");
		assertThat(stateMachine.getState().getIds(), containsInAnyOrder("S2", "S4"));
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
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public Action<String, String> action1() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-submachine.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public Action<String, String> action1() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
			return new UmlStateMachineModelFactory(model);
		}

		@Bean
		public Action<String, String> action1() {
			return new LatchAction();
		}
	}

	public static class LatchAction implements Action<String, String> {
		CountDownLatch latch = new CountDownLatch(1);
		@Override
		public void execute(StateContext<String, String> context) {
			latch.countDown();
		}
	}
}
