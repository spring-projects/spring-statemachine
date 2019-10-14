package org.springframework.statemachine.cluster;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.lock.LockService;
import org.springframework.statemachine.lock.LockStateMachineGuard;
import org.springframework.statemachine.lock.LockStateMachineListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LockStateMachineListenerTest {

    @Test
    public void stateContextCorrectStage(){
        LockService service = mock(LockService.class);
        LockStateMachineListener lockStateMachineListener = new LockStateMachineListener(service);
        StateContext stateContext = mock(StateContext.class);
        StateMachine stateMachine = mock(StateMachine.class);

        when(stateContext.getStateMachine()).thenReturn(stateMachine);
        when(stateContext.getStage()).thenReturn(StateContext.Stage.TRANSITION_END);
        lockStateMachineListener.stateContext(stateContext);

        verify(service, times(1)).unLock(stateMachine);
    }

    @Test
    public void stateContextWrongState(){
        LockService service = mock(LockService.class);
        LockStateMachineListener lockStateMachineListener = new LockStateMachineListener(service);
        StateContext stateContext = mock(StateContext.class);
        StateMachine stateMachine = mock(StateMachine.class);

        when(stateContext.getStateMachine()).thenReturn(stateMachine);
        when(stateContext.getStage()).thenReturn(StateContext.Stage.TRANSITION);
        lockStateMachineListener.stateContext(stateContext);

        verify(service, never()).unLock(stateMachine);
    }
}
