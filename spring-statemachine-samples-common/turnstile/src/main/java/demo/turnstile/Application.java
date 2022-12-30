/*
 * Copyright 2015 the original author or authors.
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
package demo.turnstile;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Bootstrap;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
public class Application  {

//tag::snippetA[]
	@Configuration
	@EnableStateMachine
	static class StateMachineConfig
			extends EnumStateMachineConfigurerAdapter<States, Events> {

		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.LOCKED)
					.states(EnumSet.allOf(States.class));
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.LOCKED)
					.target(States.UNLOCKED)
					.event(Events.COIN)
					.and()
				.withExternal()
					.source(States.UNLOCKED)
					.target(States.LOCKED)
					.event(Events.PUSH);
		}

	}
//end::snippetA[]

//tag::snippetB[]
	public enum States {
	    LOCKED, UNLOCKED
	}
//end::snippetB[]

//tag::snippetC[]
	public enum Events {
	    COIN, PUSH
	}
//end::snippetC[]

	public static void main(String[] args) throws Exception {
		Bootstrap.main(args);
	}

}
