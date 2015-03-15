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
import demo.cdplayer.Application.States;
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
		stateMachine.sendEvent(Events.FORWARD);
	}

	public void back() {
		stateMachine.sendEvent(Events.BACK);
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

	@OnTransition(target = "PLAYING")
	public void playing(ExtendedState extendedState) {
		System.out.println("playing1");
		Object object = extendedState.getVariables().get(Variables.ELAPSEDTIME);
		if (object instanceof Long) {
			long elapsed = ((Long)object) + 1000l;
			extendedState.getVariables().put(Variables.ELAPSEDTIME, elapsed);
			SimpleDateFormat format = new SimpleDateFormat("mm:ss");
			trackStatus = format.format(new Date(elapsed));
		}

	}

}
