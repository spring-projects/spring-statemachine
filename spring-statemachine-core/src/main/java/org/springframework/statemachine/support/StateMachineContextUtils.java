/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.task.TaskExecutor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.event.StateMachineEventPublisher;
import org.springframework.util.Assert;

/**
 * Utility methods for accessing common components from the BeanFactory.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineContextUtils {

	/* Default task scheduler bean name */
	public static final String TASK_SCHEDULER_BEAN_NAME = "taskScheduler";

	/* Default task executor bean name */
	public static final String TASK_EXECUTOR_BEAN_NAME = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME;

	/* Default conversion service bean name */
	public static final String CONVERSION_SERVICE_BEAN_NAME = "cloudClusterConversionService";

	/* Default evaluation context bean name */
	public static final String EVALUATION_CONTEXT_BEAN_NAME = "cloudClusterEvaluationContext";

	/**
	 * Return the {@link TaskScheduler} bean whose name is "taskScheduler" if
	 * available.
	 *
	 * @param beanFactory BeanFactory for lookup, must not be null.
	 * @return task scheduler
	 */
	public static TaskScheduler getTaskScheduler(BeanFactory beanFactory) {
		return getBeanOfType(beanFactory, TASK_SCHEDULER_BEAN_NAME, TaskScheduler.class);
	}

	/**
	 * Return the {@link TaskScheduler} bean whose name is "taskExecutor" if
	 * available.
	 *
	 * @param beanFactory BeanFactory for lookup, must not be null.
	 * @return task executor
	 */
	public static TaskExecutor getTaskExecutor(BeanFactory beanFactory) {
		return getBeanOfType(beanFactory, TASK_EXECUTOR_BEAN_NAME, TaskExecutor.class);
	}

	/**
	 * Return the {@link ConversionService} bean whose name is
	 * "yarnConversionService" if available.
	 *
	 * @param beanFactory BeanFactory for lookup, must not be null.
	 *
	 * @return The {@link ConversionService} bean whose name is
	 *         "yarnConversionService" if available.
	 */
	public static ConversionService getConversionService(BeanFactory beanFactory) {
		return getBeanOfType(beanFactory, CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
	}

	/**
	 * Return the {@link StandardEvaluationContext} bean whose name is
	 * "yarnEvaluationContext" if available.
	 *
	 * @param beanFactory BeanFactory for lookup, must not be null.
	 *
	 * @return the instance of {@link StandardEvaluationContext} bean whose name
	 *         is "yarnEvaluationContext" .
	 */
	public static StandardEvaluationContext getEvaluationContext(BeanFactory beanFactory) {
		return getBeanOfType(beanFactory, EVALUATION_CONTEXT_BEAN_NAME, StandardEvaluationContext.class);
	}

	/**
	 * Return the {@link StateMachineEventPublisher} bean whose name is "stateMachineEventPublisher" if
	 * available.
	 *
	 * @param beanFactory BeanFactory for lookup, must not be null.
	 * @return state machine event publisher
	 */
	public static StateMachineEventPublisher getEventPublisher(BeanFactory beanFactory) {
		return getBeanOfType(beanFactory, StateMachineSystemConstants.DEFAULT_ID_EVENT_PUBLISHER,
				StateMachineEventPublisher.class);
	}

	/**
	 * Gets a bean from a factory with a given name and type.
	 *
	 * @param beanFactory the bean factory
	 * @param beanName the bean name
	 * @param type the type as of a class
	 * @return Bean known to a bean factory, null if not found.
	 */
	private static <T> T getBeanOfType(BeanFactory beanFactory, String beanName, Class<T> type) {
		Assert.notNull(beanFactory, "BeanFactory must not be null");
		if (!beanFactory.containsBean(beanName)) {
			return null;
		}
		return beanFactory.getBean(beanName, type);
	}

}
