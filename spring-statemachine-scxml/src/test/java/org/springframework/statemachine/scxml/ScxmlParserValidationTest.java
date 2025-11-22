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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.DefaultStateMachineComponentResolver;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.scxml.ScxmlStateMachineModelFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validation test to verify SCXML XML parsing works correctly.
 */
public class ScxmlParserValidationTest {

	@Test
	public void testXmlParsing() throws Exception {
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-flat.scxml");
		assertThat(scxmlResource.exists()).isTrue();
		
		// Test XML parsing
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream inputStream = scxmlResource.getInputStream();
		Document document = builder.parse(inputStream);
		Element rootElement = document.getDocumentElement();
		
		assertThat(rootElement).isNotNull();
		assertThat(rootElement.getLocalName()).isEqualTo("scxml");
		assertThat(rootElement.getNamespaceURI()).isEqualTo("http://www.w3.org/2005/07/scxml");
	}

	@Test
	public void testSimpleFlatModel() {
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-flat.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> model = factory.build();
		assertThat(model).isNotNull();
		
		Collection<StateData<String, String>> states = model.getStatesData().getStateData();
		assertThat(states.size()).isEqualTo(2);
		
		boolean foundS1 = false, foundS2 = false;
		for (StateData<String, String> state : states) {
			if ("S1".equals(state.getState())) {
				foundS1 = true;
				assertThat(state.isInitial()).isTrue();
			} else if ("S2".equals(state.getState())) {
				foundS2 = true;
				assertThat(state.isInitial()).isFalse();
			}
		}
		assertThat(foundS1).isTrue();
		assertThat(foundS2).isTrue();
	}

	@Test
	public void testTransitionsParsing() {
		Resource scxmlResource = new ClassPathResource(
			"org/springframework/statemachine/scxml/simple-flat.scxml");
		ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(scxmlResource);
		
		StateMachineModel<String, String> model = factory.build();
		assertThat(model.getTransitionsData().getTransitions().size()).isGreaterThan(0);
	}
}

