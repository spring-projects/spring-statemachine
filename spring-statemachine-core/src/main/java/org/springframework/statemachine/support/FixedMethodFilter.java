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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.expression.MethodFilter;
import org.springframework.util.Assert;

/**
 * A {@link MethodFilter} implementation that will always return the same Method
 * instance within a single-element list if it is present in the candidate list.
 * If the Method is not present in the candidate list, it will return an empty
 * list.
 *
 * @author Mark Fisher
 * @author Gary Russell
 * @author Janne Valkealahti
 */
public class FixedMethodFilter implements MethodFilter {

	private final Method method;

	public FixedMethodFilter(Method method) {
		Assert.notNull(method, "method must not be null");
		this.method = method;
	}

	public List<Method> filter(List<Method> methods) {
		if (methods != null && methods.contains(this.method)) {
			List<Method> filteredList = new ArrayList<Method>(1);
			filteredList.add(this.method);
			return filteredList;
		}
		return Collections.<Method> emptyList();
	}

}
