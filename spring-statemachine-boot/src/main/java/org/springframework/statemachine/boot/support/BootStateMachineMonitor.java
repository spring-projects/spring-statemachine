/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.boot.support;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.monitor.AbstractStateMachineMonitor;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.ObjectUtils;

/**
 * Implementation of a {@link StateMachineMonitor} which converts monitoring
 * events and bridges those into supported format handled by Spring Boot's
 * tracing and metrics frameworks.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class BootStateMachineMonitor<S, E> extends AbstractStateMachineMonitor<S, E> {

	private final String METRIC_TRANSITION_BASE = "ssm.transition";
	private final String METRIC_ACTION_BASE = "ssm.action";
	private final CounterService counterService;
	private final GaugeService gaugeService;
	private final TraceRepository traceRepository;

	/**
	 * Instantiates a new boot state machine monitor.
	 *
	 * @param counterService the counter service
	 * @param gaugeService the gauge service
	 * @param traceRepository the trace repository
	 */
	public BootStateMachineMonitor(CounterService counterService, GaugeService gaugeService,
			TraceRepository traceRepository) {
		this.counterService = counterService;
		this.gaugeService = gaugeService;
		this.traceRepository = traceRepository;
	}

	@Override
	public void transition(StateMachine<S, E> stateMachine, Transition<S, E> transition, long duration) {
		String transitionName = transitionToName(transition);
		this.counterService.increment(METRIC_TRANSITION_BASE + "." + transitionName + ".transit");
		this.gaugeService.submit(METRIC_TRANSITION_BASE + "." + transitionName + ".duration", duration);
		Map<String, Object> traceInfo = new HashMap<>();
		traceInfo.put("transition", transitionToName(transition));
		traceInfo.put("duration", duration);
		traceInfo.put("machine", stateMachine.getId());
		traceRepository.add(traceInfo);
	}

	@Override
	public void action(StateMachine<S, E> stateMachine, Action<S, E> action, long duration) {
		String actionName = actionToName(action);
		this.counterService.increment(METRIC_ACTION_BASE + "." + actionName + ".execute");
		this.gaugeService.submit(METRIC_ACTION_BASE + "." + actionName + ".duration", duration);
		Map<String, Object> traceInfo = new HashMap<>();
		traceInfo.put("action", actionName);
		traceInfo.put("duration", duration);
		traceInfo.put("machine", stateMachine.getId());
		traceRepository.add(traceInfo);
	}

	private static <S, E> String transitionToName(Transition<S, E> transition) {
		String sourceId = nullStateId(transition.getSource());
		String targetId = nullStateId(transition.getTarget());
		StringBuilder buf = new StringBuilder();
		buf.append(transition.getKind());
		if (sourceId != null) {
			buf.append("_");
			buf.append(sourceId);
		}
		if (targetId != null) {
			buf.append("_");
			buf.append(targetId);
		}
		return buf.toString();
	}

	private static <S, E> String actionToName(Action<S, E> action) {
		return ObjectUtils.getDisplayString(action);
	}

	private static <S, E> String nullStateId(State<S, E> state) {
		if (state == null) {
			return null;
		}
		S id = state.getId();
		return id != null ? id.toString() : null;
	}
}
