/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.statemachine.scxml;

import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.AbstractStateMachineModelFactory;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.scxml.support.ScxmlModelParser;
import org.springframework.statemachine.scxml.support.ScxmlModelParser.DataHolder;
import org.springframework.util.Assert;

/**
 * {@link StateMachineModelFactory} which builds {@link StateMachineModel} from
 * SCXML representation.
 *
 * <p>SCXML (State Chart XML) is a W3C standard for describing state machines.
 * This factory parses SCXML files and converts them into Spring Statemachine models.
 *
 * @author Spring Statemachine Team
 */
public class ScxmlStateMachineModelFactory extends AbstractStateMachineModelFactory<String, String> {

	private Resource resource;
	private String location;

	/**
	 * Instantiates a new SCXML state machine model factory.
	 *
	 * @param resource the resource
	 */
	public ScxmlStateMachineModelFactory(Resource resource) {
		Assert.notNull(resource, "Resource must be set");
		this.resource = resource;
	}

	/**
	 * Instantiates a new SCXML state machine model factory.
	 *
	 * @param location the resource location
	 */
	public ScxmlStateMachineModelFactory(String location) {
		Assert.notNull(location, "Location must be set");
		this.location = location;
	}

	@Override
	public StateMachineModel<String, String> build() {
		Resource scxmlResource = null;
		try {
			if (this.location != null) {
				scxmlResource = getResourceLoader().getResource(this.location);
			} else if (this.resource != null) {
				scxmlResource = this.resource;
			}

			if (scxmlResource == null || !scxmlResource.exists()) {
				throw new IllegalArgumentException("Cannot find SCXML resource: " + 
					(resource != null ? resource : location));
			}

			ScxmlModelParser parser = new ScxmlModelParser(scxmlResource, this);
			DataHolder dataHolder = parser.parseModel();

			// we don't set configurationData here, so assume null
			return new DefaultStateMachineModel<String, String>(null, 
				dataHolder.getStatesData(), dataHolder.getTransitionsData());
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot build model from SCXML resource " + 
				(resource != null ? resource : location), e);
		}
	}
}

