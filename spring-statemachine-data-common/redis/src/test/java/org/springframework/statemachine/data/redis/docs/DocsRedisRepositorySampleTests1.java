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
package org.springframework.statemachine.data.redis.docs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.data.StateMachineRepository;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.data.redis.RedisRepositoryState;
import org.springframework.statemachine.data.redis.RedisRepositoryStateMachine;
import org.springframework.statemachine.data.redis.RedisRepositoryTransition;

public class DocsRedisRepositorySampleTests1 {

	public static class Config1 {
// tag::snippetA[]
		@Autowired
		StateRepository<RedisRepositoryState> stateRepository;

		@Autowired
		TransitionRepository<RedisRepositoryTransition> transitionRepository;

		void addConfig() {
			RedisRepositoryState stateS1 = new RedisRepositoryState("S1", true);
			RedisRepositoryState stateS2 = new RedisRepositoryState("S2");
			RedisRepositoryState stateS3 = new RedisRepositoryState("S3");

			stateRepository.save(stateS1);
			stateRepository.save(stateS2);
			stateRepository.save(stateS3);


			RedisRepositoryTransition transitionS1ToS2 = new RedisRepositoryTransition(stateS1, stateS2, "E1");
			RedisRepositoryTransition transitionS2ToS3 = new RedisRepositoryTransition(stateS2, stateS3, "E2");

			transitionRepository.save(transitionS1ToS2);
			transitionRepository.save(transitionS2ToS3);
		}
// end::snippetA[]
	}

	public static class Config2 {
		// tag::snippetB[]
		@Autowired
		StateRepository<RedisRepositoryState> stateRepository;

		@Autowired
		TransitionRepository<RedisRepositoryTransition> transitionRepository;

		void addConfig() {
			RedisRepositoryState stateS1 = new RedisRepositoryState("S1", true);
			RedisRepositoryState stateS2 = new RedisRepositoryState("S2");
			RedisRepositoryState stateS3 = new RedisRepositoryState("S3");

			stateRepository.save(stateS1);
			stateRepository.save(stateS2);
			stateRepository.save(stateS3);


			RedisRepositoryTransition transitionS1ToS2 = new RedisRepositoryTransition(stateS1, stateS2, "E1");
			RedisRepositoryTransition transitionS2ToS3 = new RedisRepositoryTransition(stateS2, stateS3, "E2");

			transitionRepository.save(transitionS1ToS2);
			transitionRepository.save(transitionS2ToS3);
		}
		// end::snippetB[]
	}

	public static class Config3 {

// tag::snippetC[]
		@Autowired
		StateMachineRepository<RedisRepositoryStateMachine> stateMachineRepository;

		void persist() {

			RedisRepositoryStateMachine machine = new RedisRepositoryStateMachine();
			machine.setMachineId("machine");
			machine.setState("S1");
			// raw byte[] representation of a context
			machine.setStateMachineContext(new byte[] { 0 });

			stateMachineRepository.save(machine);
		}
// end::snippetC[]
	}
}
