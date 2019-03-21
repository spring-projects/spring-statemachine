/*
 * Copyright 2015-2018 the original author or authors.
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
package org.springframework.statemachine.support;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccessor;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.ActionListener;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.security.SecurityRule;
import org.springframework.statemachine.state.EnumState;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.statemachine.trigger.Trigger;

public class StateContextExpressionMethodsTests {

	@Test
	public void testGuardBooleanExpressions() {
		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
		StateContextExpressionMethods methods = new StateContextExpressionMethods(evaluationContext);
		StateContext<SpelStates, SpelEvents> stateContext = mockStateContext(null);

		assertThat(methods.getValue(parser.parseExpression("true"), stateContext, Boolean.class), is(true));
		assertThat(methods.getValue(parser.parseExpression("event.toString().equals('E1')"), stateContext, Boolean.class), is(true));
		assertThat(methods.getValue(parser.parseExpression("event==T(org.springframework.statemachine.support.StateContextExpressionMethodsTests.SpelEvents).E1"), stateContext, Boolean.class), is(true));
		assertThat(methods.getValue(parser.parseExpression("getExtendedState().getVariables().get('boolean1')"), stateContext, Boolean.class), is(true));
		assertThat(methods.getValue(parser.parseExpression("extendedState.variables.get('boolean1')"), stateContext, Boolean.class), is(true));
		assertThat(methods.getValue(parser.parseExpression("extendedState.variables.get('boolean1')&&!extendedState.variables.get('boolean2')"), stateContext, Boolean.class), is(true));
		assertThat(methods.getValue(parser.parseExpression("extendedState.variables.get('boolean3')==NULL"), stateContext, Boolean.class), is(true));
		assertThat(methods.getValue(parser.parseExpression("transition.source.id.toString().equals('S1')"), stateContext, Boolean.class), is(true));
	}

	@Test
	public void testSendEvent() {
		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
		StateContextExpressionMethods methods = new StateContextExpressionMethods(evaluationContext);
		MockStatemachine stateMachine = new MockStatemachine();
		StateContext<SpelStates, SpelEvents> stateContext = mockStateContext(stateMachine);

		assertThat(methods.getValue(parser.parseExpression("stateMachine.sendEvent(T(org.springframework.statemachine.support.StateContextExpressionMethodsTests.SpelEvents).E1)"), stateContext, Boolean.class), is(true));
		assertThat(stateMachine.events.size(), is(1));
	}

	enum SpelStates {
		SI,S1,S2,S3,S4,SF,SH
	}

	public enum SpelEvents {
		E1,E2,E3,E4,EF
	}

	private StateContext<SpelStates, SpelEvents> mockStateContext(StateMachine<SpelStates, SpelEvents> stateMachine) {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("foo", "bar");
		MessageHeaders messageHeaders = new MessageHeaders(headers);
		ExtendedState extendedState = new DefaultExtendedState();
		extendedState.getVariables().put("key1", "val1");
		extendedState.getVariables().put("boolean1", true);
		extendedState.getVariables().put("boolean2", false);
		StateContext<SpelStates, SpelEvents> stateContext = new DefaultStateContext<SpelStates, SpelEvents>(
				null, MessageBuilder.withPayload(SpelEvents.E1).build(), messageHeaders, extendedState, new MockTransition(), stateMachine, null, null, null);
		return stateContext;
	}

	private static class MockTransition implements Transition<SpelStates, SpelEvents> {

		@Override
		public boolean transit(StateContext<SpelStates, SpelEvents> context) {
			return false;
		}

		@Override
		public void executeTransitionActions(StateContext<SpelStates, SpelEvents> context) {
		}

		@Override
		public State<SpelStates, SpelEvents> getSource() {
			return new EnumState<SpelStates, SpelEvents>(SpelStates.S1);
		}

		@Override
		public State<SpelStates, SpelEvents> getTarget() {
			return new EnumState<SpelStates, SpelEvents>(SpelStates.S2);
		}

		@Override
		public Guard<SpelStates, SpelEvents> getGuard() {
			return null;
		}

		@Override
		public Collection<Action<SpelStates, SpelEvents>> getActions() {
			return null;
		}

		@Override
		public Trigger<SpelStates, SpelEvents> getTrigger() {
			return null;
		}

		@Override
		public TransitionKind getKind() {
			return null;
		}

		@Override
		public SecurityRule getSecurityRule() {
			return null;
		}

		@Override
		public void addActionListener(ActionListener<SpelStates, SpelEvents> listener) {
		}

		@Override
		public void removeActionListener(ActionListener<SpelStates, SpelEvents> listener) {
		}
	}

	private static class MockStatemachine implements StateMachine<SpelStates, SpelEvents> {

		ArrayList<Message<SpelEvents>> events = new ArrayList<Message<SpelEvents>>();

		@Override
		public StateMachineAccessor<SpelStates, SpelEvents> getStateMachineAccessor() {
			return null;
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}

		@Override
		public boolean sendEvent(Message<SpelEvents> event) {
			events.add(event);
			return true;
		}

		@Override
		public boolean sendEvent(SpelEvents event) {
			return sendEvent(MessageBuilder.createMessage(event, new MessageHeaders(new HashMap<String, Object>())));
		}

		@Override
		public State<SpelStates, SpelEvents> getState() {
			return null;
		}

		@Override
		public Collection<State<SpelStates, SpelEvents>> getStates() {
			return null;
		}

		@Override
		public Collection<Transition<SpelStates, SpelEvents>> getTransitions() {
			return null;
		}

		@Override
		public boolean isComplete() {
			return false;
		}

		@Override
		public void setStateMachineError(Exception exception) {
		}

		@Override
		public boolean hasStateMachineError() {
			return false;
		}

		@Override
		public void addStateListener(StateMachineListener<SpelStates, SpelEvents> listener) {
		}

		@Override
		public void removeStateListener(StateMachineListener<SpelStates, SpelEvents> listener) {
		}

		@Override
		public State<SpelStates, SpelEvents> getInitialState() {
			return null;
		}

		@Override
		public ExtendedState getExtendedState() {
			return null;
		}

		@Override
		public UUID getUuid() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

	}

}
