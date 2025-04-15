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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.State;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class RegionComparator<S, E> implements Comparator<Region<S, E>> {

	private static final Log log = LogFactory.getLog(RegionComparator.class);

	private final StateComparator<S,E> stateComparator;

	public RegionComparator(StateComparator<S, E> stateComparator) {
		this.stateComparator = stateComparator;
	}

	@Override
	public int compare(Region<S, E> region1, Region<S, E> region2) {
		List<State<S, E>> sourceSortedStates = region1.getStates().stream().sorted(stateComparator).toList();
		List<State<S, E>> targetSortedStates = region2.getStates().stream().sorted(stateComparator).toList();

		List<Integer> regionComparisonResult = IntStream
				.range(0, Math.min(sourceSortedStates.size(), targetSortedStates.size()))
				// comparing pairs of states
				.mapToObj(i -> sourceSortedStates.get(i).getId().toString().compareTo(targetSortedStates.get(i).getId().toString()))
				.toList();

		// returning first "non 0" comparison result
		for (Integer comparisonResult : regionComparisonResult) {
			if (comparisonResult != 0) {
				return comparisonResult;
			}
		}

		// this should not happen...?
		log.warn("getRegionComparator: unable to compare regions!!");
		return 0;
	}
}
