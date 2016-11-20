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
			JpaRepositoryState stateS1 = new JpaRepositoryState("S1", true);
			JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
			JpaRepositoryState stateS3 = new JpaRepositoryState("S3");

			stateRepository.save(stateS1);
			stateRepository.save(stateS2);
			stateRepository.save(stateS3);

			JpaRepositoryTransition transitionS1ToS2 = new JpaRepositoryTransition(stateS1, stateS2, "E1");
			JpaRepositoryTransition transitionS2ToS3 = new JpaRepositoryTransition(stateS2, stateS3, "E2");

			transitionRepository.save(transitionS1ToS2);
			transitionRepository.save(transitionS2ToS3);
		}
// end::snippetA[]
	}

	public static class Config2 {
// tag::snippetB[]
		@Autowired
		StateRepository<JpaRepositoryState> stateRepository;

		@Autowired
		TransitionRepository<JpaRepositoryTransition> transitionRepository;

		void addConfig() {
			JpaRepositoryState stateS1 = new JpaRepositoryState("S1", true);
			JpaRepositoryState stateS2 = new JpaRepositoryState("S2");
			JpaRepositoryState stateS3 = new JpaRepositoryState("S3");

			JpaRepositoryState stateS21 = new JpaRepositoryState("S21", true);
			stateS21.setParentState(stateS2);
			JpaRepositoryState stateS22 = new JpaRepositoryState("S22");
			stateS22.setParentState(stateS2);

			stateRepository.save(stateS1);
			stateRepository.save(stateS2);
			stateRepository.save(stateS3);
			stateRepository.save(stateS21);
			stateRepository.save(stateS22);

			JpaRepositoryTransition transitionS1ToS2 = new JpaRepositoryTransition(stateS1, stateS2, "E1");
			JpaRepositoryTransition transitionS2ToS3 = new JpaRepositoryTransition(stateS21, stateS22, "E2");
			JpaRepositoryTransition transitionS21ToS22 = new JpaRepositoryTransition(stateS2, stateS3, "E3");

			transitionRepository.save(transitionS1ToS2);
			transitionRepository.save(transitionS2ToS3);
			transitionRepository.save(transitionS21ToS22);
		}
// end::snippetB[]
	}
}
