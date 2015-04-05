package demo.cdplayer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

import demo.cdplayer.Application.Events;
import demo.cdplayer.Application.Headers;
import demo.cdplayer.Application.States;
import demo.cdplayer.Application.StatesOnTransition;
import demo.cdplayer.Application.Variables;

@WithStateMachine
public class CdPlayer {

	@Autowired
	private StateMachine<States, Events> stateMachine;

	private String cdStatus = "No CD";
	private String trackStatus = "";

	public void load(Cd cd) {
		stateMachine.sendEvent(MessageBuilder.withPayload(Events.LOAD).setHeader(Variables.CD.toString(), cd).build());
	}

	public void play() {
		stateMachine.sendEvent(Events.PLAY);
	}

	public void stop() {
		stateMachine.sendEvent(Events.STOP);
	}

	public void pause() {
		stateMachine.sendEvent(Events.PAUSE);
	}

	public void eject() {
		stateMachine.sendEvent(Events.EJECT);
	}

	public void forward() {
		stateMachine
			.sendEvent(MessageBuilder
					.withPayload(Events.FORWARD)
					.setHeader(Headers.TRACKSHIFT.toString(), 1).build());
	}

	public void back() {
		stateMachine
			.sendEvent(MessageBuilder
					.withPayload(Events.BACK)
					.setHeader(Headers.TRACKSHIFT.toString(), -1).build());
	}

	public String getLdcStatus() {
		return cdStatus + " " + trackStatus;
	}

	@OnTransition(target = "BUSY")
	public void busy(ExtendedState extendedState) {
		Object cd = extendedState.getVariables().get(Variables.CD);
		if (cd != null) {
			cdStatus = ((Cd)cd).getName();
		}
	}

	@StatesOnTransition(target = States.PLAYING)
	public void playing(ExtendedState extendedState) {
		Object elapsed = extendedState.getVariables().get(Variables.ELAPSEDTIME);
		Object cd = extendedState.getVariables().get(Variables.CD);
		Object track = extendedState.getVariables().get(Variables.TRACK);
		if (elapsed instanceof Long && track instanceof Integer && cd instanceof Cd) {
			SimpleDateFormat format = new SimpleDateFormat("mm:ss");
			trackStatus = ((Cd) cd).getTracks()[((Integer) track)]
					+ " " + format.format(new Date((Long) elapsed));
		}
	}

	@StatesOnTransition(target = States.OPEN)
	public void open(ExtendedState extendedState) {
		cdStatus = "Open";
	}

	@StatesOnTransition(target = States.CLOSED)
	public void closed(ExtendedState extendedState) {
		Object cd = extendedState.getVariables().get(Variables.CD);
		if (cd != null) {
			cdStatus = ((Cd)cd).getName();
		} else {
			cdStatus = "No CD";
		}
		trackStatus = "";
	}

}
