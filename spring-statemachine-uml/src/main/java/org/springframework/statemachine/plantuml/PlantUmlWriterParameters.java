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

package org.springframework.statemachine.plantuml;

import lombok.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class allows to tweak / fine-tune PlantUml StateDiagram visualisation
 *
 * @param <S>
 */
public class PlantUmlWriterParameters<S> {

	private static final Log log = LogFactory.getLog(PlantUmlWriterParameters.class);

	public static final String DEFAULT_STATE_DIAGRAM_SETTINGS = """
            'https://plantuml.com/state-diagram
            
            'hide description area for state without description
            hide empty description
            
            'https://plantuml.com/fr/skinparam
            'https://plantuml-documentation.readthedocs.io/en/latest/formatting/all-skin-params.html
            'https://plantuml.com/fr/color
            skinparam BackgroundColor white
            skinparam DefaultFontColor black
            'skinparam DefaultFontName Impact
            skinparam DefaultFontSize 14
            skinparam DefaultFontStyle Normal
            skinparam NoteBackgroundColor #FEFFDD
            skinparam NoteBorderColor black
            
            skinparam state {
              ArrowColor black
              BackgroundColor #F1F1F1
              BorderColor #181818
              FontColor black
            '  FontName Impact
              FontSize 14
              FontStyle Normal
            }
            """;

	@Setter
	private String stateDiagramSettings = DEFAULT_STATE_DIAGRAM_SETTINGS;

	public static String getStateDiagramSettings(@Nullable PlantUmlWriterParameters<?> plantUmlWriterParameters) {
		return plantUmlWriterParameters == null
				? DEFAULT_STATE_DIAGRAM_SETTINGS
				: plantUmlWriterParameters.getStateDiagramSettings();
	}

	private String getStateDiagramSettings() {
		return stateDiagramSettings == null
				? ""
				: stateDiagramSettings;
	}

	/**
	 * Direction of an arrow connecting 2 States
	 */
	public enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT
	}


	@Value
	@EqualsAndHashCode
	private static class Connection<S> implements Comparable<Connection<S>> {
		S source;
		S target;

		@Override
		public int compareTo(Connection<S> o) {
			int sourceComparisonResult = source.toString().compareTo(o.source.toString());
			return sourceComparisonResult == 0
					? target.toString().compareTo(o.target.toString())
					: sourceComparisonResult;
		}
	}

	/**
	 * Map of ( (sourceSate, targetState) -> Direction )
	 */
	private final Map<Connection<S>, Direction> arrows = new HashMap<>();

	public PlantUmlWriterParameters<S> arrowDirection(S source, Direction direction, S target) {
		arrows.put(new Connection<>(source, target), direction);
		return this;
	}

	/**
	 * At least one of source and target must be non-null<BR/>
	 * See implementation for 'arrow direction rule priority'
	 *
	 * @param source source State
	 * @param target target State
	 * @return Direction.name()
	 */
	String getDirection(S source, S target) {
		if (source == null && target == null) {
			throw new IllegalArgumentException("source and target state cannot both be null!");
		}
		Connection<S> sourceAndTarget = new Connection<>(source, target);
		if (arrows.containsKey(sourceAndTarget)) {
			return arrows.get(sourceAndTarget).name().toLowerCase();
		}
		Connection<S> sourceOnly = new Connection<>(source, null);
		Connection<S> targetOnly = new Connection<>(null, target);
		if (arrows.containsKey(sourceOnly)
				&& arrows.containsKey(targetOnly)
				&& !arrows.get(sourceOnly).equals(arrows.get(targetOnly))
		) {
			log.warn(String.format(
					"Two 'unary' 'arrowDirection' rules found for (%s, %s) with DIFFERENT values! Using 'target' rule!",
					source, target
			));
			return arrows.get(targetOnly).name().toLowerCase();
		} else if (arrows.containsKey(sourceOnly)) {
			return arrows.get(sourceOnly).name().toLowerCase();
		} else if (arrows.containsKey(targetOnly)) {
			return arrows.get(targetOnly).name().toLowerCase();
		} else {
			return Direction.DOWN.name().toLowerCase();
		}
	}

	/**
	 * Map of ( Connection(sourceSate, targetState) -> Direction )
	 * Used to add EXTRA HIDDEN arrows, which are just helping with diagram layout.<BR/>
	 * This is typically useful to 'force' the position of a state comparing to another, EVEN IF THESE TWO STATES AR NOT CONNECTED in the statemachine :-)<BR/>
	 * <p>
	 * exemple:
	 * S1 -left[hidden]-> S2
	 */
	private final Map<Connection<S>, Direction> additionalHiddenTransitions = new HashMap<>();

	/**
	 * Add EXTRA HIDDEN transitions to align states WIHTOUT connecting them.<BR/>
	 * These transitions are NOT part of the state machine!
	 */
	public PlantUmlWriterParameters<S> addAdditionalHiddenTransition(S source, Direction direction, S target) {
		additionalHiddenTransitions.put(new Connection<>(source, target), direction);
		return this;
	}

	public String getAdditionalHiddenTransitions() {
		String hiddenTransitionsText = additionalHiddenTransitions.entrySet().stream()
				.map(hiddenTransition -> "%s -%s[hidden]-> %s"
						.formatted(
								hiddenTransition.getKey().getSource(),
								hiddenTransition.getValue().name().toLowerCase(),
								hiddenTransition.getKey().getTarget()
						))
				.collect(Collectors.joining("\n"));

		return hiddenTransitionsText.isEmpty()
				? ""
				: "\n" + hiddenTransitionsText + "\n";
	}

	// ----------

	/**
	 * Array of Connection(sourceSate, targetState) used to IGNORE EXISTING transitions.<BR/>
	 * The goal is to give the ability to IGNORE some transitions on big state machine diagram to make it clearer.
	 * ( So, this is DIFFERENT from 'additionalHiddenTransitions', which is used to add extra 'hidden' / 'fake' transitions )<BR/>
	 */
	private final TreeSet<Connection<S>> ignoredTransitions = new TreeSet<Connection<S>>();

	/**
	 * IGNORE a transition<BR/>
	 * Transition (source -> destination) will NOT be present in PlantUML diagram
	 */
	public PlantUmlWriterParameters<S> ignoreTransition(S source, S target) {
		ignoredTransitions.add(new Connection<>(source, target));
		return this;
	}

	public boolean isTransitionIgnored(S source, S target) {
		// if source is null, we always show the transition (initial state ?)
		return source != null && ignoredTransitions.contains(new Connection<>(source, target));
	}

	@Builder
	@Getter
	@EqualsAndHashCode
	static class LabelDecorator {
		private String prefix = "";
		private String suffix = "";

		String decorate(String label) {
			return prefix + label + suffix;
		}
	}

	/**
	 * Map of ( Connection(sourceSate, targetState) -> LabelDecorator(labelPrefix, labelSuffix) )
	 */
	private final Map<Connection<S>, LabelDecorator> arrowLabelDecorator = new HashMap<>();

	public PlantUmlWriterParameters<S> arrowLabelDecorator(S source, S target, String prefix, String suffix) {
		arrowLabelDecorator.put(
				new Connection<>(source, target),
				LabelDecorator.builder().prefix(prefix).suffix(suffix).build()
		);
		return this;
	}

	public String decorateLabel(
			@Nullable S source,
			@Nullable S target,
			@Nullable String transitionLabel
	) {
		if (transitionLabel == null) {
			return null;
		}

		if (source == null && target == null) {
			throw new IllegalArgumentException("source and target state cannot both be null!");
		}
		Connection<S> sourceAndTarget = new Connection<>(source, target);
		if (arrowLabelDecorator.containsKey(sourceAndTarget)) {
			return arrowLabelDecorator.get(sourceAndTarget).decorate(transitionLabel);
		}
		Connection<S> sourceOnly = new Connection<>(source, null);
		Connection<S> targetOnly = new Connection<>(null, target);
		if (arrowLabelDecorator.containsKey(sourceOnly)
				&& arrowLabelDecorator.containsKey(targetOnly)
				&& !arrowLabelDecorator.get(sourceOnly).equals(arrowLabelDecorator.get(targetOnly))
		) {
			log.warn(String.format(
					"Two 'unary' 'arrowLabelDecorator' rules found for (%s, %s) with DIFFERENT values! Using 'target' rule!",
					source, target
			));
			return arrowLabelDecorator.get(targetOnly).decorate(transitionLabel);
		} else if (arrowLabelDecorator.containsKey(sourceOnly)) {
			return arrowLabelDecorator.get(sourceOnly).decorate(transitionLabel);
		} else if (arrowLabelDecorator.containsKey(targetOnly)) {
			return arrowLabelDecorator.get(targetOnly).decorate(transitionLabel);
		} else {
			return transitionLabel;
		}
	}

	// Colors

	private String defaultStateColor = "";
	private String currentStateColor = "#FFFF77";

	@Getter
	private String transitionLabelSeparator = "\\n";

	public PlantUmlWriterParameters<S> setTransitionLabelSeparator(String transitionLabelSeparator) {
		this.transitionLabelSeparator = transitionLabelSeparator;
		return this;
	}

	public PlantUmlWriterParameters<S> defaultStateColor(String defaultStateColor) {
		this.defaultStateColor = defaultStateColor;
		return this;
	}

	public PlantUmlWriterParameters<S> currentStateColor(String currentStateColor) {
		this.currentStateColor = currentStateColor;
		return this;
	}

	public String getStateColor(S state, @Nullable S currentState) {
		if (state == null) {
			log.warn("null state!");
			return ""; // TODO check why this is happening
		}
		return state.equals(currentState) ? currentStateColor : defaultStateColor;
	}

	// FIXME [#FF0000] should not be hardcoded here!
	public String getArrowColor(boolean isCurrentTransaction) {
		return isCurrentTransaction ? "[#FF0000]" : "";
	}

}
