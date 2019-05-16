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
package org.springframework.statemachine.recipes.persist;

import java.util.Iterator;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.LifecycleObjectSupport;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.Assert;

/**
 * {@code PersistStateMachineHandler} is a recipe which can be used to
 * handle a state change of an arbitrary entity in a persistent storage.
 *
 * @author Janne Valkealahti
 *
 */
public class PersistStateMachineHandler extends LifecycleObjectSupport {

	private final StateMachine<String, String> stateMachine;
	private final PersistingStateChangeInterceptor interceptor = new PersistingStateChangeInterceptor();
	private final CompositePersistStateChangeListener listeners = new CompositePersistStateChangeListener();

	/**
	 * Instantiates a new persist state machine handler.
	 *
	 * @param stateMachine the state machine
	 */
	public PersistStateMachineHandler(StateMachine<String, String> stateMachine) {
		Assert.notNull(stateMachine, "State machine must be set");
		this.stateMachine = stateMachine;
	}

	@Override
	protected void onInit() throws Exception {
		stateMachine.getStateMachineAccessor().doWithAllRegions(new StateMachineFunction<StateMachineAccess<String,String>>() {

			@Override
			public void apply(StateMachineAccess<String, String> function) {
				function.addStateMachineInterceptor(interceptor);
			}
		});
	}

	/**
	 * Handle event with entity.
	 *
	 * @param event the event
	 * @param state the state
	 * @return true if event was accepted
	 */
	public boolean handleEventWithState(Message<String> event, String state) {
		stateMachine.stop();
		List<StateMachineAccess<String, String>> withAllRegions = stateMachine.getStateMachineAccessor().withAllRegions();
		for (StateMachineAccess<String, String> a : withAllRegions) {
			a.resetStateMachine(new DefaultStateMachineContext<String, String>(state, null, null, null));
		}
		stateMachine.start();
		return stateMachine.sendEvent(event);
	}

	/**
	 * Adds the persist state change listener.
	 *
	 * @param listener the listener
	 */
	public void addPersistStateChangeListener(PersistStateChangeListener listener) {
		listeners.register(listener);
	}

	/**
	 * The listener interface for receiving persistStateChange events.
	 * The class that is interested in processing a persistStateChange
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addPersistStateChangeListener</code> method. When
	 * the persistStateChange event occurs, that object's appropriate
	 * method is invoked.
	 */
	public interface PersistStateChangeListener {

		/**
		 * Called when state needs to be persisted.
		 *
		 * @param state the state
		 * @param message the message
		 * @param transition the transition
		 * @param stateMachine the state machine
		 */
		void onPersist(State<String, String> state, Message<String> message, Transition<String, String> transition,
				StateMachine<String, String> stateMachine);
	}

	private class PersistingStateChangeInterceptor extends StateMachineInterceptorAdapter<String, String> {

		@Override
		public void preStateChange(State<String, String> state, Message<String> message,
				Transition<String, String> transition, StateMachine<String, String> stateMachine) {
			listeners.onPersist(state, message, transition, stateMachine);
		}
	}

	private class CompositePersistStateChangeListener extends AbstractCompositeListener<PersistStateChangeListener> implements
		PersistStateChangeListener {

		@Override
		public void onPersist(State<String, String> state, Message<String> message,
				Transition<String, String> transition, StateMachine<String, String> stateMachine) {
			for (Iterator<PersistStateChangeListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				PersistStateChangeListener listener = iterator.next();
				listener.onPersist(state, message, transition, stateMachine);
			}
		}
	}

}
