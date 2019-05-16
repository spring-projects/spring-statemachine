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
package org.springframework.statemachine.config.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Data object for transitions.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class TransitionsData<S, E> {

	private final Collection<TransitionData<S, E>> transitions;
	private final Map<S, List<ChoiceData<S, E>>> choices;
	private final Map<S, List<JunctionData<S, E>>> junctions;
	private final Map<S, List<S>> forks;
	private final Map<S, List<S>> joins;
	private final Collection<EntryData<S, E>> entrys;
	private final Collection<ExitData<S, E>> exits;
	private final Collection<HistoryData<S, E>> historys;

	/**
	 * Instantiates a new transitions data.
	 *
	 * @param transitionsData the transitions data
	 */
	public TransitionsData(Collection<TransitionData<S, E>> transitionsData) {
		this(transitionsData, null, null, null, null, null, null, null);
	}

	/**
	 * Instantiates a new state machine transitions.
	 *
	 * @param transitionsData the transitions data
	 * @param choices the choices
	 * @param junctions the junctions
	 * @param forks the forks
	 * @param joins the joins
	 * @param entrys the entrys
	 * @param exits the exits
	 * @param historys the historys
	 */
	public TransitionsData(Collection<TransitionData<S, E>> transitionsData, Map<S, List<ChoiceData<S, E>>> choices,
			Map<S, List<JunctionData<S, E>>> junctions, Map<S, List<S>> forks, Map<S, List<S>> joins, Collection<EntryData<S, E>> entrys,
			Collection<ExitData<S, E>> exits, Collection<HistoryData<S, E>> historys) {
		this.transitions = transitionsData;
		this.choices = choices;
		this.junctions = junctions;
		this.forks = forks;
		this.joins = joins;
		this.entrys = entrys;
		this.exits = exits;
		this.historys = historys;
	}

	/**
	 * Gets the transitions.
	 *
	 * @return the transitions
	 */
	public Collection<TransitionData<S, E>> getTransitions() {
		return transitions;
	}

	/**
	 * Gets the choices.
	 *
	 * @return the choices
	 */
	public Map<S, List<ChoiceData<S, E>>> getChoices() {
		return choices;
	}

	/**
	 * Gets the junctions.
	 *
	 * @return the junctions
	 */
	public Map<S, List<JunctionData<S, E>>> getJunctions() {
		return junctions;
	}

	/**
	 * Gets the forks.
	 *
	 * @return the forks
	 */
	public Map<S, List<S>> getForks() {
		return forks;
	}

	/**
	 * Gets the joins.
	 *
	 * @return the joins
	 */
	public Map<S, List<S>> getJoins() {
		return joins;
	}

	/**
	 * Gets the entrys.
	 *
	 * @return the entrys
	 */
	public Collection<EntryData<S, E>> getEntrys() {
		return entrys;
	}

	/**
	 * Gets the exits.
	 *
	 * @return the exits
	 */
	public Collection<ExitData<S, E>> getExits() {
		return exits;
	}

	/**
	 * Gets the historys.
	 *
	 * @return the historys
	 */
	public Collection<HistoryData<S, E>> getHistorys() {
		return historys;
	}
}
