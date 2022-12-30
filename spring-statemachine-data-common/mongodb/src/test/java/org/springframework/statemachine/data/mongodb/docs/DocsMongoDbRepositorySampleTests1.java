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
package org.springframework.statemachine.data.mongodb.docs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.data.StateMachineRepository;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryState;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryStateMachine;
import org.springframework.statemachine.data.mongodb.MongoDbRepositoryTransition;

public class DocsMongoDbRepositorySampleTests1 {

	public static class Config1 {
// tag::snippetA[]
		@Autowired
		StateRepository<MongoDbRepositoryState> stateRepository;

		@Autowired
		TransitionRepository<MongoDbRepositoryTransition> transitionRepository;

		void addConfig() {
			MongoDbRepositoryState stateS1 = new MongoDbRepositoryState("S1", true);
			MongoDbRepositoryState stateS2 = new MongoDbRepositoryState("S2");
			MongoDbRepositoryState stateS3 = new MongoDbRepositoryState("S3");

			stateRepository.save(stateS1);
			stateRepository.save(stateS2);
			stateRepository.save(stateS3);

			MongoDbRepositoryTransition transitionS1ToS2 = new MongoDbRepositoryTransition(stateS1, stateS2, "E1");
			MongoDbRepositoryTransition transitionS2ToS3 = new MongoDbRepositoryTransition(stateS2, stateS3, "E2");

			transitionRepository.save(transitionS1ToS2);
			transitionRepository.save(transitionS2ToS3);
		}
// end::snippetA[]
	}

	public static class Config2 {
// tag::snippetB[]
		@Autowired
		StateRepository<MongoDbRepositoryState> stateRepository;

		@Autowired
		TransitionRepository<MongoDbRepositoryTransition> transitionRepository;

		void addConfig() {
			MongoDbRepositoryState stateS1 = new MongoDbRepositoryState("S1", true);
			MongoDbRepositoryState stateS2 = new MongoDbRepositoryState("S2");
			MongoDbRepositoryState stateS3 = new MongoDbRepositoryState("S3");

			MongoDbRepositoryState stateS21 = new MongoDbRepositoryState("S21", true);
			stateS21.setParentState(stateS2);
			MongoDbRepositoryState stateS22 = new MongoDbRepositoryState("S22");
			stateS22.setParentState(stateS2);

			stateRepository.save(stateS1);
			stateRepository.save(stateS2);
			stateRepository.save(stateS3);
			stateRepository.save(stateS21);
			stateRepository.save(stateS22);

			MongoDbRepositoryTransition transitionS1ToS2 = new MongoDbRepositoryTransition(stateS1, stateS2, "E1");
			MongoDbRepositoryTransition transitionS2ToS3 = new MongoDbRepositoryTransition(stateS21, stateS22, "E2");
			MongoDbRepositoryTransition transitionS21ToS22 = new MongoDbRepositoryTransition(stateS2, stateS3, "E3");

			transitionRepository.save(transitionS1ToS2);
			transitionRepository.save(transitionS2ToS3);
			transitionRepository.save(transitionS21ToS22);
		}
// end::snippetB[]
	}

	public static class Config3 {

// tag::snippetC[]
		@Autowired
		StateMachineRepository<MongoDbRepositoryStateMachine> stateMachineRepository;

		void persist() {

			MongoDbRepositoryStateMachine machine = new MongoDbRepositoryStateMachine();
			machine.setMachineId("machine");
			machine.setState("S1");
			// raw byte[] representation of a context
			machine.setStateMachineContext(new byte[] { 0 });

			stateMachineRepository.save(machine);
		}
// end::snippetC[]
	}

}
