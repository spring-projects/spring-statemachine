/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Base helper class for state machine tests.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class AbstractStateMachineJUnit5Tests {

	protected AnnotationConfigApplicationContext context;

	@BeforeEach
	public void setup() {
		context = buildContext();
	}

	@AfterEach
	public void clean() {
		if (context != null) {
			context.close();
		}
	}

	protected AnnotationConfigApplicationContext buildContext() {
		return null;
	}

	protected void registerAndRefresh(Class<?>... annotatedClasses) {
		context.register(annotatedClasses);
		context.refresh();
	}

}
