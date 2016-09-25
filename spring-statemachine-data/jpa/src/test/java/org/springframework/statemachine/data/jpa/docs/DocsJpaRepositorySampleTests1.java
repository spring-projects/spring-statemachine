/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.data.jpa.docs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.jpa.JpaRepositoryState;
import org.springframework.statemachine.data.jpa.JpaRepositoryTransition;

public class DocsJpaRepositorySampleTests1 {

	public static class Config1 {
// tag::snippetA[]
		@Autowired
		StateRepository<JpaRepositoryState> stateRepository;

		@Autowired
		TransitionRepository<JpaRepositoryTransition> transitionRepository;

		void addConfig() {
			JpaRepositoryState state1 = new JpaRepositoryState("machine1", "S1", true);
			stateRepository.save(state1);
			JpaRepositoryState state2 = new JpaRepositoryState("machine2", "S2", false);
			stateRepository.save(state2);

			JpaRepositoryTransition transition1 = new JpaRepositoryTransition("machine1", "S1", "S2", "E1");
			JpaRepositoryTransition transition2 = new JpaRepositoryTransition("machine2", "S3", "S4", "E2");
			transitionRepository.save(transition1);
			transitionRepository.save(transition2);
		}
// end::snippetA[]
	}
}
