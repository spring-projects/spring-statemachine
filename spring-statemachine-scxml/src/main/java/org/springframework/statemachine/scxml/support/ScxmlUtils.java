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
package org.springframework.statemachine.scxml.support;

import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utilities for SCXML model processing.
 *
 * @author Spring Statemachine Team
 */
public abstract class ScxmlUtils {

	/**
	 * SCXML namespace URI.
	 */
	public static final String SCXML_NAMESPACE = "http://www.w3.org/2005/07/scxml";

	/**
	 * Gets the id attribute from an element.
	 *
	 * @param element the element
	 * @return the id or null if not found
	 */
	public static String getId(Element element) {
		if (element == null) {
			return null;
		}
		String id = element.getAttribute("id");
		if (StringUtils.hasText(id)) {
			return id;
		}
		// Fallback to name attribute
		id = element.getAttribute("name");
		return StringUtils.hasText(id) ? id : null;
	}

	/**
	 * Gets the initial state from SCXML root element.
	 *
	 * @param scxmlElement the SCXML root element
	 * @return the initial state id or null if not found
	 */
	public static String getInitialState(Element scxmlElement) {
		if (scxmlElement == null) {
			return null;
		}

		// Check initial attribute on root
		String initial = scxmlElement.getAttribute("initial");
		if (StringUtils.hasText(initial)) {
			return initial;
		}

		// Check for <initial> element
		NodeList initialNodes = scxmlElement.getElementsByTagNameNS(SCXML_NAMESPACE, "initial");
		if (initialNodes.getLength() > 0) {
			Element initialElement = (Element) initialNodes.item(0);
			NodeList transitionNodes = initialElement.getElementsByTagNameNS(SCXML_NAMESPACE, "transition");
			if (transitionNodes.getLength() > 0) {
				Element transitionElement = (Element) transitionNodes.item(0);
				return getTarget(transitionElement);
			}
		}

		// Check for first state
		NodeList stateNodes = scxmlElement.getElementsByTagNameNS(SCXML_NAMESPACE, "state");
		if (stateNodes.getLength() > 0) {
			Element firstState = (Element) stateNodes.item(0);
			return getId(firstState);
		}

		return null;
	}

	/**
	 * Gets the event attribute from a transition element.
	 *
	 * @param transitionElement the transition element
	 * @return the event or null if not found
	 */
	public static String getEvent(Element transitionElement) {
		if (transitionElement == null) {
			return null;
		}
		String event = transitionElement.getAttribute("event");
		return StringUtils.hasText(event) ? event : null;
	}

	/**
	 * Gets the target attribute from a transition element.
	 *
	 * @param transitionElement the transition element
	 * @return the target or null if not found
	 */
	public static String getTarget(Element transitionElement) {
		if (transitionElement == null) {
			return null;
		}
		String target = transitionElement.getAttribute("target");
		return StringUtils.hasText(target) ? target : null;
	}

	/**
	 * Gets the source for a transition. In SCXML, transitions are defined
	 * within states, so the source is typically the parent state.
	 *
	 * @param transitionElement the transition element
	 * @param parentState the parent state id
	 * @return the source state id
	 */
	public static String getSource(Element transitionElement, String parentState) {
		if (transitionElement == null) {
			return parentState;
		}
		String source = transitionElement.getAttribute("source");
		if (StringUtils.hasText(source)) {
			return source;
		}
		return parentState;
	}

	/**
	 * Gets the condition attribute from a transition element.
	 *
	 * @param transitionElement the transition element
	 * @return the condition or null if not found
	 */
	public static String getCondition(Element transitionElement) {
		if (transitionElement == null) {
			return null;
		}
		String cond = transitionElement.getAttribute("cond");
		return StringUtils.hasText(cond) ? cond : null;
	}

	/**
	 * Gets the region id for a state. In SCXML, parallel states create regions.
	 *
	 * @param stateElement the state element
	 * @return the region id or null if not applicable
	 */
	public static String getRegionId(Element stateElement) {
		if (stateElement == null) {
			return null;
		}
		// Check if parent is parallel
		Node parent = stateElement.getParentNode();
		if (parent instanceof Element) {
			Element parentElement = (Element) parent;
			if ("parallel".equals(parentElement.getLocalName())) {
				return getId(parentElement);
			}
		}
		return null;
	}

	/**
	 * Checks if a state id represents a choice pseudostate.
	 *
	 * @param stateId the state id
	 * @return true if it's a choice state
	 */
	public static boolean isChoiceState(String stateId) {
		return stateId != null && (stateId.startsWith("choice") || stateId.contains("Choice"));
	}

	/**
	 * Checks if a state id represents a junction pseudostate.
	 *
	 * @param stateId the state id
	 * @return true if it's a junction state
	 */
	public static boolean isJunctionState(String stateId) {
		return stateId != null && (stateId.startsWith("junction") || stateId.contains("Junction"));
	}
}

