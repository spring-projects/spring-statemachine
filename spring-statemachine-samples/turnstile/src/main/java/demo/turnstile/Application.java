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
	public static enum States {
	    LOCKED, UNLOCKED
	}
//end::snippetB[]

//tag::snippetC[]
	public static enum Events {
	    COIN, PUSH
	}
//end::snippetC[]

	public static void main(String[] args) throws Exception {
		Bootstrap.main(args);
	}

}
