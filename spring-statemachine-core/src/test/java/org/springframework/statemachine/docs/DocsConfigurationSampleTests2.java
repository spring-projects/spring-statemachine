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
package org.springframework.statemachine.docs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public class DocsConfigurationSampleTests2 extends AbstractStateMachineTests {

// tag::snippetA[]
	@Configuration
	@EnableStateMachine
	public class Config2 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states)
				throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1")
					.and()
				.withInternal()
					.source("S2")
					.action(timerAction())
					.timer(1000);
		}

		@Bean
		public TimerAction timerAction() {
			return new TimerAction();
		}
	}

	public class TimerAction implements Action<String, String> {

		@Override
		public void execute(StateContext<String, String> context) {
			// do something in every 1 sec
		}
	}
// end::snippetA[]

// tag::snippetB[]
	@Configuration
	public class Config3 {

		@Bean
		@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
		StateMachine<String, String> stateMachine() throws Exception {
			Builder<String, String> builder = StateMachineBuilder.builder();
			builder.configureConfiguration()
				.withConfiguration()
					.autoStartup(true)
					.taskExecutor(new SyncTaskExecutor());
			builder.configureStates()
				.withStates()
					.initial("S1")
					.state("S2");
			builder.configureTransitions()
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
			StateMachine<String, String> stateMachine = builder.build();
			return stateMachine;
		}

	}
// end::snippetB[]

// tag::snippetC[]
	@Configuration
	@EnableStateMachine
	@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
	public static class Config4 extends StateMachineConfigurerAdapter<String, String> {

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
			config
				.withConfiguration()
					.autoStartup(true);
		}

		@Override
		public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
			states
				.withStates()
					.initial("S1")
					.state("S2");
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
			transitions
				.withExternal()
					.source("S1")
					.target("S2")
					.event("E1");
		}

	}
// end::snippetC[]

// tag::snippetD[]
	@Controller
	public class StateMachineController {

		@Autowired
		StateMachine<String, String> stateMachine;

		@RequestMapping(path="/state", method=RequestMethod.POST)
		public HttpEntity<Void> setState(@RequestParam("event") String event) {
			stateMachine.sendEvent(event);
			return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
		}

		@RequestMapping(path="/state", method=RequestMethod.GET)
		@ResponseBody
		public String getState() {
			return stateMachine.getState().getId();
		}
	}
// end::snippetD[]

}
