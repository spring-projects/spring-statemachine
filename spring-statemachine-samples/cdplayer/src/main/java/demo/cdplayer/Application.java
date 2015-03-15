package demo.cdplayer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.Bootstrap;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

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
					.initial(States.IDLE)
					.state(States.IDLE)
					.and()
					.withStates()
						.parent(States.IDLE)
						.initial(States.CLOSED)
						.state(States.CLOSED)
						.state(States.OPEN)
						.and()
				.withStates()
					.state(States.BUSY)
					.and()
					.withStates()
						.parent(States.BUSY)
						.initial(States.PLAYING)
						.state(States.PLAYING)
						.state(States.PAUSED);

		}

		@Override
		public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
				throws Exception {
			transitions
				.withExternal()
					.source(States.CLOSED).target(States.OPEN).event(Events.EJECT)
					.and()
				.withExternal()
					.source(States.OPEN).target(States.CLOSED).event(Events.EJECT)
					.and()
				.withExternal()
					.source(States.PLAYING).target(States.PAUSED).event(Events.PAUSE)
					.and()
				.withInternal()
					.source(States.PLAYING)
					.timer(1000)
					.and()
				.withExternal()
					.source(States.PAUSED).target(States.PLAYING).event(Events.PAUSE)
					.and()
				.withExternal()
					.source(States.BUSY).target(States.IDLE).event(Events.STOP)
					.and()
				.withExternal()
					.source(States.IDLE).target(States.BUSY).event(Events.PLAY)
					.action(playAction())
					.guard(playGuard())
					.and()
				.withInternal()
					.source(States.OPEN).event(Events.LOAD).action(loadAction());
		}

		@Bean
		public Action<States, Events> loadAction() {
			return new Action<States, Events>() {
				@Override
				public void execute(StateContext<States, Events> context) {
					Object cd = context.getMessageHeader(Variables.CD);
					context.getExtendedState().getVariables().put(Variables.CD, cd);
				}
			};
		}

		@Bean
		public Action<States, Events> playAction() {
			return new Action<States, Events>() {
				@Override
				public void execute(StateContext<States, Events> context) {
					context.getExtendedState().getVariables().put(Variables.ELAPSEDTIME, 0l);
				}
			};
		}

		@Bean
		public Guard<States, Events> playGuard() {
			return new Guard<States, Events>() {

				@Override
				public boolean evaluate(StateContext<States, Events> context) {
					ExtendedState extendedState = context.getExtendedState();
					return extendedState.getVariables().get(Variables.CD) != null;
				}
			};
		}

	}
//end::snippetA[]

//tag::snippetB[]
	public static enum States {
		// super state of PLAYING and PAUSED
	    BUSY,
	    PLAYING,
	    PAUSED,
		// super state of CLOSED and OPEN
	    IDLE,
	    CLOSED,
	    OPEN
	}
//end::snippetB[]

//tag::snippetC[]
	public static enum Events {
	    PLAY, STOP, PAUSE, EJECT, LOAD, FORWARD, BACK
	}
//end::snippetC[]

//tag::snippetD[]
	@Bean
	public CdPlayer cdPlayer() {
		return new CdPlayer();
	}

	@Bean
	public Library library() {
		return Library.buildSampleLibrary();
	}
//end::snippetD[]

//tag::snippetE[]
	public static enum Variables {
		CD, TRACK, ELAPSEDTIME
	}
//end::snippetE[]

	public static void main(String[] args) throws Exception {
		Bootstrap.main(args);
	}

}
