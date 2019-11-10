package org.springframework.statemachine.lock;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.lock.LockService;
import org.springframework.statemachine.lock.LockStateMachineGuard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LockStateMachineGuardTest {

    @Test
    public void testEvaluate(){
        LockService service = mock(LockService.class);
        StateContext stateContext = mock(StateContext.class);
        LockStateMachineGuard lockStateMachineGuard = new LockStateMachineGuard(service, 120);
        StateMachine stateMachine = mock(StateMachine.class);

        when(stateContext.getStateMachine()).thenReturn(stateMachine);
        when(service.lock(stateMachine, 120)).thenReturn(true);

        boolean result = lockStateMachineGuard.evaluate(stateContext);
        assertThat(result).isTrue();

        verify(service, times(1)).lock(stateMachine, 120);
    }

    @Test
    public void testEvaluateFails(){
        LockService service = mock(LockService.class);
        StateContext stateContext = mock(StateContext.class);
        LockStateMachineGuard lockStateMachineGuard = new LockStateMachineGuard(service, 120);
        StateMachine stateMachine = mock(StateMachine.class);

        when(stateContext.getStateMachine()).thenReturn(stateMachine);
        when(service.lock(stateMachine, 120)).thenReturn(true);

        boolean result = lockStateMachineGuard.evaluate(stateContext);
        assertThat(result).isTrue();

        verify(service, times(1)).lock(stateMachine, 120);

    }
}
