/*
 * Copyright 2015-2016 the original author or authors.
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
package demo.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.ensemble.EnsembleListenerAdapter;
import org.springframework.statemachine.ensemble.StateMachineEnsemble;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import demo.web.StateMachineConfig.Events;
import demo.web.StateMachineConfig.States;

@Controller
public class StateMachineController {

	private final Log log = LogFactory.getLog(StateMachineController.class);

	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	private StateMachine<States, Events> stateMachine;

	@Autowired
	private StateMachineEnsemble<States, Events> stateMachineEnsemble;

	@PostConstruct
	public void setup() {

		stateMachine.addStateListener(new StateMachineListenerAdapter<States, Events>() {
			@Override
			public void stateEntered(State<States, Events> state) {
				StateMachineMessage message = new StateMachineMessage();
				message.setMessage("Enter state " + state.getId().toString());
				simpMessagingTemplate.convertAndSend("/topic/sm.message", message);
			}

			@Override
			public void stateExited(State<States, Events> state) {
				StateMachineMessage message = new StateMachineMessage();
				message.setMessage("Exit state " + state.getId().toString());
				simpMessagingTemplate.convertAndSend("/topic/sm.message", message);
			}

			@Override
			public void stateChanged(State<States, Events> from, State<States, Events> to) {
				Map<Object, Object> variables = stateMachine.getExtendedState().getVariables();
				ArrayList<StateMachineEvent> list = new ArrayList<StateMachineEvent>();
				for (States state : stateMachine.getState().getIds()) {
					list.add(new StateMachineEvent(state.toString()));
				}
				simpMessagingTemplate.convertAndSend("/topic/sm.states", list);
				simpMessagingTemplate.convertAndSend("/topic/sm.variables", variables);
			}

			@Override
			public void transitionEnded(Transition<States, Events> transition) {
				if (transition != null && transition.getKind() == TransitionKind.INTERNAL) {
					Map<Object, Object> variables = stateMachine.getExtendedState().getVariables();
					simpMessagingTemplate.convertAndSend("/topic/sm.variables", variables);
				}
			}

			@Override
			public void stateMachineError(StateMachine<States, Events> stateMachine, Exception exception) {
				handleStateMachineError(new StateMachineException("Received error from machine", exception));
			}

		});

		stateMachineEnsemble.addEnsembleListener(new EnsembleListenerAdapter<States, Events>() {

			@Override
			public void ensembleLeaderGranted(StateMachine<States, Events> stateMachine) {
				StateMachineMessage message = new StateMachineMessage();
				message.setMessage("Leader granted " + stateMachine.getUuid().toString());
				simpMessagingTemplate.convertAndSend("/topic/sm.message", message);
			}

			@Override
			public void ensembleLeaderRevoked(StateMachine<States, Events> stateMachine) {
				StateMachineMessage message = new StateMachineMessage();
				message.setMessage("Leader revoked " + stateMachine.getUuid().toString());
				simpMessagingTemplate.convertAndSend("/topic/sm.message", message);
			}
		});
	}

	@SubscribeMapping("/sm.uuid")
	public String retrieveUuid() {
		return stateMachine.getUuid().toString();
	}

	@SubscribeMapping("/sm.states")
	public Collection<StateMachineEvent> retrieveStates() {
		ArrayList<StateMachineEvent> list = new ArrayList<StateMachineEvent>();
		for (States state : stateMachine.getState().getIds()) {
			list.add(new StateMachineEvent(state.toString()));
		}
		return list;
	}

	@SubscribeMapping("/sm.variables")
	public Map<Object, Object> retrieveVariables() {
		return stateMachine.getExtendedState().getVariables();
	}

	@RequestMapping("/event")
	@ResponseStatus(HttpStatus.OK)
	public void sendEvent(@RequestParam(value = "id") Events id,
			@RequestParam(value = "testVariable", required = false) String testVariable) {
		log.info("Got request to send event " + id + " testVariable " + testVariable);
		Message<Events> message = MessageBuilder
				.withPayload(id)
				.setHeader("testVariable", testVariable)
				.build();
		stateMachine.sendEvent(message);
	}

	@RequestMapping("/join")
	@ResponseStatus(HttpStatus.OK)
	public void joinEnsemble() {
		stateMachineEnsemble.join(stateMachine);
	}

	@RequestMapping("/leave")
	@ResponseStatus(HttpStatus.OK)
	public void leaveEnsemble() {
		stateMachineEnsemble.leave(stateMachine);
	}

	@RequestMapping(value = "/states", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Collection<States> getStates() {
		return stateMachine.getState().getIds();
	}

	@RequestMapping(value = "/status", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Map<Object, Object> getStatus() {
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		map.put("hasStateMachineError", stateMachine.hasStateMachineError());
		map.put("isComplete", stateMachine.isComplete());
		map.put("extendedStateVariables", stateMachine.getExtendedState().getVariables());
		return map;
	}

	@MessageExceptionHandler
	@SendToUser(value = "/queue/errors", broadcast = false)
	public String handleStateMachineError(StateMachineException e) {
		return e.getMessage();
	}

}