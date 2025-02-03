/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.statemachine.plantuml.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.plantuml.PlantUmlWriterParameters;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.support.AbstractStateMachine;
import org.springframework.statemachine.transition.Transition;
import org.springframework.statemachine.trigger.EventTrigger;
import org.springframework.statemachine.trigger.TimerTrigger;

import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationWords;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionHelper {

	private static final Log log = LogFactory.getLog(TransactionHelper.class);

	public static <S, E> String getTransitionDescription(
			@Nullable Transition<S, E> transition,
			PlantUmlWriterParameters<S> plantUmlWriterParameters
	) {
		if (transition == null) {
			return "";
		}

		return getTransitionDescription(
				getTransitionDescriptionEvent(transition),
				getTransitionDescriptionGuard(transition),
				getTransitionDescriptionActions(transition),
				plantUmlWriterParameters.getTransitionLabelSeparator(),
				label -> plantUmlWriterParameters.decorateLabel(
						transition.getSource() == null ? null : transition.getSource().getId(),
						transition.getTarget().getId(),
						label
				));
	}

	private static <S, E> String getTransitionDescriptionEvent(Transition<S, E> transition) {
		String event = null;
		if (transition.getTrigger() != null && transition.getTrigger().getEvent() != null) {
			event = transition.getTrigger().getEvent().toString();
		}

		if (transition.getTrigger() instanceof EventTrigger<S, E> eventTrigger) {
			if (eventTrigger.getEvent() != null) {
				event = eventTrigger.getEvent().toString();
			}
		} else if (transition.getTrigger() instanceof TimerTrigger<S, E> timerTrigger) {
			if (timerTrigger.getCount() == 0) {
				event = "every " +
						formatDurationWords(timerTrigger.getPeriod(), true, true);
			} else {
				event = "after "
						+ formatDurationWords(timerTrigger.getPeriod(), true, true);

				if (timerTrigger.getCount() > 1) {
					event += " ( " + timerTrigger.getCount() + "x )";
				}
			}
		}
		return event;
	}

	private static <S, E> String getTransitionDescriptionGuard(Transition<S, E> transition) {
		if (transition.getGuard() != null) {
			return "[" + NameGetter.getName(transition.getGuard()) + "]";
		} else {
			return null;
		}
	}

	private static <S, E> String getTransitionDescriptionActions(Transition<S, E> transition) {
		if (transition.getActions() != null && !transition.getActions().isEmpty()) {
			return "/ " + transition.getActions().stream()
					.map(NameGetter::getName)
					.collect(Collectors.joining(", "));
		} else {
			return null;
		}
	}

	private static String getTransitionDescription(
			@Nullable String event,
			@Nullable String guard,
			@Nullable String actions,
			String labelSeparator,
			UnaryOperator<String> labelDecorator
	) {
		// create guardAndAction based on guard and transaction's actions
		String guardAndAction = null;
		if (guard != null) {
			if (actions != null) {
				guardAndAction = guard + labelSeparator + actions;
			} else {
				guardAndAction = guard;
			}
		} else {
			if (actions != null) {
				guardAndAction = actions;
			}
		}

		// finally, create transition description
		if (event != null) {
			if (guardAndAction != null) {
				return ": " + labelDecorator.apply(event + labelSeparator + guardAndAction);
			} else {
				return ": " + labelDecorator.apply(event);
			}
		} else {
			if (guardAndAction != null) {
				return ": " + labelDecorator.apply(guardAndAction);
			} else {
				return "";
			}
		}
	}

	@Nullable
	public static <S, E> Transition<S, E> getInitialTransition(Region<S, E> region) {
		if (region instanceof AbstractStateMachine<?, ?>) {
			try {
				return ((Transition<S, E>) FieldUtils.readField(region, "initialTransition", true));
			} catch (IllegalAccessException ex) {
				log.error("error while getting 'initialTransition'", ex);
			}
		}
		return null;
	}

}
