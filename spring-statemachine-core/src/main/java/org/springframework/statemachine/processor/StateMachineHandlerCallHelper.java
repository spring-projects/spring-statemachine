/*
 * Copyright 2015-2017 the original author or authors.
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
package org.springframework.statemachine.processor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.OnEventNotAccepted;
import org.springframework.statemachine.annotation.OnExtendedStateChanged;
import org.springframework.statemachine.annotation.OnStateChanged;
import org.springframework.statemachine.annotation.OnStateEntry;
import org.springframework.statemachine.annotation.OnStateExit;
import org.springframework.statemachine.annotation.OnStateMachineError;
import org.springframework.statemachine.annotation.OnStateMachineStart;
import org.springframework.statemachine.annotation.OnStateMachineStop;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.OnTransitionEnd;
import org.springframework.statemachine.annotation.OnTransitionStart;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.configuration.StateMachineHandlerApplicationListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Helper class which is used from a StateMachineObjectSupport to ease handling
 * of StateMachineHandlers and provides needed caching so that a runtime calls
 * are fast. Also provides dedicated methods for each annotated methods so that
 * parameters are handled accordingly.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineHandlerCallHelper<S, E> implements InitializingBean, BeanFactoryAware {

	private final Log log = LogFactory.getLog(StateMachineHandlerCallHelper.class);
	private final Map<String, List<CacheEntry>> cache = new HashMap<>();
	private ListableBeanFactory beanFactory;
	private StateMachineHandlerApplicationListener stateMachineHandlerApplicationListener;
	private long last = Long.MIN_VALUE;

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		if (beanFactory == null) {
			return;
		}
		if (!(beanFactory instanceof ListableBeanFactory)) {
			log.info("Beanfactory is not instance of ListableBeanFactory, was " + beanFactory + " thus Disabling handlers.");
			return;
		}
		if (beanFactory.containsBean(StateMachineHandlerApplicationListener.BEAN_NAME)) {
			this.stateMachineHandlerApplicationListener = beanFactory.getBean(StateMachineHandlerApplicationListener.BEAN_NAME,
					StateMachineHandlerApplicationListener.class);
		}
		for (StateMachineHandler<? extends Annotation, S, E> handler : beanFactory.getBeansOfType(StateMachineHandler.class).values()) {
			Annotation annotation = handler.getAnnotation();
			Annotation metaAnnotation = handler.getMetaAnnotation();
			WithStateMachine withStateMachine = AnnotationUtils.findAnnotation(handler.getBeanClass(),
					WithStateMachine.class);

			// don't check name if id is set as name defaults to
			// 'stateMachine' and would cause additional cache entry
			if (StringUtils.hasText(withStateMachine.id())) {
				updateCache(metaAnnotation.annotationType().getName() + withStateMachine.id(),
						new CacheEntry(handler, annotation, metaAnnotation));
			} else if (StringUtils.hasText(withStateMachine.name())) {
				updateCache(metaAnnotation.annotationType().getName() + withStateMachine.name(),
						new CacheEntry(handler, annotation, metaAnnotation));
			}
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (beanFactory == null) {
			// just skip if beanFactory is null as outside of spring
			// app context, we usually don't have any factory features.
			// in a same way further things will be skipped in afterPropertiesSet()
			return;
		}
		Assert.isInstanceOf(ListableBeanFactory.class, beanFactory);
		this.beanFactory = (ListableBeanFactory)beanFactory;
	}

	public void callOnStateChanged(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnStateChanged.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			if (annotationHandlerSourceTargetMatch((String[]) AnnotationUtils.getValue(entry.metaAnnotation, "source"),
					(String[]) AnnotationUtils.getValue(entry.metaAnnotation, "target"), entry.annotation, stateContext.getSource(),
					stateContext.getTarget())) {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnStateEntry(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnStateEntry.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			if (annotationHandlerSourceTargetMatch((String[]) AnnotationUtils.getValue(entry.metaAnnotation, "source"),
					(String[]) AnnotationUtils.getValue(entry.metaAnnotation, "target"), entry.annotation, stateContext.getSource(),
					stateContext.getTarget())) {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnStateExit(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnStateExit.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			if (annotationHandlerSourceTargetMatch((String[]) AnnotationUtils.getValue(entry.metaAnnotation, "source"),
					(String[]) AnnotationUtils.getValue(entry.metaAnnotation, "target"), entry.annotation, stateContext.getSource(),
					stateContext.getTarget())) {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnEventNotAccepted(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnEventNotAccepted.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			E event = stateContext.getEvent();
			if (event != null) {
				if (annotationHandlerEventVariableMatch(entry.metaAnnotation, new String[]{event.toString()})) {
					handlersList.add(entry.handler);
				}
			} else {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}


	public void callOnTransitionStart(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnTransitionStart.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			if (annotationHandlerSourceTargetMatch((String[]) AnnotationUtils.getValue(entry.metaAnnotation, "source"),
					(String[]) AnnotationUtils.getValue(entry.metaAnnotation, "target"), entry.annotation,
					stateContext.getTransition().getSource(), stateContext.getTransition().getTarget())) {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnTransition(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnTransition.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			if (annotationHandlerSourceTargetMatch((String[]) AnnotationUtils.getValue(entry.metaAnnotation, "source"),
					(String[]) AnnotationUtils.getValue(entry.metaAnnotation, "target"), entry.annotation,
					stateContext.getTransition().getSource(), stateContext.getTransition().getTarget())) {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnTransitionEnd(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnTransitionEnd.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			if (annotationHandlerSourceTargetMatch((String[]) AnnotationUtils.getValue(entry.metaAnnotation, "source"),
					(String[]) AnnotationUtils.getValue(entry.metaAnnotation, "target"), entry.annotation,
					stateContext.getTransition().getSource(), stateContext.getTransition().getTarget())) {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnStateMachineStart(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnStateMachineStart.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			handlersList.add(entry.handler);
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnStateMachineStop(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnStateMachineStop.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			handlersList.add(entry.handler);
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnStateMachineError(String stateMachineId, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnStateMachineError.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			handlersList.add(entry.handler);
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	public void callOnExtendedStateChanged(String stateMachineId, Object key, Object value, StateContext<S, E> stateContext) {
		if (!StringUtils.hasText(stateMachineId)) {
			return;
		}
		List<StateMachineHandler<? extends Annotation, S, E>> handlersList = new ArrayList<StateMachineHandler<? extends Annotation, S, E>>();
		String cacheKey = OnExtendedStateChanged.class.getName() + stateMachineId;
		List<CacheEntry> list = getCacheEntries(cacheKey);
		if (list == null) {
			return;
		}
		for (CacheEntry entry : list) {
			if (annotationHandlerVariableMatch(entry.metaAnnotation, key)) {
				handlersList.add(entry.handler);
			}
		}
		getStateMachineHandlerResults(handlersList, stateContext);
	}

	private void updateCache(String key, CacheEntry cacheEntry) {
		List<CacheEntry> list = cache.get(key);
		if (list == null) {
			list = new ArrayList<>();
			cache.put(key, list);
		}
		list.add(cacheEntry);
	}

	private synchronized List<CacheEntry> getCacheEntries(String cacheKey) {
		if (stateMachineHandlerApplicationListener != null) {
			Long l = stateMachineHandlerApplicationListener.getLastRefreshTime();
			if (l != null && l < System.currentTimeMillis() ) {
				if (last != l) {
					cache.clear();
					try {
						afterPropertiesSet();
					} catch (Exception e) {
						log.error("Unable to update handler cache", e);
					}
					last = l;
				}
			}
		}
		return cache.get(cacheKey);
	}

	private boolean annotationHandlerVariableMatch(Annotation annotation, Object key) {
		boolean handle = false;
		Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
		Object object = annotationAttributes.get("key");
		Collection<String> scoll = StateMachineUtils.toStringCollection(object);
		if (!scoll.isEmpty()) {
			if (StateMachineUtils.containsAtleastOne(scoll, StateMachineUtils.toStringCollection(key))) {
				handle = true;
			}
		} else {
			handle = true;
		}
		return handle;
	}

	private boolean annotationHandlerEventVariableMatch(Annotation annotation, Object key) {
		boolean handle = false;
		Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(annotation);
		Object object = annotationAttributes.get("event");
		Collection<String> scoll = StateMachineUtils.toStringCollection(object);
		if (!scoll.isEmpty()) {
			if (StateMachineUtils.containsAtleastOne(scoll, StateMachineUtils.toStringCollection(key))) {
				handle = true;
			}
		} else {
			handle = true;
		}
		return handle;
	}

	private boolean annotationHandlerSourceTargetMatch(String[] msources, String[] mtargets, Annotation methodAnnotation,
			State<S, E> sourceState, State<S, E> targetState) {
		Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(methodAnnotation);
		Object source = annotationAttributes.get("source");
		Object target = annotationAttributes.get("target");

		Collection<String> scoll = StateMachineUtils.toStringCollection(source);
		if (scoll.isEmpty() && msources != null) {
			scoll = Arrays.asList(msources);
		}
		Collection<String> tcoll = StateMachineUtils.toStringCollection(target);
		if (tcoll.isEmpty() && mtargets != null) {
			tcoll = Arrays.asList(mtargets);
		}

		boolean handle = false;
		if (!scoll.isEmpty() && !tcoll.isEmpty()) {
			if (sourceState != null
					&& targetState != null
					&& StateMachineUtils.containsAtleastOne(scoll,
							StateMachineUtils.toStringCollection(sourceState.getIds()))
					&& StateMachineUtils.containsAtleastOne(tcoll,
							StateMachineUtils.toStringCollection(targetState.getIds()))) {
				handle = true;
			}
		} else if (!scoll.isEmpty()) {
			if (sourceState != null
					&& StateMachineUtils.containsAtleastOne(scoll,
							StateMachineUtils.toStringCollection(sourceState.getIds()))) {
				handle = true;
			}
		} else if (!tcoll.isEmpty()) {
			if (targetState != null
					&& StateMachineUtils.containsAtleastOne(tcoll,
							StateMachineUtils.toStringCollection(targetState.getIds()))) {
				handle = true;
			}
		} else if (scoll.isEmpty() && tcoll.isEmpty()) {
			handle = true;
		}

		return handle;
	}

	private List<Object> getStateMachineHandlerResults(List<StateMachineHandler<? extends Annotation, S, E>> stateMachineHandlers,
			final StateContext<S, E> stateContext) {
		StateMachineRuntime<S, E> runtime = new StateMachineRuntime<S, E>() {
			@Override
			public StateContext<S, E> getStateContext() {
				return stateContext;
			}
		};
		List<Object> results = new ArrayList<Object>();
		for (StateMachineHandler<? extends Annotation, S, E> handler : stateMachineHandlers) {
			try {
				results.add(handler.handle(runtime));
			} catch (Throwable e) {
				log.error("Error processing handler " + handler, e);
			}
		}
		return results;
	}

	private class CacheEntry {
		final StateMachineHandler<? extends Annotation, S, E> handler;
		final Annotation annotation;
		final Annotation metaAnnotation;

		public CacheEntry(StateMachineHandler<? extends Annotation, S, E> handler, Annotation annotation, Annotation metaAnnotation) {
			this.handler = handler;
			this.annotation = annotation;
			this.metaAnnotation = metaAnnotation;
		}
	}

}
