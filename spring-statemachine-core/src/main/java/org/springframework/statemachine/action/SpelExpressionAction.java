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
package org.springframework.statemachine.action;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.support.StateContextExpressionMethods;
import org.springframework.util.Assert;

/**
 * {@link Action} which uses Spring SpEL expression for action execution.
 *
 * @author Janne Valkealahti
 *
 */
public class SpelExpressionAction<S, E> implements Action<S, E> {

	private final StateContextExpressionMethods methods;

	private final Expression expression;

	/**
	 * Instantiates a new spel expression action.
	 *
	 * @param expression the expression
	 */
	public SpelExpressionAction(Expression expression) {
		Assert.notNull(expression, "Expression cannot be null");
		this.expression = expression;
		this.methods = new StateContextExpressionMethods(new StandardEvaluationContext());
	}

	@Override
	public void execute(StateContext<S, E> context) {
		methods.getValue(expression, context, Object.class);
	}

}
