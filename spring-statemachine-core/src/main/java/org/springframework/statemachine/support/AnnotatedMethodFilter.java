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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.MethodFilter;
import org.springframework.util.StringUtils;

/**
 * A MethodFilter implementation that enables the following:
 * <ol>
 * <li>matching on method name, if available</li>
 * <li>exclusion of void-returning methods if 'requiresReply' is true</li>
 * <li>limiting to annotated methods if at least one is present</li>
 * </ol>
 * <p>
 *
 * @author Mark Fisher
 * @author Janne Valkealahti
 */
public class AnnotatedMethodFilter implements MethodFilter {

	private final Class<? extends Annotation> annotationType;

	private final String methodName;

	private final boolean requiresReply;

	public AnnotatedMethodFilter(Class<? extends Annotation> annotationType, String methodName, boolean requiresReply) {
		this.annotationType = annotationType;
		this.methodName = methodName;
		this.requiresReply = requiresReply;
	}

	public List<Method> filter(List<Method> methods) {
		List<Method> annotatedCandidates = new ArrayList<Method>();
		List<Method> fallbackCandidates = new ArrayList<Method>();
		for (Method method : methods) {
			if (method.isBridge()) {
				continue;
			}
			if (this.requiresReply && method.getReturnType().equals(void.class)) {
				continue;
			}
			if (StringUtils.hasText(this.methodName) && !this.methodName.equals(method.getName())) {
				continue;
			}
			if (this.annotationType != null && AnnotationUtils.findAnnotation(method, this.annotationType) != null) {
				annotatedCandidates.add(method);
			} else {
				fallbackCandidates.add(method);
			}
		}
		return (!annotatedCandidates.isEmpty()) ? annotatedCandidates : fallbackCandidates;
	}

}
