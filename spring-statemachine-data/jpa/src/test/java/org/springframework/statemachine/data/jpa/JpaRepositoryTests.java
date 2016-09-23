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
package org.springframework.statemachine.data.jpa;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.data.RepositoryState;
import org.springframework.statemachine.data.StateRepository;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.TransitionRepository;

public class JpaRepositoryTests {

	@Test
	public void testRepository1() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		JpaStateRepository statesRepository = context.getBean(JpaStateRepository.class);
		JpaRepositoryState state = new JpaRepositoryState("S1");
		statesRepository.save(state);
		Iterable<JpaRepositoryState> findAll = statesRepository.findAll();
		assertThat(findAll.iterator().next().getState(), is("S1"));

		JpaTransitionRepository transitionsRepository = context.getBean(JpaTransitionRepository.class);
		JpaRepositoryTransition transition = new JpaRepositoryTransition("S1", "S2", "E1");
		transitionsRepository.save(transition);
		JpaRepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource(), is("S1"));
		assertThat(transition2.getTarget(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));

		context.close();
	}

	@Test
	public void testRepository2() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class);
		context.refresh();

		@SuppressWarnings("unchecked")
		StateRepository<JpaRepositoryState> statesRepository1 = context.getBean(StateRepository.class);
		JpaRepositoryState state = new JpaRepositoryState("S1");
		statesRepository1.save(state);
		@SuppressWarnings("unchecked")
		StateRepository<? extends RepositoryState> statesRepository2 = context.getBean(StateRepository.class);
		Iterable<? extends RepositoryState> findAll = statesRepository2.findAll();
		assertThat(findAll.iterator().next().getState(), is("S1"));

		@SuppressWarnings("unchecked")
		TransitionRepository<RepositoryTransition> transitionsRepository = context.getBean(TransitionRepository.class);
		RepositoryTransition transition = new JpaRepositoryTransition("S1", "S2", "E1");
		transitionsRepository.save(transition);
		RepositoryTransition transition2 = transitionsRepository.findAll().iterator().next();
		assertThat(transition2.getSource(), is("S1"));
		assertThat(transition2.getTarget(), is("S2"));
		assertThat(transition2.getEvent(), is("E1"));

		context.close();
	}

	@Test
	public void testAutowire() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(Config.class, WireConfig.class);
		context.refresh();
		context.close();
	}

	@EnableAutoConfiguration
	static class Config {
	}

	@Configuration
	static class WireConfig {

		@Autowired
		StateRepository<JpaRepositoryState> statesRepository1;

		@Autowired
		TransitionRepository<JpaRepositoryTransition> statesRepository11;

		@SuppressWarnings("rawtypes")
		@Autowired
		StateRepository statesRepository2;

		@Autowired
		JpaStateRepository statesRepository3;

		@Autowired
		StateRepository<? extends RepositoryState> statesRepository4;
	}
}
