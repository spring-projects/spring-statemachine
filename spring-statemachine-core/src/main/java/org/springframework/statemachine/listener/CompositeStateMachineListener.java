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
package org.springframework.statemachine.listener;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

/**
 * Default {@link StateMachineListener} dispatcher.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class CompositeStateMachineListener<S, E> extends AbstractCompositeListener<StateMachineListener<S, E>>
		implements StateMachineListener<S, E> {

	private final static Log log = LogFactory.getLog(CompositeStateMachineListener.class);

	@Override
	public void stateChanged(State<S, E> from, State<S, E> to) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.stateChanged(from, to);
			} catch (Throwable e) {
				log.warn("Error during stateChanged", e);
			}
		}
	}

	@Override
	public void stateEntered(State<S, E> state) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.stateEntered(state);
			} catch (Throwable e) {
				log.warn("Error during stateEntered", e);
			}
		}
	}

	@Override
	public void stateExited(State<S, E> state) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.stateExited(state);
			} catch (Throwable e) {
				log.warn("Error during stateExited", e);
			}
		}
	}

	@Override
	public void eventNotAccepted(Message<E> event) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.eventNotAccepted(event);
			} catch (Throwable e) {
				log.warn("Error during eventNotAccepted", e);
			}
		}
	}

	@Override
	public void transition(Transition<S, E> transition) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.transition(transition);
			} catch (Throwable e) {
				log.warn("Error during transition", e);
			}
		}
	}

	@Override
	public void transitionStarted(Transition<S, E> transition) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.transitionStarted(transition);
			} catch (Throwable e) {
				log.warn("Error during transitionStarted", e);
			}
		}
	}

	@Override
	public void transitionEnded(Transition<S, E> transition) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.transitionEnded(transition);
			} catch (Throwable e) {
				log.warn("Error during transitionEnded", e);
			}
		}
	}

	@Override
	public void stateMachineStarted(StateMachine<S, E> stateMachine) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.stateMachineStarted(stateMachine);
			} catch (Throwable e) {
				log.warn("Error during stateMachineStarted", e);
			}
		}
	}

	@Override
	public void stateMachineStopped(StateMachine<S, E> stateMachine) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.stateMachineStopped(stateMachine);
			} catch (Throwable e) {
				log.warn("Error during stateMachineStopped", e);
			}
		}
	}

	@Override
	public void stateMachineError(StateMachine<S, E> stateMachine, Exception exception) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.stateMachineError(stateMachine, exception);
			} catch (Throwable e) {
				log.warn("Error during stateMachineError", e);
			}
		}
	}

	@Override
	public void extendedStateChanged(Object key, Object value) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.extendedStateChanged(key, value);
			} catch (Throwable e) {
				log.warn("Error during extendedStateChanged", e);
			}
		}
	}

	@Override
	public void stateContext(StateContext<S, E> stateContext) {
		for (Iterator<StateMachineListener<S, E>> iterator = getListeners().reverse(); iterator.hasNext();) {
			StateMachineListener<S, E> listener = iterator.next();
			try {
				listener.stateContext(stateContext);
			} catch (Throwable e) {
				log.warn("Error during stateContext", e);
			}
		}
	}

}
