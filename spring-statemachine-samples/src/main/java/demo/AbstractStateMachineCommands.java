package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AbstractStateMachineCommands<S, E> implements CommandMarker {

	@Autowired
	private StateMachine<S, E> stateMachine;

	protected StateMachine<S, E> getStateMachine() {
		return stateMachine;
	}

	@Autowired
	@Qualifier("stateChartModel")
	private String stateChartModel;

	@CliCommand(value = "sm state", help = "Prints state machine state")
	public String state() {
		return StringUtils.collectionToCommaDelimitedString(stateMachine.getState().getIds());
	}

	@CliCommand(value = "sm start", help = "Start a state machine")
	public String start() {
		stateMachine.start();
		return "State machine started";
	}

	@CliCommand(value = "sm stop", help = "Stop a state machine")
	public String stop() {
		stateMachine.stop();
		return "State machine stopped";
	}

	@CliCommand(value = "sm print", help = "Print state machine")
	public String print() {
		return stateChartModel;
	}

}