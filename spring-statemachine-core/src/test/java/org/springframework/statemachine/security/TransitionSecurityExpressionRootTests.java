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
package org.springframework.statemachine.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

public class TransitionSecurityExpressionRootTests {

	SpelExpressionParser parser = new SpelExpressionParser();
	TransitionSecurityExpressionRoot root;
	StandardEvaluationContext ctx;
	private AuthenticationTrustResolver trustResolver;
	private Authentication user;
	private Transition<?, ?> transition;

	@Before
	public void createContext() {
		user = mock(Authentication.class);
		transition = mock(Transition.class);
		root = new TransitionSecurityExpressionRoot(user, transition);
		ctx = new StandardEvaluationContext();
		ctx.setRootObject(root);
		trustResolver = mock(AuthenticationTrustResolver.class);
		root.setTrustResolver(trustResolver);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testSourceTarget() throws Exception {
		State source = mock(State.class);
		when(source.getId()).thenReturn("S1");

		State target = mock(State.class);
		when(target.getId()).thenReturn("S2");

		when(transition.getSource()).thenReturn(source);
		when(transition.getTarget()).thenReturn(target);

		Expression e1 = parser.parseExpression("hasSource('S1')");
		assertTrue(ExpressionUtils.evaluateAsBoolean(e1, ctx));
		Expression e2 = parser.parseExpression("hasTarget('S2')");
		assertTrue(ExpressionUtils.evaluateAsBoolean(e2, ctx));
	}

	@Test
	public void canCallMethodsOnVariables() throws Exception {
		ctx.setVariable("var", "somestring");
		Expression e = parser.parseExpression("#var.length() == 10");
		assertTrue(ExpressionUtils.evaluateAsBoolean(e, ctx));
	}

	@Test
	public void isAnonymousReturnsTrueIfTrustResolverReportsAnonymous() {
		when(trustResolver.isAnonymous(user)).thenReturn(true);
		assertTrue(root.isAnonymous());
	}

	@Test
	public void isAnonymousReturnsFalseIfTrustResolverReportsNonAnonymous() {
		when(trustResolver.isAnonymous(user)).thenReturn(false);
		assertFalse(root.isAnonymous());
	}

	@Test
	public void hasPermissionOnDomainObjectReturnsFalseIfPermissionEvaluatorDoes() throws Exception {
		final Object dummyDomainObject = new Object();
		final PermissionEvaluator pe = mock(PermissionEvaluator.class);
		ctx.setVariable("domainObject", dummyDomainObject);
		root.setPermissionEvaluator(pe);
		when(pe.hasPermission(user, dummyDomainObject, "ignored")).thenReturn(false);
		assertFalse(root.hasPermission(dummyDomainObject, "ignored"));
	}

	@Test
	public void hasPermissionOnDomainObjectReturnsTrueIfPermissionEvaluatorDoes() throws Exception {
		final Object dummyDomainObject = new Object();
		final PermissionEvaluator pe = mock(PermissionEvaluator.class);
		ctx.setVariable("domainObject", dummyDomainObject);
		root.setPermissionEvaluator(pe);
		when(pe.hasPermission(user, dummyDomainObject, "ignored")).thenReturn(true);
		assertTrue(root.hasPermission(dummyDomainObject, "ignored"));
	}

	@Test
	public void hasPermissionOnDomainObjectWorksWithIntegerExpressions() throws Exception {
		final Object dummyDomainObject = new Object();
		ctx.setVariable("domainObject", dummyDomainObject);
		final PermissionEvaluator pe = mock(PermissionEvaluator.class);
		root.setPermissionEvaluator(pe);
		when(pe.hasPermission(eq(user), eq(dummyDomainObject), any(Integer.class))).thenReturn(true).thenReturn(true).thenReturn(false);

		Expression e = parser.parseExpression("hasPermission(#domainObject, 0xA)");
		// evaluator returns true
		assertTrue(ExpressionUtils.evaluateAsBoolean(e, ctx));
		e = parser.parseExpression("hasPermission(#domainObject, 10)");
		// evaluator returns true
		assertTrue(ExpressionUtils.evaluateAsBoolean(e, ctx));
		e = parser.parseExpression("hasPermission(#domainObject, 0xFF)");
		// evaluator returns false, make sure return value matches
		assertFalse(ExpressionUtils.evaluateAsBoolean(e, ctx));
	}

}
