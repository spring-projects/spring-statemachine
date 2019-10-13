package org.springframework.statemachine.cluster;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.lock.LockInterceptor;
import org.springframework.statemachine.lock.LockService;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class LockInterceptorTest {

    @Test
    public void postStateChangeTest(){
        LockService<String, String> service = mock(LockService.class);
        LockInterceptor<String, String> interceptor = new LockInterceptor<>(service, 120);
        StateMachine<String, String> stateMachine = mock(StateMachine.class);
        State<String, String> state = mock(State.class);
        Message<String> message = mock(Message.class);
        Transition<String, String> transition = mock(Transition.class);

        interceptor.postStateChange(state, message, transition, stateMachine);

        verify(service, times(1)).unLock(stateMachine);
    }

    @Test
    public void preEventTestLockAcquired(){
        LockService<String, String> service = mock(LockService.class);
        LockInterceptor<String, String> interceptor = new LockInterceptor<>(service, 120);
        Message<String> message = mock(Message.class);
        StateMachine<String, String> stateMachine = mock(StateMachine.class);

        when(service.lock(stateMachine, 120)).thenReturn(true);

        Message<String> result = interceptor.preEvent(message, stateMachine);
        assertThat(result).isEqualTo(message);

        verify(service, times(1)).lock(stateMachine, 120);
    }

    @Test
    public void preEventTestAlreadyLocked(){
        LockService<String, String> service = mock(LockService.class);
        LockInterceptor<String, String> interceptor = new LockInterceptor<>(service, 120);
        Message<String> message = mock(Message.class);
        StateMachine<String, String> stateMachine = mock(StateMachine.class);

        when(service.lock(stateMachine, 120)).thenReturn(false);

        Message<String> result = interceptor.preEvent(message, stateMachine);
        assertThat(result).isNull();

        verify(service, times(1)).lock(stateMachine, 120);
    }

}
