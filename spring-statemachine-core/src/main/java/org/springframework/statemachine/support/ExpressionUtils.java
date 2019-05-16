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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;

/**
 * Utility class with static methods for helping with establishing environments for
 * SpEL expressions.
 *
 * @author Gary Russell
 * @author Oleg Zhurakousky
 * @author Artem Bilan
 */
public abstract class ExpressionUtils {

	private static final Log logger = LogFactory.getLog(ExpressionUtils.class);

	/**
	 * Create a {@link StandardEvaluationContext} with a {@link MapAccessor} in its
	 * property accessor property and the supplied {@link ConversionService} in its
	 * conversionService property.
	 *
	 * @param conversionService the conversion service.
	 * @return the evaluation context.
	 */
	private static StandardEvaluationContext createStandardEvaluationContext(ConversionService conversionService,
			BeanFactory beanFactory) {
		StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
		evaluationContext.addPropertyAccessor(new MapAccessor());
		if (conversionService != null) {
			evaluationContext.setTypeConverter(new StandardTypeConverter(conversionService));
		}
		if (beanFactory != null) {
			evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
		}
		return evaluationContext;
	}

	/**
	 * Used to create a context with no BeanFactory, usually in tests.
	 * @return The evaluation context.
	 */
	public static StandardEvaluationContext createStandardEvaluationContext() {
		return doCreateContext(null);
	}

	/**
	 * Obtains the context from the beanFactory if not null; emits a warning if the beanFactory
	 * is null.
	 * @param beanFactory The bean factory.
	 * @return The evaluation context.
	 */
	public static StandardEvaluationContext createStandardEvaluationContext(BeanFactory beanFactory) {
		if (beanFactory == null) {
			logger.warn("Creating EvaluationContext with no beanFactory", new RuntimeException("No beanfactory"));
		}
		return doCreateContext(beanFactory);
	}

	private static StandardEvaluationContext doCreateContext(BeanFactory beanFactory) {
		ConversionService conversionService = null;
		StandardEvaluationContext evaluationContext = null;
		if (beanFactory != null) {
			evaluationContext = StateMachineContextUtils.getEvaluationContext(beanFactory);
		}
		if (evaluationContext == null) {
			if (beanFactory != null) {
				conversionService = StateMachineContextUtils.getConversionService(beanFactory);
			}
			evaluationContext = createStandardEvaluationContext(conversionService, beanFactory);
		}
		return evaluationContext;
	}

}
