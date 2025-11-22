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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.core.io.Resource;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.config.model.ChoiceData;
import org.springframework.statemachine.config.model.EntryData;
import org.springframework.statemachine.config.model.ExitData;
import org.springframework.statemachine.config.model.HistoryData;
import org.springframework.statemachine.config.model.JunctionData;
import org.springframework.statemachine.config.model.StateData;
import org.springframework.statemachine.config.model.StateMachineComponentResolver;
import org.springframework.statemachine.config.model.StatesData;
import org.springframework.statemachine.config.model.TransitionData;
import org.springframework.statemachine.config.model.TransitionsData;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.Guards;
import org.springframework.statemachine.guard.SpelExpressionGuard;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import reactor.core.publisher.Mono;

/**
 * Model parser which constructs states and transitions data out from an SCXML
 * model. This implementation is not thread safe and model parsing can only be
 * used once per instance.
 *
 * @author Spring Statemachine Team
 */
public class ScxmlModelParser {

	public final static String LANGUAGE_BEAN = "bean";
	public final static String LANGUAGE_SPEL = "spel";

	private final Resource scxmlResource;
	private final StateMachineComponentResolver<String, String> resolver;

	private final Collection<StateData<String, String>> stateDatas = new ArrayList<>();
	private final Collection<TransitionData<String, String>> transitionDatas = new ArrayList<>();
	private final Collection<EntryData<String, String>> entrys = new ArrayList<>();
	private final Collection<ExitData<String, String>> exits = new ArrayList<>();
	private final Collection<HistoryData<String, String>> historys = new ArrayList<>();
	private final Map<String, LinkedList<ChoiceData<String, String>>> choices = new HashMap<>();
	private final Map<String, LinkedList<JunctionData<String, String>>> junctions = new HashMap<>();
	private final Map<String, List<String>> forks = new HashMap<>();
	private final Map<String, List<String>> joins = new HashMap<>();

	private final List<String> seenStateData = new ArrayList<>();
	private final List<String> seenTransitionData = new ArrayList<>();

	/**
	 * Instantiates a new SCXML model parser.
	 *
	 * @param scxmlResource the SCXML resource
	 * @param resolver the resolver
	 */
	public ScxmlModelParser(Resource scxmlResource, StateMachineComponentResolver<String, String> resolver) {
		Assert.notNull(scxmlResource, "SCXML resource must be set");
		Assert.notNull(resolver, "Resolver must be set");
		this.scxmlResource = scxmlResource;
		this.resolver = resolver;
	}

	/**
	 * Parses the SCXML model.
	 *
	 * @return the data holder for states and transitions
	 */
	public DataHolder parseModel() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			InputStream inputStream = scxmlResource.getInputStream();
			Document document = builder.parse(inputStream);
			Element rootElement = document.getDocumentElement();

			// Check if root element is <scxml>
			if (!"scxml".equals(rootElement.getLocalName())) {
				throw new IllegalArgumentException("Root element must be <scxml>");
			}

			// Parse initial state from root
			String initialStateId = ScxmlUtils.getInitialState(rootElement);
			
			// Parse all states first
			parseStates(rootElement, null, null);

			// Process initial elements to mark child initial states
			processInitialElements(rootElement, null);

			// Parse all transitions
			parseTransitions(rootElement, null);

			// Mark root initial state
			if (initialStateId != null) {
				for (StateData<String, String> stateData : stateDatas) {
					if (initialStateId.equals(stateData.getState()) && stateData.getParent() == null) {
						stateData.setInitial(true);
						break;
					}
				}
			}

			// Copy collections to avoid modification issues
			HashMap<String, List<ChoiceData<String, String>>> choicesCopy = new HashMap<>();
			choicesCopy.putAll(choices);
			HashMap<String, List<JunctionData<String, String>>> junctionsCopy = new HashMap<>();
			junctionsCopy.putAll(junctions);

			return new DataHolder(new StatesData<>(stateDatas),
				new TransitionsData<String, String>(transitionDatas, choicesCopy, junctionsCopy, 
					forks, joins, entrys, exits, historys));
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to parse SCXML model", e);
		}
	}

	private void parseStates(Element parentElement, String parentState, String regionId) {
		// Only get direct child elements, not nested ones
		NodeList childNodes = parentElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String localName = element.getLocalName();
				String namespaceURI = element.getNamespaceURI();
				
				if (ScxmlUtils.SCXML_NAMESPACE.equals(namespaceURI)) {
					if ("state".equals(localName)) {
						parseStateElement(element, parentState, regionId);
					} else if ("parallel".equals(localName)) {
						parseParallelElement(element, parentState, regionId);
					} else if ("final".equals(localName)) {
						parseFinalElement(element, parentState, regionId);
					} else if ("initial".equals(localName)) {
						parseInitialElement(element, parentState, regionId);
					} else if ("history".equals(localName)) {
						parseHistoryElement(element, parentState, regionId);
					}
				}
			}
		}
	}

	private void parseStateElement(Element stateElement, String parentState, String regionId) {
		String stateId = ScxmlUtils.getId(stateElement);
		if (stateId == null) {
			return;
		}

		StateData<String, String> stateData = new StateData<>(parentState, regionId, stateId, false);
		
		// Parse entry actions
		parseEntryActions(stateElement, stateData);
		
		// Parse exit actions
		parseExitActions(stateElement, stateData);
		
		// Parse state actions (do activities)
		parseStateActions(stateElement, stateData);

		addStateData(stateData);

		// Recursively parse child states - check if state has child states
		// Only parse direct children to avoid duplicate processing
		NodeList childNodes = stateElement.getChildNodes();
		boolean hasChildStates = false;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String localName = element.getLocalName();
				if (ScxmlUtils.SCXML_NAMESPACE.equals(element.getNamespaceURI()) &&
					("state".equals(localName) || "parallel".equals(localName) || 
					 "final".equals(localName) || "initial".equals(localName) || 
					 "history".equals(localName))) {
					hasChildStates = true;
					break;
				}
			}
		}
		
		if (hasChildStates) {
			String currentRegionId = ScxmlUtils.getRegionId(stateElement);
			parseStates(stateElement, stateId, currentRegionId);
		}
	}

	private void parseParallelElement(Element parallelElement, String parentState, String regionId) {
		String parallelId = ScxmlUtils.getId(parallelElement);
		if (parallelId == null) {
			return;
		}

		StateData<String, String> stateData = new StateData<>(parentState, regionId, parallelId, false);
		stateData.setPseudoStateKind(PseudoStateKind.FORK);
		addStateData(stateData);

		// Parse regions within parallel - only direct child states
		NodeList childNodes = parallelElement.getChildNodes();
		int regionIndex = 0;
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				if ("state".equals(element.getLocalName()) && 
					ScxmlUtils.SCXML_NAMESPACE.equals(element.getNamespaceURI())) {
					String regionIdForParallel = "region" + regionIndex++;
					parseStates(element, parallelId, regionIdForParallel);
				}
			}
		}
	}

	private void parseFinalElement(Element finalElement, String parentState, String regionId) {
		String finalId = ScxmlUtils.getId(finalElement);
		if (finalId == null) {
			finalId = "final";
		}

		StateData<String, String> stateData = new StateData<>(parentState, regionId, finalId, false);
		stateData.setEnd(true);
		stateData.setPseudoStateKind(PseudoStateKind.END);
		addStateData(stateData);
	}

	private void parseInitialElement(Element initialElement, String parentState, String regionId) {
		String initialId = ScxmlUtils.getId(initialElement);
		if (initialId == null) {
			initialId = "initial";
		}

		StateData<String, String> stateData = new StateData<>(parentState, regionId, initialId, false);
		stateData.setPseudoStateKind(PseudoStateKind.INITIAL);
		addStateData(stateData);
	}

	private void processInitialElements(Element parentElement, String parentState) {
		// Process initial elements to mark child initial states
		NodeList childNodes = parentElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String localName = element.getLocalName();
				if (ScxmlUtils.SCXML_NAMESPACE.equals(element.getNamespaceURI())) {
					if ("initial".equals(localName)) {
						// Parse transition inside initial element to find initial child state
						NodeList transitionNodes = element.getElementsByTagNameNS(
							ScxmlUtils.SCXML_NAMESPACE, "transition");
						if (transitionNodes.getLength() > 0) {
							Element transitionElement = (Element) transitionNodes.item(0);
							String targetStateId = ScxmlUtils.getTarget(transitionElement);
							if (targetStateId != null) {
								// Mark the target state as initial within its parent
								for (StateData<String, String> sd : stateDatas) {
									if (targetStateId.equals(sd.getState()) && 
										(parentState == null ? sd.getParent() == null : parentState.equals(sd.getParent()))) {
										sd.setInitial(true);
										break;
									}
								}
							}
						}
					} else if ("state".equals(localName)) {
						// Recursively process nested states
						String stateId = ScxmlUtils.getId(element);
						if (stateId != null) {
							processInitialElements(element, stateId);
						}
					}
				}
			}
		}
	}

	private void parseHistoryElement(Element historyElement, String parentState, String regionId) {
		String historyId = ScxmlUtils.getId(historyElement);
		if (historyId == null) {
			historyId = "history";
		}

		String type = historyElement.getAttribute("type");
		PseudoStateKind kind = "deep".equals(type) ? 
			PseudoStateKind.HISTORY_DEEP : PseudoStateKind.HISTORY_SHALLOW;

		StateData<String, String> stateData = new StateData<>(parentState, regionId, historyId, false);
		stateData.setPseudoStateKind(kind);
		addStateData(stateData);
	}

	private void parseTransitions(Element parentElement, String parentState) {
		// Only get direct child transitions, not nested ones
		// Skip transitions inside initial elements as they are handled separately
		NodeList childNodes = parentElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String localName = element.getLocalName();
				String namespaceURI = element.getNamespaceURI();
				
				// Skip initial elements - their transitions are handled in parseInitialElement
				if ("initial".equals(localName) && ScxmlUtils.SCXML_NAMESPACE.equals(namespaceURI)) {
					continue;
				}
				
				// Parse direct child transitions
				if ("transition".equals(localName) && 
					ScxmlUtils.SCXML_NAMESPACE.equals(namespaceURI)) {
					parseTransitionElement(element, parentState);
				}
			}
		}

		// Recursively parse transitions in child states
		// Use direct child iteration to avoid processing nested states multiple times
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node instanceof Element) {
				Element element = (Element) node;
				String localName = element.getLocalName();
				if (ScxmlUtils.SCXML_NAMESPACE.equals(element.getNamespaceURI())) {
					if ("state".equals(localName)) {
						String stateId = ScxmlUtils.getId(element);
						if (stateId != null) {
							parseTransitions(element, stateId);
						}
					} else if ("parallel".equals(localName)) {
						String parallelId = ScxmlUtils.getId(element);
						if (parallelId != null) {
							parseTransitions(element, parallelId);
						}
					}
				}
			}
		}
	}

	private void parseTransitionElement(Element transitionElement, String parentState) {
		String event = ScxmlUtils.getEvent(transitionElement);
		String target = ScxmlUtils.getTarget(transitionElement);
		String source = ScxmlUtils.getSource(transitionElement, parentState);
		String cond = ScxmlUtils.getCondition(transitionElement);

		if (target == null) {
			return;
		}

		// Parse guard
		Guard<String, String> guard = resolveGuard(cond);

		// Parse actions
		Collection<Function<StateContext<String, String>, Mono<Void>>> actions = 
			parseTransitionActions(transitionElement);

		// Determine transition kind
		TransitionKind kind = TransitionKind.EXTERNAL;
		String type = transitionElement.getAttribute("type");
		if ("internal".equals(type)) {
			kind = TransitionKind.INTERNAL;
		} else if ("local".equals(type)) {
			kind = TransitionKind.LOCAL;
		}

		// Handle choice/junction pseudostates
		// In SCXML, choice is typically a state with multiple conditional transitions
		// We detect it by checking if source state has multiple outgoing transitions with guards
		if (ScxmlUtils.isChoiceState(source) || ScxmlUtils.isJunctionState(source)) {
			// For choice states, collect all transitions with guards
			LinkedList<ChoiceData<String, String>> list = choices.get(source);
			if (list == null) {
				list = new LinkedList<>();
				choices.put(source, list);
			}
			Collection<Action<String, String>> actionBeans = new ArrayList<>();
			for (Function<StateContext<String, String>, Mono<Void>> actionFunc : actions) {
				// Convert function to action
				Action<String, String> action = context -> {
					actionFunc.apply(context).block();
				};
				actionBeans.add(action);
			}
			if (guard == null) {
				list.addLast(new ChoiceData<>(source, target, guard, actionBeans));
			} else {
				list.addFirst(new ChoiceData<>(source, target, guard, actionBeans));
			}
		} else {
			addTransitionData(new TransitionData<>(source, target, event,
				actions, Guards.from(guard), kind, null));
		}
	}

	private void parseEntryActions(Element stateElement, StateData<String, String> stateData) {
		NodeList entryNodes = stateElement.getElementsByTagNameNS(
			ScxmlUtils.SCXML_NAMESPACE, "onentry");
		if (entryNodes.getLength() > 0) {
			Element entryElement = (Element) entryNodes.item(0);
			Collection<Function<StateContext<String, String>, Mono<Void>>> entryActions = 
				parseActions(entryElement);
			stateData.setEntryActions(new ArrayList<>(entryActions));
		}
	}

	private void parseExitActions(Element stateElement, StateData<String, String> stateData) {
		NodeList exitNodes = stateElement.getElementsByTagNameNS(
			ScxmlUtils.SCXML_NAMESPACE, "onexit");
		if (exitNodes.getLength() > 0) {
			Element exitElement = (Element) exitNodes.item(0);
			Collection<Function<StateContext<String, String>, Mono<Void>>> exitActions = 
				parseActions(exitElement);
			stateData.setExitActions(new ArrayList<>(exitActions));
		}
	}

	private void parseStateActions(Element stateElement, StateData<String, String> stateData) {
		// SCXML doesn't have a direct "do" activity, but we can parse custom elements
		// or use invoke elements as state actions
		NodeList invokeNodes = stateElement.getElementsByTagNameNS(
			ScxmlUtils.SCXML_NAMESPACE, "invoke");
		if (invokeNodes.getLength() > 0) {
			Collection<Function<StateContext<String, String>, Mono<Void>>> stateActions = 
				new ArrayList<>();
			for (int i = 0; i < invokeNodes.getLength(); i++) {
				Element invokeElement = (Element) invokeNodes.item(i);
				String beanId = invokeElement.getAttribute("bean");
				if (StringUtils.hasText(beanId)) {
					try {
						Action<String, String> bean = resolver.resolveAction(beanId);
						if (bean != null) {
							stateActions.add(Actions.from(bean));
						}
					} catch (Exception e) {
						// If action resolution fails, skip it
					}
				}
			}
			if (!stateActions.isEmpty()) {
				stateData.setStateActions(new ArrayList<>(stateActions));
			}
		}
	}

	private Collection<Function<StateContext<String, String>, Mono<Void>>> parseActions(Element parentElement) {
		Collection<Function<StateContext<String, String>, Mono<Void>>> actions = new ArrayList<>();
		
		// Parse <log> elements
		NodeList logNodes = parentElement.getElementsByTagNameNS(
			ScxmlUtils.SCXML_NAMESPACE, "log");
		for (int i = 0; i < logNodes.getLength(); i++) {
			Element logElement = (Element) logNodes.item(i);
			String expr = logElement.getAttribute("expr");
			if (StringUtils.hasText(expr)) {
				SpelExpressionParser parser = new SpelExpressionParser(
					new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
				actions.add(Actions.from(new SpelExpressionAction<>(parser.parseExpression(expr))));
			}
		}

		// Parse <action> elements and custom action elements (bean references)
		// Look for elements with bean attribute or custom action elements
		NodeList actionNodes = parentElement.getElementsByTagNameNS(
			ScxmlUtils.SCXML_NAMESPACE, "action");
		for (int i = 0; i < actionNodes.getLength(); i++) {
			Element actionElement = (Element) actionNodes.item(i);
			String beanId = actionElement.getAttribute("bean");
			if (StringUtils.hasText(beanId)) {
				try {
					Action<String, String> bean = resolver.resolveAction(beanId);
					if (bean != null) {
						actions.add(Actions.from(bean));
					}
				} catch (RuntimeException e) {
					// If action resolution fails, skip it
				}
			}
		}
		
		// Also check direct child elements for custom action elements
		NodeList allNodes = parentElement.getChildNodes();
		for (int i = 0; i < allNodes.getLength(); i++) {
			Node node = allNodes.item(i);
			if (node instanceof Element) {
				Element actionElement = (Element) node;
				String localName = actionElement.getLocalName();
				String namespaceURI = actionElement.getNamespaceURI();
				
				// Skip known SCXML elements
				if (ScxmlUtils.SCXML_NAMESPACE.equals(namespaceURI) &&
					("log".equals(localName) || "action".equals(localName) ||
					 "onentry".equals(localName) || "onexit".equals(localName) ||
					 "transition".equals(localName) || "invoke".equals(localName))) {
					continue;
				}
				
				// Check if element has bean attribute
				String beanId = actionElement.getAttribute("bean");
				if (StringUtils.hasText(beanId)) {
					try {
						Action<String, String> bean = resolver.resolveAction(beanId);
						if (bean != null) {
							actions.add(Actions.from(bean));
						}
					} catch (RuntimeException e) {
						// If action resolution fails, skip it
					}
				} else if (StringUtils.hasText(localName)) {
					// Check if element name matches a registered action bean
					try {
						Action<String, String> bean = resolver.resolveAction(localName);
						if (bean != null) {
							actions.add(Actions.from(bean));
						}
					} catch (RuntimeException e) {
						// If action resolution fails, skip it
					}
				}
			}
		}

		return actions;
	}

	private Collection<Function<StateContext<String, String>, Mono<Void>>> parseTransitionActions(
			Element transitionElement) {
		return parseActions(transitionElement);
	}

	private Guard<String, String> resolveGuard(String condition) {
		if (!StringUtils.hasText(condition)) {
			return null;
		}

		try {
			// Check if it's a bean reference (starts with "bean:")
			if (condition.startsWith("bean:")) {
				String beanId = condition.substring(5).trim();
				try {
					return resolver.resolveGuard(beanId);
				} catch (Exception e) {
					// If guard resolution fails, return null
					return null;
				}
			}

			// Otherwise treat as SpEL expression
			SpelExpressionParser parser = new SpelExpressionParser(
				new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
			return new SpelExpressionGuard<>(parser.parseExpression(condition));
		} catch (Exception e) {
			// If parsing fails, return null to allow transition without guard
			return null;
		}
	}

	private void addStateData(StateData<String, String> stateData) {
		String key = stateData.getState();
		if (!seenStateData.contains(key)) {
			stateDatas.add(stateData);
			seenStateData.add(key);
		}
	}

	private void addTransitionData(TransitionData<String, String> transitionData) {
		String skey = transitionData.getSource() != null ? transitionData.getSource() : "null";
		String tkey = transitionData.getTarget() != null ? transitionData.getTarget() : "null";
		String ekey = transitionData.getEvent() != null ? transitionData.getEvent() : "null";
		String kkey = transitionData.getKind() != null ? transitionData.getKind().toString() : "null";
		String key = skey + "_" + tkey + "_" + ekey + "_" + kkey;
		if (!seenTransitionData.contains(key)) {
			transitionDatas.add(transitionData);
			seenTransitionData.add(key);
		}
	}

	/**
	 * Holder object for results returned from SCXML parser.
	 */
	public static class DataHolder {
		private final StatesData<String, String> statesData;
		private final TransitionsData<String, String> transitionsData;

		/**
		 * Instantiates a new data holder.
		 *
		 * @param statesData the states data
		 * @param transitionsData the transitions data
		 */
		public DataHolder(StatesData<String, String> statesData, 
				TransitionsData<String, String> transitionsData) {
			this.statesData = statesData;
			this.transitionsData = transitionsData;
		}

		/**
		 * Gets the states data.
		 *
		 * @return the states data
		 */
		public StatesData<String, String> getStatesData() {
			return statesData;
		}

		/**
		 * Gets the transitions data.
		 *
		 * @return the transitions data
		 */
		public TransitionsData<String, String> getTransitionsData() {
			return transitionsData;
		}
	}
}

