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

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.plantuml.transition.Arrow;
import org.springframework.statemachine.plantuml.transition.TransitionParameters;

import java.util.Map;
import java.util.TreeMap;

/**
 * This class allows to tweak / fine-tune PlantUml StateDiagram visualisation
 *
 * @param <S>
 */
public class PlantUmlWriterParameters<S> {

	private static final Log log = LogFactory.getLog(PlantUmlWriterParameters.class);

	// State Diagram Settings

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

	// States Notes

	private final Map<S, String> stateNotes = new TreeMap();

	public PlantUmlWriterParameters<S> note(S state, String note) {
		// escape line return in note as it must fit in one single line
		note = note
				.replace("\n", "\\n")
				.replace("\r", "\\r")
		;
		stateNotes.put(state, note);
		return this;
	}

	public String getNote(S state) {
		return stateNotes.get(state);
	}

	// TransitionParameters

	private final TransitionParameters<S> transitionParameters = new TransitionParameters<>();

	public PlantUmlWriterParameters<S> arrow(S source, Arrow.Direction direction, S target) {
		transitionParameters.arrow(source, direction, target);
		return this;
	}

	public PlantUmlWriterParameters<S> arrow(S source, Arrow.Direction direction, S target, int length) {
		transitionParameters.arrow(source, direction, target, length);
		return this;
	}

	String getDirection(S source, S target) {
		return transitionParameters.getDirection(source, target);
	}

	String getArrowLength(S source, S target) {
		return transitionParameters.getArrowLength(source, target);
	}

	/**
	 * Add EXTRA HIDDEN transitions to align states WITHOUT connecting them.<BR/>
	 * These transitions are NOT part of the state machine!
	 */
	public PlantUmlWriterParameters<S> addAdditionalHiddenTransition(S source, Arrow.Direction direction, S target) {
		transitionParameters.addAdditionalHiddenTransition(source, direction, target);
		return this;
	}

	public String getAdditionalHiddenTransitions() {
		return transitionParameters.getAdditionalHiddenTransitions();
	}

	/**
	 * IGNORE a transition<BR/>
	 * Transition (source -> destination) will NOT be present in PlantUML diagram
	 */
	public PlantUmlWriterParameters<S> ignoreTransition(S source, S target) {
		transitionParameters.ignoreTransition(source, target);
		return this;
	}

	public boolean isTransitionIgnored(S source, S target) {
		return transitionParameters.isTransitionIgnored(source, target);
	}

	public PlantUmlWriterParameters<S> arrowLabelDecorator(S source, S target, String prefix, String suffix) {
		transitionParameters.arrowLabelDecorator(source, target, prefix, suffix);
		return this;
	}

	public String decorateLabel(
			@Nullable S source,
			@Nullable S target,
			@Nullable String transitionLabel
	) {
		return transitionParameters.decorateLabel(source, target, transitionLabel);
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
