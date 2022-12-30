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
package demo.washer;

import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Bootstrap;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer.History;

@Configuration
public class Application  {

	@Configuration
	@EnableStateMachine
	static class StateMachineConfig
			extends EnumStateMachineConfigurerAdapter<States, Events> {

//tag::snippetAA[]
		@Override
		public void configure(StateMachineStateConfigurer<States, Events> states)
				throws Exception {
			states
				.withStates()
					.initial(States.RUNNING)
					.state(States.POWEROFF)
					.end(States.END)
					.and()
					.withStates()
						.parent(States.RUNNING)
						.initial(States.WASHING)
						.state(States.RINSING)
						.state(States.DRYING)
						.history(States.HISTORY, History.SHALLOW);
		}
//end::snippetAA[]

//tag::snippetAB[]
		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.WASHING).target(States.RINSING)
					.event(Events.RINSE)
					.and()
				.withExternal()
					.source(States.RINSING).target(States.DRYING)
					.event(Events.DRY)
					.and()
				.withExternal()
					.source(States.RUNNING).target(States.POWEROFF)
					.event(Events.CUTPOWER)
					.and()
				.withExternal()
					.source(States.POWEROFF).target(States.HISTORY)
					.event(Events.RESTOREPOWER)
					.and()
				.withExternal()
					.source(States.RUNNING).target(States.END)
					.event(Events.STOP);
		}
//end::snippetAB[]

	}

//tag::snippetB[]
	public enum States {
	    RUNNING, HISTORY, END,
	    WASHING, RINSING, DRYING,
	    POWEROFF
	}
//end::snippetB[]

//tag::snippetC[]
	public enum Events {
	    RINSE, DRY, STOP,
	    RESTOREPOWER, CUTPOWER
	}
//end::snippetC[]

	public static void main(String[] args) throws Exception {
		Bootstrap.main(args);
	}

}
