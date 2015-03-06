package demo.turnstile;

import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import demo.AbstractStateMachineCommands;
import demo.turnstile.Application.Events;
import demo.turnstile.Application.States;

@Component
public class StateMachineCommands extends AbstractStateMachineCommands<States, Events> {

	@CliCommand(value = "sm event", help = "Sends an event to a state machine")
	public String event(@CliOption(key = { "", "event" }, mandatory = true, help = "The event") final Events event) {
		getStateMachine().sendEvent(event);
		return "Event " + event + " send";
	}

}