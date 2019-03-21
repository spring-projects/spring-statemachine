/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Default implementation of {@link StateMachineEventPublisher}.
 *
 * @author Janne Valkealahti
 *
 */
public class DefaultStateMachineEventPublisher implements StateMachineEventPublisher, ApplicationEventPublisherAware {

	private ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Instantiates a new state machine event publisher.
	 */
	public DefaultStateMachineEventPublisher() {
	}

	/**
	 * Instantiates a new state machine event publisher.
	 *
	 * @param applicationEventPublisher the application event publisher
	 */
	public DefaultStateMachineEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void publishStateChanged(Object source, State<?, ?> sourceState, State<?, ?> targetState) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnStateChangedEvent(source, sourceState, targetState));
		}
	}

	@Override
	public void publishStateEntered(Object source, State<?, ?> state) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnStateEntryEvent(source, state));
		}
	}

	@Override
	public void publishStateExited(Object source, State<?, ?> state) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnStateExitEvent(source, state));
		}
	}

	@Override
	public void publishEventNotAccepted(Object source, Message<?> event) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnEventNotAcceptedEvent(source, event));
		}
	}

	@Override
	public void publishTransitionStart(Object source, Transition<?, ?> transition) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnTransitionStartEvent(source, transition));
		}
	}

	@Override
	public void publishTransitionEnd(Object source, Transition<?, ?> transition) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnTransitionEndEvent(source, transition));
		}
	}

	@Override
	public void publishTransition(Object source, Transition<?, ?> transition) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnTransitionEvent(source, transition));
		}
	}

	@Override
	public void publishStateMachineStart(Object source, StateMachine<?, ?> stateMachine) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnStateMachineStart(source, stateMachine));
		}
	}

	@Override
	public void publishStateMachineStop(Object source, StateMachine<?, ?> stateMachine) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnStateMachineStop(source, stateMachine));
		}
	}

	@Override
	public void publishStateMachineError(Object source, StateMachine<?, ?> stateMachine, Exception exception) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnStateMachineError(source, stateMachine, exception));
		}
	}

	@Override
	public void publishExtendedStateChanged(Object source, Object key, Object value) {
		if (applicationEventPublisher != null) {
			applicationEventPublisher.publishEvent(new OnExtendedStateChanged(source, key, value));
		}
	}

}
