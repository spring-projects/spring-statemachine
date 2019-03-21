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
package org.springframework.statemachine.security;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.transition.Transition;
import org.springframework.util.ObjectUtils;

/**
 * Root object for security spel evaluation.
 *
 * @author Janne Valkealahti
 *
 */
public class TransitionSecurityExpressionRoot extends SecurityExpressionRoot {

	public final Transition<?, ?> transition;

	/**
	 * Instantiates a new transition security expression root.
	 *
	 * @param authentication the authentication
	 * @param transition the transition
	 */
	public TransitionSecurityExpressionRoot(Authentication authentication, Transition<?, ?> transition) {
		super(authentication);
		this.transition = transition;
	}

	public final boolean hasSource(Object source) {
		return ObjectUtils.nullSafeEquals(source, transition.getSource().getId());
	}

	public final boolean hasTarget(Object target) {
		return ObjectUtils.nullSafeEquals(target, transition.getTarget().getId());
	}

}
