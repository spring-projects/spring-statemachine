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
package org.springframework.statemachine.guard;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.support.StateContextExpressionMethods;
import org.springframework.util.Assert;

/**
 * {@link Guard} which uses Spring SpEL expression for condition evaluation.
 *
 * @author Janne Valkealahti
 *
 */
public class SpelExpressionGuard<S, E> implements Guard<S, E> {

	private final StateContextExpressionMethods methods;

	private final Expression expression;

	/**
	 * Instantiates a new spel expression guard.
	 *
	 * @param expression the expression
	 */
	public SpelExpressionGuard(Expression expression) {
		Assert.notNull(expression, "Expression cannot be null");
		this.expression = expression;
		this.methods = new StateContextExpressionMethods(new StandardEvaluationContext());
	}

	@Override
	public boolean evaluate(StateContext<S, E> context) {
		return methods.getValue(expression, context, Boolean.class);
	}

}
