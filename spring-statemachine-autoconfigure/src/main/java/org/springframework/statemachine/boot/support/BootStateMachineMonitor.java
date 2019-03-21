/*
 * Copyright 2016-2018 the original author or authors.
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
import java.util.concurrent.TimeUnit;

import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.boot.actuate.StateMachineTraceRepository;
import org.springframework.statemachine.monitor.AbstractStateMachineMonitor;
import org.springframework.statemachine.monitor.StateMachineMonitor;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.ObjectUtils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

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

	private final StateMachineTraceRepository traceRepository;
	private final MeterRegistry meterRegistry;

	/**
	 * Instantiates a new boot state machine monitor.
	 *
	 * @param meterRegistry the meter registry
	 * @param stateMachineTraceRepository the statemachine trace repository
	 */
	public BootStateMachineMonitor(MeterRegistry meterRegistry,
			StateMachineTraceRepository stateMachineTraceRepository) {
		this.meterRegistry = meterRegistry;
		this.traceRepository = stateMachineTraceRepository;
	}

	@Override
	public void transition(StateMachine<S, E> stateMachine, Transition<S, E> transition, long duration) {
		getTransitionCounterBuilder(transition).register(meterRegistry).increment();
		getTransitionTimerBuilder(transition).register(meterRegistry).record(duration, TimeUnit.MILLISECONDS);
		Map<String, Object> traceInfo = new HashMap<>();
		traceInfo.put("transition", transitionToName(transition));
		traceInfo.put("duration", duration);
		traceInfo.put("machine", stateMachine.getId());
		traceRepository.add(traceInfo);
	}

	@Override
	public void action(StateMachine<S, E> stateMachine, Action<S, E> action, long duration) {
		String actionName = actionToName(action);
		getActionCounterBuilder(action).register(meterRegistry).increment();
		getActionTimerBuilder(action).register(meterRegistry).record(duration, TimeUnit.MILLISECONDS);
		Map<String, Object> traceInfo = new HashMap<>();
		traceInfo.put("action", actionName);
		traceInfo.put("duration", duration);
		traceInfo.put("machine", stateMachine.getId());
		traceRepository.add(traceInfo);
	}

	private Counter.Builder getTransitionCounterBuilder(Transition<S, E> transition) {
		String transitionName = transitionToName(transition);
		Counter.Builder builder = Counter.builder("ssm.transition.transit")
				.tags("transitionName", transitionName)
				.description("Counter of Transition");
		return builder;
	}

	private Timer.Builder getTransitionTimerBuilder(Transition<S, E> transition) {
		String transitionName = transitionToName(transition);
		Timer.Builder builder = Timer.builder("ssm.transition.duration")
				.tags("transitionName", transitionName)
				.description("Timer of Transition");
		builder.publishPercentileHistogram();
		return builder;
	}

	private Counter.Builder getActionCounterBuilder(Action<S, E> action) {
		String actionName = actionToName(action);
		Counter.Builder builder = Counter.builder("ssm.action.execute")
				.tags("actionName", actionName)
				.description("Counter of Action");
		return builder;
	}

	private Timer.Builder getActionTimerBuilder(Action<S, E> action) {
		String actionName = actionToName(action);
		Timer.Builder builder = Timer.builder("ssm.action.duration")
				.tags("actionName", actionName)
				.description("Timer of Action");
		builder.publishPercentileHistogram();
		return builder;
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
