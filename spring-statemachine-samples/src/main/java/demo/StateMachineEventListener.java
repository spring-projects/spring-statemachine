package demo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.statemachine.event.OnStateEntryEvent;
import org.springframework.statemachine.event.OnStateExitEvent;
import org.springframework.statemachine.event.OnTransitionEvent;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.stereotype.Component;

@Component
class StateMachineEventListener implements ApplicationListener<StateMachineEvent> {

    private final static Log log = LogFactory.getLog(StateMachineEventListener.class);

    @Override
    public void onApplicationEvent(StateMachineEvent stateMachineEvent) {
        if (stateMachineEvent instanceof OnStateEntryEvent event) {
            log.info("Entry state " + event.getState().getId());
        } else if (stateMachineEvent instanceof OnStateExitEvent event) {
            log.info("Exit state " + event.getState().getId());
        } else if (stateMachineEvent instanceof OnTransitionEvent event) {
            if (event.getTransition().getKind() == TransitionKind.INTERNAL) {
                log.info("Internal transition source=" + event.getTransition().getSource().getId());
            }
        }
    }

}