/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.data.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.data.repository.init.AbstractRepositoryPopulatorFactoryBean;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.data.repository.init.ResourceReaderRepositoryPopulator;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link FactoryBean} to set up a {@link ResourceReaderRepositoryPopulator} with a {@link StateMachineJackson2ResourceReader}.
 *
 * @author Oliver Gierke
 * @author Janne Valkealahti
 */
public class StateMachineJackson2RepositoryPopulatorFactoryBean extends AbstractRepositoryPopulatorFactoryBean {

	private ObjectMapper mapper;

	/**
	 * Configures the {@link ObjectMapper} to be used.
	 *
	 * @param mapper the new mapper
	 */
	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	protected ResourceReader getResourceReader() {
		return new StateMachineJackson2ResourceReader(mapper);
	}
}
