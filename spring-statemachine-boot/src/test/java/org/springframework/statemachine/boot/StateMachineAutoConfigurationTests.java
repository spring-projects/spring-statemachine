/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.boot;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.boot.autoconfigure.StateMachineAutoConfiguration;
import org.springframework.statemachine.boot.support.BootStateMachineMonitor;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Tests for {@link StateMachineAutoConfiguration}.
 *
 * @author Janne Valkealahti
 *
 */
@SuppressWarnings("rawtypes")
public class StateMachineAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		if (context != null) {
			context.close();
		}
	}

	@Test
	public void testDefaults() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(StateMachineAutoConfiguration.class);
		context.refresh();
		assertThat(context.containsBean("bootStateMachineMonitor"), is(true));
	}

	@Test
	public void testMonitorDisabled() throws Exception {
		context = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(context, "spring.statemachine.monitor.enabled:false");
		context.register(StateMachineAutoConfiguration.class);
		context.refresh();
		assertThat(context.containsBean("bootStateMachineMonitor"), is(false));
	}

	@Test
	public void testMonitoringAddedViaAutoconfig() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(StateMachineAutoConfiguration.class, Config1.class);
		context.refresh();
		StateMachine stateMachine = context.getBean(StateMachine.class);
		Object compositeStateMachineMonitor = TestUtils.readField("stateMachineMonitor", stateMachine);
		Object orderedCompositeItem = TestUtils.readField("items", compositeStateMachineMonitor);
		List<Object> list = TestUtils.readField("list", orderedCompositeItem);
		assertThat(list, notNullValue());
		assertThat(list.size(), is(1));
		assertThat(list.get(0), instanceOf(BootStateMachineMonitor.class));
	}

	@Configuration
	@EnableStateMachine
	public static class Config1 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2")
					.state("S3");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S1").target("S2").event("E1")
					.and()
				.withExternal()
					.source("S2").target("S3").event("E2");
		}
	}
}
